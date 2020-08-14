package com.uzykj.smsSystem.core.controller;


import com.uzykj.smsSystem.core.domain.SysUser;
import com.uzykj.smsSystem.core.service.SysUserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;

/**
 * @author ghostxbh
 * @date 2020/2/13
 * @description Base
 */
public class BaseController {
    @Autowired
    private SysUserService sysUserService;
    protected Logger log = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    protected SysUser checkUser(HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("user");
        try {
            if (user == null) {
                return null;
            }
            user = sysUserService.get(user.getId());
        } catch (Exception e) {
            log.error("get user error", e);
        }
        return user;
    }
}
