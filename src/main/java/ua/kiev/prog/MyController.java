package ua.kiev.prog;

import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.kiev.prog.service.AdminService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    public void crm(@RequestParam(name="code", required=false) String code, @RequestParam(name="state", required=false) long state, HttpServletResponse response) throws IOException {
        if(code!=null) {
            if(adminService.authAmoCRM(code)) {
                response.sendRedirect("tg://user?id=" + state);
                chatBot.sendMessage(state, "AmoCRM подключена", false);
//                chatBot.sendInlineButtons(state,
//                        new InlineKeyboardButton()
//                                .setText("AmoCRM")
//                                .setUrl("https://www.amocrm.ru/oauth?client_id="+crmClientID+"&state="+state+"&mode=post_message"),
//                        "Авторизуйтесь в системе AmoCRM"
//                );
            }
        }
    }

    @GetMapping("/google")
    public void crm() {

    }

    @GetMapping("/")
    public String google() {
        return "index";
    }
}
