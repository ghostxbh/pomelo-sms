function add() {
    var mobiles = $("#mobiles").val();
    var upfile = $("#upfile")[0].files[0];
    var phoneList = mobiles.split(/[\s\n]/);//多条电话
    var content = $("#content").val();//短信类容
    var tab1 = $("#ordinary_send_tab").parent().hasClass('active');
    var tab2 = $("#text_send_tab").parent().hasClass('active');
    var option = {callback: callback()};
    var url = '/';
    var data;
    if (content.length < 1) {
        modals.info('无短信内容');
        return;
    }
    if (tab1) {
        url = '/sms/batchAdd';
        data = {"phoneList": phoneList, "content": content};
    } else if (tab2) {
        data = new FormData();
        url = '/sms/fileAdd';
        data.append("dataFile", upfile);
        data.append("content", content);
        option.processData = false;
        option.contentType = false;
    }
    postAjax(url, data, option);
}

function callback() {
    $("#mobiles").val('');
    $("#upfile").val('');
    $("#content").val('');
    $("#mobile_number").empty();
    $("#text_number").empty();
}

function updatecount() {
    var reg = '^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(16[0-9])|(17[013678])|(18[0,5-9])|(19[0-9]))\\d{8}$';
    var mobiles = $("#mobiles").val();
    if (mobiles != null && mobiles.length > 0) {
        var arr = mobiles.split(/[\s\n]/);//多条电话
        // var mobiles = [];
        // for (var ph in arr) {
        //     if (reg.match(arr[ph])) {
        //         mobiles.push(arr[ph]);
        //     }
        // }
        $("#mobile_number").empty();
        $("#mobile_number").append("<span>" + arr.length + "</span>")
    }
}

function updatecontent() {
    var content = $("#content").val();//短信类容
    if (content != null) {
        var count = content.length;
        $("#text_number").empty();
        $("#text_number").append("<span>" + count + "</span>");
        if (count % 67 > 0) {
            var contentcount = parseInt(count / 67) + 1;
        } else {
            var contentcount = parseInt(count / 67);
        }
        $("#sm_number").empty();
        $("#sm_number").append("<span>" + contentcount + "</span>");
    }
}