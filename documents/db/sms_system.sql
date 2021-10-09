/*
 Navicat Premium Data Transfer

 Source Server         : 203Customer
 Source Server Type    : MySQL
 Source Server Version : 50731
 Source Host           : 203.189.232.25:3306
 Source Schema         : sms_system

 Target Server Type    : MySQL
 Target Server Version : 50731
 File Encoding         : 65001

 Date: 28/08/2020 10:10:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sms_account
-- ----------------------------
DROP TABLE IF EXISTS `sms_account`;
CREATE TABLE `sms_account` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `code` varchar(20) NOT NULL COMMENT '编码',
  `system_id` varchar(30) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `url` varchar(200) NOT NULL COMMENT '地址',
  `port` smallint(5) unsigned NOT NULL COMMENT '端口',
  `channel_pwd` varchar(100) DEFAULT NULL COMMENT '通道密码',
  `description` varchar(1000) DEFAULT NULL COMMENT '描述',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `is_invalid` tinyint(1) DEFAULT '0' COMMENT '是否失效',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COMMENT='smpp账户表';

-- ----------------------------
-- Table structure for sms_collect
-- ----------------------------
DROP TABLE IF EXISTS `sms_collect`;
CREATE TABLE `sms_collect` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `collect_id` varchar(64) COLLATE utf8_bin NOT NULL COMMENT '自定义id',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `contents` varchar(1000) COLLATE utf8_bin DEFAULT NULL COMMENT '短信内容',
  `status` varchar(10) COLLATE utf8_bin DEFAULT NULL COMMENT '发送状态',
  `total` int(10) DEFAULT '0' COMMENT '总条数',
  `pending_num` int(11) DEFAULT '0' COMMENT '发送中数',
  `success_num` int(11) DEFAULT '0' COMMENT '成功数',
  `fail_num` int(11) DEFAULT '0' COMMENT '失败数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_key` (`collect_id`) USING BTREE,
  KEY `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=137 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='短信汇总表';

-- ----------------------------
-- Table structure for sms_details
-- ----------------------------
DROP TABLE IF EXISTS `sms_details`;
CREATE TABLE `sms_details` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `details_id` varchar(64) NOT NULL COMMENT '短信记录唯一标识',
  `resp_message_id` varchar(64) DEFAULT NULL COMMENT 'SMSC SUBMIT_SM_RESP消息messageId',
  `collect_id` varchar(64) DEFAULT NULL COMMENT '汇总id',
  `user_id` int(11) NOT NULL COMMENT '用户Id',
  `direction` tinyint(3) unsigned NOT NULL COMMENT '短信发送方向, 1: 上行, 2: 下行',
  `account_code` varchar(20) DEFAULT NULL COMMENT '对端号码',
  `phone` varchar(32) NOT NULL COMMENT '手机号',
  `contents` varchar(1024) NOT NULL COMMENT '短信内容',
  `status` tinyint(3) NOT NULL DEFAULT '0' COMMENT '短信发送状态, 0: 发送中、1: 已发送、-1: 发送失败',
  `batch_id` varchar(30) DEFAULT NULL COMMENT '短信发送批次号',
  `send_time` datetime DEFAULT NULL COMMENT '短信发送时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收信人接收短信时间, 以收到 ISMG 响应报文为准',
  `report_stat` char(7) DEFAULT NULL COMMENT '状态报告',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `details_id` (`details_id`),
  KEY `collect_id` (`collect_id`,`batch_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=230697 DEFAULT CHARSET=utf8mb4 COMMENT='短信详情表\n';

-- ----------------------------
-- Table structure for sys_link
-- ----------------------------
DROP TABLE IF EXISTS `sys_link`;
CREATE TABLE `sys_link` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `url` varchar(1000) COLLATE utf8_bin DEFAULT NULL,
  `enable` tinyint(2) DEFAULT '1',
  `remark` varchar(1000) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='系统管理表';

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `account_id` int(11) DEFAULT '0' COMMENT '账户ID',
  `name` varchar(100) DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `allowance` int(10) DEFAULT '0' COMMENT '余额',
  `rate` double(10,2) DEFAULT '0.00' COMMENT '费率',
  `con_limit` int(10) DEFAULT NULL COMMENT '并发限制',
  `send_limit` int(10) DEFAULT NULL COMMENT '总条数限制',
  `contactor` varchar(30) DEFAULT NULL COMMENT '联系人',
  `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
  `industry` varchar(255) DEFAULT NULL COMMENT '行业',
  `company` varchar(255) DEFAULT NULL COMMENT '公司',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `role` varchar(10) DEFAULT NULL COMMENT '角色',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `index_key` (`name`,`account_id`,`send_limit`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 COMMENT='用户表';

SET FOREIGN_KEY_CHECKS = 1;
