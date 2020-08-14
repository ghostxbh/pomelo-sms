$(".form_datetime").datetimepicker({
    format: "yyyy-mm-dd hh:ii",
    autoclose: true,
    todayBtn: true,
    language: 'zh-CN',
    pickerPosition: "bottom-left"
});

$(function () {
    var current = $("input[name='current']").val();
    var pageCount = $("input[name='pageCount']").val();
    //初始化加载分页
    getPage(current, pageCount);

    $('#sms_query_click').click(function () {
        var startTime = $('#start_time').val();
        var endTime = $('#end_time').val();
        var searchName = $('#search_name').val();
        var searchPhone = $('#search_phone').val();
        var statisId = $('#statisId').val();
        var url = '/sms/list?startTime=' + startTime + '&endTime=' + endTime + '&searchName=' + searchName + '&searchPhone=' + searchPhone + '&statisId=' + statisId;
        $.get(url, null, function (data) {
            $("#mainDiv").empty();
            $("#mainDiv").append(data);
        });
    });

    $('#sms_reset_click').click(function () {
        $('#start_time').val('');
        $('#end_time').val('');
        $('#search_name').val('');
        $('#search_phone').val('');
    });

    $('#sms_export').click(function () {
        var startTime = $('#start_time').val();
        var endTime = $('#end_time').val();
        var searchName = $('#search_name').val();
        var searchPhone = $('#search_phone').val();
        var statisId = $('#statisId').val();
        var url;
        if (confirm('是否导出全部?')) {
            url = '/sms/export?startTime=' + startTime + '&endTime=' + endTime + '&searchName=' + searchName + '&searchPhone=' + searchPhone + '&statisId=' + statisId;
        } else {
            url = '/sms/export?page=1&pageSize=20&startTime=' + startTime + '&endTime=' + endTime + '&searchName=' + searchName + '&searchPhone=' + searchPhone + '&statisId=' + statisId;
        }
        var tempwindow = window.open('_blank');
        tempwindow.location = url;
    });
});

function getPage(pageIndex, totalPage) {
    var startTime = $('#start_time').val();
    var endTime = $('#end_time').val();
    var searchName = $('#search_name').val();
    var searchPhone = $('#search_phone').val();
    var statisId = $('#statisId').val();
    var url = '/sms/list?&startTime=' + startTime + '&endTime=' + endTime + '&searchPhone=' + searchPhone + '&statisId=' + statisId;
    if (searchName) url += '&searchName=' + searchName;
    url += '&page=';
    $(".information_page").createPage({    //创建分页
        pageCount: totalPage,      //总页数
        current: pageIndex,         //当前页
        url: url,
        backFn: function (p) {    //p不用管
            getPage(p, totalPage);         //点击页码或者跳转页码时的回掉函数，p为要跳转的页码
        }
    });
}