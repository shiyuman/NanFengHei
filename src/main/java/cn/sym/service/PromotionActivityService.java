package cn.sym.service;

import cn.sym.dto.PromotionActivityDTO;
import cn.sym.dto.PromotionActivityQuery;
import cn.sym.entity.PromotionActivityDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *   促销活动服务接口
 * </p>
 * @author user
 */
public interface PromotionActivityService extends IService<PromotionActivityDO> {

    /**
     * 新增促销活动
     * @param promotionActivityDTO 活动信息
     * @return 是否成功
     */
    Boolean addPromotionActivity(PromotionActivityDTO promotionActivityDTO);

    /**
     * 更新促销活动
     * @param promotionActivityDTO 活动信息
     * @return 是否成功
     */
    Boolean updatePromotionActivity(PromotionActivityDTO promotionActivityDTO);

    /**
     * 删除促销活动
     * @param id 活动ID
     * @return 是否成功
     */
    Boolean deletePromotionActivity(Long id);

    /**
     * 查询促销活动列表
     * @param query 查询条件
     * @return 分页结果
     */
    Page<PromotionActivityDO> listPromotionActivities(PromotionActivityQuery query);

    /**
     * 获取促销活动详情
     * @param id 活动ID
     * @return 活动详情
     */
    PromotionActivityDO getPromotionActivityDetail(Long id);
}