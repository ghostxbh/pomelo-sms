$(function () {
    var current = $("input[name='current']").val();
    var pageCount = $("input[name='pageCount']").val();
    //初始化加载分页
    getPage(current, pageCount);
});

function getPage(pageIndex, totalPage) {
    var userName = $.trim($('#statis_userName').val());
    var url = '/statis/list?&userName=' + userName + '&page=';
    $(".information_page").createPage({    //创建分页
        pageCount: totalPage,      //总页数
        current: pageIndex,         //当前页
        url: url,
        backFn: function (p) {    //p不用管
            getPage(p, totalPage);         //点击页码或者跳转页码时的回掉函数，p为要跳转的页码
        }
    });
}

function statisReset() {
    $('#statis_userName').val('');
}

function statisQuery() {
    var userName = $.trim($('#statis_userName').val());
    $.get('/statis/list?userName=' + userName, function (data) {
        $('#mainDiv').empty().append(data);
    });
}

function goDetail(msg) {
    var statisId = msg.id;
    var searchName = msg.user.name;
    var url = '/sms/list?statisId=' + statisId + '&searchName=' + searchName;
    $.get(url, null, function (data) {
        $("#mainDiv").empty();
        $("#mainDiv").append(data);
    });
}