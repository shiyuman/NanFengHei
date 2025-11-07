package cn.sym.dto;

import javax.validation.constraints.Min;
import lombok.Data;

/**
 * 分页查询商品列表传输对象
 * @author user
 */
@Data
public class ProductPageQueryDTO {

    /**
     * 页码，默认为1
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    /**
     * 每页条数，默认为10
     */
    @Min(value = 1, message = "每页条数最小为1")
    private Integer size = 10;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品状态
     */
    private Integer status;
    
    /**
     * 游标ID，用于游标分页优化
     */
    private Long cursorId;
    
    /**
     * 排序字段
     */
    private String orderBy = "create_time";
    
    /**
     * 排序方式：asc/desc
     */
    private String orderType = "desc";
}