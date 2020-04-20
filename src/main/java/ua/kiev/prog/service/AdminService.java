package ua.kiev.prog.service;

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

import java.util.HashMap;
import java.util.Map;

@Service
@PropertySource("classpath:telegram.properties")
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final AdminRepository adminRepository;
    private final RestTemplate restTemplate;

    @Value("${crm.url}")
    private String crmUrl;
    @Value("${crm.client.id}")
    private String crmClientID;
    @Value("${crm.client.secret}")
    private String crmClientSecret;
    @Value("${crm.redirect.uri}")
    private String crmRedirectUri;

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

    public boolean getPost(long chatID, String code) {
        AdminUser adminUser = this.findByUserID(chatID);
        String url = crmUrl+"/oauth2/access_token/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> map = new HashMap<>();
        map.put("client_id", crmClientID);
        map.put("client_secret", crmClientSecret);
        map.put("grant_type", "authorization_code");
//        map.put("code", adminUser.getCode());
        map.put("code", code);
        map.put("redirect_uri", crmRedirectUri);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

        Post post;
        try {
            post = this.restTemplate.postForObject(url, entity, Post.class);
            assert post != null;
            adminUser.setAccessToken(post.getAccessToken());
            adminUser.setRefreshToken(post.getRefreshToken());
//            adminUser.setStep(7);
            logger.warn(adminUser.toString());
            return true;
        } catch (HttpClientErrorException hce) {
            logger.error(hce.getMessage());
            return false;
        }
    }
}