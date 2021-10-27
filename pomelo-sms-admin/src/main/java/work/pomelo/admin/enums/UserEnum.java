package work.pomelo.admin.enums;

import lombok.Getter;

/**
 * @author ghostxbh
 * @date 2020/10/13
 */
@Getter
public enum UserEnum {
    NOMUST(400, "未输入账号密码"),
    PWDERROR(401, "密码错误"),
    NOEXIST(402, "用户名不存在"),
    EXIST(403, "用户名已存在"),
    OLDERROR(404, "原密码错误"),
    NOMODIFY(405, "无修改"),
    VALIDERROR(406, "验证码错误"),
    ;
    /**
     * 编码
     */
    private Integer code;
    /**
     * 信息
     */
    private String message;

    UserEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
