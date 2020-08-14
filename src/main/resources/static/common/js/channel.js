function resetChannel() {
    $("#channel_code").val('');
    $("#channel_url").val('');
    $("#channel_port").val('');
    $("#channel_systemId").val('');
    $("#channel_password").val('');
    $("#channel_channelPwd").val('');
    $("#channel_enable").val(1);
    $("#channel_description").val('');
    $("#channel_remark").val('');
}

function addChannel() {
    var id = $.trim($("#channel_id").val());
    var code = $.trim($("#channel_code").val());
    var url = $.trim($("#channel_url").val());
    var port = $.trim($("#channel_port").val());
    var systemId = $.trim($("#channel_systemId").val());
    var password = $.trim($("#channel_password").val());
    var channelPwd = $.trim($("#channel_channelPwd").val());
    var enabled = $.trim($("#channel_enable").val());
    var description = $.trim($("#channel_description").val());
    var remark = $.trim($("#channel_remark").val());
    if (!code && !url && !port && !systemId && !password && !channelEnable) {
        modals.error('必填项未输入，带*号为必填项！');
        return;
    }
    var option = {
        headers: {
            'Content-Type': 'application/json'
        }
    };
    var channel = {
        "code": code,
        "url": url,
        "port": port,
        "systemId": systemId,
        "password": password,
        "channelPwd": channelPwd,
        "enabled": enabled,
        "description": description,
        "remark": remark
    };
    if (id) {
        channel.id = id;
        postAjax('/channel/update', JSON.stringify(channel), option);
    } else {
        postAjax('/channel/add', JSON.stringify(channel), option);
    }
}