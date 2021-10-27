package work.pomelo.admin.enums;

import lombok.Getter;

/**
 * @author ghostxbh
 * @date 2020/10/13
 * 
 */
@Getter
public enum CommenEnum {
    SUCCESS(200, "ok!"), FAIL(500, "fail!")
    ;
    /**
     * 编码
     */
    private Integer code;
    /**
     * 信息
     */
    private String message;

    CommenEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
