package com.horzits.business.service;

import java.util.List;
import com.horzits.business.domain.LyhCheckin;

/**
 * 每日打卡Service接口
 * 
 * @author horzits
 * @date 2024-07-02
 */
public interface ILyhCheckinService 
{
    /**
     * 查询每日打卡
     * 
     * @param checkinId 每日打卡主键
     * @return 每日打卡
     */
    public LyhCheckin selectLyhCheckinByCheckinId(Long checkinId);

    /**
     * 查询每日打卡列表
     * 
     * @param lyhCheckin 每日打卡
     * @return 每日打卡集合
     */
    public List<LyhCheckin> selectLyhCheckinList(LyhCheckin lyhCheckin);

    /**
     * 新增每日打卡
     * 
     * @param lyhCheckin 每日打卡
     * @return 结果
     */
    public int insertLyhCheckin(LyhCheckin lyhCheckin);

    /**
     * 修改每日打卡
     * 
     * @param lyhCheckin 每日打卡
     * @return 结果
     */
    public int updateLyhCheckin(LyhCheckin lyhCheckin);

    /**
     * 批量删除每日打卡
     * 
     * @param checkinIds 需要删除的每日打卡主键集合
     * @return 结果
     */
    public int deleteLyhCheckinByCheckinIds(Long[] checkinIds);

    /**
     * 删除每日打卡信息
     * 
     * @param checkinId 每日打卡主键
     * @return 结果
     */
    public int deleteLyhCheckinByCheckinId(Long checkinId);

    /**
     * 用户打卡
     * @param userId
     * @return 打卡结果
     */
    public LyhCheckin checkin(Long userId);
    
    /**
     * 检查用户今日是否已打卡
     * @param userId
     * @return
     */
    public boolean hasCheckedInToday(Long userId);
}
