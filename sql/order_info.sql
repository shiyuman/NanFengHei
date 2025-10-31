
CREATE TABLE order_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单唯一标识',
    user_id BIGINT NOT NULL COMMENT '下单用户ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    delivery_type TINYINT COMMENT '配送方式：1-自取，2-快递',
    status TINYINT DEFAULT 1 COMMENT '订单状态：1-待支付，2-已支付，3-已完成，4-已取消',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '修改人',
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='订单信息表';