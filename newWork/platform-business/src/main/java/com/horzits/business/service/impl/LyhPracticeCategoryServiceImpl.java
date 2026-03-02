package com.horzits.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.horzits.business.domain.LyhPracticeCategory;
import com.horzits.business.mapper.LyhPracticeCategoryMapper;
import com.horzits.business.service.ILyhPracticeCategoryService;

@Service
public class LyhPracticeCategoryServiceImpl implements ILyhPracticeCategoryService {

    @Autowired
    private LyhPracticeCategoryMapper mapper;

    @Override
    public LyhPracticeCategory selectLyhPracticeCategoryByCategoryId(Long categoryId) {
        return mapper.selectLyhPracticeCategoryByCategoryId(categoryId);
    }

    @Override
    public List<LyhPracticeCategory> selectLyhPracticeCategoryList(LyhPracticeCategory entity) {
        return mapper.selectLyhPracticeCategoryList(entity);
    }

    @Override
    public int insertLyhPracticeCategory(LyhPracticeCategory entity) {
        return mapper.insertLyhPracticeCategory(entity);
    }

    @Override
    public int updateLyhPracticeCategory(LyhPracticeCategory entity) {
        return mapper.updateLyhPracticeCategory(entity);
    }

    @Override
    public int deleteLyhPracticeCategoryByCategoryIds(Long[] categoryIds) {
        return mapper.deleteLyhPracticeCategoryByCategoryIds(categoryIds);
    }

    @Override
    public int deleteLyhPracticeCategoryByCategoryId(Long categoryId) {
        return mapper.deleteLyhPracticeCategoryByCategoryId(categoryId);
    }
}

