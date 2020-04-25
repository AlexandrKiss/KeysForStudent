package ua.kiev.prog;

import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.kiev.prog.service.AdminService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@PropertySource("classpath:telegram.properties")
public class MyController {
    private final AdminService adminService;
    private final ChatBot chatBot;

    public MyController(AdminService adminService, ChatBot chatBot) {
        this.adminService = adminService;
        this.chatBot = chatBot;
    }

    @GetMapping("/amocrm")
    public void crm(@RequestParam(name="code", required=false) String code,
                    @RequestParam(name="state", required=false) long state,
                    HttpServletResponse response) throws IOException, GeneralSecurityException {
        if(code!=null) {
            if(adminService.authAmoCRM(code)) {
                response.sendRedirect("tg://user?id=" + state);
                chatBot.sendMessage(state, "AmoCRM подключена", false);
                AdminService.service();
            }
        }
    }

    @GetMapping("/Callback")
    public void google() {

    }
}
