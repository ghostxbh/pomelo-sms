$(function () {
    var current = $("input[name='current']").val();
    var pageCount = $("input[name='pageCount']").val();
    //初始化加载分页
    getPage(current, pageCount);

    $('#link_query_click').click(function () {
        var searchName = $('#searchName').val();
        var url = '/link/list?searchName=' + searchName;
        $.get(url, null, function (data) {
            $("#mainDiv").empty();
            $("#mainDiv").append(data);
        });
    });

    $('#link_reset_click').click(function () {
        $('#searchName').val('');
    });
});

function getPage(pageIndex, totalPage) {
    var url = '/link/list?&page=';
    $(".information_page").createPage({    //创建分页
        pageCount: totalPage,      //总页数
        current: pageIndex,         //当前页
        url: url,
        backFn: function (p) {    //p不用管
            getPage(p, totalPage);         //点击页码或者跳转页码时的回掉函数，p为要跳转的页码
        }
    });
}

function del(linkId) {
    if (confirm("确定删除该地址吗?")) {
        //点击确定后操作
        $.ajax({
            url: '/link/del/' + linkId,
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

function callback() {
    var current = $("input[name='current']").val();
    $.get('/link/list?page=' + current, function (data) {
        $('#mainDiv').empty().append(data);
    });
}

function goInfo(linkId) {
    var url = '/link/addPage?linkId=' + linkId;
    $.get(url, null, function (data) {
        $("#mainDiv").empty();
        $("#mainDiv").append(data);
    });
}