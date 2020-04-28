package ua.kiev.prog.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ua.kiev.prog.models.AdminUser;
import ua.kiev.prog.models.Post;
import ua.kiev.prog.repository.AdminRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
@PropertySource("classpath:telegram.properties")
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final AdminRepository adminRepository;
    private final RestTemplate restTemplate;

    //Values for AmoCRM
    @Value("${crm.url}")
    private String crmUrl;
    @Value("${crm.client.id}")
    private String crmClientID;
    @Value("${crm.client.secret}")
    private String crmClientSecret;
    @Value("${crm.redirect.uri}")
    private String crmRedirectUri;

    //Values for Google Drive
    private static final String APPLICATION_NAME = "Keys for students";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";
    @Value("${google.drive.folder.new}")
    private String googleDriveFolderNew;
    @Value("${google.drive.folder.use}")
    private String googleDriveFolderUse;

    public AdminService(AdminRepository adminRepository, RestTemplateBuilder restTemplateBuilder) {
        this.adminRepository = adminRepository;
        this.restTemplate = restTemplateBuilder.build();
    }

    @Transactional(readOnly = true)
    public AdminUser findByID(long id) {
        return adminRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public AdminUser findByUserID(long id) {
        return adminRepository.findByUserID(id);
    }

    @Transactional
    public void addUser(AdminUser user) {
        adminRepository.save(user);
    }

    @Transactional
    public void updateUser(AdminUser user) {
        adminRepository.save(user);
    }

    @Transactional
    public AdminUser findAdmin(boolean admin) {return adminRepository.findByAdmin(admin);}


    //AmoCRM methods
    public boolean authAmoCRM(String code) {
        AdminUser adminUser = this.findAdmin(true);
        String url = crmUrl+"/oauth2/access_token/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> map = new HashMap<>();
        map.put("client_id", crmClientID);
        map.put("client_secret", crmClientSecret);
        if (code != null) {
            map.put("grant_type", "authorization_code");
            map.put("code", code);
        } else {
            map.put("grant_type", "refresh_token");
            map.put("refresh_token", adminUser.getRefreshToken());
        }
        map.put("redirect_uri", crmRedirectUri);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

        Post post;
        try {
            post = this.restTemplate.postForObject(url, entity, Post.class);
            assert post != null;
            adminUser.setAccessToken(post.getAccessToken());
            adminUser.setRefreshToken(post.getRefreshToken());
            this.updateUser(adminUser);
            return true;
        } catch (HttpClientErrorException hce) {
            logger.error(hce.getMessage());
            return false;
        }
    }

    public void updateToken() {
        AdminUser adminUser = this.findAdmin(true);
        String url = crmUrl+"/oauth2/access_token/";


    }

    //Google Drive methods
    public static Credential authGoogleDrive(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
            // Load client secrets.
            InputStream in = AdminService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8889).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public List<File> getGoogleRootFolders(Drive driveService, String googleFolderIdParent) throws IOException {

        String pageToken = null;
        List<File> list = new ArrayList<>();

        String query = null;
        if (googleFolderIdParent == null) {
            query = " mimeType = 'application/vnd.google-apps.folder' "
                    + " and 'root' in parents";
        } else {
            query = " mimeType != 'application/vnd.google-apps.folder' "
                    + " and '" + googleFolderIdParent + "' in parents";
        }

        do {
            FileList result = driveService.files().list().setQ(query).setSpaces("drive") //
                    // Fields will be assigned values: id, name, createdTime
                    .setFields("nextPageToken, files(id, name, webViewLink)")//
                    .setPageToken(pageToken).execute();
            list.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return list;
    }

    public List<File> getGoogleFilesByName(Drive driveService, String fileNameLike, String folder) throws IOException {
        String pageToken = null;
        List<File> list = new ArrayList<File>();

        String query = null;
        if (folder.equals("!=")) {
            query = " name contains '" + fileNameLike + "' "
                    + " and mimeType "+folder+" 'application/vnd.google-apps.folder' ";
        } else {
            query = " name = '" + fileNameLike + "' "
                    + " and mimeType "+folder+" 'application/vnd.google-apps.folder' "
                    + " and 'root' in parents";
        }

        do {
            FileList result = driveService.files().list().setQ(query).setSpaces("drive") //
                    // Fields will be assigned values: id, name, createdTime, mimeType
                    .setFields("nextPageToken, files(id, name, createdTime, mimeType, webViewLink, parents)")//
                    .setPageToken(pageToken).execute();
            list.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return list;
    }

    public void createPublicPermission(Drive driveService, String googleFileId) throws IOException {
        // All values: user - group - domain - anyone
        String permissionType = "anyone";
        // All values: organizer - owner - writer - commenter - reader
        String permissionRole = "reader";

        Permission newPermission = new Permission();
        newPermission.setType(permissionType);
        newPermission.setRole(permissionRole);

        driveService.permissions().create(googleFileId, newPermission).execute();
    }

    public void moveFile(Drive service, String fileId, String folderId) throws IOException {
        // Retrieve the existing parents to remove
        File file = service.files().get(fileId)
                .setFields("parents")
                .execute();
        StringBuilder previousParents = new StringBuilder();
        for (String parent : file.getParents()) {
            previousParents.append(parent);
            previousParents.append(',');
        }
        // Move the file to the new folder
        service.files().update(fileId, null)
                .setAddParents(folderId)
                .setRemoveParents(previousParents.toString())
                .setFields("id, parents")
                .execute();
    }

    public static Drive service() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, authGoogleDrive(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public File getKeys() throws GeneralSecurityException, IOException {
        Drive service = service();

        String folderNewId = null;
        List<File> newFolder = getGoogleFilesByName(service, googleDriveFolderNew,"=");
        for (File folder : newFolder) {
            folderNewId = folder.getId();
        }

        //Get the first file
        File file = null;
        List<File> googleFiles = getGoogleRootFolders(service, folderNewId);
        if (googleFiles.size()>0) {
            file = googleFiles.get(0);

            String folderOldId = null;
            List<File> useFolder = getGoogleFilesByName(service, googleDriveFolderUse,"=");
            for (File folder : useFolder) {
                folderOldId = folder.getId();
            }

            //Moving file between folders
            moveFile(service, file.getId(), folderOldId);

            // Share for everyone
            createPublicPermission(service, file.getId());
        } else {
            logger.error("Not keys!");
        }
        return file;
    }
}