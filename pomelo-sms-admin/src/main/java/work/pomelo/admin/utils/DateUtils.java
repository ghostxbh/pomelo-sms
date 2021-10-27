package work.pomelo.admin.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * 
 */
public class DateUtils {
    public static String getBatchNo() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(new Date());
    }
}
