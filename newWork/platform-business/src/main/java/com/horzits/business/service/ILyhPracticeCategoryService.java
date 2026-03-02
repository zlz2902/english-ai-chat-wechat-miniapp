package com.horzits.business.service;

import java.util.List;
import com.horzits.business.domain.LyhPracticeCategory;

/**
 * 练习分类Service接口
 */
public interface ILyhPracticeCategoryService {
    public LyhPracticeCategory selectLyhPracticeCategoryByCategoryId(Long categoryId);
    public List<LyhPracticeCategory> selectLyhPracticeCategoryList(LyhPracticeCategory entity);
    public int insertLyhPracticeCategory(LyhPracticeCategory entity);
    public int updateLyhPracticeCategory(LyhPracticeCategory entity);
    public int deleteLyhPracticeCategoryByCategoryIds(Long[] categoryIds);
    public int deleteLyhPracticeCategoryByCategoryId(Long categoryId);
}

