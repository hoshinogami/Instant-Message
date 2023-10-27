/*
 Navicat MySQL Data Transfer

 Source Server Type    : MySQL
 Source Server Version : 80022
 Source Schema         : talkit
 
 执行出错的话，可以不要后面的字符集之类的声明
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_friend
-- ----------------------------
DROP TABLE IF EXISTS `tb_friend`;
CREATE TABLE `tb_friend`  (
  `user_id` bigint UNSIGNED NOT NULL,
  `friend_id` bigint UNSIGNED NOT NULL,
  PRIMARY KEY (`user_id`, `friend_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_msg
-- ----------------------------
DROP TABLE IF EXISTS `tb_msg`;
CREATE TABLE `tb_msg`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `src` bigint UNSIGNED NOT NULL,
  `target` bigint UNSIGNED NOT NULL,
  `time` bigint UNSIGNED NOT NULL,
  `text` varchar(4096) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `type` bigint UNSIGNED NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `recv-user`(`target`, `time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `pswd` varchar(30) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
  `sync` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
