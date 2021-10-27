$(function () {
    $('#userInfo_submit').click(function () {
        var id = $.trim($("#userInfo_id").val());//姓名
        var industry = $.trim($("#userInfo_industry").val());//行业
        var address = $.trim($("#userInfo_address").val());//所属地址
        var contactor = $.trim($("#userInfo_contactor").val());//联系人
        var mobile = $.trim($("#userInfo_mobile").val());//电话
        var company = $.trim($("#userInfo_company").val());//公司名
        if (!industry && !address && !contactor && !mobile && !company) {
            modals.info('信息无修改！');
            return;
        }
        var option = {
            headers: {
                'Content-Type': 'application/json'
            }
        };
        var user = {id, industry, address, contactor, mobile, company};
        postAjax('/user/update', JSON.stringify(user), option);
    });

    $('#userInfo_reset').click(function () {
        $("#userInfo_industry").val('');//行业
        $("#userInfo_address").val('');//所属地址
        $("#userInfo_contactor").val('');//联系人
        $("#userInfo_mobile").val('');//电话
        $("#userInfo_company").val('');//公司名
        $("#userInfo_remark").val('');//备注
    });
});
