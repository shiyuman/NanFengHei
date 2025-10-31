package cn.sym.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件存储信息实体类
 *
 * @author user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("file_storage")
public class FileDO {

    /**
     * 文件记录唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * OSS存储路径Key
     */
    private String ossKey;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 上传者
     */
    private String uploadUser;

    /**
     * 上传时间
     */
    private Date uploadTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 修改时间
     */
    private Date updateTime;
}