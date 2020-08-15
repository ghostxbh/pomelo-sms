package com.uzykj.sms.core.controller.module;

import com.uzykj.sms.core.controller.BaseController;
import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.enums.CommenEnum;
import com.uzykj.sms.core.enums.UserEnum;
import com.uzykj.sms.core.service.SysUserService;
import com.uzykj.sms.core.common.json.JsonResult;
import com.uzykj.sms.core.util.OtherUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/info")
    public String info(HttpSession session, Model model) {
        SysUser user = checkUser(session);
        model.addAttribute("userInfo", user);
        return "user/info";
    }

    @GetMapping("/pwd")
    public String pwd() {
        return "user/pwd";
    }

    @PostMapping("/add")
    @ResponseBody
    public JsonResult addUserInfo(@RequestBody SysUser user) {
        String name = user.getName();
        String password = user.getPassword();
        if (!OtherUtils.checkParams(name, password)) {
            return new JsonResult(UserEnum.NOMUST.getCode(), UserEnum.NOMUST.getMessage());
        }
        try {
            SysUser sysUser = sysUserService.login(user.getName());
            if (sysUser != null) {
                return new JsonResult(UserEnum.EXIST.getCode(), UserEnum.EXIST.getMessage());
            }
            user.setRole("member");
            sysUserService.add(user);
        } catch (Exception e) {
            log.error("add user error", e);
            return new JsonResult(CommenEnum.FAIL.getCode(), CommenEnum.FAIL.getMessage());
        }
        return new JsonResult(CommenEnum.SUCCESS.getCode(), CommenEnum.SUCCESS.getMessage());
    }

    @PostMapping("/update")
    @ResponseBody
    public JsonResult update(@RequestBody SysUser user) {
        if (!OtherUtils.checkUser(user)) {
            return new JsonResult(UserEnum.NOMODIFY.getCode(), UserEnum.NOMODIFY.getMessage());
        }
        try {
            sysUserService.update(user);
        } catch (Exception e) {
            log.error("user update error", e);
            return new JsonResult(CommenEnum.FAIL.getCode(), CommenEnum.FAIL.getMessage());
        }
        return new JsonResult(CommenEnum.SUCCESS.getCode(), CommenEnum.SUCCESS.getMessage());
    }

    /**
     * 修改密码
     */
    @PostMapping("/resetPwd")
    @ResponseBody
    public JsonResult updatePwd(@RequestParam String oldPwd, @RequestParam String newPwd, @RequestParam String configPwd, HttpSession session) {
        JsonResult jsonResult = new JsonResult();
        try {
            if (!OtherUtils.checkParams(oldPwd, newPwd, configPwd)) {
                jsonResult.setCode(UserEnum.NOMUST.getCode()).setMessage(UserEnum.NOMUST.getMessage());
                return jsonResult;
            }
            SysUser user = checkUser(session);
            if (!user.getPassword().equals(oldPwd)) {
                jsonResult.setCode(UserEnum.OLDERROR.getCode()).setMessage(UserEnum.OLDERROR.getMessage());
                return jsonResult;
            }
            if (!newPwd.equals(configPwd)) {
                jsonResult.setCode(UserEnum.PWDERROR.getCode()).setMessage(UserEnum.PWDERROR.getMessage());
                return jsonResult;
            }
            sysUserService.resetPwd(newPwd, user);
            jsonResult.setCode(CommenEnum.SUCCESS.getCode()).setMessage(CommenEnum.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("updatePwd error", e);
            jsonResult.setCode(CommenEnum.FAIL.getCode()).setMessage(CommenEnum.FAIL.getMessage());
        }
        return jsonResult;
    }
}
