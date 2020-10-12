package com.uzykj.sms.module.http.domian;

import com.uzykj.sms.core.domain.SmsDetails;
import lombok.Data;

@Data
public class HttpSendDTO {
    private String to;

    private String from;

    private String server;

    // Username that is to be used for submission
    private String username;

    // password that is to be used along with username
    private String password;

    // Message content that is to be transmitted
    private String message;

    private String type;

    private String port;

    private String dlr;

    private SmsDetails details;

    public HttpSendDTO() {
    }

    public HttpSendDTO(String server, String username, String password, String message, String type, String to, String from) {
        this.username = username;
        this.password = password;
        this.message = message;
        this.type = type;
        this.to = to;
        this.from = from;
        this.server = server;
    }
}