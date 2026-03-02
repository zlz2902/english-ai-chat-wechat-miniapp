package com.horzits.business.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.horzits.business.domain.LyhScenario;
import com.horzits.business.service.ILyhScenarioService;
import com.horzits.common.core.domain.AppRestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/business/lyh/chat")
public class LyhChatController {

    @Autowired
    private ILyhScenarioService scenarioService;

    // TODO: Move to configuration
    private static final String ALIYUN_API_KEY = "sk-3976b23400644cb688d5c793e36f9e0e";
    private static final String ALIYUN_API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    @PostMapping("/completions")
    public AppRestResult chat(@RequestBody Map<String, Object> request) {
        Long scenarioId = Long.valueOf(request.get("scenarioId").toString());
        String userMessage = (String) request.get("message");
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");

        LyhScenario scenario = scenarioService.selectLyhScenarioByScenarioId(scenarioId);
        if (scenario == null) {
            return AppRestResult.error("Scenario not found");
        }

        String systemPrompt = scenario.getPersonaPrompt();
        if (systemPrompt == null || systemPrompt.isEmpty()) {
            systemPrompt = "You are a helpful assistant.";
        }

        // Construct messages
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        if (history != null) {
            for (Map<String, String> msg : history) {
                // Ensure only valid roles are passed (system, user, assistant)
                // Filter out any other metadata if present
                Map<String, String> cleanMsg = new HashMap<>();
                cleanMsg.put("role", msg.get("role"));
                cleanMsg.put("content", msg.get("content"));
                messages.add(cleanMsg);
            }
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        // Prepare request body for Aliyun
        JSONObject aliyunRequest = new JSONObject();
        aliyunRequest.put("model", "qwen-turbo");
        aliyunRequest.put("messages", messages);

        return callAliyunApi(aliyunRequest);
    }

    @PostMapping("/coach")
    public AppRestResult coach(@RequestBody Map<String, Object> request) {
        String userMessage = (String) request.get("message");
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");

        // System prompt for English Coach
        String systemPrompt = "You are an expert English language coach. Your task is to help the user improve their English.\n"
                +
                "When the user sends a sentence or question:\n" +
                "1. If it's a question about English, answer it clearly.\n" +
                "2. If it's a practice sentence, identify any grammatical errors, suggest more natural vocabulary, and provide a corrected version.\n"
                +
                "3. Keep your response encouraging and concise.\n" +
                "Format your response with clear sections like 'Correction', 'Explanation', and 'Suggestion' if applicable.";

        // Construct messages
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        if (history != null) {
            for (Map<String, String> msg : history) {
                Map<String, String> cleanMsg = new HashMap<>();
                cleanMsg.put("role", msg.get("role"));
                cleanMsg.put("content", msg.get("content"));
                messages.add(cleanMsg);
            }
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        // Prepare request body for Aliyun
        JSONObject aliyunRequest = new JSONObject();
        aliyunRequest.put("model", "qwen-turbo");
        aliyunRequest.put("messages", messages);

        return callAliyunApi(aliyunRequest);
    }

    private AppRestResult callAliyunApi(JSONObject aliyunRequest) {
        try {
            HttpResponse response = HttpRequest.post(ALIYUN_API_URL)
                    .header("Authorization", "Bearer " + ALIYUN_API_KEY)
                    .header("Content-Type", "application/json")
                    .body(aliyunRequest.toString())
                    .execute();

            if (response.isOk()) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    String content = message.getStr("content");
                    return AppRestResult.success((Object) content);
                } else {
                    return AppRestResult.error("Empty response from AI provider");
                }
            } else {
                return AppRestResult.error("AI Provider Error: " + response.getStatus() + " " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AppRestResult.error("Internal Server Error: " + e.getMessage());
        }
    }
}
