function pwd() {
    var oldPwd = $.trim($("#old_pwd").val());//原密码
    var newPwd = $.trim($("#new_pwd").val());//新密码
    var configPwd = $.trim($("#confirm_pwd").val());//新密码
    if (!newPwd && !oldPwd && !configPwd) {
        modals.error('请输入密码！');
        return;
    } else if (newPwd !== configPwd) {
        modals.info('新密码两次输入不一致！');
        return;
    }
    var data = {"oldPwd": oldPwd, "newPwd": newPwd, "configPwd": configPwd};
    postAjax('/user/resetPwd', data, {callback: callback()});
}

function callback() {
    $('#old_pwd').val('');
    $('#new_pwd').val('');
    $('#confirm_pwd').val('');
}