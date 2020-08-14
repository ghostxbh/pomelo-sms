var form = null;
var queryForm = null;
var dataForm = null;
var dtTable;

function initBasePage(saveOrDeleteUrl, getUrl, tableId) {
    //选择日期后不能触发Validform，故让其失去焦点
    $('input[data-flag="datepicker"]').on("change", function () {
        $(this).blur();
    });

    tableId = tableId ? tableId : "data-table";

    if ($("#"+tableId).length > 0) {
        dtTable = new DtTable(tableId, "searchDiv");

        //button event
        $("#"+tableId).on("click", "[data-btn-type]", function () {
            var action = $(this).attr('data-btn-type');
            //var rowId = dtTable.getSelectedRowId();
            var rowId = $(this).data("value");
            switch (action) {
                case 'remove':
                    if (!rowId) {
                        modals.info('请选择要删除的行');
                        return false;
                    }
                    modals.confirm("是否要删除这些记录？", function () {
                        ajaxPost(saveOrDeleteUrl + "/" + rowId, null, function (data) {
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
                    });
                    break;
            }
        });
    }

    if ($("#searchDiv").length > 0) {
        var searchDiv = $("#searchDiv").form({baseEntity: false});
        searchDiv.initComponent();
    }

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
                ajaxPost(saveOrDeleteUrl, params, function (data, status) {
                    if (data.success) {
                        modals.info(data.message);
                    } else {
                        modals.error(data.message);
                    }
                });
                return false;//实际不提交
            }
        });

        //回填id
        if (getUrl && getUrl != "") {
            ajaxPost(getUrl, null, function (data) {
                form.initFormData(data);
            })
        }
    }

    if ($("#query-form").length > 0) {
        $("#query-form").attr("onkeydown", "if(event.keyCode==13){$('[data-btn-type=search]').click();return false;}");
        queryForm = $("#query-form").Validform({
            tiptype: function (msg, o) {
                if (msg != "") {
                    //modals.popup({text: msg});
                    //showPopover(msg, o.obj);
                    layer.tips(msg, o.obj, {tips: 1});
                }
            }
        });
    }
}