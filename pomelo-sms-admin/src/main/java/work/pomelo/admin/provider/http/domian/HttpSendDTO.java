package work.pomelo.admin.provider.http.domian;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import work.pomelo.admin.domain.SmsDetails;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}