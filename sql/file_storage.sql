
CREATE TABLE file_storage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件记录唯一标识',
    original_name VARCHAR(200) NOT NULL COMMENT '原始文件名',
    oss_key VARCHAR(200) NOT NULL COMMENT 'OSS存储路径Key',
    file_size BIGINT COMMENT '文件大小（字节）',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    upload_user VARCHAR(50) COMMENT '上传者',
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '修改人',
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='文件存储信息表';