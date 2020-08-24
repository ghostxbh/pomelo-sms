package com.uzykj.sms.core.controller.module;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.sms.core.controller.BaseController;
import com.uzykj.sms.core.domain.SmsCollect;
import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.domain.dto.PageDto;
import com.uzykj.sms.core.service.SmsCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/statis")
public class StatisController extends BaseController {

    @Autowired
    private SmsCollectService smsCollectService;

    @GetMapping("/list")
    public String list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                       @RequestParam(value = "userName", required = false) String userName,
                       Model model, HttpSession session) {
        SysUser user = checkUser(session);
        try {
            //根据用户id查询总条数
            PageDto dto = new PageDto(page, pageSize);
            Page<SmsCollect> all = smsCollectService.getAll(user, dto, userName);

            model.addAttribute("page", all);
            model.addAttribute("userName", userName);
        } catch (Exception e) {
            log.error("统计列表error：", e);
        }
        return "statis/list";
    }
}
