
CREATE TABLE promotion_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '促销活动唯一标识',
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    activity_type TINYINT COMMENT '活动类型：1-限时折扣，2-秒杀',
    start_time DATETIME COMMENT '活动开始时间',
    end_time DATETIME COMMENT '活动结束时间',
    status TINYINT DEFAULT 1 COMMENT '活动状态：1-进行中，2-已结束，3-已暂停',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '修改人',
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='促销活动表';