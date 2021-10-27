$(function () {
    var VALIDCODE = false;
    $("#validCode").blur(function () {
        var validCode = $.trim($("#validCode").val());
        $.ajax({
            url: "/login/validCode",
            type: "POST",
            data: {
                "validCode": validCode,
            },
            success: function (data) {
                var code = data.code;
                var msg = data.message;
                var errConfig = {
                    ValidatorCode: 'validCode'
                };
                if (code !== 200) {
                    errConfig.code = 2;
                    errConfig.message = msg;
                    $(".help-block").remove();
                    new LoginError(errConfig);
                    $(".help-block strong").css("color", "red");
                }
                VALIDCODE = code === 200;
            },
            beforeSend: function () {
            },
            error: function (data) {
            }
        });
    })

    $("#loginClient").click(function () {
        //表单验证
        var loginName = $.trim($("#loginName").val());
        var loginPassword = $.trim($("#loginPassword").val());
        if (!loginName && !loginPassword && !VALIDCODE) return;
        $.ajax({
            url: "/login",
            type: "post",
            data: {
                "loginName": loginName,
                "loginPassword": loginPassword,
            },
            success: function (data) {
                var code = data.code;
                var msg = data.message;
                var errConfig = {
                    code: 0,
                    message: '',
                    userName: 'loginName',
                    password: 'loginPassword',
                };
                if (code === 200) {
                    location.href = "/";
                } else if (code === 400 || code === 401 || code === 402) {
                    errConfig.code = 1;
                    errConfig.message = msg;
                    $(".help-block").remove();
                    new LoginError(errConfig);
                }
            },
            beforeSend: function () {
                $("#btn_login").val("登录中...");
            },
            error: function (data) {

            }
        });
    });
    $('#login_forget').click(function () {
        $('#myModalLabel').empty().text('忘记密码');
        $('.modal-body').empty().append('<p style="color: red">请联系管理员！</p>')
        $('.modal-footer btn btn-primary').empty();
        $('#myModal').modal('show');
    });
    $('#login_register').click(function () {
        $('#myModalLabel').empty().text('注册用户');
        $('#myModal').modal('show');
    });
});

function LoginError(config) {
    this.code = config.code;
    this.message = config.message;
    this.userName = config.userName;
    this.password = config.password;
    this.ValidatorCode = config.ValidatorCode;
    this.initValidator();
}

//1 账号密码错误 2验证码错误
LoginError.prototype.initValidator = function () {
    if (!this.code)
        return;
    if (this.code == 1) {
        this.addUserNameErrorStyle();
        this.addPasswordErrorStyle();
        this.addPasswordErrorMsg();
    } else if (this.code == 2) {
        this.addValidatorCodeErrorStyle();
        this.addValidatorCodeErrorMsg();
    }
    return;
}

LoginError.prototype.addUserNameErrorStyle = function () {
    this.addErrorStyle(this.userName);
}

LoginError.prototype.addPasswordErrorStyle = function () {
    this.addErrorStyle(this.password);
}

LoginError.prototype.addValidatorCodeErrorStyle = function () {
    this.addErrorStyle(this.ValidatorCode);
}

LoginError.prototype.addUserNameErrorMsg = function () {
    this.addErrorMsg(this.userName);
}

LoginError.prototype.addPasswordErrorMsg = function () {
    this.addErrorMsg(this.password);
}

LoginError.prototype.addValidatorCodeErrorMsg = function () {
    this.addErrorMsg(this.ValidatorCode);
}


LoginError.prototype.addErrorMsg = function (field) {
    $("input[name='" + field + "']").parent().append('<h4  data-bv-validator="notEmpty" data-bv-validator-for="' + field + '" class="help-block"><strong>' + this.message + '</strong></h4>');
}

LoginError.prototype.addErrorStyle = function (field) {
    $("input[name='" + field + "']").parent().addClass("has-error");
}
