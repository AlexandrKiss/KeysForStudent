package ua.kiev.prog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.kiev.prog.service.AdminService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class MyController {
    private final AdminService adminService;

    public MyController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/")
    public void crm(@RequestParam(name="code", required=false) String code, @RequestParam(name="state", required=false) long state, HttpServletResponse response) throws IOException {
        if(code!=null) {
            adminService.authAmoCRM(state, code);
            response.sendRedirect("tg://user?id="+state);
        }
    }
}
