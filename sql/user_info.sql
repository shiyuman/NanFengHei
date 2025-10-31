CREATE TABLE `user_info` (
	`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户唯一标识',
	`username` VARCHAR(50) NOT NULL COMMENT '用户名',
	`password` VARCHAR(100) NOT NULL COMMENT '密码（加密后）',
	`phone` VARCHAR(20) COMMENT '手机号码',
	`status` TINYINT COMMENT '账户状态：1-正常，0-禁用',
	`create_by` VARCHAR(50) COMMENT '创建人',
	`create_time` DATETIME COMMENT '创建时间',
	`update_by` VARCHAR(50) COMMENT '修改人',
	`update_time` DATETIME COMMENT '修改时间'
) ENGINE = InnoDB CHARSET = utf8mb4 COMMENT '用户信息表';