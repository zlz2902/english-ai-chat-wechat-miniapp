package com.horzits.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.horzits.business.domain.LyhScenario;
import com.horzits.business.mapper.LyhScenarioMapper;
import com.horzits.business.service.ILyhScenarioService;

@Service
public class LyhScenarioServiceImpl implements ILyhScenarioService {

    @Autowired
    private LyhScenarioMapper mapper;

    @Override
    public LyhScenario selectLyhScenarioByScenarioId(Long scenarioId) {
        return mapper.selectLyhScenarioByScenarioId(scenarioId);
    }

    @Override
    public List<LyhScenario> selectLyhScenarioList(LyhScenario entity) {
        return mapper.selectLyhScenarioList(entity);
    }

    @Override
    public int insertLyhScenario(LyhScenario entity) {
        return mapper.insertLyhScenario(entity);
    }

    @Override
    public int updateLyhScenario(LyhScenario entity) {
        return mapper.updateLyhScenario(entity);
    }

    @Override
    public int deleteLyhScenarioByScenarioIds(Long[] scenarioIds) {
        return mapper.deleteLyhScenarioByScenarioIds(scenarioIds);
    }

    @Override
    public int deleteLyhScenarioByScenarioId(Long scenarioId) {
        return mapper.deleteLyhScenarioByScenarioId(scenarioId);
    }
}

