
CREATE TABLE limit_purchase (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '限购记录唯一标识',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    max_quantity INT NOT NULL COMMENT '最大购买数量',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '修改人',
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='限购配置表';