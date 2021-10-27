package work.pomelo.admin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
/**
 * @author ghostxbh
 * @date 2020/8/14
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sms_collect")
public class SmsCollect {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String collectId;
    private Integer userId;
    private String accountCode;

    private String contents;
    private String status;

    private Integer total;
    private Integer pendingNum;
    private Integer successNum;
    private Integer failNum;

    private Date createTime;
    private Date updateTime;
    private Date completeTime;

    @TableField(exist = false)
    private SysUser user;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}