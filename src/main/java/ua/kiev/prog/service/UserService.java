package ua.kiev.prog.service;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ua.kiev.prog.models.AdminUser;
import ua.kiev.prog.models.CustomUser;
import ua.kiev.prog.models.contatct.Contact;
import ua.kiev.prog.models.contatct.ContactGet;
import ua.kiev.prog.models.lead.Lead;
import ua.kiev.prog.models.lead.LeadGet;
import ua.kiev.prog.models.pipelines.Status;
import ua.kiev.prog.repository.UserRepository;

import java.util.List;
import java.util.Map;

@Service
@PropertySource("classpath:telegram.properties")
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final AdminService adminService;

    @Value("${crm.url}")
    private String crmUrl;

    private boolean validateStatus = false;

    public UserService(UserRepository userRepository, RestTemplateBuilder restTemplateBuilder, AdminService adminService) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplateBuilder.build();
        this.adminService = adminService;
    }

    @Transactional(readOnly = true)
    public CustomUser findByUserID(long id) {
        return userRepository.findByUserID(id);
    }

    @Transactional(readOnly = true)
    public List<CustomUser> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void addUser(CustomUser user) {
        userRepository.save(user);
    }

    @Transactional
    public int countByAdmin(boolean val) {return userRepository.countByAdmin(val);}

    @Transactional
    public void updateUser(CustomUser user) {
        userRepository.save(user);
    }

    public Contact[] searchUser(AdminUser adminUser,String query){
        String url = crmUrl+"/api/v2/contacts?query="+query;
        HttpEntity<HttpHeaders> request = request(adminUser.getAccessToken());
        try {
            ResponseEntity<ContactGet> re = this.restTemplate.exchange(url, HttpMethod.GET, request, ContactGet.class);
            if (re.getBody() != null)
                return re.getBody().getContactEmbedded().getContacts();
        } catch (HttpClientErrorException hce) {
            logger.error(hce.getMessage());
            adminService.updateToken(adminUser);
        }
        return null;
    }
    public Lead[] viewLeads(AdminUser adminUser, int query){
        String url = crmUrl+"/api/v2/leads?id="+query;
        HttpEntity<HttpHeaders> request = request(adminUser.getAccessToken());
        try {
            ResponseEntity<LeadGet> re = this.restTemplate.exchange(url, HttpMethod.GET, request, LeadGet.class);
            if (re.getBody() != null)
                return re.getBody().getLeadEmbedded().getLeads();
        } catch (HttpClientErrorException hce) {
            logger.error(hce.getMessage());
        }
        return null;
    }

    public boolean viewStatuses(AdminUser adminUser, int query, long status){
        String url = crmUrl+"/api/v2/pipelines?id="+query;
        HttpEntity<HttpHeaders> request = request(adminUser.getAccessToken());
        try {
            ResponseEntity<Map> st = this.restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            if (st.getBody() != null) {
                return equalsStatusName(st.getBody(), status);
            }
        } catch (HttpClientErrorException hce) {
            logger.error(hce.getMessage());
        }
        return false;
    }

    public boolean equalsStatusName(Map map, long statusCode){
        boolean profit = false;
        for (Object o: map.values()){
            if (o.getClass() != String.class && o.getClass() != Integer.class && o.getClass() != Boolean.class){
                map = (Map) o;
                if (map.get(statusCode+"")!=null) {
                    Gson gson = new Gson();
                    Status status = gson.fromJson(gson.toJson(map.get(statusCode+"")), Status.class);
                    String statusName = status.getName().toLowerCase();
                    if (statusName.equals("получена предоплата") || statusName.equals("успешно реализовано"))
                        return true;
                }
                profit = equalsStatusName(map, statusCode);
            }
        }
        return profit;
    }

    private HttpEntity<HttpHeaders> request(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        return new HttpEntity<>(headers);
    }
}

