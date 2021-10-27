package work.pomelo.admin.web.module;

import lombok.extern.slf4j.Slf4j;
import work.pomelo.admin.domain.Permission;
import work.pomelo.admin.domain.SysUser;
import work.pomelo.admin.service.PermissionService;
import work.pomelo.admin.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@Controller
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/404")
    public String notFound() {
        return "404";
    }

    @PostMapping("/main/menu/{id}")
    @ResponseBody
    public List<Permission> menu(@PathVariable String id, HttpSession session, HttpServletResponse response) {
        try {
            SysUser user = (SysUser) session.getAttribute("USER_SESSION");
            if (user == null) {
                response.sendRedirect("/");
            } else {
                return permissionService.permissions(user);
            }
        } catch (Exception e) {
            log.error("获取栏目error", e);
        }
        return null;
    }
}
