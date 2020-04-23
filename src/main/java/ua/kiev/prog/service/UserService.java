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
import java.util.Objects;

@Service
@PropertySource("classpath:telegram.properties")
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final AdminService adminService;
    private final RestTemplate restTemplate;

    @Value("${crm.url}")
    private String crmUrl;

    private boolean validateStatus = false;

    public UserService(UserRepository userRepository, AdminService adminService, RestTemplateBuilder restTemplateBuilder) {
        this.userRepository = userRepository;
        this.adminService = adminService;
        this.restTemplate = restTemplateBuilder.build();
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

    public Contact[] searchUser(String query){
        AdminUser adminUser = adminService.findAdmin(true);
        String url = crmUrl+"/api/v2/contacts?query="+query;
        HttpEntity<HttpHeaders> request = request(adminUser.getAccessToken());
        Contact[] contacts = null;
        try {
            contacts = this.restTemplate.exchange(url, HttpMethod.GET, request, ContactGet.class)
                    .getBody().getContactEmbedded().getContacts();
//            for (Contact contact : contacts) {
//                logger.info("В AmoCRM обнаружен пользователь "+contact.getName());
//                int[] leads = contact.getLeads().getLeads();
//                if (leads != null) {
//                    logger.info("Количество сделок: "+leads.length);
//                    for (int leadID : leads) viewLeads(adminUser, leadID);
//                } else {
//                    logger.info("У пользователя "+ contact.getName() +" нет текущих сделок");
//                }
//            }
        } catch (HttpClientErrorException hce) {
            logger.error(hce.getMessage());
        }
        return contacts;
    }
    private void viewLeads(AdminUser adminUser, int query){
        String url = crmUrl+"/api/v2/leads?id="+query;
        HttpEntity<HttpHeaders> request = request(adminUser.getAccessToken());
        try {
            Lead[] leads = Objects.requireNonNull(this.restTemplate.exchange(url, HttpMethod.GET, request, LeadGet.class)
                    .getBody()).getLeadEmbedded().getLeads();
            for(Lead lead: leads) {
                viewStatuses(adminUser, lead.getPipeline().getId(), lead.getStatus());
            }
        } catch (HttpClientErrorException hce) {
            logger.error(hce.getMessage());
        }
    }

    private void viewStatuses(AdminUser adminUser, int query, long status){
        String url = crmUrl+"/api/v2/pipelines?id="+query;
        HttpEntity<HttpHeaders> request = request(adminUser.getAccessToken());
        try {
            ResponseEntity<Map> st = Objects.requireNonNull(this.restTemplate.exchange(url, HttpMethod.GET, request, Map.class));
            equalsStatusName(st.getBody(), status);
        } catch (HttpClientErrorException hce) {
            logger.error(hce.getMessage());
        }
    }

    private void equalsStatusName(Map map, long statusCode){
        for (Object o: map.values()){
            if (o.getClass() != String.class && o.getClass() != Integer.class &&o.getClass() != Boolean.class){
                map = (Map) o;
                if (map.get(statusCode+"")!=null) {
                    Gson gson = new Gson();
                    Status status = gson.fromJson(gson.toJson(map.get(statusCode+"")), Status.class);
                    String statusName = status.getName().toLowerCase();
                    logger.info("Сделка в статусе: "+status.getName());
                    validateStatus = statusName.equals("получена предоплата") || statusName.equals("успешно реализовано");
                }
                equalsStatusName(map, statusCode);
            }
        }
    }

    private HttpEntity<HttpHeaders> request(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        return new HttpEntity<>(headers);
    }
}

