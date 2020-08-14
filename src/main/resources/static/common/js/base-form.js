/**
 *  add by alonxiong 2016/10/9
 *  form 表单数据回填/获取/重置
 */
(function ($, window, document, undefined) {
    'use strict';

    var pluginName = 'form';

    $.fn[pluginName] = function (options) {
        if (this == null)
            return null;
        return new BaseForm(this, $.extend(true, {}, options));
    };

    var BaseForm = function (element, options) {
        this.$element = $(element);
        this.options = options;
        //icheck
        this.icheckElement = "[data-flag='icheck']";
        //datetimepicker
        this.datetimepickerElement = "[data-flag='datetimepicker']";
        //datepicker
        this.datepickerElement = "[data-flag='datepicker']";
        //timepicker
        this.timepickerElement = "[data-flag='timepicker']";
        //daterangepicker
        this.daterangepickerElement = "[data-flag='daterangepicker']";
        //selector
        this.dictSelectorElement = "[data-flag='dictSelector']";
        this.urlSelectorElement = "[data-flag='urlSelector']";
        this.select2Element = ".select2";
        this.init();
    }

    //初始化
    BaseForm.prototype.init = function () {
        //baseEntity
        this.initBaseEntity();
        //datetimepicker
        this.initDateTimePicker();
        // datepicker
        this.initDatePicker();
        //timepicker
        this.initTimePicker();
        //daterangepicker
        this.initDateRangePicker();
        //dictSelector
        this.initDictSelector();
        //urlSelector
        this.initUrlSelector();
    }

    //主要解决icheck 校验对勾错位的问题
    BaseForm.prototype.initComponent = function () {
        // icheck
        this.initICheck();
        //select2
        this.initSelect2();
    }

    BaseForm.prototype.initSelect2 = function () {
        var form = this.$element;
        form.find(this.select2Element).select2({
            language: 'zh-CN',
            //allowClear: true,
            minimumResultsForSearch: Infinity
        });
    }

    /**
     * 页面增加BaseEntity中的属性
     * 通过是否baseentity配置，baseentity 不配置或为true
     */
    BaseForm.prototype.initBaseEntity = function () {
        if (this.options.baseEntity === false)
            return;
        var form = this.$element;
        // if (form.find('[name="deleted"]').length == 0) {
        //     form.prepend("<input type='hidden' name='deleted' value='0'>");
        // }
        // if (form.find(':hidden[name="createDateTime"]').length == 0) {
        //     form.prepend('<input type="hidden" name="createDateTime" data-flag="date" data-format="yyyy-mm-dd hh:ii:ss">');
        // }
        // if (form.find(':hidden[name="version"]').length == 0) {
        //     form.prepend("<input type='hidden' name='version'>");
        // }
        if (form.find(':hidden[name="id"]').length == 0) {
            form.prepend("<input type='hidden' id='id' name='id'>");
        }
    }

    /**
     */
    BaseForm.prototype.initICheck = function () {
        var form = this.$element;
        if (form.find('[data-flag="icheck"]').length > 0) {
            form.find('[data-flag="icheck"]').each(function () {
                var cls = $(this).attr("class") ? $(this).attr("class") : "square-green";
                $(this).iCheck(
                    {
                        checkboxClass: 'icheckbox_' + cls,
                        radioClass: 'iradio_' + cls
                    }
                ).on('ifChanged', function (e) {
                    var field = $(this).attr('name');
                    var validator = form.data('bootstrapValidator');
                    if (validator && validator.options.fields[field])
                        validator.updateStatus(field, 'NOT_VALIDATED', null).validateField(field);
                });
            });
        }
    }

    /**
     * 初始化datetimepicker
     */
    BaseForm.prototype.initDateTimePicker = function () {
        var form = this.$element;
        if (form.find(this.datetimepickerElement).length > 0) {
            form.find(this.datetimepickerElement).datetimepicker({
                format: $(this).data("format") ? $(this).data("format") : "yyyy-mm-dd hh:ii",
                autoclose: true,
                clearBtn: true,
                language: 'zh-CN'
            })
            // .on('change', function (e) {
            //     var field = $(this).attr('name');
            //     var validator = form.data('bootstrapValidator');
            //     if (validator && validator.options.fields[field])
            //         validator.updateStatus(field, 'NOT_VALIDATED', null).validateField(field);
            // })
                .parent().css("padding-left", "15px").css("padding-right", "15px");
        }
    }

    /**
     * 初始化datepicker
     */
    BaseForm.prototype.initDatePicker = function () {
        var form = this.$element;
        if (form.find(this.datepickerElement).length > 0) {
            form.find(this.datepickerElement).datetimepicker({
                format: $(this).data("format") ? $(this).data("format") : "yyyy-mm-dd",
                autoclose: true,
                clearBtn: true,
                language: 'zh-CN',
                minView: "month",
                todayHighlight: true
            })
            // .on('change', function (e) {
            //     var field = $(this).attr('name');
            //     var validator = form.data('bootstrapValidator');
            //     if (validator && validator.options.fields[field])
            //         validator.updateStatus(field, 'NOT_VALIDATED', null).validateField(field);
            // })
            //不记得为什么要加上下面这段，故删之
            //.parent().css("padding-left", "15px").css("padding-right", "15px");
        }
    }

    /**
     * 初始化datetimepicker
     */
    BaseForm.prototype.initTimePicker = function () {
        var form = this.$element;
        if (form.find(this.timepickerElement).length > 0) {
            form.find(this.timepickerElement).datetimepicker({
                format: $(this).data("format") ? $(this).data("format") : "hh:ii",
                autoclose: true,
                clearBtn: true,
                minuteStep: 10,
                startView: 1,
                language: 'zh-CN'
            })
            // .on('change', function (e) {
            //     var field = $(this).attr('name');
            //     var validator = form.data('bootstrapValidator');
            //     if (validator && validator.options.fields[field])
            //         validator.updateStatus(field, 'NOT_VALIDATED', null).validateField(field);
            // })
                .parent().css("padding-left", "15px").css("padding-right", "15px");
        }
    }

    BaseForm.prototype.initDateRangePicker = function () {
        var form = this.$element;
        if (form.find(this.daterangepickerElement).length > 0) {
            form.find(this.daterangepickerElement).daterangepicker({
                autoUpdateInput: false,
                format: 'YYYY-MM-DD HH:mm',
                showDropdowns: true,
                showWeekNumbers: false,
                timePicker: false,
                timePickerIncrement: 60,
                timePicker12Hour: false,
                ranges: {
                    '今天': [moment().startOf('days'), moment().endOf('days')],
                    '昨日': [moment().subtract(1, 'days').startOf('days'), moment().subtract(1, 'days').endOf('days')],
                    '最近7日': [moment().subtract(6, 'days').startOf('days'), moment().endOf('days')],
                    '最近30日': [moment().subtract(29, 'days').startOf('days'), moment().endOf('days')],
                    '本月': [moment().startOf('month'), moment().endOf('month')],
                    '上月': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')],
                    // '全部'    : ['2018-1-1 00:00:01', moment().endOf('days')],
                },
                buttonClasses: ['btn btn-success'],
                applyClass: 'btn-small btn-primary blue',
                cancelClass: 'btn-small',
                separator: ' to ',
                locale: {
                    format: 'YYYY-MM-DD HH:mm',
                    applyLabel: '确定',
                    cancelLabel: '清除',
                    fromLabel: '起始时间',
                    toLabel: '结束时间',
                    customRangeLabel: '自定义',
                    daysOfWeek: ['日', '一', '二', '三', '四', '五', '六'],
                    monthNames: ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'],
                    firstDay: 1
                }
            }).on('apply.daterangepicker', function (ev, picker) {
                var startDate = picker.startDate.format('YYYY-MM-DD');
                var endDate = picker.endDate.format('YYYY-MM-DD');
                $(this).val(startDate + ' 00:00 - ' + endDate + ' 23:59');
                //自动提交
                form.find("button[data-btn-type='search']").trigger("click");
            }).on('cancel.daterangepicker', function (ev, picker) {
                $(this).val('');
                //自动提交
                form.find("button[data-btn-type='search']").trigger("click");
            });
        }
    }

    /**
     * 字典类型的控件
     */
    BaseForm.prototype.initDictSelector = function (dictSelectorElement) {
        var _this = this;
        var element = dictSelectorElement ? dictSelectorElement : this.dictSelectorElement;
        var elements = this.$element.find(element);
        $(elements).each(function (index, item) {
            var code = $(item).data("code");
            var autoload = $(item).data("autoload") == "false" ? false : true;
            if (code) {
                if (autoload) {
                    if ($(item).is("input"))
                        _this.buildAjaxDictBox(this, code);
                    else if ($(item).is("select"))
                        _this.buildAjaxDictSelect(this, code);
                } else {
                    var that = this;
                    $(this).click(function () {
                        _this.buildAjaxDictSelect(that, code);
                    })
                }
            }
        });
    }

    /**
     * url外部数据控件
     */
    BaseForm.prototype.initUrlSelector = function (urlSelectElement) {
        var _this = this;
        var element = urlSelectElement ? urlSelectElement : this.urlSelectorElement;
        var elements = this.$element.find(element);
        $(elements).each(function (index, item) {
            var url = $(item).data("src");
            var autoload = $(item).data("autoload") == "false" ? false : true;
            if (url) {
                if (autoload) {
                    if ($(item).is("input"))
                        _this.buildAjaxUrlBox(this, url);
                    else if ($(item).is("select"))
                        _this.buildAjaxUrlSelect(this, url);
                } else {
                    var that = this;
                    $(this).click(function () {
                        _this.buildAjaxUrlSelect(that, url);
                    })
                }
            }
        });
    }

    //数据来源为字典的radio checkbox
    BaseForm.prototype.buildAjaxDictBox = function (selector, dictCode) {
        var builder = this.buildAjaxBox(selector);
        $dataSource.getDict(dictCode, builder);
    }
    //数据来源为url的 radio checkbox
    BaseForm.prototype.buildAjaxUrlBox = function (selector, url) {
        var builder = this.buildAjaxBox(selector);
        $dataSource.getDataByUrl(url, builder);
    }
    //数据来源为字典的下拉框
    BaseForm.prototype.buildAjaxDictSelect = function (selector, dictCode) {
        var builder = this.buildAjaxSelector(selector);
        $dataSource.getDict(dictCode, builder);
    }
    //数据来源为url的下拉框
    BaseForm.prototype.buildAjaxUrlSelect = function (selector, url) {
        var builder = this.buildAjaxSelector(selector);
        $dataSource.getDataByUrl(url, builder);
    }
    //radio checkbox 渲染并生成
    BaseForm.prototype.buildAjaxBox = function (selector) {
        var type = $(selector).attr("type");
        var name = $(selector).attr("name");
        var value = $(selector).data("value") ? $(selector).data("value") : "value";
        var text = $(selector).data("text") ? $(selector).data("text") : "text";
        var nullmsg = $(selector).data("nullmsg");
        var datatype = $(selector).data("datatype");
        var boxtype = type.replace("icheck-", "");
        var id =  $(selector).data("id") ? $(selector).data("id") : name;
        var flag = "";
        if ($(selector).prop("disabled") == true) flag = " disabled";
        else if ($(selector).prop("readonly") == true) flag = " readonly";

        var builder = function (data) {
            if (type == "switch") {
                var onText, offText;
                for (var i = data.length-1; i >=0 ; i--) {
                    if (data[i][value] == 0) {
                        offText = data[i][text];
                    } else {
                        onText = data[i][text];
                    }
                }
                var obj = $("<input type='checkbox' name='" + name + "' id='" + name + "' data-type='switch' data-on-text='" + onText + "' data-off-text='" + offText + "' value='1'" + flag + ">");
                $(selector).after(obj);

                var script;
                if (flag == " disabled") {
                    script = $('<script>setTimeout(function (){$("#' + name + '").bootstrapSwitch();$("#' + name + '").parent().find("*").css("cursor", "not-allowed");},100);<\/script>');
                } else {
                    script = $('<script>setTimeout(function (){$("#' + name + '").bootstrapSwitch();},100);<\/script>');
                }
                $('body').append(script);
            } else {
                for (var i = data.length-1; i >=0 ; i--) {
                    var obj;
                    if (type.startWith("icheck")) {
                        obj = $("<label class='control-label'> " +
                            "<input type='" + boxtype + "' name='" + name + "' class='square-blue' data-flag='icheck' class='flat-red' value='" + data[i][value] + "'" + flag + "> " + data[i][text] + "</label>");
                    } else {
                        obj = "<input type='" + boxtype + "' name='" + name + "' id='" + id + "-" + data[i][value] + "' value='" + data[i][value] + "'class='magic-" + boxtype + "'";
                        if (nullmsg && i == 0) {
                            obj += " nullmsg='" + nullmsg + "' datatype='" + datatype + "'";
                        }
                        obj += flag + "><label for='" + id +"-"+ data[i][value] + "'>" + data[i][text] + "</label>&nbsp;";
                        obj = $(obj);
                    }
                    $(selector).after(obj);
                    $(selector).after("&nbsp;&nbsp;")
                }
            }
            $(selector).remove();
        }
        return builder;
    }
    //下拉框组件生成
    BaseForm.prototype.buildAjaxSelector = function (selector) {
        var sel = $(selector);
        if (sel.children().length > 0) {
            return false;
        }
        var blank_value = sel.data("blank-value");
        var blank_text = sel.data("blank-text");
        var is_blank = sel.data("blank") ? true : false;
        var default_value = sel.data("default-value");
        var builder = function (data) {
            if (is_blank) {
                if (!blank_value && !blank_text)
                    sel.append($('<option></option>'));
                else if (!blank_text)
                    sel.append($("<option value='" + blank_value + "'></option>"));
                else if (!blank_value)
                    sel.append($("<option>" + blank_text + "</option>"));
                else
                    sel.append($("<option value='" + blank_value + "'>" + blank_text + "</option>"));
            }
            if (data && data.length > 0) {
                var value = sel.data("value") ? sel.data("value") : "value";
                var text = sel.data("text") ? sel.data("text") : "text";
                var checked = "";
                for (var i = 0; i < data.length; i++) {
                    if(default_value && default_value==data[i][value]){
                        checked = " selected";
                    }else{
                        checked = "";
                    }
                    var option = $("<option value='" + data[i][value] + "'"+checked+">" + data[i][text] + "</option>");
                    sel.append(option);
                }
            }
        }
        return builder;
    }


    /**
     * 获取表单数据
     */
    BaseForm.prototype.getFormSimpleData = function () {
        var datas = {};
        var form = this.$element;
        if (form.length == 0)
            return datas;
        var elems = form.find('input[name], select[name], textarea[name]').not(':file').not(':disabled');

        // 设置datas属性 初始化为空对象，或空字符串
        elems.each(function (ind, elem) {
            var el_name = elem.name;

            if (!el_name)
                return;
            // var assembly = function (name) {
            //     var res = {}, sind = name.indexOf('.');
            //     res[sind > -1 ? name.substring(0, sind) : name] = sind > -1 ? assembly(name.substring(sind + 1)) : '';
            //     return res;
            // };
            // var ind = el_name.indexOf('.');
            // datas[ind > -1 ? el_name.substring(0, ind) : el_name] = ind > -1 ? assembly(el_name.substring(ind + 1)) : null;
            datas[el_name] = null;
        });

        // 设置datas属性值 赋值
        elems.each(function (ind, elem) {
            var el_name = elem.name, is_radio = elem.type == 'radio', is_ckbox = elem.type == 'checkbox';
            //$(elem).data("flag") == "datepicker" || $(elem).data("flag") == "date"||
            var is_date = $(elem).data("flag") == "datetimepicker";

            if (!el_name || ((is_radio || is_ckbox) && !elem.checked))
                return;
            var old_val = eval('datas.' + el_name); // checkbox值用逗号分割
            var value = is_date ? elem.value.strToDate() : elem.value;
            //var value = is_date ? $(elem).val().strToDate() : $(elem).val();
            if (elem.type == 'select-one' && elem.selectedIndex==0 && elem.getAttribute("data-blank-text") == elem.value) {
                //如果选中第一个option，并且第一个option的value不存在
                value = null;
            }
            else if (elem.type == 'select-multiple') {
                value = $(elem).val().join();
            }

            if (value) {
                if (typeof value == "string") {
                    value = value.replaceAll("\n", "\\n").replaceAll("\r", "\\r").replaceAll('\"', '\\"');
                    value = $.trim(value);
                }
                eval('datas.' + el_name + '="' + (is_ckbox ? (old_val ? (old_val + ',') : '') : '') + value + '"');
            }
            else if (el_name == 'id' && $("#id").attr("value")) {
                //如果是id的话还要再获取一下attr的值
                eval('datas.' + el_name + '="' +  $("#id").attr("value") + '"');
            }
        });
        //console.log("--------------------------------");
        //console.log(datas);
        return datas;
    }

    /**
     *    表单数据回填
     * @param json_data 回填的数据
     */
    BaseForm.prototype.initFormData = function (json_data) {
        if (!json_data)
            return;
        var form = this.$element;
        if (form.length == 0)
            return;
        form.find('input[name], select[name], textarea[name], label[name],img[name],audio[name]').not(":file").each(function (ind, elem) {
            var obj = $(elem), el_name = obj.attr('name'), value;
            try {
                value = eval('json_data.' + el_name);
            } catch (e) {
                value = null;
            }
            if (value != undefined && value != null && $.trim(value) != '') {
                var is_radio = elem.type == 'radio', is_ckbox = elem.type == 'checkbox';
                var is_date = $(elem).data("flag") == "datepicker" || $(elem).data("flag") == "date" || $(elem).data("flag") == "datetimepicker";
                var date_format = $(elem).data("format") || "yyyy-mm-dd";
                if (is_date)
                    value = formatDate(value, date_format);
                if (is_radio) {
                    //icheck
                    if ($(elem).data("flag") == "icheck") {
                        $(elem).iCheck(elem.value == value ? 'check' : 'uncheck');
                        var validator = form.data('bootstrapValidator');
                        if (validator && validator.options.fields[el_name])
                            validator.updateStatus(el_name, 'NOT_VALIDATED', null);
                    } else {
                        //原生radio
                        elem.checked = elem.value == value;
                        //触发change事件
                        obj.trigger("change");
                    }
                } else if (is_ckbox) {
                    //icheck
                    if ($(elem).data("flag") == "icheck") {
                        $(elem).iCheck($.inArray(elem.value, value.split(',')) > -1 ? 'check' : 'uncheck');
                        var validator = form.data('bootstrapValidator');
                        if (validator && validator.options.fields[el_name])
                            validator.updateStatus(el_name, 'NOT_VALIDATED', null);
                    } else if ($(elem).data("type") == "switch") {
                        if (value == "0") {
                            setTimeout(function () {
                                $(elem).bootstrapSwitch('state', false);
                            }, 100);
                        } else {
                            setTimeout(function () {
                                $(elem).bootstrapSwitch('state', true);
                            }, 100);
                        }
                    } else {
                        //原生checkbox
                        if (typeof value == "number") {
                            elem.checked = (elem.value == value ? true : false);
                        }
                        else {
                            elem.checked = $.inArray(elem.value, value.split(',')) > -1 ? true : false;
                        }
                        //触发change事件
                        obj.trigger("change");
                    }
                } else if (elem.tagName.toUpperCase() == 'LABEL') {
                    elem.innerText = value;
                } else if (elem.tagName.toUpperCase() == 'AUDIO' || elem.tagName.toUpperCase() == 'IMG') {
                    elem.src = value;
                } else if (elem.tagName.toUpperCase() == 'SELECT') {
                    var is_select2 = $(elem).hasClass("select2");
                    if (is_select2) {
                        if(elem.type.toUpperCase() == 'SELECT-MULTIPLE'){
                            var arr = value.split(",");
                            $(elem).select2("val",[arr]);
                        }else {
                            $(elem).val(value).trigger("change");
                        }
                        // var validator = form.data('bootstrapValidator');
                        // if (validator && validator.options.fields[el_name])
                        //     validator.updateStatus(el_name, 'NOT_VALIDATED', null);
                    }
                    else {
                        $(elem).val(value);
                    }

                } else {
                    elem.value = value;
                }
                //console.log(el_name+"==="+elem.type);
            }
        });
    }

    /**
     * 表单重置
     */
    BaseForm.prototype.clearForm = function () {
        if (this.$element.length > 0) {
            var form = this.$element;
            form.find(':input[name]:not(:radio):not(:checkbox)').val('');
            form.find(':radio').prop('checked', false).trigger("change");//触发change事件
            form.find(':radio[data-flag]').iCheck('update');
            form.find(':checkbox').prop('checked', false).trigger("change");//触发change事件
            form.find(':checkbox[data-flag]').iCheck('update');
            form.find('label[name]').text('');
            form.find('select:not(.select2)').val("");
            form.find("select.select2").val(null).trigger("change");//触发change事件
            form.find(".bootstrap-switch :checkbox").bootstrapSwitch('state', false);
            //去掉验证
            $(".Validform_checktip").removeClass("Validform_right").removeClass("Validform_wrong").html("");
        } else {
            $(':input[name]:not(:radio)').val('');
            $(':radio').removeAttr('checked').trigger("change");//触发change事件
            $(':radio[data-flag]').iCheck('update');
            $(':checkbox').removeAttr('checked').trigger("change");//触发change事件
            $(':checkbox[data-flag]').iCheck('update');
            $('label[name]').text('');
            $('select:not(.select2)').val("");
            $("select.select2").val(null).trigger("change");//触发change事件
            $(".bootstrap-switch :checkbox").bootstrapSwitch('state', false);
        }
    }


})(jQuery, window, document);


