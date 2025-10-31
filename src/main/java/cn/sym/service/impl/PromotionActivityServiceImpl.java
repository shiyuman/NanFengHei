package cn.sym.service.impl;

import cn.sym.dto.PromotionActivityDTO;
import cn.sym.dto.PromotionActivityQuery;
import cn.sym.entity.PromotionActivityDO;
import cn.sym.exception.BusinessException;
import cn.sym.repository.PromotionActivityMapper;
import cn.sym.service.PromotionActivityService;
import cn.sym.utils.ResultCodeConstant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 *   促销活动服务实现类
 * </p>
 * @author user
 */
@Slf4j
@Service
public class PromotionActivityServiceImpl extends ServiceImpl<PromotionActivityMapper, PromotionActivityDO> implements PromotionActivityService {

    @Override
    public Boolean addPromotionActivity(PromotionActivityDTO promotionActivityDTO) {
        // 校验时间是否合法
        if (promotionActivityDTO.getStartTime().after(promotionActivityDTO.getEndTime())) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "活动开始时间不能晚于结束时间");
        }

        // 校验是否有重叠的时间段
        QueryWrapper<PromotionActivityDO> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.ge("start_time", promotionActivityDTO.getStartTime())
                .le("end_time", promotionActivityDTO.getEndTime()))
                .or()
                .and(w -> w.le("start_time", promotionActivityDTO.getStartTime())
                        .ge("end_time", promotionActivityDTO.getStartTime()))
                .or()
                .and(w -> w.le("start_time", promotionActivityDTO.getEndTime())
                        .ge("end_time", promotionActivityDTO.getEndTime()));
        List<PromotionActivityDO> existingActivities = this.list(wrapper);
        if (!existingActivities.isEmpty()) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "活动时间冲突或非法");
        }

        // 构建并插入新活动
        PromotionActivityDO activityDO = new PromotionActivityDO();
        activityDO.setName(promotionActivityDTO.getName());
        activityDO.setActivityType(promotionActivityDTO.getActivityType());
        activityDO.setStartTime(promotionActivityDTO.getStartTime());
        activityDO.setEndTime(promotionActivityDTO.getEndTime());
        activityDO.setStatus(1); // 默认状态为进行中
        activityDO.setCreateTime(new Date());
        activityDO.setUpdateTime(new Date());

        return this.save(activityDO);
    }

    @Override
    public Boolean updatePromotionActivity(PromotionActivityDTO promotionActivityDTO) {
        // 判断是否存在该活动
        PromotionActivityDO oldActivity = this.getById(promotionActivityDTO.getId());
        if (oldActivity == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "活动信息不存在");
        }

        // 更新活动信息
        oldActivity.setName(promotionActivityDTO.getName());
        oldActivity.setActivityType(promotionActivityDTO.getActivityType());
        oldActivity.setStartTime(promotionActivityDTO.getStartTime());
        oldActivity.setEndTime(promotionActivityDTO.getEndTime());
        oldActivity.setStatus(promotionActivityDTO.getStatus());
        oldActivity.setUpdateTime(new Date());

        return this.updateById(oldActivity);
    }

    @Override
    public Boolean deletePromotionActivity(Long id) {
        // 判断是否存在该活动
        PromotionActivityDO activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "活动信息不存在");
        }

        return this.removeById(id);
    }

    @Override
    public Page<PromotionActivityDO> listPromotionActivities(PromotionActivityQuery query) {
        Page<PromotionActivityDO> page = new Page<>(query.getPageNo(), query.getPageSize());
        return this.page(page);
    }

    @Override
    public PromotionActivityDO getPromotionActivityDetail(Long id) {
        PromotionActivityDO activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "活动信息不存在");
        }
        return activity;
    }
}