package work.pomelo.admin.web;

import lombok.extern.slf4j.Slf4j;
import work.pomelo.admin.domain.SysUser;
import work.pomelo.admin.service.SmsCollectService;
import work.pomelo.admin.service.SmsDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
@Controller
public class HomeController {

    @Autowired
    private SmsDetailsService smsDetailsService;

    @Autowired
    private SmsCollectService smsCollectService;

    @GetMapping("/index")
    public String home(HttpSession session, Model model, HttpServletResponse response) throws IOException {
        SysUser user = (SysUser) session.getAttribute("USER_SESSION");
        if (user == null) {
            response.sendRedirect("/");
        } else {
            model.addAttribute("userName", user.getName());
            return "base";
        }
        return null;
    }

    @GetMapping("/home")
    public String main(HttpSession session, Model model, HttpServletResponse response) {
        SysUser user = (SysUser) session.getAttribute("USER_SESSION");
        try {
            if (user == null) {
                response.sendRedirect("/");
                return null;
            } else {
                int currentCount = smsDetailsService.currentCount(user.getId());
                int successCount = smsCollectService.successCountToday(user.getId());
                int failCount = smsCollectService.failCountToday(user.getId());
                model.addAttribute("currentCount", currentCount);
                model.addAttribute("successCount", successCount);
                model.addAttribute("failCount", failCount);
                model.addAttribute("userInfo", user);
            }
        } catch (Exception e) {
            log.error("Go home error", e);
        }
        return "home";
    }
}
