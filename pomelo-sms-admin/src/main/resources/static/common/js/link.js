function resetLink() {
    $("#link_name").val('');
    $("#link_url").val('');
    $("#link_enable").val(1);
    $("#link_remark").val('');
}

function addLink() {
    var id = $.trim($("#link_id").val());
    var name = $.trim($("#link_name").val());
    var url = $.trim($("#link_url").val());
    var enabled = $.trim($("#link_enable").val());
    var remark = $.trim($("#link_remark").val());
    if (!name && !url) {
        modals.error('必填项未输入，带*号为必填项！');
        return;
    }
    var option = {
        headers: {
            'Content-Type': 'application/json'
        }
    };
    var link = {
        "name": name,
        "url": url,
        "enabled": enabled,
        "remark": remark
    };
    if (id) {
        link.id = id;
        postAjax('/link/update', JSON.stringify(link), option);
    } else {
        postAjax('/link/add', JSON.stringify(link), option);
    }
}