package com.autotest.jmeter.jmeteragent.service.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.autotest.data.mode.ScenarioReport;


@Service
@Transactional(rollbackFor = Exception.class)
public class APIReportService {
	private static Map<String, ScenarioReport> cache = new LinkedHashMap<String, ScenarioReport>();
	public void addResult(String historyId,ScenarioReport res) {
        if (res!=null) {
            cache.put(historyId, res);
        } else {
            new Exception("test_not_found");
        }
    }
    public ScenarioReport getResult(String testId, String test) {
        Object res = cache.get(testId);
        if (res != null) {
            cache.remove(testId);
            ScenarioReport reportResult = new ScenarioReport();
            reportResult=(ScenarioReport)res;//.setContent(JSON.toJSONString(res));
            return reportResult;
        }
        return null;
    }
}
