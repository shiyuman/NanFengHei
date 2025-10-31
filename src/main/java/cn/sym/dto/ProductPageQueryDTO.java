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
    private Integer pageNo = 1;

    /**
     * 每页条数，默认为10
     */
    @Min(value = 1, message = "每页条数最小为1")
    private Integer pageSize = 10;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 分类ID
     */
    private Long categoryId;
}