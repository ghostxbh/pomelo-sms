package work.pomelo.admin.enums;

import lombok.Getter;

/**
 * @author ghostxbh
 * @date 2020/10/13
 * 
 */
@Getter
public enum ChannelEnum {
    SUCCESS(200, "线路通道链接成功"), FAIL(500, "线路通道链接失败")
    ;
    /**
     * 编码
     */
    private Integer code;
    /**
     * 信息
     */
    private String message;

    ChannelEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
