package cn.sym.service.impl;

import cn.sym.dto.PromotionActivityDTO;
import cn.sym.dto.PromotionActivityQuery;
import cn.sym.entity.PromotionActivityDO;
import cn.sym.repository.PromotionActivityMapper;
import cn.sym.service.PromotionActivityService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 *   促销活动业务逻辑实现类
 * </p>
 * @author user
 */
@Service
public class PromotionActivityServiceImpl extends ServiceImpl<PromotionActivityMapper, PromotionActivityDO> implements PromotionActivityService {

    @Override
    public Boolean addPromotionActivity(PromotionActivityDTO promotionActivityDTO) {
        PromotionActivityDO promotionActivityDO = new PromotionActivityDO();
        BeanUtils.copyProperties(promotionActivityDTO, promotionActivityDO);
        return this.save(promotionActivityDO);
    }

    @Override
    public Boolean updatePromotionActivity(PromotionActivityDTO promotionActivityDTO) {
        PromotionActivityDO promotionActivityDO = this.getById(promotionActivityDTO.getId());
        if (promotionActivityDO == null) {
            return false;
        }
        BeanUtils.copyProperties(promotionActivityDTO, promotionActivityDO);
        return updateById(promotionActivityDO);
    }

    @Override
    public Boolean deletePromotionActivity(Long id) {
        return removeById(id);
    }

    @Override
    public Page<PromotionActivityDO> listPromotionActivities(PromotionActivityQuery query) {
        int pageNo = query.getPageNo() == null ? 1 : query.getPageNo();
        int pageSize = query.getPageSize() == null ? 10 : query.getPageSize();
        
        Page<PromotionActivityDO> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<PromotionActivityDO> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.orderByDesc(PromotionActivityDO::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public PromotionActivityDO getPromotionActivityDetail(Long id) {
        return getById(id);
    }
}