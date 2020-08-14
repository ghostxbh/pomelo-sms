package com.uzykj.smsSystem.core.controller.module;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.smsSystem.core.common.Globle;
import com.uzykj.smsSystem.core.controller.BaseController;
import com.uzykj.smsSystem.core.domain.SmsAccount;
import com.uzykj.smsSystem.core.domain.SysUser;
import com.uzykj.smsSystem.core.domain.dto.PageDto;
import com.uzykj.smsSystem.core.domain.dto.SysUserDto;
import com.uzykj.smsSystem.core.domain.dto.UserCacheDto;
import com.uzykj.smsSystem.core.enums.CommenEnum;
import com.uzykj.smsSystem.core.enums.UserEnum;
import com.uzykj.smsSystem.core.service.SmsAccountService;
import com.uzykj.smsSystem.core.service.SysUserService;
import com.uzykj.smsSystem.core.common.json.JsonResult;
import com.uzykj.smsSystem.core.util.OtherUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

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
            //查询所有条数
            int count = 0;
            SysUserDto userDto = new SysUserDto();
            if ((searchName != null && !"".equals(searchName)) && (searchPhone != null && !"".equals(searchPhone))) {
                userDto.setName(searchName);
                userDto.setMobile(searchPhone);
                count = userService.userAllCount(userDto);
            } else {
                count = userService.userAllCount(userDto);
            }
            //总共多少页
            int total = (count % pageSize > 0) ? (count / pageSize + 1) : count / pageSize;
            //起始索引
            pageDto.setOffset((page - 1) * pageSize);
            pageDto.setCount(count);
            pageDto.setTotal(total);
            pageDto.setPage(page);
            pageDto.setPageSize(pageSize);

            //分页查询
            Page<SysUser> allUser = userService.getAllUser(pageDto, userDto);
            List<SysUser> userList = allUser.getRecords();
            SysUser user = (SysUser) session.getAttribute("user");

            Page<SmsAccount> all = smsAccountService.getAll(null, null);
            List<SmsAccount> smsAccountList = all.getRecords();

            List<SysUserDto> userDtoList = new ArrayList<SysUserDto>(userList.size());
            userList.forEach(item -> {
                SmsAccount smsAccount = smsAccountService.get(item.getAccountId());
                SysUserDto sysUserDto = new SysUserDto(item, smsAccount);
                userDtoList.add(sysUserDto);
            });

            model.addAttribute("accountList", smsAccountList);
            model.addAttribute("searchName", searchName != null ? searchName : "");
            model.addAttribute("searchPhone", searchPhone != null ? searchPhone : "");
            model.addAttribute("userList", userDtoList);
            model.addAttribute("currentId", user.getId());
            model.addAttribute("current", page);
            model.addAttribute("pageCount", total);
            model.addAttribute("count", count);
        } catch (Exception e) {
            log.error("用户列表展示", e);
        }
        return "manager/users";
    }

    @PostMapping("/allowance")
    @ResponseBody
    public JsonResult modifyAllowance(Integer id, Integer allowance, Integer account) {
        if (!OtherUtils.checkParams(id, allowance, account)) {
            return new JsonResult(UserEnum.NOMODIFY.getCode(), UserEnum.NOMODIFY.getMessage());
        }
        try {
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
