package cn.sym.entity;

import java.util.Date;
import lombok.*;
import com.baomidou.mybatisplus.annotation.*;

/**
 * 用户实体类
 *
 * @author user
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("user_info")
public class UserInfo {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密后）
     */
    private String password;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 账户状态：1-正常，0-禁用
     */
    private Integer status;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
