package work.pomelo.admin.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import work.pomelo.admin.common.json.JsonResult;
import work.pomelo.admin.domain.SysUser;
import work.pomelo.admin.enums.CommenEnum;
import work.pomelo.admin.enums.UserEnum;
import work.pomelo.admin.service.SmsDetailsService;
import work.pomelo.admin.service.SysUserService;
import work.pomelo.admin.utils.OtherUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

@Slf4j
@Controller
public class LoginController {
    @Autowired
    private SysUserService sysUserService;

    @GetMapping(value = "/")
    public String goLogin(HttpSession session, HttpServletResponse response) {
        SysUser user = (SysUser) session.getAttribute("USER_SESSION");
        if (user != null) {
            try {
                response.sendRedirect("/index");
            } catch (IOException e) {
                log.error("index page error: ", e);
            }
        }
        return "login";
    }

    @GetMapping("/logout")
    public String goLogout(HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("USER_SESSION");
        if (user != null) {
            session.removeAttribute("USER_SESSION");
            session.removeAttribute("USER_ID");
        }
        return "login";
    }

    @PostMapping("/login/validCode")
    @ResponseBody
    public JsonResult<String> validCode(String validCode, HttpSession session) {
        JsonResult<String> jsonResult = new JsonResult<String>();
        String imageCode = (String) session.getAttribute("imageCode");
        if (!imageCode.equalsIgnoreCase(validCode)) {
            jsonResult.setCode(UserEnum.VALIDERROR.getCode()).setMessage(UserEnum.VALIDERROR.getMessage());
            return jsonResult;
        }
        jsonResult.setCode(CommenEnum.SUCCESS.getCode()).setMessage(CommenEnum.SUCCESS.getMessage());
        return jsonResult;
    }

    @PostMapping("/login")
    @ResponseBody
    public JsonResult<SysUser> login(String loginName, String loginPassword, HttpSession session) throws IOException {
        JsonResult<SysUser> jsonResult = new JsonResult<>();
        if (!OtherUtil.checkParams(loginName, loginPassword)) {
            jsonResult.setCode(UserEnum.NOMUST.getCode()).setMessage(UserEnum.NOMUST.getMessage());
            return jsonResult;
        }

        SysUser login = sysUserService.login(loginName);
        if (login == null) {
            jsonResult.setCode(UserEnum.NOEXIST.getCode()).setMessage(UserEnum.NOEXIST.getMessage());
            return jsonResult;
        }

        if (!login.getPassword().equals(loginPassword)) {
            jsonResult.setCode(UserEnum.PWDERROR.getCode()).setMessage(UserEnum.PWDERROR.getMessage());
            return jsonResult;
        }

        jsonResult.setCode(CommenEnum.SUCCESS.getCode()).setMessage(CommenEnum.SUCCESS.getMessage()).setData(login);
        session.setAttribute("USER_SESSION", login);
        session.setAttribute("USER_ID", login.getId());
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
        String imageCode = OtherUtil.getVercode(length);
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



