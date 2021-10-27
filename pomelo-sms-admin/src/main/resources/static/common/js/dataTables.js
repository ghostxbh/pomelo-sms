/**
 * 通用表格组件，基于jquery-DataTable组件的扩展
 *
 * @param tableId
 *            table组件id
 * @param searchDiv
 *            查询条件div的id
 * @author bill1012 qq:475572229
 *
 */
(function ($, window, document, undefined) {
//'use strict';

    window.DtTable2 = function (config) {
        return new DtTable(config.tableId, config.searchDiv, config.url, config.config);
    }

    //window.DtTable=window.DtTable;
    window.DtTable = function (tableId, searchDiv, url, config) {
        this.tableId = tableId;
        this.searchDiv = searchDiv;
        this.url = url;
        if (!this.url) {
            this.url = ctrlPath + "/loadData";
        }
        this.data = null;
        this.loaded = false;
        this.config = config;
        this.serverCallback = null;

        // 用作缓存一些数据
        var dataCache = $("#dataCache" + tableId);
        if (dataCache.length == 0) {
            dataCache = $("<div></div>");
            dataCache.attr("id", "dataCache" + tableId);
            $(document.body).append(dataCache);
        }
        this.dataCache = dataCache;
        this.dataCache.data("tableId", this.tableId);
        //绑定查询事件
        var searchButton = $("#" + searchDiv + " button[data-btn-type='search']");
        this.searchButton = searchButton;
        var resetButton = $("#" + searchDiv + " button[data-btn-type='reset']");
        this.resetButton = resetButton;

        //用户自定义列
        var customButton = $("#" + searchDiv + " button[data-btn-type='task']");
        this.customButton = customButton;
        // 表格横向自适应
        $("#" + this.tableId).css("width", "100%");
        // 初始化表格
        this.initTable(tableId, searchDiv);
    }

    /**
     * 初始化表格
     */
    DtTable.prototype.initTable = function (tableId, searchDiv) {
        this.data = this.getServerData(this.dataCache.data("pageInfo"), tableId);
        if (this.data == null) return;
        this.dataCache.data("data", this.data);
        //console.log(JSON.stringify(this.data));
        var that = this;
        var columns = [];
        var obj;
        var hasCheckbox = this.data.query.hasCheckbox;
        if (hasCheckbox != null) {
            obj = {
                "data": "DT_RowId",
                "title": "<input type='checkbox' id='checkidAll'>全选",
                "sortable": false,
                "visible": hasCheckbox == 0 ? false : true,//不知道为什么无效果
                "width": '50px',
                "class": hasCheckbox == 0 ? "desktop" : "",
                render: function (data) {
                    return "<input type='checkbox' name='checkid' value='" + data + "'>";
                }
            };
            columns.push(obj);
        }
        if (this.data.query.hasSeq) {
            obj = {
                "data": null,
                "title": "序号",
                "sortable": false,
                "width": '35px',
                "render": function (data, type, full, meta) {
                    return meta.row + 1 + meta.settings._iDisplayStart;
                }
            };
            columns.push(obj);
        }
        var hiddenCols = this.getCustomHiddenColumns(this.customButton.data('pagename'));
        for (var i = 0; i < this.data.columns.length; i++) {
            var column = this.data.columns[i];
            obj = {};
            obj["data"] = column.key;
            obj["title"] = column.title;
            obj["name"] = column.id || column.key;
            obj["visible"] = !column.hidden;
            if (column.allowSort) {
                obj["sortable"] = column.allowSort;
            } else {
                obj["sortable"] = false;
            }
            // if (column.align) {
            //     obj["class"] = "text-" + column.align;
            // } else {
            //     obj["class"] = "text-center";
            // }
            if (column.class) {
                obj["class"] = column.class;
            }
            if (column.width) {
                obj["width"] = column.width;
            }
            //obj["sWidthOrig"]=null;

            //判断是否通过自定义隐藏
            if (hiddenCols && hiddenCols.length > 0) {
                for (var j = 0; j < hiddenCols.length; j++) {
                    if (column.key == hiddenCols[j])
                        obj["visible"] = false;
                }
            }
            columns.push(obj);
        }

        // alert(JSON.stringify(columns));
        var allowPaging = this.data.query.allowPaging;
        this.table = $('#' + tableId).DataTable($.extend({
            "paging": allowPaging, // 分页
            "lengthChange": allowPaging, // 每页记录数可选项
            "lengthMenu": [[20, 50, 100], [20, 50, 100]],
            "searching": false, // 过滤
            "sort": this.data.query.sort,//是否启动各个字段的排序功能
            "info": allowPaging, // 分页明细
            "autoWidth": false,
            //"stateSave" : true,// 这样就可以在删除返回时，保留在同一页上
            "dom": 'rt<"row"<"col-sm-5"il><"col-sm-7"p>>',
            "processing": true,// 是否显示取数据时的那个等待提示
            "pagingType": "full_numbers",// 分页样式
            "language": { // 中文支持
                "sUrl": "/assets/common/json/datatables_zh_CN.json"
            },
            "displayLength": that.data.pageInfo.pageSize,// 每页记录条数，默认为10
            "serverSide": true,
            "ajaxDataProp": "data",
            "ajaxSource": this.url,
            "fnServerData": $.proxy(that.fillDataTable, that),
            "fnInitComplete": that.data.query.fnInitComplete ? eval(that.data.query.fnInitComplete) : $.proxy(that.fnInitComplete, that),
            "singleSelect": true,  //单选
            "aoColumns": columns
        }, that.config));

        //全选
        if (this.data.query.hasCheckbox != null) {
            $(document).on("click", "#checkidAll", function () {
                //console.log($(this).prop("checked"));
                var check = $(this).prop("checked");
                $("input[name='checkid']").prop("checked", check);
            });
        }

        if (this.searchButton) {
            this.searchButton.click(function () {
                if (queryForm && queryForm.check(false)) {
                    that.table.page('first').draw(false);
                    // 执行查询的回调函数
                    if (that.searchButton.data("callback")) {
                        eval(that.searchButton.data("callback"));
                    }
                }
            });
        }

        //用户自定义列
        if (this.customButton) {
            this.customButton.click(function () {
                var pagename = that.customButton.data("pagename");
                if (!pagename) {
                    modals.info("该按钮未定义data-pagename属性，请先定义");
                    return;
                }
                modals.openWin({
                    winId: 'customWin',
                    title: '【' + that.data.query.tableName + '】自定义列',
                    width: '400px',
                    url: ctrlPath + "/getCustomColumn?pageName=" + pagename,
                    hideFunc: function () {
                        that.setVisible();
                    }
                });
            })
        }

        if (this.resetButton) {
            this.resetButton.click(function () {
                //清除查询条件
                that.clearSearchDiv(that.searchDiv);
                //清除排序、分页、重置初始长度
                //that.table.order([]).page.len(20).draw();
                //自动提交
                that.searchButton.trigger("click");
                if (that.resetButton.data("callback")) {
                    eval(that.resetButton.data("callback"));
                }
            });
        }
    }

    //自定义单元格的可见性
    DtTable.prototype.setVisible = function () {
        var hiddenCols = this.getCustomHiddenColumns(this.customButton.data('pagename'));
        if (!hiddenCols)
            return;
        var dataArr = this.table.columns().dataSrc();
        var self = this;
        $.each(dataArr, function (index, columnName) {
            var column = self.data.columns[index];
            if (column.hidden) {
                self.table.column(index).visible(false, false);
            } else {
                self.table.column(index).visible(true, false);
            }
            $.each(hiddenCols, function (hindex, hcolName) {
                if ((column.id || column.key) == hcolName)
                    self.table.column(index).visible(false, false);
            })
        })
        this.table.columns.adjust().draw(false);
    }

    DtTable.prototype.showCheckbox = function (b) {
        this.table.column(0).visible(b);
    }


    DtTable.prototype.clearSearchDiv = function (selector) {
        var sel = $(selector).length > 0 ? $(selector) : $("#" + this.searchDiv);
        sel.find(':input[name]:not(:radio):not([data-noreset])').val('');
        sel.find(':radio').prop('checked', false);
        //sel.find(':radio[data-flag]').iCheck('update');
        sel.find(':checkbox').prop('checked', false);
        //sel.find(':checkbox[data-flag]').iCheck('update');
        sel.find('select:not(.select2)').val("");
        sel.find("select.select2").val(null).trigger("change");
    }

// 表格初始化后移动查询组件位置 oSettings=配置；json=数据记录；
    DtTable.prototype.fnInitComplete = function (oSettings, json) {
        // 移动查询框的位置 与记录/页同行
        var _this = this;
        /*if (!$('.col-sm-9:eq(0)', this.table.table().container()).html()) {
            $("#" + this.searchDiv).appendTo($('.col-sm-9:eq(0)', this.table.table().container())).show();
        }*/

        // 列头文本居中
        //this.tableId=oSettings.sTableId
        $("#" + this.tableId + " thead tr th").removeClass("text-left").removeClass("text-right").addClass("text-center");

        //行单选
        if (oSettings.oInit.singleSelect == true) {
            $('#' + this.tableId + ' tbody').on('click', 'tr', function () {
                if (!$(this).hasClass('selected')) {
                    _this.table.$('tr.selected').removeClass('selected');
                    $(this).addClass('selected');

                    if (oSettings.oInit.rowClick) {
                        oSettings.oInit.rowClick.call(this, _this.getSelectedRowData(), $(this).hasClass('selected'));
                    }
                }
            });
        } else if (oSettings.oInit.singleSelect == false) {
            $('#' + this.tableId + ' tbody').on('click', 'tr', function () {
                $(this).toggleClass('selected');
            })
        }

        if (oSettings.oInit.loadComplete) {
            oSettings.oInit.loadComplete.call(this);
        }

        //如果分页不可选 则空出位置 让条件区域更宽
        if (!oSettings.oInit.lengthChange) {
            $("#" + this.tableId + "_wrapper div.row").eq(0).find("div.col-sm-3").remove();
            $("#" + this.tableId + "_wrapper div.row").eq(0).find("div.col-sm-9").removeClass("col-sm-9").addClass("col-sm-12");
        }

        //Y轴滚动时，设置列头自适应
        if (oSettings.oInit.scrollY) {
            setTimeout(function () {
                _this.table.columns.adjust();
            }, 200);
            //setTimeout(function(){_this.fixHeaderWidth()},100);
        }
    }


    DtTable.prototype.fixHeaderWidth = function () {
        var _this = this;
        var width = $("#" + this.tableId).find("tbody tr:first").width();
        console.log(width)
        if (width > 0) {
            //$("#"+_this.tableId+"_wrapper div.dataTables_scrollHeadInner table").css("width",width).parent().css("width",width);
            $("#" + this.tableId).find("tbody tr:first td").each(function (index, item) {
                console.log($("#" + _this.tableId + "_wrapper div.dataTables_scrollHeadInner table").find("thead").length);
                console.log("width" + index + ":" + $("#" + _this.tableId).find("thead tr:first th").eq(index).css('width'));
                var thwidth = $("#" + _this.tableId).find("thead tr:first th").eq(index).css('width')
                //if(thwidth=="0px"){
                $("#" + _this.tableId).find("thead tr:first th").eq(index).css("width", $(item).width());
                $("#" + _this.tableId + "_wrapper div.dataTables_scrollHeadInner table").css("width", width).parent().css("width", width);
                $("#" + _this.tableId + "_wrapper div.dataTables_scrollHeadInner table").find("thead tr:first th").eq(index).css("width", $(item).width());
                //}
                console.log($(item).width());
            })
        } else {
            //console.log("this.fixHeaderWidth();");
            this.fixHeaderWidth();
        }

    }

    DtTable.prototype.getTable = function () {
        return this.table;
    }

    DtTable.prototype.getSelectedRowId = function () {
        if (this.table.row('.selected').length > 0)
            return this.table.row('.selected').id();
        return null;
    }

    DtTable.prototype.getSelectedRowIndex = function () {
        if (this.table.row('.selected').length > 0)
            return this.table.row('.selected').login();
        return null;
    }

    DtTable.prototype.getCheckedRowId = function () {
        var a = [];
        var i = 0;
        $("input[name='checkid']:checked").each(function () {
            a[i++] = $(this).val()
        });
        return a;
    }
    /**
     * 获取当前选中行的数据 单选
     */
    DtTable.prototype.getSelectedRowData = function () {
        if (this.table.row('.selected').length > 0)
            return this.table.row('.selected').data();
        return null;
    }


    DtTable.prototype.getRowDataByRowId = function (id) {
        if (this.table.row("#" + id).length > 0)
            return this.table.row("#" + id).data();
        return null;
    }
    /**
     * 获取当前选中行的数据 多选
     */
    DtTable.prototype.getSelectedRowsData = function () {
        var datas = null;
        var rows = this.table.rows('.selected').data();
        if (rows.length == 0)
            return datas;
        datas = [];
        for (var i = 0; i < rows.length; i++) {
            datas.push(rows[i]);
        }
        return datas;
    }


    //新增，刷新界面
    DtTable.prototype.reloadData = function () {
        this.table.page('first').draw(false);
    }

    //刷新当前页面，并定位到行
    DtTable.prototype.reloadRowData = function (rowId) {
        var dataCache = $("#dataCache" + this.tableId);
        var pageInfo = dataCache.data("pageInfo");
        var pageIndex = pageInfo == null ? "first" : pageInfo.pageNum - 1;
        this.table.page(pageIndex).draw(false);
        if (rowId) {//定位选中到当前行
            this.selectRow(rowId);
        }
    }

    //选中行
    DtTable.prototype.selectRow = function (rowId, triggerEvent) {
        if (rowId) {
            this.selectRowWithSelector("#" + rowId, triggerEvent)
        }
    }

    //选中第一行
    DtTable.prototype.selectFirstRow = function (triggerEvent) {
        this.selectRowWithSelector("tr:first", triggerEvent);
    }

    //通用选择
    DtTable.prototype.selectRowWithSelector = function (selector, triggerEvent) {
        if (selector) {
            if (triggerEvent) {
                this.table.$(selector).click();
            } else {
                this.table.$('tr.selected').removeClass('selected');
                this.table.$(selector).addClass('selected');
            }
        }
    }


    /**
     * 清除行选中
     */
    DtTable.prototype.clearSelection = function () {
        this.table.row('.selected').remove().draw(false);
    }

// 获取查询框中的查询数据
// isCondition默认为true;likeOption默认false 即不拼接%
    DtTable.prototype.fnGetConditions = function (searchDiv) {
        var searchDiv = $("#" + this.searchDiv);
        var conditions = {};
        if (searchDiv !== null && searchDiv.length > 0) {
            var ele = searchDiv.find(':input[name]');
            ele.each(function (i) {
                if ($(this).attr("readonly") == "readonly" || $(this).attr("disabled") == "disabled")
                    return;
                var key = $(this).attr("name");
                // alert("key:"+key+" id:"+$(this).attr("id"));
                var value = $(this).val();
                var type = $(this).attr("type");

                //type == "text" || type == "search"
                if ((type && (type.toLowerCase() == 'checkbox' || type.toLowerCase() == 'radio'))) {
                    if (!$(this).prop("checked")) {
                        value = "";
                    }
                }
                if ((type && (type == 'select-one'))) {
                    if (!value) {
                        value = "";
                    }
                }
                if (!value || value == "") {
                    return true;
                }

                if (conditions.hasOwnProperty(key)) {
                    conditions[key] += "," + value;
                } else {
                    conditions[key] = value;
                    // alert("key:"+key+" value:"+value);
                }
            });
        } else {
            // no search conditions found.
        }
        //console.log("conditons:"+JSON.stringify(conditions));
        return conditions;
    }

    /**
     * 获取服务器中的数据
     *
     * @param pageInfo
     *            分页信息
     * @param tableId
     *            table的ID
     */
    DtTable.prototype.getServerData = function (pageInfo, tableId) {
        var dataCache = $("#dataCache" + tableId);
        //console.log(document.getElementById("mainDiv"));
        var reqParam = {
            pageInfo: pageInfo,
            sortInfo: dataCache.data("sortInfo"),
            conditions: this.fnGetConditions(this.searchDiv)
        };
        dataCache.data("pageInfo", pageInfo);
        var retData = null;
        //console.log("reqObj:");
        //console.log(reqParam);
        //console.log(JSON);
        //注释以上部分，统一用ajaxPost处理，以便处理session超时（ajax请求超时）
        ajaxPost(this.url, {"reqObj": this.toJSONString(reqParam)}, function (result, status) {
            retData = result;
        });
        return retData;
    }

    //获取用户自定义的隐藏列
    DtTable.prototype.getCustomHiddenColumns = function (pageName) {
        var retData = null;
        if (!pageName)
            return retData;
        ajaxPost(ctrlPath + "/getHideColumns", {
            pageName: pageName
        }, function (hideCols) {
            retData = hideCols;
        });
        return retData;
    }

    /**
     * 换页、排序、查询按钮调用此方法
     *
     * @param sSource
     *            服务器请求方法
     * @param aoData
     *            基本信息
     * @param fnCallback
     *            重绘dataTable的回调函数
     * @param oSettings
     *            dataTable全局配置
     */
    DtTable.prototype.fillDataTable = function (sSource, aoData, fnCallback, oSettings) {
        var result = this.data;
        var map = oSettings.oAjaxData;
        var dataCache = $("#dataCache" + oSettings.sTableId);
        if (this.loaded) {// 换页
            var pageInfo = {};
            pageInfo.pageSize = map.iDisplayLength;
            pageInfo.pageNum = map.iDisplayStart % map.iDisplayLength == 0 ? map.iDisplayStart / map.iDisplayLength + 1
                : map.iDisplayStart / map.iDisplayLength;
            // console.log(dataCache.data("getServerData"));
            // 构造排序
            // var columnNames = map.sColumns.split(',');
            // var sortArr = [];
            // for (var i = 0; i < map.iSortingCols; i++) {
            //     if (map["iSortCol_" + i] != 0)// 过滤掉rowIndex的排序
            //         sortArr.push(columnNames[map["iSortCol_" + i]] + " " + map["sSortDir_" + i]);
            // }
            // dataCache.data("sortInfo", sortArr.join());

            result = this.getServerData(pageInfo, oSettings.sTableId);
            this.data = result;
        } else {// 首次加载
            result = this.data;
            this.loaded = true;
        }
        var obj = {};
        obj['data'] = result.rows;
        //obj["iDisplayStart"] = (result.pageInfo.pageNum-1)*result.pageInfo.pageSize;
        obj["iTotalRecords"] = result.pageInfo.recordNum;
        obj["iTotalDisplayRecords"] = result.pageInfo.recordNum;
        fnCallback(obj);
        //序号排序
        $("table.table thead tr").each(function () {
            $(this).find("th").eq(0).removeClass("sorting_asc").addClass("sorting_disabled");
        });
        //加载完成以后做一些其他处理
        if (this.serverCallback) {
            this.serverCallback.call(this, result, oSettings);
        }
    }

    DtTable.prototype.toJSONString = function (value) {
        var _array_tojson = Array.prototype.toJSON;
        delete Array.prototype.toJSON;
        var r = JSON.stringify(value);
        Array.prototype.toJSON = _array_tojson;
        return r;
    }

})(jQuery, window, document);
