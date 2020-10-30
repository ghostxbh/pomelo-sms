function resetInfo() {
    $("#create_name").val('');//用户名
    $("#create_pwd").val('');//用户密码
    $("#create_pwd_too").val('');//密码验证
    $("#create_allowance").val('');//短信余额
    $("#phone_prefix").val('');//号码前缀
    $("#create_remark").val('');//备注
    // $("#create_company").val('');//公司名
    // $("#create_contactor").val('');//联系人
    // $("#create_mobile").val('');//联系电话
    // $("#create_industry").val('');//行业
    // $("#create_address").val('');//地址
}

function createUser() {
    var name = $.trim($("#create_name").val());//用户名
    var pwd = $.trim($("#create_pwd").val());//用户密码
    var pwdToo = $.trim($("#create_pwd_too").val());//密码验证
    var allowance = $.trim($("#create_allowance").val());//短信余额
    var remark = $("#create_remark").val();//备注
    var account = $("#create_account").val();//通道
    var phonePrefix = $("#phone_prefix").val();//号码
    var textSuffix = $("#text_suffix").val();//通道
    // var company = $.trim($("#create_company").val());//公司名
    // var contactor = $.trim($("#create_contactor").val());//联系人
    // var mobile = $.trim($("#create_mobile").val());//联系电话
    // var industry = $.trim($("#create_industry").val());//行业
    // var address = $.trim($("#create_address").val());//地址
    if (!name && !pwd && !pwdToo && !account) {
        modals.error('必填项未输入，带*号为必填项！');
        return;
    }
    if (pwd !== pwdToo) {
        modals.error('两次输入的密码不一致！');
        return;
    }
    var option = {
        headers: {
            'Content-Type': 'application/json'
        }
    };
    var user = {
        "name": name,
        "password": pwd,
        "allowance": allowance,
        "remark": remark,
        "accountId": account,
        "phonePrefix": phonePrefix,
        "textSuffix": textSuffix,
        // "company": company,
        // "contactor": contactor,
        // "mobile": mobile,
        // "industry": industry,
        // "address": address,
    };
    postAjax('/user/add', JSON.stringify(user), option);
}