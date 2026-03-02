package com.horzits.business.service;

import java.util.List;
import com.horzits.business.domain.LyhScenario;

public interface ILyhScenarioService {
    public LyhScenario selectLyhScenarioByScenarioId(Long scenarioId);
    public List<LyhScenario> selectLyhScenarioList(LyhScenario entity);
    public int insertLyhScenario(LyhScenario entity);
    public int updateLyhScenario(LyhScenario entity);
    public int deleteLyhScenarioByScenarioIds(Long[] scenarioIds);
    public int deleteLyhScenarioByScenarioId(Long scenarioId);
}

