/**
 * 通用文件管理组件
 * @author alonxiong qq:42457678
 */
(function ($, window, document, undefined) {
    'use strict';

    //初始化fileinput
    window.FileInput = function (ctrlName, uploadUrl, callback, fileSize, allowedFileExt ) {
        //初始化fileinput控件（第一次初始化）
        this.init(ctrlName, uploadUrl, callback, fileSize, allowedFileExt);
    };

    FileInput.prototype.init = function (ctrlName, uploadUrl, callback, fileSize, allowedFileExt ) {
        var control = $('#' + ctrlName);
        if(!allowedFileExt){
            allowedFileExt = ['jpg', 'jpeg', 'gif', 'png'];
        }
        if(!fileSize){
            fileSize = 0;
        }
        //初始化上传控件的样式
        control.fileinput({
            language: 'zh', //设置语言
            uploadUrl: uploadUrl, //上传的地址
            elErrorContainer: '#file-errors',
            allowedFileExtensions: allowedFileExt,//接收的文件后缀
            showPreview: false,//是否显示预览
            showUpload: true, //是否显示上传按钮
            //showCaption: false,//是否显示标题
            browseClass: "btn btn-primary", //按钮样式
            //dropZoneEnabled: false,//是否显示拖拽区域
            //minImageWidth: 50, //图片的最小宽度
            //minImageHeight: 50,//图片的最小高度
            //maxImageWidth: 1000,//图片的最大宽度
            //maxImageHeight: 1000,//图片的最大高度
            maxFileSize: fileSize,//单位为kb，如果为0表示不限制文件大小
            maxFileCount: 10, //表示允许同时上传的最大文件个数
            enctype: 'multipart/form-data',
            validateInitialCount: true,
            previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
            msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
        });
        //导入文件上传完成之后的事件
        control.on("fileuploaded", function (event, data, previewId, index) {
            if (data.response.success) {
                callback.call(this, data.response.message);
            }else{
                modals.error('上传失败');
                return;
            }
        });
    }

})(jQuery, window, document);