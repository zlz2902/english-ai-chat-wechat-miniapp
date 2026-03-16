package com.horzits.business.mapper;

import java.util.Date;
import java.util.List;
import com.horzits.business.domain.LyhCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 每日打卡Mapper接口
 */
@Mapper
public interface LyhCheckinMapper 
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
     * 删除每日打卡
     * 
     * @param checkinId 每日打卡主键
     * @return 结果
     */
    public int deleteLyhCheckinByCheckinId(Long checkinId);

    /**
     * 批量删除每日打卡
     * 
     * @param checkinIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLyhCheckinByCheckinIds(Long[] checkinIds);
    
    /**
     * 根据用户和日期查询打卡记录
     * @param userId 用户ID
     * @param date 日期
     * @return
     */
    public LyhCheckin selectByUserIdAndDate(@Param("userId") Long userId, @Param("date") Date date);

    /**
     * 获取用户最近一次打卡记录
     * @param userId
     * @return
     */
    public LyhCheckin selectLatestByUserId(@Param("userId") Long userId);

    /**
     * 获取用户总打卡天数
     * @param userId
     * @return
     */
    public int selectCountByUserId(Long userId);

    /**
     * 查询今日打卡总数
     * @param date
     * @return
     */
    public int selectCountByDate(String date);
}
