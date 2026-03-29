package com.horzits.business.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import com.horzits.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.horzits.business.mapper.LyhCheckinMapper;
import com.horzits.business.domain.LyhCheckin;
import com.horzits.business.service.ILyhCheckinService;
import com.horzits.common.core.text.Convert;
import org.springframework.transaction.annotation.Transactional;

/**
 * 每日打卡Service业务层处理
 * 
 * @author horzits
 * @date 2024-07-02
 */
@Service
public class LyhCheckinServiceImpl implements ILyhCheckinService 
{
    @Autowired
    private LyhCheckinMapper lyhCheckinMapper;

    /**
     * 查询每日打卡
     * 
     * @param checkinId 每日打卡主键
     * @return 每日打卡
     */
    @Override
    public LyhCheckin selectLyhCheckinByCheckinId(Long checkinId)
    {
        return lyhCheckinMapper.selectLyhCheckinByCheckinId(checkinId);
    }

    /**
     * 查询每日打卡列表
     * 
     * @param lyhCheckin 每日打卡
     * @return 每日打卡
     */
    @Override
    public List<LyhCheckin> selectLyhCheckinList(LyhCheckin lyhCheckin)
    {
        return lyhCheckinMapper.selectLyhCheckinList(lyhCheckin);
    }

    /**
     * 新增每日打卡
     * 
     * @param lyhCheckin 每日打卡
     * @return 结果
     */
    @Override
    public int insertLyhCheckin(LyhCheckin lyhCheckin)
    {
        if (lyhCheckin.getUserId() == null || lyhCheckin.getCheckinDate() == null) {
            return 0;
        }
        Date dateOnly = Date.from(lyhCheckin.getCheckinDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        LyhCheckin exists = lyhCheckinMapper.selectByUserIdAndDate(lyhCheckin.getUserId(), dateOnly);
        if (exists != null) {
            throw new RuntimeException("该用户该日期已打卡");
        }
        if (lyhCheckin.getStreakAfter() == null) {
            LyhCheckin prev = lyhCheckinMapper.selectLatestBeforeByUserId(lyhCheckin.getUserId(), dateOnly);
            int streak = 1;
            if (prev != null) {
                LocalDate prevDate = prev.getCheckinDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate thisDate = dateOnly.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (prevDate.plusDays(1).equals(thisDate)) {
                    streak = prev.getStreakAfter() + 1;
                }
            }
            lyhCheckin.setStreakAfter(streak);
        }
        lyhCheckin.setCheckinDate(dateOnly);
        if (lyhCheckin.getCreateTime() == null) {
            lyhCheckin.setCreateTime(DateUtils.getNowDate());
        }
        return lyhCheckinMapper.insertLyhCheckin(lyhCheckin);
    }

    /**
     * 修改每日打卡
     * 
     * @param lyhCheckin 每日打卡
     * @return 结果
     */
    @Override
    public int updateLyhCheckin(LyhCheckin lyhCheckin)
    {
        return lyhCheckinMapper.updateLyhCheckin(lyhCheckin);
    }

    /**
     * 批量删除每日打卡
     * 
     * @param checkinIds 需要删除的每日打卡主键
     * @return 结果
     */
    @Override
    public int deleteLyhCheckinByCheckinIds(Long[] checkinIds)
    {
        return lyhCheckinMapper.deleteLyhCheckinByCheckinIds(checkinIds);
    }

    /**
     * 删除每日打卡信息
     * 
     * @param checkinId 每日打卡主键
     * @return 结果
     */
    @Override
    public int deleteLyhCheckinByCheckinId(Long checkinId)
    {
        return lyhCheckinMapper.deleteLyhCheckinByCheckinId(checkinId);
    }

    @Override
    @Transactional
    public LyhCheckin checkin(Long userId) {
        // 1. Check if already checked in today
        Date today = new Date();
        LocalDate todayLocalDate = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Date todayDateOnly = Date.from(todayLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        LyhCheckin existingCheckin = lyhCheckinMapper.selectByUserIdAndDate(userId, todayDateOnly);
        if (existingCheckin != null) {
            throw new RuntimeException("Today already checked in");
        }

        // 2. Calculate streak
        LyhCheckin lastCheckin = lyhCheckinMapper.selectLatestByUserId(userId);
        int streak = 1;
        if (lastCheckin != null) {
            LocalDate lastCheckinDate = lastCheckin.getCheckinDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (lastCheckinDate.plusDays(1).equals(todayLocalDate)) {
                streak = lastCheckin.getStreakAfter() + 1;
            }
        }

        // 3. Create new checkin record
        LyhCheckin newCheckin = new LyhCheckin();
        newCheckin.setUserId(userId);
        newCheckin.setCheckinDate(todayDateOnly);
        newCheckin.setStreakAfter(streak);
        newCheckin.setCreateTime(today);
        
        lyhCheckinMapper.insertLyhCheckin(newCheckin);
        
        return newCheckin;
    }

    @Override
    public boolean hasCheckedInToday(Long userId) {
        Date today = new Date();
        LocalDate todayLocalDate = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Date todayDateOnly = Date.from(todayLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        return lyhCheckinMapper.selectByUserIdAndDate(userId, todayDateOnly) != null;
    }
}
