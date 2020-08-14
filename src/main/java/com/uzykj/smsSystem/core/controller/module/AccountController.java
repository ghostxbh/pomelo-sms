package com.uzykj.smsSystem.core.controller.module;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.smsSystem.core.controller.BaseController;
import com.uzykj.smsSystem.core.domain.SmsAccount;
import com.uzykj.smsSystem.core.domain.dto.PageDto;
import com.uzykj.smsSystem.core.domain.dto.SmsAccountDto;
import com.uzykj.smsSystem.core.enums.CommenEnum;
import com.uzykj.smsSystem.core.enums.UserEnum;
import com.uzykj.smsSystem.core.service.SmsAccountService;
import com.uzykj.smsSystem.core.common.json.JsonResult;
import com.uzykj.smsSystem.core.util.OtherUtils;
import org.apache.ibatis.annotations.Param;
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
        boolean check = OtherUtils.checkParams(a.getCode(), a.getSystemId(), a.getPassword(), a.getPort(), a.getUrl());
        if (!check) {
            return new JsonResult(UserEnum.NOMUST.getCode(), UserEnum.NOMUST.getMessage());
        }
        try {
            SmsAccount byCode = smsAccountService.getByCode(a.getCode());
            if (byCode != null) {
                return new JsonResult(UserEnum.EXIST.getCode(), UserEnum.EXIST.getMessage());
            }
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
}
