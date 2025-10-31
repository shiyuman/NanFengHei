CREATE TABLE `product_info` (
	`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品唯一标识',
	`name` VARCHAR(100) NOT NULL COMMENT '商品名称',
	`description` TEXT COMMENT '商品描述',
	`category_id` BIGINT COMMENT '分类ID',
	`price` int NOT NULL COMMENT '单价',
	`stock` INT COMMENT '库存数量',
	`status` TINYINT COMMENT '上下架状态：1-上架，0-下架',
	`create_by` VARCHAR(50) COMMENT '创建人',
	`create_time` DATETIME COMMENT '创建时间',
	`update_by` VARCHAR(50) COMMENT '修改人',
	`update_time` DATETIME COMMENT '修改时间',
	PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARSET = utf8mb4 COMMENT '商品信息表';