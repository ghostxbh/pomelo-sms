package work.pomelo.admin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import work.pomelo.admin.enums.ChannelTypeEnum;

import java.util.Date;

/**
 * @author ghostxbh
 * @date 2020/8/14
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("sms_account")
public class SmsAccount {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String code;
    private ChannelTypeEnum channelType;
    private String systemId;
    private String password;
    private String url;
    private Integer port;
    private String channelPwd;
    private String description;
    private Integer enabled;
    private Integer isInvalid;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
