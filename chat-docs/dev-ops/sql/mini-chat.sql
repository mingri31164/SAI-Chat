/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80031 (8.0.31)
 Source Host           : localhost:3306
 Source Schema         : mini-chat

 Target Server Type    : MySQL
 Target Server Version : 80031 (8.0.31)
 File Encoding         : 65001

 Date: 28/01/2025 20:38:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_group
-- ----------------------------
DROP TABLE IF EXISTS `chat_group`;
CREATE TABLE `chat_group`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `create_time` timestamp(3) NOT NULL,
  `update_time` timestamp(3) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_group
-- ----------------------------
INSERT INTO `chat_group` VALUES ('1', '这是个群聊', NULL, '2025-01-27 21:00:24.000', '2025-01-27 21:00:28.000');

-- ----------------------------
-- Table structure for chat_list
-- ----------------------------
DROP TABLE IF EXISTS `chat_list`;
CREATE TABLE `chat_list`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `target_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `target_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `unread_count` int NULL DEFAULT 0,
  `last_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` timestamp(3) NOT NULL,
  `update_time` timestamp(3) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_list
-- ----------------------------
INSERT INTO `chat_list` VALUES ('013c02cc-8ae5-447b-8596-766a46385476', '25', '24', '{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 2, '{\"id\":\"1ddb1aab-b104-4f09-8351-2af6aab4b004\",\"fromId\":\"24\",\"toId\":\"25\",\"fromInfo\":{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"+999\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":false,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738041977143,\"updateTime\":1738041977143}', 'user', '2025-01-28 13:24:05.715', '2025-01-28 13:26:17.146');
INSERT INTO `chat_list` VALUES ('01adcf37-cb78-4b72-8ac0-0bccd1870a53', '24', '23', '{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":\"9451e8a0-fe93-47f5-bd38-b9308e5aeebd\",\"fromId\":\"23\",\"toId\":\"24\",\"fromInfo\":{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"111\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":false,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738041996224,\"updateTime\":1738041996224}', 'user', '2025-01-28 13:25:51.424', '2025-01-28 17:25:15.831');
INSERT INTO `chat_list` VALUES ('1096f1d6f7844eb8ace8ed32552ca547', '26', '27', '{\"id\":27,\"name\":\"oo\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":null,\"fromId\":null,\"toId\":null,\"fromInfo\":null,\"message\":null,\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":null,\"type\":null,\"source\":null,\"createTime\":null,\"updateTime\":null}', 'user', '2025-01-28 17:25:07.604', '2025-01-28 17:25:07.604');
INSERT INTO `chat_list` VALUES ('1e59c09553fa4cc38ab120f68c0b80c5', '21', '1', '{\"id\":1,\"name\":\"这是个群聊\",\"avatar\":null,\"type\":null,\"badge\":null}', 0, '{\"id\":\"b5b36256-816f-4dd2-88aa-47c1a49b4c56\",\"fromId\":\"21\",\"toId\":\"1\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"tt\\\"}]\",\"isShowTime\":true,\"type\":\"text\",\"source\":\"group\",\"createTime\":1738066534291,\"updateTime\":1738066534291}', 'group', '2025-01-28 13:11:01.776', '2025-01-28 20:15:34.371');
INSERT INTO `chat_list` VALUES ('2329e88888554f0f8b39dface9343084', '26', '1', '{\"id\":1,\"name\":\"这是个群聊\",\"avatar\":null,\"type\":null,\"badge\":null}', 0, '{\"id\":\"b5b36256-816f-4dd2-88aa-47c1a49b4c56\",\"fromId\":\"21\",\"toId\":\"1\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"tt\\\"}]\",\"isShowTime\":true,\"type\":\"text\",\"source\":\"group\",\"createTime\":1738066534291,\"updateTime\":1738066534291}', 'group', '2025-01-28 17:24:56.033', '2025-01-28 17:24:56.033');
INSERT INTO `chat_list` VALUES ('25641633-4fe4-4030-b099-dd1328590991', '23', '21', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', 0, '{\"id\":\"9f4aba9c-67f6-4d59-b863-265e30e3330c\",\"fromId\":\"23\",\"toId\":\"21\",\"fromInfo\":{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]},\"message\":\"\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":true,\"type\":\"recall\",\"source\":\"user\",\"createTime\":1738041928905,\"updateTime\":1738041934367}', 'user', '2025-01-28 13:11:06.085', '2025-01-28 13:27:34.932');
INSERT INTO `chat_list` VALUES ('27d59c4b-1a89-4dd6-82ea-da9c2b31f8cc', '25', '21', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', 2, '{\"id\":\"938e8ab8-52ed-445e-b5c2-e74a482cbee6\",\"fromId\":\"21\",\"toId\":\"25\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"9999\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":false,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738041264630,\"updateTime\":1738041264630}', 'user', '2025-01-28 13:11:43.805', '2025-01-28 13:14:24.634');
INSERT INTO `chat_list` VALUES ('2afa3c9676ce4a9dac3035356ca72bdd', '23', '1', '{\"id\":1,\"name\":\"这是个群聊\",\"avatar\":null,\"type\":null,\"badge\":null}', 0, '{\"id\":\"b5b36256-816f-4dd2-88aa-47c1a49b4c56\",\"fromId\":\"21\",\"toId\":\"1\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"tt\\\"}]\",\"isShowTime\":true,\"type\":\"text\",\"source\":\"group\",\"createTime\":1738066534291,\"updateTime\":1738066534291}', 'group', '2025-01-28 13:25:04.800', '2025-01-28 13:28:06.299');
INSERT INTO `chat_list` VALUES ('34908ba047f94218a9db749ff9163f17', '26', '23', '{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":null,\"fromId\":null,\"toId\":null,\"fromInfo\":null,\"message\":null,\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":null,\"type\":null,\"source\":null,\"createTime\":null,\"updateTime\":null}', 'user', '2025-01-28 17:24:59.702', '2025-01-28 17:24:59.702');
INSERT INTO `chat_list` VALUES ('388ff8f7-0e73-4668-9d4f-8255422c91af', '27', '23', '{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":\"1f3e1ddf-e81b-4188-9010-a6d4c926af48\",\"fromId\":\"23\",\"toId\":\"27\",\"fromInfo\":{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"。\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":true,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738042099157,\"updateTime\":1738042099157}', 'user', '2025-01-28 13:28:19.159', '2025-01-28 13:28:22.338');
INSERT INTO `chat_list` VALUES ('3ebdc58654ce4691b24aaa0418101dad', '21', '26', '{\"id\":26,\"name\":\"xx\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":\"674d210f-202f-4a83-b04e-3474af14f130\",\"fromId\":\"21\",\"toId\":\"26\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":true,\"type\":\"recall\",\"source\":\"user\",\"createTime\":1738056669737,\"updateTime\":1738056671714}', 'user', '2025-01-28 13:27:14.314', '2025-01-28 20:38:24.815');
INSERT INTO `chat_list` VALUES ('408c7410-d8ce-40be-87c8-827e9a4dc48b', '20', '21', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', 0, '{\"id\":\"4b56caf2-8799-4c21-a688-6a940c548428\",\"fromId\":\"21\",\"toId\":\"20\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"566\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":true,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738056458410,\"updateTime\":1738056458410}', 'user', '2025-01-28 13:14:47.634', '2025-01-28 17:27:50.397');
INSERT INTO `chat_list` VALUES ('5266446d-6771-473c-8a36-45e1ad4a803e', '26', '21', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', 2, '{\"id\":\"674d210f-202f-4a83-b04e-3474af14f130\",\"fromId\":\"21\",\"toId\":\"26\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":true,\"type\":\"recall\",\"source\":\"user\",\"createTime\":1738056669737,\"updateTime\":1738056671714}', 'user', '2025-01-28 13:11:48.020', '2025-01-28 17:31:11.717');
INSERT INTO `chat_list` VALUES ('54514ebca397489690718b43a603e211', '24', '27', '{\"id\":27,\"name\":\"oo\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":\"d3b3a793-4d6c-4c51-a511-49c8253d5f4b\",\"fromId\":\"24\",\"toId\":\"27\",\"fromInfo\":{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"44444\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":true,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738041980632,\"updateTime\":1738041980632}', 'user', '2025-01-28 13:26:18.624', '2025-01-28 13:26:26.455');
INSERT INTO `chat_list` VALUES ('5641ab87-1b25-4135-bb62-5df6622da979', '27', '24', '{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":\"d3b3a793-4d6c-4c51-a511-49c8253d5f4b\",\"fromId\":\"24\",\"toId\":\"27\",\"fromInfo\":{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"44444\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":true,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738041980632,\"updateTime\":1738041980632}', 'user', '2025-01-28 13:26:20.636', '2025-01-28 13:28:09.530');
INSERT INTO `chat_list` VALUES ('5b3b83d4339d459b92b2a886c7e9766e', '21', '23', '{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":null,\"fromId\":null,\"toId\":null,\"fromInfo\":null,\"message\":null,\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":null,\"type\":null,\"source\":null,\"createTime\":null,\"updateTime\":null}', 'user', '2025-01-28 17:31:45.938', '2025-01-28 20:38:22.715');
INSERT INTO `chat_list` VALUES ('6cccadd213e2409886dee3a9b0e19e41', '27', '1', '{\"id\":1,\"name\":\"这是个群聊\",\"avatar\":null,\"type\":null,\"badge\":null}', 0, '{\"id\":\"b5b36256-816f-4dd2-88aa-47c1a49b4c56\",\"fromId\":\"21\",\"toId\":\"1\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"tt\\\"}]\",\"isShowTime\":true,\"type\":\"text\",\"source\":\"group\",\"createTime\":1738066534291,\"updateTime\":1738066534291}', 'group', '2025-01-28 13:27:55.582', '2025-01-28 13:27:55.582');
INSERT INTO `chat_list` VALUES ('7c1f584c513645f89d71618f1725c690', '21', '24', '{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":null,\"fromId\":null,\"toId\":null,\"fromInfo\":null,\"message\":null,\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":null,\"type\":null,\"source\":null,\"createTime\":null,\"updateTime\":null}', 'user', '2025-01-28 13:27:09.321', '2025-01-28 20:38:23.387');
INSERT INTO `chat_list` VALUES ('84ca704bac244ce1acea418c72bb7795', '24', '25', '{\"id\":25,\"name\":\"1\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":\"1ddb1aab-b104-4f09-8351-2af6aab4b004\",\"fromId\":\"24\",\"toId\":\"25\",\"fromInfo\":{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"+999\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":false,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738041977143,\"updateTime\":1738041977143}', 'user', '2025-01-28 13:24:03.253', '2025-01-28 13:26:26.159');
INSERT INTO `chat_list` VALUES ('8ccb0ca65fdd425a8c12b89356be7bb5', '24', '26', '{\"id\":26,\"name\":\"xx\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":\"f6954675-0f64-43fd-b410-c5135ea0721d\",\"fromId\":\"24\",\"toId\":\"26\",\"fromInfo\":{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"888\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":true,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738041984835,\"updateTime\":1738041984835}', 'user', '2025-01-28 13:26:22.097', '2025-01-28 13:26:25.807');
INSERT INTO `chat_list` VALUES ('8df8587d-cfbb-4e5a-abbc-543a42bf88b2', '24', '21', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', 0, '{\"id\":\"203973c0-e2f4-48a7-ac40-6bd6df8ecd5b\",\"fromId\":\"21\",\"toId\":\"24\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":false,\"type\":\"recall\",\"source\":\"user\",\"createTime\":1738041525535,\"updateTime\":1738041527320}', 'user', '2025-01-28 13:11:27.910', '2025-01-28 13:24:17.292');
INSERT INTO `chat_list` VALUES ('916f736f60ae4531a4f6e84993b3cf47', '21', '25', '{\"id\":25,\"name\":\"1\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":null,\"fromId\":null,\"toId\":null,\"fromInfo\":null,\"message\":null,\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":null,\"type\":null,\"source\":null,\"createTime\":null,\"updateTime\":null}', 'user', '2025-01-28 20:38:23.373', '2025-01-28 20:38:24.133');
INSERT INTO `chat_list` VALUES ('a2768443daf044008b7561f63ed91f29', '23', '27', '{\"id\":27,\"name\":\"oo\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":\"1f3e1ddf-e81b-4188-9010-a6d4c926af48\",\"fromId\":\"23\",\"toId\":\"27\",\"fromInfo\":{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"。\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":true,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738042099157,\"updateTime\":1738042099157}', 'user', '2025-01-28 13:28:16.358', '2025-01-28 13:28:19.232');
INSERT INTO `chat_list` VALUES ('b3948a4b4d7d4d7eb45a097ce9418b3c', '24', '1', '{\"id\":1,\"name\":\"这是个群聊\",\"avatar\":null,\"type\":null,\"badge\":null}', 0, '{\"id\":\"b5b36256-816f-4dd2-88aa-47c1a49b4c56\",\"fromId\":\"21\",\"toId\":\"1\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"tt\\\"}]\",\"isShowTime\":true,\"type\":\"text\",\"source\":\"group\",\"createTime\":1738066534291,\"updateTime\":1738066534291}', 'group', '2025-01-28 13:23:19.729', '2025-01-28 13:23:52.041');
INSERT INTO `chat_list` VALUES ('b51bd805b228474f95ed819f72a32c9f', '21', '27', '{\"id\":27,\"name\":\"oo\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":\"ba04414d-2cc3-4f0f-8574-ceb1ca29c90d\",\"fromId\":\"21\",\"toId\":\"27\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"44\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":true,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738056654509,\"updateTime\":1738056654509}', 'user', '2025-01-28 17:30:45.481', '2025-01-28 20:38:25.641');
INSERT INTO `chat_list` VALUES ('bca54ed4d4e147589aa030ad8420dd65', '23', '24', '{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":\"9451e8a0-fe93-47f5-bd38-b9308e5aeebd\",\"fromId\":\"23\",\"toId\":\"24\",\"fromInfo\":{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"111\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":false,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738041996224,\"updateTime\":1738041996224}', 'user', '2025-01-28 13:25:43.290', '2025-01-28 13:28:02.749');
INSERT INTO `chat_list` VALUES ('c539b89c41e245b681fef20972dceb04', '20', '1', '{\"id\":1,\"name\":\"这是个群聊\",\"avatar\":null,\"type\":null,\"badge\":null}', 0, '{\"id\":\"b5b36256-816f-4dd2-88aa-47c1a49b4c56\",\"fromId\":\"21\",\"toId\":\"1\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"tt\\\"}]\",\"isShowTime\":true,\"type\":\"text\",\"source\":\"group\",\"createTime\":1738066534291,\"updateTime\":1738066534291}', 'group', '2025-01-28 17:27:45.643', '2025-01-28 17:27:57.434');
INSERT INTO `chat_list` VALUES ('d35706dd07cc4360aed3ccdacc0d96e3', '21', '20', '{\"id\":20,\"name\":\"mm\",\"avatar\":null,\"type\":\"user\",\"badge\":null}', 0, '{\"id\":\"4b56caf2-8799-4c21-a688-6a940c548428\",\"fromId\":\"21\",\"toId\":\"20\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"566\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":true,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738056458410,\"updateTime\":1738056458410}', 'user', '2025-01-28 13:27:06.993', '2025-01-28 20:38:21.998');
INSERT INTO `chat_list` VALUES ('d4054f00-28dd-4e2d-bb75-a67b7cc9ccf2', '26', '24', '{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":\"f6954675-0f64-43fd-b410-c5135ea0721d\",\"fromId\":\"24\",\"toId\":\"26\",\"fromInfo\":{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"888\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":true,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738041984835,\"updateTime\":1738041984835}', 'user', '2025-01-28 13:26:24.839', '2025-01-28 17:25:07.620');
INSERT INTO `chat_list` VALUES ('dcac76a8-5d5a-430e-a183-bb6a91edc1d9', '27', '21', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', 1, '{\"id\":\"ba04414d-2cc3-4f0f-8574-ceb1ca29c90d\",\"fromId\":\"21\",\"toId\":\"27\",\"fromInfo\":{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"44\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":true,\"type\":\"text\",\"source\":\"user\",\"createTime\":1738056654509,\"updateTime\":1738056654509}', 'user', '2025-01-28 13:12:37.125', '2025-01-28 17:30:54.512');
INSERT INTO `chat_list` VALUES ('ef2b913eae2a4a08b52057722b55d53c', '20', '26', '{\"id\":26,\"name\":\"xx\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', 0, '{\"id\":null,\"fromId\":null,\"toId\":null,\"fromInfo\":null,\"message\":null,\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":null,\"type\":null,\"source\":null,\"createTime\":null,\"updateTime\":null}', 'user', '2025-01-28 17:27:48.214', '2025-01-28 17:27:53.062');

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `from_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `to_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `from_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `reference_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `at_user` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `is_show_time` tinyint(1) NULL DEFAULT 0,
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` timestamp(3) NOT NULL,
  `update_time` timestamp(3) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_message_from_id_to_id`(`from_id` ASC, `to_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES ('1b2a96e6-08f7-4b79-b61f-b78a47334030', '23', '1', '{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '[{\"type\":\"text\",\"content\":\"a\"}]', NULL, NULL, 1, 'text', 'group', '2025-01-28 13:25:18.673', '2025-01-28 13:25:18.673');
INSERT INTO `message` VALUES ('1ddb1aab-b104-4f09-8351-2af6aab4b004', '24', '25', '{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '[{\"type\":\"text\",\"content\":\"+999\"}]', NULL, NULL, 0, 'text', 'user', '2025-01-28 13:26:17.143', '2025-01-28 13:26:17.143');
INSERT INTO `message` VALUES ('1f3e1ddf-e81b-4188-9010-a6d4c926af48', '23', '27', '{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '[{\"type\":\"text\",\"content\":\"。\"}]', NULL, NULL, 1, 'text', 'user', '2025-01-28 13:28:19.157', '2025-01-28 13:28:19.157');
INSERT INTO `message` VALUES ('203973c0-e2f4-48a7-ac40-6bd6df8ecd5b', '21', '24', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '', NULL, NULL, 0, 'recall', 'user', '2025-01-28 13:18:45.535', '2025-01-28 13:18:47.320');
INSERT INTO `message` VALUES ('214c434d-fda9-4dc8-9bbb-297931798d64', '21', '23', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '', NULL, NULL, 0, 'recall', 'user', '2025-01-28 13:14:32.439', '2025-01-28 13:16:53.457');
INSERT INTO `message` VALUES ('23579ab8-c3d0-4cfd-b058-240c3e374a86', '21', '1', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"1111\"}]', NULL, NULL, 1, 'text', 'group', '2025-01-28 13:11:35.197', '2025-01-28 13:11:35.197');
INSERT INTO `message` VALUES ('24bb8703-9ebd-4600-8dad-f713507dd354', '21', '1', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"99999\"}]', NULL, NULL, 0, 'text', 'group', '2025-01-28 13:14:38.346', '2025-01-28 13:14:38.346');
INSERT INTO `message` VALUES ('36d0a641-2e6f-464c-8660-181f9c933d2f', '20', '1', '{\"id\":20,\"name\":\"mm\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '[{\"type\":\"text\",\"content\":\"haha1\\\\\"}]', '{\"id\":\"f491a912-fe01-42b2-b00f-aa343663a90e\",\"fromId\":\"23\",\"toId\":\"1\",\"fromInfo\":{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"oi\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":false,\"type\":\"text\",\"source\":\"group\",\"createTime\":1738042086217,\"updateTime\":1738042086217}', NULL, 1, 'text', 'group', '2025-01-28 17:27:57.424', '2025-01-28 17:27:57.424');
INSERT INTO `message` VALUES ('37ca01be-030e-4790-b079-991ac4a13d13', '24', '25', '{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '[{\"type\":\"text\",\"content\":\"哈喽\"}]', NULL, NULL, 1, 'text', 'user', '2025-01-28 13:24:05.707', '2025-01-28 13:24:05.707');
INSERT INTO `message` VALUES ('37ffe1eb-dad7-4d38-8773-1df24596a9a4', '21', '1', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"9999\"}]', NULL, NULL, 0, 'text', 'group', '2025-01-28 13:13:04.005', '2025-01-28 13:13:04.005');
INSERT INTO `message` VALUES ('46657d72-a18e-40a8-a975-1e4de38a637e', '21', '26', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"11111\"}]', NULL, NULL, 1, 'text', 'user', '2025-01-28 13:11:48.014', '2025-01-28 13:11:48.014');
INSERT INTO `message` VALUES ('4b192e50-ba7e-4b3d-a6ac-f86f4cf5c59d', '23', '24', '{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '[{\"type\":\"text\",\"content\":\"Dddd\"}]', NULL, NULL, 1, 'text', 'user', '2025-01-28 13:25:51.419', '2025-01-28 13:25:51.419');
INSERT INTO `message` VALUES ('4b56caf2-8799-4c21-a688-6a940c548428', '21', '20', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"566\"}]', NULL, NULL, 1, 'text', 'user', '2025-01-28 17:27:38.410', '2025-01-28 17:27:38.410');
INSERT INTO `message` VALUES ('4b780710-22fd-43a4-9d14-66b1651b6737', '21', '23', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '', NULL, NULL, 0, 'recall', 'user', '2025-01-28 13:11:21.658', '2025-01-28 13:11:23.345');
INSERT INTO `message` VALUES ('51c2db60-ae1b-41d9-a099-4ef1e4b12ddb', '21', '24', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '', NULL, NULL, 0, 'recall', 'user', '2025-01-28 13:15:23.988', '2025-01-28 13:17:05.285');
INSERT INTO `message` VALUES ('55d7a148-1e5e-4b92-b1f4-3338c6cc1691', '21', '25', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"999999\"}]', NULL, NULL, 1, 'text', 'user', '2025-01-28 13:11:43.800', '2025-01-28 13:11:43.800');
INSERT INTO `message` VALUES ('5a74bc50-ed9d-4225-964b-5b83bf860d00', '21', '27', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"88888888\"}]', NULL, NULL, 0, 'text', 'user', '2025-01-28 13:14:43.165', '2025-01-28 13:14:43.165');
INSERT INTO `message` VALUES ('60707b84-d2cc-4fae-856b-e2b13663bfc5', '21', '24', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"999\"}]', NULL, NULL, 0, 'text', 'user', '2025-01-28 13:14:15.031', '2025-01-28 13:14:15.031');
INSERT INTO `message` VALUES ('66f5b0fb-4a71-4583-a1b7-f1b3a88e8b7b', '21', '27', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '', NULL, NULL, 1, 'recall', 'user', '2025-01-28 13:12:37.119', '2025-01-28 13:12:47.210');
INSERT INTO `message` VALUES ('674d210f-202f-4a83-b04e-3474af14f130', '21', '26', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '', NULL, NULL, 1, 'recall', 'user', '2025-01-28 17:31:09.737', '2025-01-28 17:31:11.714');
INSERT INTO `message` VALUES ('774bd5ca-906a-41cd-bc62-6070d78279fe', '21', '26', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"4444\"}]', NULL, NULL, 0, 'text', 'user', '2025-01-28 13:14:28.409', '2025-01-28 13:14:28.409');
INSERT INTO `message` VALUES ('78aa7c4e-1221-474a-ba42-dd58dd7260c3', '21', '1', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"4\"}]', NULL, NULL, 1, 'text', 'group', '2025-01-28 13:23:46.289', '2025-01-28 13:23:46.289');
INSERT INTO `message` VALUES ('7de2b4d4-8ad3-46e4-89f6-d7495a26564a', '24', '1', '{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '[{\"type\":\"text\",\"content\":\"我来了\"}]', NULL, NULL, 1, 'text', 'group', '2025-01-28 13:23:31.832', '2025-01-28 13:23:31.832');
INSERT INTO `message` VALUES ('80299e78-bae9-4e2a-94f7-b03063866591', '21', '23', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"66666\"}]', NULL, NULL, 0, 'text', 'user', '2025-01-28 13:16:58.609', '2025-01-28 13:16:58.609');
INSERT INTO `message` VALUES ('84eaa732-fca2-4d27-84a2-b8f78880adea', '21', '20', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"151548941\"}]', NULL, NULL, 1, 'text', 'user', '2025-01-28 13:14:47.630', '2025-01-28 13:14:47.630');
INSERT INTO `message` VALUES ('89300d0e-ed9a-49a5-9446-e05118db67b4', '23', '1', '{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '[{\"type\":\"text\",\"content\":\"有点问题\"}]', NULL, NULL, 0, 'text', 'group', '2025-01-28 13:26:57.488', '2025-01-28 13:26:57.488');
INSERT INTO `message` VALUES ('902880dc-43d8-4ab2-b3bd-80c7c3947e8e', '21', '1', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"a\"}]', NULL, NULL, 0, 'text', 'group', '2025-01-28 13:24:49.062', '2025-01-28 13:24:49.062');
INSERT INTO `message` VALUES ('938e8ab8-52ed-445e-b5c2-e74a482cbee6', '21', '25', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"9999\"}]', NULL, NULL, 0, 'text', 'user', '2025-01-28 13:14:24.630', '2025-01-28 13:14:24.630');
INSERT INTO `message` VALUES ('9451e8a0-fe93-47f5-bd38-b9308e5aeebd', '23', '24', '{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '[{\"type\":\"text\",\"content\":\"111\"}]', NULL, NULL, 0, 'text', 'user', '2025-01-28 13:26:36.224', '2025-01-28 13:26:36.224');
INSERT INTO `message` VALUES ('9f4aba9c-67f6-4d59-b863-265e30e3330c', '23', '21', '{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '', NULL, NULL, 1, 'recall', 'user', '2025-01-28 13:25:28.905', '2025-01-28 13:25:34.367');
INSERT INTO `message` VALUES ('a1fd4967-35cd-4d10-a2e6-50dce5bb7b82', '21', '24', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '', NULL, NULL, 0, 'recall', 'user', '2025-01-28 13:18:00.317', '2025-01-28 13:18:03.295');
INSERT INTO `message` VALUES ('b464b825-541d-405e-954d-dfa27effd884', '21', '24', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"9999\"}]', NULL, NULL, 1, 'text', 'user', '2025-01-28 13:11:27.906', '2025-01-28 13:11:27.906');
INSERT INTO `message` VALUES ('b5b36256-816f-4dd2-88aa-47c1a49b4c56', '21', '1', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"tt\"}]', NULL, NULL, 1, 'text', 'group', '2025-01-28 20:15:34.291', '2025-01-28 20:15:34.291');
INSERT INTO `message` VALUES ('ba04414d-2cc3-4f0f-8574-ceb1ca29c90d', '21', '27', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"44\"}]', NULL, NULL, 1, 'text', 'user', '2025-01-28 17:30:54.509', '2025-01-28 17:30:54.509');
INSERT INTO `message` VALUES ('d2840cd1-dde1-4f78-82f1-444f7b674ef5', '21', '1', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"at\",\"content\":\"{\\\"name\\\":\\\"pp\\\",\\\"id\\\":23,\\\"type\\\":\\\"user\\\"}\"},{\"type\":\"text\",\"content\":\"haha\"}]', '{\"id\":\"89300d0e-ed9a-49a5-9446-e05118db67b4\",\"fromId\":\"23\",\"toId\":\"1\",\"fromInfo\":{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]},\"message\":\"[{\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"有点问题\\\"}]\",\"referenceMsg\":null,\"atUser\":null,\"isShowTime\":false,\"type\":\"text\",\"source\":\"group\",\"createTime\":1738042017488,\"updateTime\":1738042017488}', NULL, 1, 'text', 'group', '2025-01-28 17:28:14.363', '2025-01-28 17:28:14.363');
INSERT INTO `message` VALUES ('d2a1593a-2450-4336-909c-582e1a41f0ff', '21', '20', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"66\"}]', NULL, NULL, 0, 'text', 'user', '2025-01-28 13:27:12.407', '2025-01-28 13:27:12.407');
INSERT INTO `message` VALUES ('d3b3a793-4d6c-4c51-a511-49c8253d5f4b', '24', '27', '{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '[{\"type\":\"text\",\"content\":\"44444\"}]', NULL, NULL, 1, 'text', 'user', '2025-01-28 13:26:20.632', '2025-01-28 13:26:20.632');
INSERT INTO `message` VALUES ('e4c01587-0ea9-427e-a25f-be65a4b3da38', '24', '23', '{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '[{\"type\":\"text\",\"content\":\"666\"}]', NULL, NULL, 0, 'text', 'user', '2025-01-28 13:26:14.197', '2025-01-28 13:26:14.197');
INSERT INTO `message` VALUES ('f491a912-fe01-42b2-b00f-aa343663a90e', '23', '1', '{\"id\":23,\"name\":\"pp\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '[{\"type\":\"text\",\"content\":\"oi\"}]', NULL, NULL, 0, 'text', 'group', '2025-01-28 13:28:06.217', '2025-01-28 13:28:06.217');
INSERT INTO `message` VALUES ('f6954675-0f64-43fd-b410-c5135ea0721d', '24', '26', '{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '[{\"type\":\"text\",\"content\":\"888\"}]', NULL, NULL, 1, 'text', 'user', '2025-01-28 13:26:24.835', '2025-01-28 13:26:24.835');
INSERT INTO `message` VALUES ('f99b9a66-0f73-407b-884d-3b670533e9c0', '21', '23', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '', NULL, NULL, 1, 'recall', 'user', '2025-01-28 13:11:06.076', '2025-01-28 13:11:10.007');
INSERT INTO `message` VALUES ('fada48c4-fb00-46f1-9615-bff6a607bfe9', '21', '20', '{\"id\":21,\"name\":\"tt\",\"avatar\":\"\",\"type\":\"user\",\"badge\":[\"diamond\",\"clover\"]}', '[{\"type\":\"text\",\"content\":\"啊？\"}]', NULL, NULL, 1, 'text', 'user', '2025-01-28 13:24:29.279', '2025-01-28 13:24:29.279');
INSERT INTO `message` VALUES ('fdea8a1f-f3e8-44bd-9066-82c7d8cd0ea2', '24', '1', '{\"id\":24,\"name\":\"ddd\",\"avatar\":null,\"type\":\"user\",\"badge\":[\"clover\"]}', '', NULL, NULL, 0, 'recall', 'group', '2025-01-28 13:23:41.381', '2025-01-28 13:23:51.972');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `menu_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NULL' COMMENT '菜单名',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件路径',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` timestamp(3) NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` timestamp(3) NULL DEFAULT NULL,
  `del_flag` int NULL DEFAULT 0 COMMENT '是否删除（0未删除 1已删除）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色权限字符串',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '角色状态（0正常 1停用）',
  `del_flag` int NULL DEFAULT 0 COMMENT 'del_flag',
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` timestamp(3) NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` timestamp(3) NULL DEFAULT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '管理员', 'ceo', '0', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role` VALUES (2, '普通成员', 'coder', '0', 0, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `menu_id` bigint NOT NULL DEFAULT 0 COMMENT '菜单id',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1);
INSERT INTO `sys_role_menu` VALUES (1, 2);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NULL' COMMENT '用户名',
  `nick_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NULL' COMMENT '昵称',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NULL' COMMENT '密码',
  `status` int NULL DEFAULT 0 COMMENT '账号状态（0正常 1停用）',
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `sex` int NULL DEFAULT NULL COMMENT '用户性别（0男，1女，2未知）',
  `avatar` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `user_type` int NOT NULL DEFAULT 1 COMMENT '用户类型（0管理员，1普通用户）',
  `badge` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `login_time` timestamp(3) NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人的用户id',
  `create_time` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `update_time` timestamp(3) NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` int NULL DEFAULT 0 COMMENT '删除标志（0代表未删除，1代表已删除）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (20, 'mm', 'NULL', '$2a$10$akECSTsJz33fPGCH3k8Q8u1wqR5CkszO9XtR2txooZ10gRw1tcqYW', 0, '3116430062@qq.com', NULL, NULL, NULL, 1, '[\"clover\"]', '2025-01-28 17:27:45.624', NULL, '2025-01-25 21:12:23.000', NULL, '2025-01-28 17:27:45.625', 0);
INSERT INTO `sys_user` VALUES (21, 'tt', 'NULL', '$2a$10$ziESc.jJTAzOfRLqhgaOkero9o2OYvCPQE2YSCXwXtr8rTyuEDclG', 0, '3116430063@qq.com', NULL, NULL, '', 1, '[\"diamond\",\"clover\"]', '2025-01-28 20:37:25.774', NULL, '2025-01-25 23:32:14.000', NULL, '2025-01-28 20:37:25.776', 0);
INSERT INTO `sys_user` VALUES (23, 'pp', 'NULL', '$2a$10$CnnNt1kYCmB8HDxqd7aA/em75fUfMFOAP3j2D8cVkdCGfLeskRrtq', 0, '3116430064@qq.com', NULL, NULL, NULL, 1, '[\"clover\"]', '2025-01-28 13:25:04.588', NULL, '2025-01-25 23:51:35.000', NULL, '2025-01-28 13:25:04.590', 0);
INSERT INTO `sys_user` VALUES (24, 'ddd', 'NULL', '$2a$10$a3/J8523S/gQAt.mkJXBJOTIxvdDr8aR//ZvHVRPFOMjnn8R2fzPi', 0, '3116430065@qq.com', NULL, NULL, NULL, 1, '[\"clover\"]', '2025-01-28 20:37:41.636', NULL, '2025-01-26 03:35:53.000', NULL, '2025-01-28 20:37:41.636', 0);
INSERT INTO `sys_user` VALUES (25, '1', 'NULL', '$2a$10$8DmPLKSbo9g0dLdt2kJeauS1tioX4rbUs9dTrnrS9p7jZdp4LCmyq', 0, '3116430066@qq.com', NULL, NULL, NULL, 1, '[\"clover\"]', '2025-01-26 09:03:32.000', NULL, '2025-01-26 05:57:29.000', NULL, '2025-01-26 08:56:12.000', 0);
INSERT INTO `sys_user` VALUES (26, 'xx', 'NULL', '$2a$10$kyxBprkFyMuNY0rcD4YWJeRG.dPu0ceEf1OZYPU6/6BTyTFc8UEFG', 0, '3116430062@qq.com', NULL, NULL, NULL, 1, '[\"clover\"]', '2025-01-28 17:24:55.992', NULL, '2025-01-26 16:02:40.000', NULL, '2025-01-28 17:24:55.996', 0);
INSERT INTO `sys_user` VALUES (27, 'oo', 'NULL', '$2a$10$2Hiv0UPOxU3a.0uMWKjw5.GcGUZlVwEmqLyFKQuM6FNg38QlItDFe', 0, '3116430062@qq.com', NULL, NULL, NULL, 1, '[\"clover\"]', '2025-01-28 13:27:55.560', NULL, '2025-01-26 17:11:47.000', NULL, '2025-01-28 13:27:55.561', 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `role_id` bigint NOT NULL DEFAULT 0 COMMENT '角色id',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (20, 1);

SET FOREIGN_KEY_CHECKS = 1;
