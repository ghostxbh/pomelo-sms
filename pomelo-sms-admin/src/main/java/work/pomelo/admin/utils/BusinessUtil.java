package work.pomelo.admin.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author xbh
 * @date 2020/11/11
 * 
 */
public class BusinessUtil {
    public static List<String> filterNumber(List<String> nums) {
        return new ArrayList<>(new HashSet<>(nums));
    }
}
