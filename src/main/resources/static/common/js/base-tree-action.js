//初始化form表单
var $treeForm;
var treeForm;

function initTreePage(saveUrl) {
    $treeForm = $("#tree-form");
    if ($treeForm.length > 0) {
        treeForm = $treeForm.form();
        //初始化校验
        dataForm = $treeForm.Validform({
            tiptype: 2,
            label: "label",
            showAllError: true
        });

        dataForm.config({
            beforeSubmit: function (curform) {
                //Save Data，对应'submit-提交'
                var params = treeForm.getFormSimpleData();

                ajaxPost(saveUrl, params, function (data, status) {
                    if (data.success) {
                        var selectedArr=$("#tree").data("treeview").getSelected();
                        var selectedNodeId=selectedArr.length>0?selectedArr[0].nodeId:0;
                        initTree(selectedNodeId);

                        $(".Validform_checktip").removeClass("Validform_right").removeClass("Validform_wrong").html("");
                    }else{
                        modals.error(data.message);
                    }
                });
                return false;//实际不提交
            }
        });

        initTree(0);
        treeForm.initComponent();
        //按钮事件
        var btntype = null;
        $('[data-btn-type]').click(function () {
            var action = $(this).attr('data-btn-type');
            var selectedArr = $("#tree").data("treeview").getSelected();
            var selectedNode = selectedArr.length > 0 ? selectedArr[0] : null;
            switch (action) {
                case 'addRoot':
                    formWritable(action);
                    treeForm.clearForm();
                    $("#icon_i").removeClass();
                    //填充上级菜单和层级编码
                    fillParentAndLevelCode(null);
                    btntype = 'add';
                    break;
                case 'add':
                    if (!selectedNode) {
                        modals.info('请先选择上级菜单');
                        return false;
                    }
                    formWritable(action);
                    treeForm.clearForm();
                    $("#icon_i").removeClass();
                    //填充上级菜单和层级编码
                    fillParentAndLevelCode(selectedNode);
                    btntype = 'add';
                    break;
                case 'edit':
                    if (!selectedNode) {
                        modals.info('请先选择要编辑的节点');
                        return false;
                    }
                    if (btntype == 'add') {
                        fillDictForm(selectedNode);
                    }
                    formWritable(action);
                    btntype = 'edit';
                    break;
                case 'remove':
                    if (!selectedNode) {
                        modals.info('请先选择要删除的节点');
                        return false;
                    }
                    if (btntype == 'add')
                        fillDictForm(selectedNode);
                    formReadonly();
                    $(".box-header button[data-btn-type='remove']").removeClass("btn-default").addClass("btn-primary");
                    if (selectedNode.nodes) {
                        modals.info('该节点含有子节点，请先删除子节点');
                        return false;
                    }
                    modals.confirm('是否删除该节点', function () {
                        ajaxPost(ctrlPath + "/delete/" + selectedNode.id, null, function (data) {
                            if (data.success) {
                                modals.correct('删除成功');
                            } else {
                                modals.info(data.message);
                            }
                            //定位
                            var brothers = $("#tree").data("treeview").getSiblings(selectedNode);
                            if (brothers.length > 0)
                                initTree(brothers[brothers.length - 1].nodeId);
                            else {
                                var parent = $("#tree").data("treeview").getParent(selectedNode);
                                initTree(parent ? parent.nodeId : 0);
                            }
                        });
                    });
                    break;
                case 'cancel':
                    if (btntype == 'add')
                        fillDictForm(selectedNode);
                    formReadonly();
                    break;
                case 'selectIcon':
                    var disabled = $(this).hasClass("disabled");
                    if (disabled)
                        break;
                    var iconName;
                    if ($("#pms_ico").val())
                        iconName = encodeURIComponent($("#pms_ico").val());
                    modals.openWin({
                        winId: 'iconWin',
                        title: '图标选择器（双击选择）',
                        width: '1000px',
                        url: "/comm/icon/nodecorator?icon_name=" + iconName
                    });
                    break;
            }
        });
    }
}

function initTree(selectNodeId) {
    var treeData = null;
    ajaxPost(ctrlPath + "/loadData", null, function (data) {
        treeData = data;
        //console.log(JSON.stringify(treeData));
    });
    $("#tree").treeview({
        data: treeData,
        showBorder: true,
        levels: 2,
        onNodeSelected: function (event, data) {
            /*   alert("i am selected");
              alert(data.nodeId); */
            fillDictForm(data);
            formReadonly();
            //console.log(JSON.stringify(data));
        }
    });
    if (treeData.length == 0)
        return;
    //默认选中第一个节点
    selectNodeId = selectNodeId || 0;
    $("#tree").data('treeview').selectNode(selectNodeId);
    $("#tree").data('treeview').expandNode(selectNodeId);
    $("#tree").data('treeview').revealNode(selectNodeId);

    //点击文字也能展开或收缩
    $('#tree').on('click','.list-group-item',function(event){
        if(event.target.nodeName!='SPAN') {
            var g = $(event.target).find(".glyphicon");
            if(g.length>0 && (g.eq(0).hasClass("glyphicon-plus") || g.eq(0).hasClass("glyphicon-minus"))) {
                g.eq(0).click();
            }
        }
    });
}

//新增时，带入父级菜单名称id
function fillParentAndLevelCode(selectedNode) {
    $("input[name='parent_name']").val(selectedNode ? selectedNode.text : '系统菜单');
    //$("input[name='deleted'][value='0']").prop("checked", "checked");
    if (selectedNode) {
        $("input[name='parent_id']").val(selectedNode.id);
    }else{
        $("input[name='parent_id']").val("#");
    }
}

//填充form
function fillDictForm(node) {
    treeForm.clearForm();
    ajaxPost(ctrlPath + "/get/" + node.id, null, function (data) {
        treeForm.initFormData(data);
        fillBackIconName(data.pms_ico);
    })
}

//设置form为只读
function formReadonly() {
    //所有文本框只读
    $("input[name],textarea[name]").attr("readonly", "readonly");
    $("input:checkbox").attr("disabled", "disabled");
    $("input:radio").attr("disabled", "disabled");
    //隐藏取消、保存按钮
    $treeForm.find(".box-footer").hide();
    //还原新增、编辑、删除按钮样式
    $(".box-header button").removeClass("btn-primary").addClass("btn-default");
    //选择图标按钮只读
    $("#selectIcon").addClass("disabled");
    //还原校验框
    $(".Validform_checktip").removeClass("Validform_right").removeClass("Validform_wrong").html("");
}

function formWritable(action) {
    $("input[name],textarea[name]").removeAttr("readonly");
    $("input:checkbox").removeAttr("disabled");
    $("input:radio").removeAttr("disabled");
    $treeForm.find(".box-footer").show();
    $(".box-header button").removeClass("btn-primary").addClass("btn-default");
    $("#selectIcon").removeClass("disabled");
    if (action)
        $(".box-header button[data-btn-type='" + action + "']").removeClass("btn-default").addClass("btn-primary");
}

//回填图标
function fillBackIconName(icon_name) {
    $("#pms_ico").val(icon_name);
    $("#icon_i").removeClass().addClass("form-control-feedback").addClass(icon_name);
}