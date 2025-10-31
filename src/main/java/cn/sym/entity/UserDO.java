package cn.sym.entity;

import java.util.Date;
import javax.persistence.*;
import lombok.*;
import org.apache.ibatis.type.Alias;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@NoArgsConstructor
@AllArgsConstructor
@Alias("UserDO")
@Entity
@Table(name = "user_info")
@TableName("user_info")
@Data
@ApiModel(description = "用户信息实体类")
public class UserDO {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "用户ID")
    private Long id;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String username;

    /**
     * 密码（加密后）
     */
    @ApiModelProperty(value = "密码（加密后）")
    private String password;

    /**
     * 手机号码
     */
    @ApiModelProperty(value = "手机号码")
    private String phone;

    /**
     * 账户状态：1-正常，0-禁用
     */
    @ApiModelProperty(value = "账户状态：1-正常，0-禁用")
    private Integer status;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 修改人
     */
    @ApiModelProperty(value = "修改人")
    private String updateBy;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;
}
