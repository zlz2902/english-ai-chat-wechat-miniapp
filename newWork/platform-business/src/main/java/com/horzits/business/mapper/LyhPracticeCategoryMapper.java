package com.horzits.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.horzits.business.domain.LyhPracticeCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 练习分类Mapper接口
 */
@Mapper
public interface LyhPracticeCategoryMapper extends BaseMapper<LyhPracticeCategory> {
    public LyhPracticeCategory selectLyhPracticeCategoryByCategoryId(Long categoryId);
    public List<LyhPracticeCategory> selectLyhPracticeCategoryList(LyhPracticeCategory entity);
    public int insertLyhPracticeCategory(LyhPracticeCategory entity);
    public int updateLyhPracticeCategory(LyhPracticeCategory entity);
    public int deleteLyhPracticeCategoryByCategoryId(Long categoryId);
    public int deleteLyhPracticeCategoryByCategoryIds(Long[] categoryIds);
}

