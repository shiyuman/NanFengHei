
CREATE TABLE coupon_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '优惠券唯一标识',
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    type TINYINT COMMENT '类型：1-满减券，2-折扣券',
    discount_amount DECIMAL(10, 2) COMMENT '减免金额或折扣比例',
    min_amount DECIMAL(10, 2) COMMENT '最低消费金额',
    start_time DATETIME COMMENT '生效开始时间',
    end_time DATETIME COMMENT '生效结束时间',
    status TINYINT DEFAULT 1 COMMENT '可用状态：1-未使用，2-已使用，3-已过期',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '修改人',
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='优惠券信息表';