package com.uzykj.sms.core.util;

import java.util.Random;

/**
 * @author ghostxbh
 * @date 2020/10/30
 * @description
 */
public class StringUtils {
    private static final String UPPERCHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWERCHARS = "abcdefghjklmnpqrstuvwxyz";
    private static final String NUMBERS = "0123456789";

    /**
     * @exmple (ULN, 5) Tp4Sj
     * @param type U大写 L小写 N数字
     * @param size 长度
     * @return
     */
    public static String getVercode(String type, int size) {
        StringBuilder chars = new StringBuilder();
        if (type.contains("U")) {
            chars.append(UPPERCHARS);
        }
        if (type.contains("L")) {
            chars.append(LOWERCHARS);
        }
        if (type.contains("N")) {
            chars.append(NUMBERS);
        }
        StringBuilder vercode = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < size; ++i) {
            vercode.append(chars.charAt(r.nextInt(chars.length())));
        }
        return new String(vercode);
    }

    public static void main(String[] args) {
        String nl = getVercode("ULN", 32);
        System.out.println(nl);
    }
}
