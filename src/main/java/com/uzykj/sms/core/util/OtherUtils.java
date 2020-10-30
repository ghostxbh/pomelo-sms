package com.uzykj.sms.core.util;

import com.uzykj.sms.core.domain.SysUser;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class OtherUtils {
    private boolean isEmpty = false;
    private static final String REGEX_MOBILE = "^((1[3-9][0-9]))\\d{8}$";

    public static String getVercode(int size) {
        String chars = "abcdefghjklmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
        String vercode = "";
        Random r = new Random();
        for (int i = 0; i < size; ++i) {
            vercode = vercode + chars.charAt(r.nextInt(chars.length()));
        }
        return vercode;
    }

    public static boolean checkUser(SysUser user) {
        String name = user.getName();
        String password = user.getPassword();
        String contactor = user.getContactor();
        String mobile = user.getMobile();
        String address = user.getAddress();
        String role = user.getRole();
        String remark = user.getRemark();
        Integer allowance = user.getAllowance();
        OtherUtils otherUtils = new OtherUtils();
        otherUtils.isEmptys(name, password, contactor, mobile, address, role, remark, allowance);
        return otherUtils.isEmpty;
    }

    public static boolean checkParams(Object... strs) {
        OtherUtils otherUtils = new OtherUtils();
        otherUtils.isEmptys(strs);
        return otherUtils.isEmpty;
    }

    public static String checkNull(String str) {
        return "".equals(str) ? null : str;
    }

    private void isEmptys(Object... strs) {
        Arrays.asList(strs).forEach(str -> isEmpty = !isEmpty ? !(str == null || "".equals(str)) : true);
    }

    public static boolean checkStrIsNum(String str) {
        try {
            String bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String splitStr(String str) {
        if (str.split(",").length > 0) return str.split(",")[0];
        if (str.split(" ").length > 0) return str.split(" ")[0];
        return str;
    }

    public static List<String> filterNumber(List<String> nums) {
        Set<String> filter = new HashSet<>(nums.size());
        for (String num : nums) {
            num = parseStr(num);
            boolean isInt = isInteger(num);
            boolean isPhone = isPhone(num);
            if (!StringUtils.isEmpty(num) && isInt && isPhone) {
                String substring = num.substring(0, 2);
                if (!"86".equals(substring) && num.length() == 11) {
                    num = "86" + num;
                    filter.add(num);
                } else if ("86".equals(substring) && num.length() == 13) {
                    filter.add(num);
                }
            }
        }
        return new ArrayList<>(filter);
    }

    private static boolean isPhone(String str) {
        if (str.length() > 11) {
            str = str.substring(2, str.length());
        } else if (str.length() < 11) {
            return false;
        }
        Pattern pattern = Pattern.compile(REGEX_MOBILE);
        return pattern.matcher(str).matches();
    }

    private static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    private static String parseStr(String param) {
        if (param.contains(".")) {
            String[] split = param.split("\\.");
            for (int i = 0; i < split.length; i++) {
                String str = split[i];
                if (str.length() > 10) {
                    return str;
                }
            }
        }
        return param;
    }
}
