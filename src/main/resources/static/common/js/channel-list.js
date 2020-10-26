$(function () {
    var current = $("input[name='current']").val();
    var pageCount = $("input[name='pageCount']").val();
    //初始化加载分页
    getPage(current, pageCount);

    $('#channel_query_click').click(function () {
        var searchCode = $('#searchCode').val();
        var searchSystemId = $('#searchSystemId').val();
        var url = '/channel/list?searchCode=' + searchCode + '&searchSystemId=' + searchSystemId;
        $.get(url, null, function (data) {
            $("#mainDiv").empty();
            $("#mainDiv").append(data);
        });
    });

    $('#channel_reset_click').click(function () {
        $('#searchCode').val('');
        $('#searchSystemId').val('');
    });
});

function getPage(pageIndex, totalPage) {
    var url = '/channel/list?&page=';
    $(".information_page").createPage({    //创建分页
        pageCount: totalPage,      //总页数
        current: pageIndex,         //当前页
        url: url,
        backFn: function (p) {    //p不用管
            getPage(p, totalPage);         //点击页码或者跳转页码时的回掉函数，p为要跳转的页码
        }
    });
}

function delAccount(account) {
    if (confirm("确定删除该通道吗?")) {
        //点击确定后操作
        $.ajax({
            url: '/channel/del/' + account.id,
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
            },
            error: function (data) {
            }
        });
    }
}

function checkAccount(account) {
    if (account.code.substring(0, 1) === 'H') {
        modals.info('HTTP线路通道正常');
        return;
    }
    //点击确定后操作
    $.ajax({
        url: '/channel/check/' + account.code,
        type: "GET",
        data: {},
        success: function (data) {
            if (data.code == 200) {
                modals.info(data.message);
            } else {
                modals.error(data.message);
            }
            callback();
        },
        beforeSend: function () {
        },
        error: function (data) {
        }
    });
}

function refrenshAccount(account) {
    if (account.code.substring(0, 1) === 'H') {
        modals.info('重启HTTP线路通道成功');
        return;
    }
    //点击确定后操作
    $.ajax({
        url: '/channel/refrensh/' + account.code,
        type: "GET",
        data: {},
        success: function (data) {
            if (data.code == 200) {
                modals.info(data.message);
            } else {
                modals.error(data.message);
            }
            callback();
        },
        beforeSend: function () {
        },
        error: function (data) {
        }
    });
}

function callback() {
    var current = $("input[name='current']").val();
    $.get('/channel/list?page=' + current, function (data) {
        $('#mainDiv').empty().append(data);
    });
}

function goInfo(channelId) {
    var url = '/channel/addPage?channelId=' + channelId;
    $.get(url, null, function (data) {
        $("#mainDiv").empty();
        $("#mainDiv").append(data);
    });
}