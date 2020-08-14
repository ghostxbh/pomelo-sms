function initTablePage(saveUrl) {
    //init table and fill data
    if ($("#data-table").length > 0) {
        //选择日期后不能触发Validform，故让其失去焦点
        $('input[data-flag="datepicker"]').on("change", function () {
            $(this).blur();
        });

        dtTable = new DtTable("data-table", "searchDiv");

        $('[data-btn-type=add]').click(function () {
            setTitle("新增");
            historyPushState(ctrlPath + "/get");
            ajaxPost(ctrlPath + "/get", null, function (data) {
                $("#nav-tab-edit").click();
                form.initFormData(data);
            });
        });

        $('[data-btn-type=cancel]').click(function () {
            $("#nav-tab-list").click();
        });

        $("#nav-tab-edit").click(function () {
            if ($form.length > 0) {
                form.clearForm();
            }
        });

        $("#data-table").on("click", "[data-btn-type]", function () {
            var action = $(this).attr('data-btn-type');
            switch (action) {
                case 'edit':
                    //var rowId = dtTable.getSelectedRowId();
                    var rowId = $(this).data("value");
                    if (!rowId) {
                        modals.info('请选择要编辑的行');
                        return false;
                    }
                    setTitle("编辑");
                    historyPushState(ctrlPath + "/get/" + rowId);
                    ajaxPost(ctrlPath + "/get/" + rowId, null, function (data) {
                        $("#nav-tab-edit").click();
                        form.initFormData(data);
                    });
                    break;
                case 'remove':
                    //var rowId = dtTable.getSelectedRowId();
                    var rowId = $(this).data("value");
                    if (!rowId) {
                        modals.info('请选择要删除的行');
                        return false;
                    }
                    modals.confirm("是否要删除该行数据？", function () {
                        ajaxPost(ctrlPath + "/delete/" + rowId, null, function (data) {
                            if (data.success) {
                                //modals.correct("已删除该数据");
                                dtTable.reloadRowData();
                            } else {
                                if (data.message) {
                                    modals.error(data.message);
                                } else {
                                    modals.error("记录可能被引用，不可删除！");
                                }
                            }
                        });
                    })
                    break;
            }
        });
    }

    if ($("#searchDiv").length > 0) {
        var searchDiv = $("#searchDiv").form({baseEntity: false});
        searchDiv.initComponent();
    }

    //when user click nav-tab-list tab, then set default title
    $("#nav-tab-list").on("click", function () {
        setTitle("列表");
    });

    var $form = $("#data-form");
    if ($form.length > 0) {
        form = $form.form();
        form.initComponent();

        dataForm = $form.Validform({
            tiptype: 2,
            label: "label",
            showAllError: true
        });

        dataForm.config({
            beforeSubmit: function (curform) {
                //Save Data，对应'submit-提交'
                var params = form.getFormSimpleData();
                ajaxPost(saveUrl, params, function (data, status) {
                    if (data.success) {
                        if (dtTable) {
                            if (dtTable.getSelectedRowId()) {//更新
                                dtTable.reloadRowData(dtTable.getSelectedRowId());
                            } else {//新增
                                dtTable.reloadData();
                            }
                            $("#nav-tab-list").click();
                        } else {
                            modals.info(data.message);
                        }
                    } else {
                        modals.error(data.message);
                    }
                });
                return false;//实际不提交
            }
        });
    }

    if ($("#query-form").length > 0) {
        $("#query-form").attr("onkeydown", "if(event.keyCode==13){$('[data-btn-type=search]').click();return false;}"); // 回车键查询
        queryForm = $("#query-form").Validform({
            tiptype: function (msg, o) {
                if (msg != "") {
                    //modals.popup({text: msg});
                    layer.tips(msg, o.obj, {tips: 1});
                }
            }
        });
    }
}

//set title for current tab
function setTitle(title) {
    $("ul.nav-tabs li.header small").text(title);
}