package com.uzykj.sms.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * @description
 */
public class DateUtils {
    public static String getBatchNo() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(new Date());
    }

    public static void main(String[] args) {
        System.out.println(getBatchNo());
    }
}
