/**
 * Created by HANZO on 2016/6/17.
 */

/**
 * popover（弹出框）2秒后定时消失
 * showPopover("#tableName","表名已存在");
 * @param target
 * @param msg
 */
function showPopover(msg, targetId) {
    var target;
    if (targetId instanceof jQuery) {
        target = targetId;
    } else {
        target = $(targetId);
    }
    target.attr("data-original-title", msg);
    $('[data-toggle="tooltip"]').tooltip();
    target.tooltip('show');
    target.focus();

    //2秒后消失提示框
    var id = setTimeout(
        function () {
            target.attr("data-original-title", "");
            target.tooltip('hide');
        }, 2000
    );
}

/**
 * 改变id值触发器
 * @param func 解发的函数,函数必须带一参数，是新的value值
 */
function onchangeId(func) {
    var __id = document.getElementById("id");
    if (__id.__defineSetter__) {
        __id.__defineSetter__('value', function (v) {
            this.setAttribute('value', v);//注意这里，要使用setAttribute来设置value值，不能this.value=v，要不会死循环。如果注释掉这句，无法修改input的value值
            setTimeout(function () {
                func.call(this, v);
            }, 100);
        });
    } else {
        __id.onpropertychange = func;
    }
}

/**
 *
 * @param url
 * @param params
 * @param callback
 * @returns {*}
 */
//(function ($) {

function loadPage(url, container) {
    if (!container)
        container = "#mainDiv";

    jQuery(container).load(url, function (response, status, xhr) {
        //if (status == "success") {
        if (response) {
            try {
                var result = jQuery.parseJSON(response);
                if (result.code) {
                    //jQuery(container).html("");
                    modals.error(url + "<br/>" + result.message);
                }
            } catch (e) {
                return response;
            }
        }
        //}
    });
}

/**
 * Load a url into a page
 * 增加beforeSend以便拦截器在将该请求识别为非ajax请求
 */
var _old_load = jQuery.fn.load;
jQuery.fn.load = function (url, params, callback) {
    //update for HANZO, 2016/12/22
    if (typeof url !== "string" && _old_load) {
        return _old_load.apply(this, arguments);
    }

    var selector, type, response,
        self = this,
        off = url.indexOf(" ");
    if (off > -1) {
        selector = jQuery.trim(url.slice(off));
        url = url.slice(0, off);
    }
    if (jQuery.isFunction(params)) {
        callback = params;
        params = undefined;
    } else if (params && typeof params === "object") {
        type = "POST";
    }
    if (self.length > 0) {
        jQuery.ajaxSetup({cache: true});
        jQuery.ajax({
            url: url,
            beforeSend: function (xhr) {
                xhr.setRequestHeader('X-Requested-With', {
                    toString: function () {
                        return '';
                    }
                });
            },
            type: type || "GET",
            dataType: "html",
            data: params
        }).done(function (responseText) {
            //console.log(responseText);
            response = arguments;
            //页面超时跳转到首页
            if (responseText.startWith("<!--login_page_identity-->")) {
                alert("登录超时，请重新登录!");
                window.location.href = "/";
            } else {
                self.html(selector ?
                    jQuery("<div>").append(jQuery.parseHTML(responseText)).find(selector) :
                    responseText);
            }
        }).always(callback && function (jqXHR, status) {
            self.each(function () {
                callback.apply(this, response || [jqXHR.responseText, status, jqXHR]);
            });
        });
    }

    return this;
};

//递归删除空属性防止把null变成空值
function deleteEmptyProp(obj) {
    for (var a in obj) {
        if (typeof (obj[a]) == "object" && obj[a] != null) {
            deleteEmptyProp(obj[a]);
        } else {
            if (!obj[a]) {
                delete obj[a];
            }
        }
    }
    return obj;
}

function postAjax(url, params, option) {
    var result = null;
    jQuery.ajax({
        type: 'post',
        async: false,
        url: url,
        data: params,
        dataType: 'json',
        headers: option.headers,
        processData: option.processData,
        contentType: option.contentType,
        success: function (data, status) {
            result = data;
            if (data && data.code) {
                if (data.code == 200) {
                    modals.info("操作结果 ：" + data.message);
                    return false;
                } else {
                    modals.error(data.message);
                    return false;
                }
                if (option.callback) {
                    option.callback();
                }
            }
        },
        error: function (err, err1, err2) {
            console.log(url);
            console.log("ajaxPost发生异常，请仔细检查请求url是否正确");
            if (err && err.readyState && err.readyState == '4') {
                var sessionstatus = err.getResponseHeader("session-status");
                console.log(err);
                if (sessionstatus == "timeout") {
                    //如果超时就处理 ，指定要跳转的页面
                    window.location.href = "/";
                } else {
                    var responseBody = JSON.parse(err.responseText);
                    if (responseBody) {
                        modals.error({
                            text: url + "<br/>" + responseBody.message
                        });
                    }
                    return;
                }
            }
            var responseBody = JSON.parse(err.responseText);
            if (responseBody) {
                modals.error({
                    text: url + "<br/>" + responseBody.message
                });
            }
        },
        beforeSend: function () {
            if (option.beforeSend) {
                option.beforeSend();
            }
        }
    });
    return result;
}

function ajaxPost(url, params, callback, uploadFileMode) {
    var result = null;
    var headers = {};
    // headers['Content-Type'] = 'application/json';
    jQuery.ajax({
        type: 'post',
        async: false,
        url: url,
        data: params,
        dataType: 'json',
        headers: headers,
        processData: uploadFileMode,
        contentType: uploadFileMode,
        success: function (data, status) {
            result = data;
            if (data && data.code) {
                if (data.code == 200) {
                    modals.info("操作结果 ：" + data.message);
                    return false;
                } else {
                    modals.error(data.message);
                    return false;
                }
            }
            if (callback) {
                callback.call(this, data, status);
            }
        },
        error: function (err, err1, err2) {
            console.log(url);
            console.log("ajaxPost发生异常，请仔细检查请求url是否正确");
            //console.log(err.responseText);
            if (err && err.readyState && err.readyState == '4') {
                var sessionstatus = err.getResponseHeader("session-status");
                console.log(err);
                //console.log(err1);
                //console.log(err2);
                if (sessionstatus == "timeout") {
                    //如果超时就处理 ，指定要跳转的页面
                    window.location.href = "/";
                } else {
                    var responseBody = JSON.parse(err.responseText);
                    if (responseBody) {
                        modals.error({
                            text: url + "<br/>" + responseBody.message
                        });
                    }
                    return;
                }
            }

            var responseBody = JSON.parse(err.responseText);
            if (responseBody) {
                modals.error({
                    text: url + "<br/>" + responseBody.message
                });
            }
        }
    });

    return result;
}

/**
 * 改变radio使其不可用
 * $("input[name=is_take_msgid]").on("change", "",{value: "0", inputs: "input[name=msgid_format]"}, DisabledInputsByChangeValue);
 * 第三个参数为json, 包含value(使其不可用的值)及inputs(不可用的元素,多元素用,号间隔)
 * 如果value为不选择，要用null
 * @param event
 */
function DisabledInputsByChangeValue(event) {
    var val;
    if (this.type == "radio") {
        val = $("input[name=" + $(this).attr("name") + "]:checked").val();
    } else {
        val = $(this).val();
    }
    if (val == null || val == event.data.value) {
        $(event.data.inputs).prop("disabled", true);
        dataForm.ignore(event.data.inputs);
    } else {
        $(event.data.inputs).prop("disabled", false);
        dataForm.unignore(event.data.inputs);
    }
}

/**
 * 改变radio使其可用
 * 使用方法
 * $("input[name=mo_format]").on("change", "",{value: "0", inputs: "#mo_record_space,#mo_field_space"}, EnabledInputsByChangeValue);
 * 第三个参数为json, 包含value(使其可用的值)及inputs(可用的元素,多元素用,号间隔)
 * 如果value为不选择，要用null
 * @param event
 */
function EnabledInputsByChangeValue(event) {
    var val;
    if (this.type == "radio") {
        val = $("input[name=" + $(this).attr("name") + "]:checked").val();
    } else {
        val = $(this).val();
    }
    //if ((val != null && val == event.data.value)) {
    if (val == event.data.value) {
        $(event.data.inputs).prop("disabled", false);
        dataForm.unignore(event.data.inputs);
    } else {
        $(event.data.inputs).prop("disabled", true);
        dataForm.ignore(event.data.inputs);
    }
}

/*
** randomWord 产生任意长度随机字母数字组合
** randomFlag-是否任意长度 min-任意长度最小位[固定位数] max-任意长度最大位
*/
function randomWord(randomFlag, min, max) {
    var str = "",
        range = min,
        arr = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];

    // 随机产生
    if (randomFlag) {
        range = Math.round(Math.random() * (max - min)) + min;
    }
    for (var i = 0; i < range; i++) {
        pos = Math.round(Math.random() * (arr.length - 1));
        str += arr[pos];
    }
    return str;
}

/**
 * 替换字符串中的字段.
 * @param {String} str 模版字符串
 * @param {Object} o json data
 * @param {RegExp} [regexp] 匹配字符串的正则表达式
 */
function substitute(str, o, regexp) {
    return str.replace(regexp || /\\?\{([^{}]+)\}/g, function (match, name) {
        return (o[name] === undefined) ? '' : o[name];
    });
}

function getServerTime(base_path, format) {
    var result = null;

    var sdate = new Date(ajaxPost(base_path + '/base/getServerTime'));
    if (sdate != 'Invalid Date') {
        result = formatDate(sdate, format || 'yyyy/mm/dd');
    }

    return result;
}

/**
 * 格式化日期
 */
function formatDate(date, format) {
    if (!date) return date;
    //date = (typeof date == "number") ? new Date(date) : date;
    date = new Date(date);
    return date.Format(format);
}

Date.prototype.Format = function (fmt) {
    var o = {
        "m+": this.getMonth() + 1, // 月份
        "d+": this.getDate(), // 日
        "h+": this.getHours(), // 小时
        "i+": this.getMinutes(), // 分
        "s+": this.getSeconds(), // 秒
        "q+": Math.floor((this.getMonth() + 3) / 3), // 季度
        "S": this.getMilliseconds()
        // 毫秒
    };
    if (/(y+)/.test(fmt))
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

/**
 * 比较两个时间的大小 d1>d2 返回大于0
 * @param d1
 * @param d2
 * @returns {number}
 * @constructor
 */
function DateDiff(d1, d2) {
    var result = Date.parse(d1.replace(/-/g, "/")) - Date.parse(d2.replace(/-/g, "/"));
    return result;
}

/**
 * 字符串转日期
 * @returns {number}
 */
String.prototype.strToDate = function () {
    if (this && this != "") {
        return Date.parse(this.replace(/-/g, "/"));
    } else
        return "";
}

/**
 * 将map类型[name,value]的数据转化为对象类型
 */
function getObjectFromMap(aData) {
    var map = {};
    for (var i = 0; i < aData.length; i++) {
        var item = aData[i];
        if (!map[item.name]) {
            map[item.name] = item.value;
        }
    }
    return map;
}

/**
 * 获取下一个编码 000001，000001000006，6
 * 得到结果 000001000007
 */
function getNextCode(prefix, maxCode, length) {
    if (maxCode == null) {
        var str = "";
        for (var i = 0; i < length - 1; i++) {
            str += "0";
        }
        return prefix + str + 1;
    } else {
        var str = "";
        var sno = parseInt(maxCode.substring(prefix.length)) + 1;
        for (var i = 0; i < length - sno.toString().length; i++) {
            str += "0";
        }
        return prefix + str + sno;
    }

}

/**
 * 收缩左边栏时，触发markdown编辑的resize
 */
/*$("[data-toggle='offcanvas']").click(function () {
 if (editor) {
 setTimeout(function () {
 editor.resize()
 }, 500);
 }
 });*/


//获取布尔值
/*String.prototype.BoolValue=function(){
 if(this==undefined)
 return false;
 if(this=="false"||this=="0")
 return false;
 return true;
 }*/

/**
 * TextArea 指定位置插入内容
 *
 * 使用方法 $(".tarea").insert("");
 *
 * @param $
 */
(function ($) {
    $.fn.extend({
        "insert": function (value) {
            var result = false;
            var dthis = $(this)[0]; //将jQuery对象转换为DOM元素

            //IE下
            if (document.selection) {
                $(dthis).focus();		//输入元素textara获取焦点
                var fus = document.selection.createRange();//获取光标位置
                fus.text = value;	//在光标位置插入值
                $(dthis).focus();	///输入元素textara获取焦点
                result = true;
            }
            //火狐下标准
            else if (dthis.selectionStart || dthis.selectionStart == '0') {
                var start = dthis.selectionStart;
                var end = dthis.selectionEnd;
                var top = dthis.scrollTop;

                //以下这句，应该是在焦点之前，和焦点之后的位置，中间插入我们传入的值
                if (start > 0 && start < dthis.value.length) {
                    dthis.value = dthis.value.substring(0, start) + value + dthis.value.substring(end, dthis.value.length);
                    this.focus();
                    dthis.selectionStart = start + value.length;
                    dthis.selectionEnd = start + value.length;
                    result = true;
                }
            }
            //在输入元素textara没有定位光标的情况
            else {
                //this.value += value;
                //this.focus();
            }

            return result;
        }
    })
})(jQuery);

/**
 * TextArea 文本框根据输入内容自动适应高度
 *
 * 使用方法 $(".chackTextarea-area").autoTextarea({maxHeight:100});
 *
 * @param $
 */
(function ($) {
    $.fn.autoTextarea = function (options) {
        var defaults = {
            maxHeight: null,// 文本框是否自动撑高，默认：null，不自动撑高；如果自动撑高必须输入数值，该值作为文本框自动撑高的最大高度
            minHeight: $(this).height()
            // 默认最小高度，也就是文本框最初的高度，当内容高度小于这个高度的时候，文本以这个高度显示
        };
        var opts = $.extend({}, defaults, options);
        return $(this).each(function () {
            $(this).bind("paste cut keydown keyup focus blur", function () {
                var height, style = this.style;
                this.style.height = opts.minHeight + 'px';
                if (this.scrollHeight > opts.minHeight) {
                    if (opts.maxHeight && this.scrollHeight > opts.maxHeight) {
                        height = opts.maxHeight;
                        style.overflowY = 'scroll';
                    } else {
                        height = this.scrollHeight;
                        style.overflowY = 'hidden';
                    }
                    style.height = height + 'px';
                }
            });
        });
    };
})(jQuery);

var HtmlUtil = {
    /*1.用浏览器内部转换器实现html转码*/
    htmlEncode: function (html) {
        //1.首先动态创建一个容器标签元素，如DIV
        var temp = document.createElement("div");
        //2.然后将要转换的字符串设置为这个元素的innerText(ie支持)或者textContent(火狐，google支持)
        (temp.textContent != undefined) ? (temp.textContent = html) : (temp.innerText = html);
        //3.最后返回这个元素的innerHTML，即得到经过HTML编码转换的字符串了
        var output = temp.innerHTML;
        temp = null;
        return output;
    },
    /*2.用浏览器内部转换器实现html解码*/
    htmlDecode: function (text) {
        //1.首先动态创建一个容器标签元素，如DIV
        var temp = document.createElement("div");
        //2.然后将要转换的字符串设置为这个元素的innerHTML(ie，火狐，google都支持)
        temp.innerHTML = text;
        //3.最后返回这个元素的innerText(ie支持)或者textContent(火狐，google支持)，即得到经过HTML解码的字符串了。
        var output = temp.innerText || temp.textContent;
        temp = null;
        return output;
    }
};

String.prototype.startWith = function (s) {
    if (s == null || s == "" || this.length == 0 || s.length > this.length)
        return false;
    if (this.substr(0, s.length) == s)
        return true;
    else
        return false;
    return true;
}

String.prototype.replaceAll = function (s1, s2) {
    return this.replace(new RegExp(s1, "gm"), s2);
}

String.prototype.format = function () {
    if (arguments.length == 0) return this;
    for (var s = this, i = 0; i < arguments.length; i++)
        s = s.replace(new RegExp("\\{" + i + "\\}", "g"), arguments[i]);
    return s;
};


//})(jQuery)