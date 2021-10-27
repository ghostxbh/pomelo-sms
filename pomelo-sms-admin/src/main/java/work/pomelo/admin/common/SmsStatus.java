package work.pomelo.admin.common;

import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author ghostxbh
 * @date 2020/8/15
 * 
 */
public class SmsStatus {
    public static String switchSmsStatus(String status) {
        String result = "";
        if (!StringUtils.isEmpty(status)) {
            switch (status) {
                case "success":
                case "delivrd":
                    result = "发送完成";
                    break;
                case "expired":
                    result = "响应超时";
                    break;
                case "undeliv":
                    result = "无法送达";
                    break;
                case "rejectd":
                    result = "消息被拒绝";
                    break;
                case "deleted":
                    result = "消息被删除";
                    break;
                case "unknown":
                    result = "状态未知";
                    break;
                case "pending":
                case "submited":
                case "checking":
                default:
                    result = "已发送";
                    break;
            }
        }
        return result;
    }

}
