
CREATE TABLE pre_sale_ticket (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '早鸟票唯一标识',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sale_start_time DATETIME COMMENT '预售开始时间',
    sale_end_time DATETIME COMMENT '预售结束时间',
    pre_price DECIMAL(10, 2) NOT NULL COMMENT '预售价格',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '修改人',
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='早鸟票配置表';