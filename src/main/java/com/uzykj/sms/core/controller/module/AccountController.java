package com.uzykj.sms.core.controller.module;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.sms.core.controller.BaseController;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.dto.PageDto;
import com.uzykj.sms.core.domain.dto.SmsAccountDto;
import com.uzykj.sms.core.enums.ChannelEnum;
import com.uzykj.sms.core.enums.ChannelTypeEnum;
import com.uzykj.sms.core.enums.CommenEnum;
import com.uzykj.sms.core.enums.UserEnum;
import com.uzykj.sms.core.service.SmsAccountService;
import com.uzykj.sms.core.common.json.JsonResult;
import com.uzykj.sms.core.util.OtherUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/channel")
public class AccountController extends BaseController {

    @Autowired
    private SmsAccountService smsAccountService;

    @GetMapping("/list")
    public String list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                       @RequestParam(value = "searchCode", required = false) String code,
                       @RequestParam(value = "searchSystemId", required = false) String systemId,
                       @RequestParam(value = "enabled", required = false) Integer enabled,
                       @RequestParam(value = "isInvalid", required = false) Integer isInvalid,
                       Model model, HttpSession session) {
        try {
            //根据用户id查询总条数
            PageDto dto = new PageDto((page - 1) * pageSize, pageSize);
            SmsAccountDto accountDto = new SmsAccountDto(code, systemId, enabled, isInvalid);
            Page<SmsAccount> all = smsAccountService.getAll(dto, accountDto);

            model.addAttribute("page", all);
            model.addAttribute("searchCode", code != null ? code : "");
            model.addAttribute("searchSystemId", systemId != null ? systemId : "");
        } catch (Exception e) {
            log.error("统计列表error：", e);
        }
        return "channel/list";
    }

    @GetMapping("/addPage")
    public String addPage(@RequestParam(value = "channelId", required = false) Integer channelId,
                          Model model, HttpSession session) {
        if (channelId != null) {
            SmsAccount smsAccount = smsAccountService.get(channelId);
            model.addAttribute("channel", smsAccount);
        }
        return "channel/add";
    }

    @PostMapping("/add")
    @ResponseBody
    public JsonResult add(@RequestBody SmsAccount a) {
        boolean check = OtherUtils.checkParams(a.getCode(), a.getSystemId(), a.getPassword(), a.getPort(), a.getUrl(), a.getChannelType());
        if (!check) {
            return new JsonResult(UserEnum.NOMUST.getCode(), UserEnum.NOMUST.getMessage());
        }
        try {
            SmsAccount byCode = smsAccountService.getByCode(a.getCode());
            if (byCode != null) {
                return new JsonResult(UserEnum.EXIST.getCode(), UserEnum.EXIST.getMessage());
            }
            String code = a.getChannelType().equals(ChannelTypeEnum.HTTP) ? "H" + a.getCode() : "S" + a.getCode();
            a.setCode(code);
            smsAccountService.add(a);
        } catch (Exception e) {
            log.error("add account error", e);
            return new JsonResult(CommenEnum.FAIL.getCode(), CommenEnum.FAIL.getMessage());
        }
        return new JsonResult(CommenEnum.SUCCESS.getCode(), CommenEnum.SUCCESS.getMessage());
    }

    @PostMapping("/update")
    @ResponseBody
    public JsonResult update(@RequestBody SmsAccount a) {
        boolean check = OtherUtils.checkParams(a.getId(), a.getCode(), a.getSystemId(), a.getPassword(), a.getPort(), a.getUrl());
        if (!check) {
            return new JsonResult(UserEnum.NOMUST.getCode(), UserEnum.NOMUST.getMessage());
        }
        try {
            String code = a.getChannelType().equals(ChannelTypeEnum.HTTP) ? "H" + a.getCode() : "S" + a.getCode();
            a.setCode(code);
            smsAccountService.update(a);
        } catch (Exception e) {
            log.error("account update error", e);
            return new JsonResult(CommenEnum.FAIL.getCode(), CommenEnum.FAIL.getMessage());
        }
        return new JsonResult(CommenEnum.SUCCESS.getCode(), CommenEnum.SUCCESS.getMessage());
    }

    @DeleteMapping("/del/{id}")
    @ResponseBody
    public JsonResult del(@PathVariable Integer id) {
        boolean check = OtherUtils.checkParams(id);
        if (!check) {
            return new JsonResult(UserEnum.NOMUST.getCode(), UserEnum.NOMUST.getMessage());
        }
        try {
            smsAccountService.del(id);
        } catch (Exception e) {
            log.error("account del error", e);
            return new JsonResult(CommenEnum.FAIL.getCode(), CommenEnum.FAIL.getMessage());
        }
        return new JsonResult(CommenEnum.SUCCESS.getCode(), CommenEnum.SUCCESS.getMessage());
    }

    @GetMapping("/check/{code}")
    @ResponseBody
    public JsonResult check(@PathVariable String code) {
        boolean check = OtherUtils.checkParams(code);
        if (!check) {
            return new JsonResult(UserEnum.NOMUST.getCode(), UserEnum.NOMUST.getMessage());
        }
        try {
            boolean checked = smsAccountService.check(code);
            if (checked) {
                return new JsonResult(ChannelEnum.SUCCESS.getCode(), ChannelEnum.SUCCESS.getMessage());
            }
            return new JsonResult(ChannelEnum.FAIL.getCode(), ChannelEnum.FAIL.getMessage());
        } catch (Exception e) {
            log.error("account check error", e);
            return new JsonResult(CommenEnum.FAIL.getCode(), CommenEnum.FAIL.getMessage());
        }
    }

    @GetMapping("/refrensh/{code}")
    @ResponseBody
    public JsonResult refrensh(@PathVariable String code) {
        boolean check = OtherUtils.checkParams(code);
        if (!check) {
            return new JsonResult(UserEnum.NOMUST.getCode(), UserEnum.NOMUST.getMessage());
        }
        try {
            smsAccountService.refrensh(code);
        } catch (Exception e) {
            log.error("account refrensh error", e);
            return new JsonResult(CommenEnum.FAIL.getCode(), CommenEnum.FAIL.getMessage());
        }
        return new JsonResult(CommenEnum.SUCCESS.getCode(), CommenEnum.SUCCESS.getMessage());
    }

    @GetMapping("/stop/{code}")
    @ResponseBody
    public JsonResult stop(@PathVariable String code) {
        boolean check = OtherUtils.checkParams(code);
        if (!check) {
            return new JsonResult(UserEnum.NOMUST.getCode(), UserEnum.NOMUST.getMessage());
        }
        try {
            smsAccountService.stopEndpoint(code);
        } catch (Exception e) {
            log.error("account stop error", e);
            return new JsonResult(CommenEnum.FAIL.getCode(), CommenEnum.FAIL.getMessage());
        }
        return new JsonResult(CommenEnum.SUCCESS.getCode(), CommenEnum.SUCCESS.getMessage());
    }
}
