package work.pomelo.admin.provider.http;

import work.pomelo.admin.domain.SmsAccount;
import work.pomelo.admin.domain.SmsDetails;

import java.util.List;
import java.util.Map;

/**
 * @author ghostxbh
 * @date 2020/7/9
 *  短信发送接口
 */
public interface HttpSender {
    /**
     * 短信发送
     *
     * @param phones
     * @param message
     * @param account
     */
    void submitMessage(List<String> phones, Map<String, SmsDetails> detilsMap, String message, SmsAccount account);
}
