$(function () {
    var current = $("input[name='current']").val();
    var pageCount = $("input[name='pageCount']").val();
    //初始化加载分页
    getPage(current, pageCount);

    $('#users_query_click').click(function () {
        var searchName = $('#search_name').val();
        var searchPhone = $('#search_phone').val();
        var url = '/manager/users?searchName=' + searchName + '&searchPhone=' + searchPhone;
        $.get(url, null, function (data) {
            $("#mainDiv").empty();
            $("#mainDiv").append(data);
        });
    });

    $('#users_reset_click').click(function () {
        $('#search_name').val('');
        $('#search_phone').val('');
    });
});

function getPage(pageIndex, totalPage) {
    var url = '/manager/users?&page=';
    $(".information_page").createPage({    //创建分页
        pageCount: totalPage,      //总页数
        current: pageIndex,         //当前页
        url: url,
        backFn: function (p) {    //p不用管
            getPage(p, totalPage);         //点击页码或者跳转页码时的回掉函数，p为要跳转的页码
        }
    });
}

function delUser(user) {
    if (confirm("确定删除该用户吗?")) {
        //点击确定后操作
        $.ajax({
            url: '/manager/del/' + user.id,
            type: "DELETE",
            data: {},
            success: function (data) {
                if (data.code == 200) {
                    modals.info("操作结果 ：" + data.message);
                } else {
                    modals.error(data.message);
                }
                callback();
            },
            beforeSend: function () {
                // $("#btn_login").val("登录中...");
            },
            error: function (data) {

            }
        });
    }
}

function modifyAllowanceModal(user) {
    $('#allowance_id').val(user.id);
    $('#allowance_name').val(user.name);
    $('#allowance').val(user.allowance);
    $('#myModal').modal('show');
}

function modifyAllowance() {
    var id = $('#allowance_id').val();
    var allowance = $('#allowance').val();
    var account = $('#account').val();
    var reg = /^\d+(\.\d+)?$/;
    if (!reg.test(allowance)) {
        $('#manager_users_modal_close').click();
        modals.error('余额输入必须为数字');
        return;
    }
    if (!account) {
        modals.error('未选择短信通道');
        return;
    }
    $.post('/manager/allowance', {"id": id, "allowance": allowance, "account": account}, function (data) {
        if (data.code == 200) {
            modals.info("操作结果 ：" + data.message);
        } else {
            modals.error(data.message);
        }
        callback();
    });
}

function callback() {
    var current = $("input[name='current']").val();
    $('#manager_users_modal_close').click();
    $.get('/manager/users?page=' + current, function (data) {
        $('#mainDiv').empty().append(data);
    });
}