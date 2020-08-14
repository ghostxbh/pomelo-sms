package com.uzykj.smsSystem.core.controller.module;

import com.uzykj.smsSystem.core.controller.BaseController;
import com.uzykj.smsSystem.core.domain.Permission;
import com.uzykj.smsSystem.core.domain.SysUser;
import com.uzykj.smsSystem.core.service.PermissionService;
import com.uzykj.smsSystem.core.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class PermissionController extends BaseController {

    @Autowired
    private SysUserService sysUserService;

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
            SysUser user = checkUser(session);
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
