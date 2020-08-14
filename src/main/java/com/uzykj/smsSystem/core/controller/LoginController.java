package com.uzykj.smsSystem.core.controller;

import com.uzykj.smsSystem.core.domain.SysUser;
import com.uzykj.smsSystem.core.enums.CommenEnum;
import com.uzykj.smsSystem.core.enums.UserEnum;
import com.uzykj.smsSystem.core.service.SmsDetailsService;
import com.uzykj.smsSystem.core.service.SysUserService;
import com.uzykj.smsSystem.core.common.json.JsonResult;
import com.uzykj.smsSystem.core.util.OtherUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

@Controller
public class LoginController extends BaseController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SmsDetailsService smsDetailsService;

    @GetMapping(value = "/")
    public String goLogin(HttpSession session, HttpServletResponse response) {
        SysUser user = checkUser(session) != null ? checkUser(session) : null;
        if (user != null) {
            try {
                response.sendRedirect("/index");
            } catch (IOException e) {
                log.error("index page error: ", e.getMessage());
            }
        }
        return "login";
    }

    @GetMapping("/logout")
    public String goLogout(HttpSession session) {
        SysUser user = checkUser(session);
        if (user != null) {
            session.removeAttribute("user");
        }
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public JsonResult login(String loginName, String loginPassword, String validCode, HttpSession session) throws IOException {
        JsonResult jsonResult = new JsonResult();
        if (OtherUtils.checkParams(loginName, loginPassword)) {
            /*String imageCode = (String) session.getAttribute("imageCode");
            if (!imageCode.equalsIgnoreCase(validCode)) {
                jsonResult.setCode(UserEnum.VALIDERROR.getCode()).setMessage(UserEnum.VALIDERROR.getMessage());
                return jsonResult;
            }*/
            try {
                SysUser login = sysUserService.login(loginName);
                if (login != null) {
                    if (login.getPassword().equals(loginPassword)) {
                        jsonResult.setCode(CommenEnum.SUCCESS.getCode())
                                .setMessage(CommenEnum.SUCCESS.getMessage())
                                .setData(login);
                        session.setAttribute("user", login);
                    } else {
                        jsonResult.setCode(UserEnum.PWDERROR.getCode()).setMessage(UserEnum.PWDERROR.getMessage());
                    }
                } else {
                    jsonResult.setCode(UserEnum.NOEXIST.getCode()).setMessage(UserEnum.NOEXIST.getMessage());
                }
            } catch (Exception e) {
                log.error("login api error: ", e);
            }
        } else {
            jsonResult.setCode(UserEnum.NOMUST.getCode()).setMessage(UserEnum.NOMUST.getMessage());
        }
        return jsonResult;
    }

    @GetMapping("/getCode")
    public void getCode(String v, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        int width = 100;
        int height = 34;
        int lineCount = 8;
        int length = 4;
        int fontSize = 24;
        int x_position = 15;
        BufferedImage image = new BufferedImage(width, height, 1);
        Graphics g = image.getGraphics();
        g.setColor(new Color(204, 204, 204));
        g.fillRect(0, 0, width, height);
        Random r = new Random();
        String imageCode = OtherUtils.getVercode(length);
        session.setAttribute("imageCode", imageCode);
        int i;
        for (i = 0; i < imageCode.length(); ++i) {
            g.setColor(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
            g.setFont(new Font((String) null, 1, fontSize));
            g.drawString(imageCode.charAt(i) + "", x_position + i * fontSize, 30);
        }

        for (i = 0; i < lineCount; ++i) {
            g.setColor(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
            g.drawLine(r.nextInt(120), r.nextInt(30), r.nextInt(120), r.nextInt(30));
        }
        response.setContentType("image/jpeg");
        OutputStream output = response.getOutputStream();
        ImageIO.write(image, "jpeg", output);
        output.close();
    }
}



