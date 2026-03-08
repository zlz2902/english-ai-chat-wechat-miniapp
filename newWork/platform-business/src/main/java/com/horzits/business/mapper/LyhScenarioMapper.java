package com.horzits.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.horzits.business.domain.LyhScenario;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LyhScenarioMapper extends BaseMapper<LyhScenario> {
    public LyhScenario selectLyhScenarioByScenarioId(Long scenarioId);
    public List<LyhScenario> selectLyhScenarioList(LyhScenario entity);
    public int insertLyhScenario(LyhScenario entity);
    public int updateLyhScenario(LyhScenario entity);
    public int deleteLyhScenarioByScenarioId(Long scenarioId);
    public int deleteLyhScenarioByScenarioIds(Long[] scenarioIds);
}

