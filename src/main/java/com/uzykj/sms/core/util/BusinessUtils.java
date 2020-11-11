package com.uzykj.sms.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author xbh
 * @date 2020/11/11
 * @description
 */
public class BusinessUtils {
    public static List<String> filterNumber(List<String> nums) {
        return new ArrayList<>(new HashSet<>(nums));
    }
}
