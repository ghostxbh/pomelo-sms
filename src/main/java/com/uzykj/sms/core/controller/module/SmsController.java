package com.uzykj.sms.core.controller.module;

import com.uzykj.sms.core.common.excel.ExcelUtils;
import com.uzykj.sms.core.common.json.JsonResult;
import com.uzykj.sms.core.controller.BaseController;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.domain.dto.PageDto;
import com.uzykj.sms.core.domain.dto.SmsDetailsDto;
import com.uzykj.sms.core.enums.CommenEnum;
import com.uzykj.sms.core.enums.SmsEnum;
import com.uzykj.sms.core.service.SmsDetailsService;
import com.uzykj.sms.core.service.SysUserService;
import com.uzykj.sms.core.util.OtherUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;

@Controller
@RequestMapping("/sms")
public class SmsController extends BaseController {
    private static final int MAXBATCHSENDNUM = 10 * 1000;
    private static final int MAXFILESENDNUM = 20 * 1000;

    @Autowired
    private SmsDetailsService smsDetailsService;

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/add")
    public String create() {
        return "sms/add";
    }

    @GetMapping("/log")
    public String log() {
        return "sms/log";
    }

    /**
     * 短信列表
     */
    @GetMapping("/list")
    public String list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                       @RequestParam(value = "startTime", required = false) String startTime,
                       @RequestParam(value = "endTime", required = false) String endTime,
                       @RequestParam(value = "searchName", required = false) String searchName,
                       @RequestParam(value = "searchPhone", required = false) String searchPhone,
                       @RequestParam(value = "collectId", required = false) String collectId,
                       HttpSession session, Model model) {
        SysUser user = checkUser(session);
        startTime = OtherUtils.checkNull(startTime);
        endTime = OtherUtils.checkNull(endTime);
        searchName = OtherUtils.checkNull(searchName);
        searchPhone = OtherUtils.checkNull(searchPhone);
        collectId = OtherUtils.checkNull(collectId);
        Integer userId = searchName != null ? null : user.getId();
        //根据用户id查询总条数
        try {
            //起始索引
            PageDto pageDto = new PageDto();
            pageDto.setPage((page - 1) * pageSize);
            pageDto.setPageSize(pageSize);

            SmsDetailsDto dto = new SmsDetailsDto(userId, collectId, searchName, searchPhone, startTime, endTime, pageDto);

            List<SmsDetails> smsDetailsList = smsDetailsService.getList(dto);
            int count = smsDetailsService.getListCount(dto);
            int pageCount = (count % pageSize > 0) ? (count / pageSize + 1) : count / pageSize;
            model.addAttribute("smsDetailsList", smsDetailsList);
            model.addAttribute("count", count);
            model.addAttribute("current", page);
            model.addAttribute("pageCount", pageCount);
            model.addAttribute("startTime", startTime);
            model.addAttribute("endTime", endTime);
            model.addAttribute("searchName", searchName);
            model.addAttribute("searchPhone", searchPhone);
            model.addAttribute("collectId", collectId);
            model.addAttribute("user", user);
        } catch (Exception e) {
            log.error("短信列表error：", e);
        }
        return "sms/list";
    }


    @PostMapping("/batchAdd")
    @ResponseBody
    public JsonResult<?> batchAdd(@RequestParam("phoneList[]") List<String> phoneList, String content, HttpSession session) {
        long startTime = System.currentTimeMillis();
        try {
            SysUser user = checkUser(session);
            SysUser smsUser = sysUserService.get(user.getId());
            if (phoneList.size() < 1 || phoneList == null || content == null || content.isEmpty()) {
                log.warn("参数异常");
                return new JsonResult(SmsEnum.NOMUST.getCode(), SmsEnum.NOMUST.getMessage());
            }
            if (phoneList.size() > MAXBATCHSENDNUM) {
                log.warn("超出发送上限");
                return new JsonResult(SmsEnum.MAXOUT.getCode(), SmsEnum.MAXOUT.getMessage());
            }

            List<String> filterList = new ArrayList<>(new HashSet<>(phoneList));
            int filterNum = phoneList.size() - filterList.size();
            JsonResult<?> result = smsDetailsService.processSmsList(filterList, content, smsUser);
            if (result.getCode() != 200) {
                return result;
            }

            String resultCount = "已发送 " + filterList.size() + " 条短信";
            resultCount = filterNum > 0 ? resultCount + ", 重复 " + filterNum + "个号码" : resultCount;
            log.info("数据异步API耗费时间: " + (System.currentTimeMillis() - startTime) + " ms");
            return new JsonResult(CommenEnum.SUCCESS.getCode(), resultCount);
        } catch (Exception e) {
            log.error("群发短信error: ", e);
            return new JsonResult(CommenEnum.FAIL.getCode(), CommenEnum.FAIL.getMessage());
        }
    }

    @PostMapping("/fileAdd")
    @ResponseBody
    public JsonResult<?> fileAdd(@RequestParam("dataFile") MultipartFile multipartFile,
                                 @RequestParam("content") String content, HttpSession session) {
        long startTime = System.currentTimeMillis();
        try {
            SysUser user = checkUser(session);
            SysUser smsUser = sysUserService.get(user.getId());

            String originalFilename = multipartFile.getOriginalFilename();
            InputStream ins = multipartFile.getInputStream();
            assert originalFilename != null;
            List<String> phoneList = ExcelUtils.fileImport(ins, originalFilename);

            if (phoneList.size() < 1 || content == null || content.isEmpty()) {
                log.warn("参数异常");
                return new JsonResult(SmsEnum.NOMUST.getCode(), SmsEnum.NOMUST.getMessage());
            }
            if (phoneList.size() > MAXFILESENDNUM) {
                log.warn("超出发送上限");
                return new JsonResult(SmsEnum.MAXOUT.getCode(), SmsEnum.MAXOUT.getMessage());
            }

            List<String> filterList = new ArrayList<>(new HashSet<>(phoneList));
            int filterNum = phoneList.size() - filterList.size();
            JsonResult<?> result = smsDetailsService.processSmsList(filterList, content, smsUser);
            if (result.getCode() != 200) {
                return result;
            }

            log.info("文件异步API耗费时间: " + (System.currentTimeMillis() - startTime) + " ms");
            String resultCount = "已发送 " + filterList.size() + " 条短信";
            resultCount = filterNum > 0 ? resultCount + ", 重复 " + filterNum + "个号码" : resultCount;
            return new JsonResult(CommenEnum.SUCCESS.getCode(), resultCount);
        } catch (Exception e) {
            log.error("fileAdd error: ", e);
            return new JsonResult(CommenEnum.FAIL.getCode(), CommenEnum.FAIL.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportExcel(@RequestParam(value = "page", required = false) Integer page,
                            @RequestParam(value = "pageSize", required = false) Integer pageSize,
                            @RequestParam(value = "startTime", required = false) String startTime,
                            @RequestParam(value = "endTime", required = false) String endTime,
                            @RequestParam(value = "searchName", required = false) String searchName,
                            @RequestParam(value = "searchPhone", required = false) String searchPhone,
                            @RequestParam(value = "collectId", required = false) String collectId,
                            HttpSession session, HttpServletResponse response) {
        SysUser user = checkUser(session);
        startTime = OtherUtils.checkNull(startTime);
        endTime = OtherUtils.checkNull(endTime);
        searchName = OtherUtils.checkNull(searchName);
        searchName = ("undefined").equals(searchName) ? null : searchName;
        searchPhone = OtherUtils.checkNull(searchPhone);
        collectId = OtherUtils.checkNull(collectId);
        Integer userId;
        if (searchName != null) {
            userId = Optional.ofNullable(sysUserService.login(searchName))
                    .orElse(new SysUser())
                    .getId();
        } else {
            userId = user.getId();
        }
        try {
            String sheetName = UUID.randomUUID().toString();
            //起始索引
            PageDto pageDto = new PageDto();
            if (page != null && pageSize != null) {
                pageDto.setPage((page - 1) * pageSize);
                pageDto.setPageSize(pageSize);
            }

            SmsDetailsDto dto = new SmsDetailsDto(userId, collectId, searchName, searchPhone, startTime, endTime, pageDto);
            List<SmsDetails> SmsDetailsList = smsDetailsService.getList(dto);
            if (SmsDetailsList != null && SmsDetailsList.size() > 0) {
                XSSFWorkbook xssfSheets = ExcelUtils.downLoadExcelModel(sheetName, SmsDetailsList);
                ServletOutputStream outputStream = response.getOutputStream();
                response.setHeader("content-Type", "application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(sheetName + ".xlsx", "UTF-8"));
                xssfSheets.write(outputStream);
                outputStream.close();
            }
            response.sendError(501, "当前没有可以导出的数据");
        } catch (Exception e) {
            log.error("exportExcel error: ", e);
        }
    }
}
