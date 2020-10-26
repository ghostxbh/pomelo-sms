package com.uzykj.sms.core.controller.module;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.controller.BaseController;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.domain.dto.PageDto;
import com.uzykj.sms.core.enums.CommenEnum;
import com.uzykj.sms.core.enums.UserEnum;
import com.uzykj.sms.core.service.SmsAccountService;
import com.uzykj.sms.core.service.SysUserService;
import com.uzykj.sms.core.common.json.JsonResult;
import com.uzykj.sms.core.util.OtherUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager")
public class ManagerController extends BaseController {

    @Autowired
    private SysUserService userService;
    @Autowired
    private SmsAccountService smsAccountService;

    @GetMapping("/add")
    public String createUser(Model model, HttpSession session) {
        Page<SmsAccount> all = smsAccountService.getAll(null, null);
        List<SmsAccount> smsAccountList = all.getRecords();
        model.addAttribute("accountList", smsAccountList);
        return "manager/add";
    }

    /**
     * 用户列表展示
     */
    @GetMapping("/users")
    public String list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                       @RequestParam(value = "searchName", required = false) String searchName,
                       @RequestParam(value = "searchPhone", required = false) String searchPhone,
                       Model model, HttpSession session) {
        try {
            PageDto pageDto = new PageDto();
            pageDto.setPage(page);
            pageDto.setPageSize(pageSize);

            SysUser sysUser = new SysUser();
            sysUser.setName(!("".equals(searchName)) ? searchName : null);
            sysUser.setMobile(!("".equals(searchPhone)) ? searchPhone : null);

            //分页查询
            Page<SysUser> allUser = userService.getAllUser(pageDto, sysUser);
            List<SysUser> userList = allUser.getRecords();
            List<SysUser> collect = Optional.ofNullable(userList)
                    .orElse(new ArrayList<SysUser>(0))
                    .stream()
                    .peek(user -> {
                        SysUser cacheUser = Globle.USER_CACHE.get(user.getId());
                        user.setAccount(cacheUser.getAccount());
                    })
                    .collect(Collectors.toList());
            allUser.setRecords(collect);

            Page<SmsAccount> all = smsAccountService.getAll(null, null);
            List<SmsAccount> smsAccountList = Optional.ofNullable(all.getRecords()).orElse(new ArrayList<SmsAccount>(0));

            SysUser user = (SysUser) session.getAttribute("user");

            model.addAttribute("searchName", searchName != null ? searchName : "");
            model.addAttribute("searchPhone", searchPhone != null ? searchPhone : "");
            model.addAttribute("currentId", user.getId());
            model.addAttribute("accountList", smsAccountList);
            model.addAttribute("page", allUser);
        } catch (Exception e) {
            log.error("用户列表展示", e);
        }
        return "manager/users";
    }

    @PostMapping("/allowance")
    @ResponseBody
    public JsonResult modifyAllowance(Integer id, Integer allowance,
                                      Integer add, Integer minus,
                                      Integer account) {
        if (!OtherUtils.checkParams(id, allowance, account)) {
            return new JsonResult(UserEnum.NOMODIFY.getCode(), UserEnum.NOMODIFY.getMessage());
        }
        try {
            add = add != null ? add : 0;
            minus = minus != null ? minus : 0;
            allowance = allowance + add - minus;
            userService.modifyAllowance(id, allowance, account);
        } catch (Exception e) {
            log.error("user update error", e);
            return new JsonResult(CommenEnum.FAIL.getCode(), CommenEnum.FAIL.getMessage());
        }
        return new JsonResult(CommenEnum.SUCCESS.getCode(), CommenEnum.SUCCESS.getMessage());
    }

    @DeleteMapping("/del/{userId}")
    @ResponseBody
    public JsonResult delUser(@PathVariable Integer userId) {
        if (!OtherUtils.checkParams(userId)) {
            return new JsonResult(UserEnum.NOMODIFY.getCode(), UserEnum.NOMODIFY.getMessage());
        }
        try {
            userService.del(userId);
        } catch (Exception e) {
            log.error("user update error: ", e.getMessage());
            return new JsonResult(CommenEnum.FAIL.getCode(), CommenEnum.FAIL.getMessage());
        }
        return new JsonResult(CommenEnum.SUCCESS.getCode(), CommenEnum.SUCCESS.getMessage());
    }
}
