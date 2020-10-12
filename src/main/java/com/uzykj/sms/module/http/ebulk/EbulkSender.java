package com.uzykj.sms.module.http.ebulk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uzykj.sms.module.http.HttpSender;
import com.uzykj.sms.module.http.domian.HttpSendDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
@Service
public class EbulkSender implements HttpSender {
    private final static String CONTEXTTYPE = "application/json";
    private final static String SMSURL = "https://restapi.bulksmsonline.com/rest/api/v1/sms/send";
    private final static String USERNAME = "JallAb871";
    private final static String PASSWORD = "AA123123";
    private final static String TOKENURL = "https://restapi.bulksmsonline.com/rest/api/v1/sms/gettoken/username/" + USERNAME + "/password/" + PASSWORD;
    private String smsToken = null;

    public static String getToken() throws InterruptedException, IOException {
        String token;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(TOKENURL)
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            token = response.body().string();
        } catch (Exception e) {
            log.error("get sms token error: ", e);
            Thread.sleep(2 * 1000);
            log.warn("get token again");
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            token = response.body().string();
        }
        JSONObject jsonObject = JSON.parseObject(token);
        return jsonObject.get("token").toString();
    }

    @Override
    public String submitMessage(HttpSendDTO sender) {
        // 验证 token是否存在
        if (StringUtils.isEmpty(smsToken)) {
            try {
                smsToken = getToken();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
        String sendTo = sender.getTo();
        String pBody;
        String message = sender.getMessage();
        if (message.contains("\"")) {
            message.replaceAll("\"", "\\\\\"");
            sender.setMessage(message);
        }
        if (sendTo.indexOf(",") > -1) {
            String[] split = sendTo.split("\\,");
            String to = JSON.toJSONString(split);
            pBody = "{\"from\":\"" + sender.getFrom() + "\",\"to\": " + to + ",\"type\":\"UniCode\",\"content\":\"" + sender.getMessage() + "\"}";
        } else {
            pBody = "{\"from\":\"" + sender.getFrom() + "\",\"to\": [\"" + sendTo + "\"],\"type\":\"UniCode\",\"content\":\"" + sender.getMessage() + "\"}";
        }
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse(CONTEXTTYPE);
        RequestBody body = RequestBody.create(mediaType, pBody);
        Request request = new Request.Builder()
                .url(SMSURL)
                .post(body)
                .addHeader("token", smsToken)
                .addHeader("content-type", CONTEXTTYPE)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String resp = response.body().string();
            JSONObject jsonObject = JSON.parseObject(resp);
            return jsonObject.get("message").toString();
        } catch (Exception e) {
            log.error("send sms error: ", e);
            try {
                Thread.sleep(2 * 1000);
                log.warn("send sms again");
                Response response = client.newCall(request).execute();
                String resp = response.body().string();
                JSONObject jsonObject = JSON.parseObject(resp);
                return jsonObject.get("message").toString();
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
