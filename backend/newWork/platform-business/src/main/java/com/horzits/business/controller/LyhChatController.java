package com.horzits.business.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.StrUtil;
import com.horzits.business.domain.LyhScenario;
import com.horzits.business.service.ILyhScenarioService;
import com.horzits.common.core.domain.AppRestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@RestController
@RequestMapping("/business/lyh/chat")
public class LyhChatController {

    @Autowired
    private ILyhScenarioService scenarioService;

    private static final String ALIYUN_API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String DEFAULT_MODEL = "qwen-turbo";

    private static final String COACH_SYSTEM_PROMPT = String.join("\n",
            "You are an encouraging English speaking coach who actively guides practice.",
            "Rules:",
            "1) Each turn: briefly correct errors (max 2 points), explain key point(s) concisely, then ask ONE follow-up question to keep practice going.",
            "2) Keep difficulty aligned with user's CEFR level; adapt vocabulary and sentence length.",
            "3) Stay on topic; if the user is silent or drifts, re-focus with a simple question.",
            "4) Keep tone supportive and concise.",
            "5) Use available tools to fetch user profile, scenarios and standard exercises; do not invent rules or facts when a tool can provide them.",
            "Output JSON with fields:",
            "{",
            "  \"coach_action\": \"feedback_and_ask\",",
            "  \"message\": \"short feedback with correction and explanation\",",
            "  \"question\": \"one concrete follow-up question\",",
            "  \"topic\": \"topic name\",",
            "  \"focus\": \"target grammar or vocabulary\",",
            "  \"difficulty\": \"A1|A2|B1|B2|C1\"",
            "}");

    @Value("${dashscope.apiKey:}")
    private String dashscopeApiKey;
    @Value("${dashscope.model:qwen-turbo}")
    private String dashscopeModel;
    @Value("${dashscope.temperature:0.7}")
    private double dashscopeTemperature;
    @Value("${dashscope.presencePenalty:0.6}")
    private double dashscopePresencePenalty;
    @Value("${dashscope.maxContextChars:32000}")
    private int dashscopeMaxContextChars;
    @Value("${dashscope.maxConcurrent:8}")
    private int dashscopeMaxConcurrent;

    private volatile Semaphore semaphore;

    private Semaphore getSemaphore() {
        if (semaphore == null) {
            synchronized (this) {
                if (semaphore == null) {
                    semaphore = new Semaphore(dashscopeMaxConcurrent > 0 ? dashscopeMaxConcurrent : 8, true);
                }
            }
        }
        return semaphore;
    }

    private String getApiKey() {
        String key = dashscopeApiKey;
        if (key != null && !key.isEmpty()) {
            return key;
        }
        key = System.getenv("DASHSCOPE_API_KEY");
        if (key == null || key.isEmpty()) {
            key = System.getProperty("DASHSCOPE_API_KEY", "");
        }
        return key;
    }

    private static final Map<String, SessionState> SESSIONS = new ConcurrentHashMap<>();

    private SessionState getSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = "default";
        }
        return SESSIONS.computeIfAbsent(sessionId, k -> {
            SessionState s = new SessionState();
            s.summary = "";
            s.difficulty = "A2";
            s.lastFocus = "";
            s.updatedAt = System.currentTimeMillis();
            return s;
        });
    }

    private String buildStatePrompt(SessionState s) {
        StringBuilder sb = new StringBuilder();
        sb.append("Session state:\n");
        sb.append("Summary: ").append(s.summary == null ? "" : s.summary).append("\n");
        sb.append("Difficulty: ").append(s.difficulty == null ? "" : s.difficulty).append("\n");
        sb.append("Last focus: ").append(s.lastFocus == null ? "" : s.lastFocus).append("\n");
        sb.append("Use this state to keep continuity and ask one follow-up question.");
        return sb.toString();
    }

    private void updateSessionFromModel(SessionState s, String contentJson, String userMessage) {
        try {
            JSONObject o = JSONUtil.parseObj(contentJson);
            String focus = o.getStr("focus", "");
            String difficulty = o.getStr("difficulty", s.difficulty);
            String question = o.getStr("question", "");
            String message = o.getStr("message", "");
            String newSummary = "Focus: " + focus + "; Difficulty: " + difficulty + "; Last Q: " + question + "; User: "
                    + userMessage;
            if (s.summary != null && !s.summary.isEmpty()) {
                if (s.summary.length() > 800) {
                    s.summary = s.summary.substring(s.summary.length() - 800);
                }
                s.summary = s.summary + " | " + newSummary;
            } else {
                s.summary = newSummary;
            }
            s.lastFocus = focus;
            s.difficulty = difficulty != null && !difficulty.isEmpty() ? difficulty : s.difficulty;
            s.updatedAt = System.currentTimeMillis();
        } catch (Exception ignore) {
            String newSummary = "Msg: " + userMessage;
            if (s.summary != null && !s.summary.isEmpty()) {
                if (s.summary.length() > 800) {
                    s.summary = s.summary.substring(s.summary.length() - 800);
                }
                s.summary = s.summary + " | " + newSummary;
            } else {
                s.summary = newSummary;
            }
            s.updatedAt = System.currentTimeMillis();
        }
    }

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
        String personaName = scenario.getPersonaName();
        if (personaName == null || personaName.isEmpty()) {
            personaName = "Assistant";
        }
        String level = scenario.getLevel();
        String levelHint = "A2";
        if ("1".equals(level) || "easy".equalsIgnoreCase(level)) {
            levelHint = "A1";
        } else if ("3".equals(level) || "hard".equalsIgnoreCase(level)) {
            levelHint = "B1";
        }
        String roleplayPolicy = String.join("\n",
                "ROLEPLAY MODE (IMPORTANT):",
                "- Stay in character as: " + personaName + ".",
                "- Context: " + (scenario.getTitle() == null ? "" : scenario.getTitle()) + ". " + (scenario.getDescription() == null ? "" : scenario.getDescription()) + ".",
                "- Your goal is to complete the in-scenario interaction by asking ONE short, practical question each turn.",
                "- Do NOT teach English, do NOT correct grammar, do NOT behave like a language coach.",
                "- Do NOT output JSON; output natural dialogue only.",
                "- Keep replies short and specific. Difficulty: " + levelHint + ".");
        systemPrompt = roleplayPolicy + "\n\n" + systemPrompt;

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

        boolean lastIsSameUser = false;
        if (!messages.isEmpty()) {
            Map<String, String> last = messages.get(messages.size() - 1);
            if (last != null && "user".equalsIgnoreCase(last.get("role"))) {
                String lastContent = last.get("content");
                if (StrUtil.isNotBlank(lastContent) && StrUtil.isNotBlank(userMessage)
                        && lastContent.trim().equals(userMessage.trim())) {
                    lastIsSameUser = true;
                }
            }
        }
        if (!lastIsSameUser) {
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
        }

        messages = applyContextWindow(messages, dashscopeMaxContextChars);

        JSONObject aliyunRequest = new JSONObject();
        aliyunRequest.put("model", dashscopeModel != null ? dashscopeModel : DEFAULT_MODEL);
        aliyunRequest.put("messages", messages);
        aliyunRequest.put("temperature", dashscopeTemperature);
        aliyunRequest.put("presence_penalty", dashscopePresencePenalty);

        return callAliyunApiPlain(aliyunRequest);
    }

    @PostMapping("/coach")
    public AppRestResult coach(@RequestBody Map<String, Object> request) {
        // 支持两种入参方式：
        // 1) { message: "..." , history: [...] } —— 标准教练接口（带工具/会话）
        // 2) { messages: [ {role, content}, ... ] } —— 小程序练习页快速调用（不走工具，仅返回JSON对象）
        if (request.get("messages") != null) {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> provided = (List<Map<String, String>>) request.get("messages");
                List<Map<String, String>> msgs = new ArrayList<>();
                Map<String, String> sys = new HashMap<>();
                sys.put("role", "system");
                // 保证包含“JSON”指令，满足 DashScope 对 json_object 的校验要求
                sys.put("content", COACH_SYSTEM_PROMPT);
                msgs.add(sys);
                if (provided != null) {
                    msgs.addAll(provided);
                }
                JSONObject aliyunRequest = new JSONObject();
                aliyunRequest.put("model", dashscopeModel != null ? dashscopeModel : DEFAULT_MODEL);
                aliyunRequest.put("messages", applyContextWindow(msgs, dashscopeMaxContextChars));
                aliyunRequest.put("temperature", dashscopeTemperature);
                aliyunRequest.put("presence_penalty", dashscopePresencePenalty);
                JSONObject responseFormat = new JSONObject().putOnce("type", "json_object");
                aliyunRequest.put("response_format", responseFormat);
                return callAliyunApi(aliyunRequest);
            } catch (Exception e) {
                return AppRestResult.error("Invalid messages payload");
            }
        }

        String userMessage = (String) request.get("message");
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
        String userId = request.get("userId") != null ? String.valueOf(request.get("userId")) : "";
        String sessionId = request.get("sessionId") != null ? String.valueOf(request.get("sessionId")) : "";
        String scenarioId = request.get("scenarioId") != null ? String.valueOf(request.get("scenarioId")) : "";

        String systemPrompt = COACH_SYSTEM_PROMPT;
        SessionState state = getSession(sessionId);
        String statePrompt = buildStatePrompt(state);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);
        Map<String, String> stateMsg = new HashMap<>();
        stateMsg.put("role", "system");
        stateMsg.put("content", statePrompt);
        messages.add(stateMsg);

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

        boolean enableTools = StrUtil.isNotBlank(userId) || StrUtil.isNotBlank(sessionId) || StrUtil.isNotBlank(scenarioId);
        JSONArray tools = enableTools ? buildToolsSchema() : null;
        JSONObject first = new JSONObject();
        first.put("model", dashscopeModel != null ? dashscopeModel : DEFAULT_MODEL);
        first.put("messages", applyContextWindow(messages, dashscopeMaxContextChars));
        first.put("temperature", dashscopeTemperature);
        first.put("presence_penalty", dashscopePresencePenalty);
        JSONObject responseFormat = new JSONObject().putOnce("type", "json_object");
        first.put("response_format", responseFormat);
        if (tools != null && !tools.isEmpty()) {
            first.put("tools", tools);
            first.put("tool_choice", "auto");
        }

        try {
            JSONObject response = doAliReq(first);
            boolean toolsUsed = false;
            for (int loop = 0; loop < 4; loop++) {
                JSONArray choices = response.getJSONArray("choices");
                if (choices == null || choices.isEmpty()) {
                    return AppRestResult.error("Empty response from AI provider");
                }
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                JSONArray toolCalls = message.getJSONArray("tool_calls");
                if (toolCalls != null && !toolCalls.isEmpty() && tools != null && !tools.isEmpty()) {
                    for (int i = 0; i < toolCalls.size(); i++) {
                        JSONObject tc = toolCalls.getJSONObject(i);
                        String id = tc.getStr("id");
                        JSONObject fn = tc.getJSONObject("function");
                        String name = fn.getStr("name");
                        String argsStr = fn.getStr("arguments");
                        JSONObject args = new JSONObject();
                        if (StrUtil.isNotBlank(argsStr)) {
                            try {
                                args = JSONUtil.parseObj(argsStr);
                            } catch (Exception ignore) {
                            }
                        }
                        String result = executeTool(name, args, userId, sessionId, scenarioId);
                        Map<String, Object> toolMsg = new HashMap<>();
                        toolMsg.put("role", "tool");
                        toolMsg.put("content", result);
                        toolMsg.put("tool_call_id", id);
                        toolMsg.put("name", name);
                        messages.add((Map) toolMsg);
                    }
                    toolsUsed = true;
                    JSONObject follow = new JSONObject();
                    follow.put("model", dashscopeModel != null ? dashscopeModel : DEFAULT_MODEL);
                    follow.put("messages", applyContextWindow(messages, dashscopeMaxContextChars));
                    follow.put("temperature", dashscopeTemperature);
                    follow.put("presence_penalty", dashscopePresencePenalty);
                    follow.put("response_format", responseFormat);
                    if (!toolsUsed) {
                        follow.put("tools", tools);
                        follow.put("tool_choice", "auto");
                    }
                    response = doAliReq(follow);
                    continue;
                }
                String content = message.getStr("content");
                try {
                    JSONObject obj = JSONUtil.parseObj(content);
                    if (obj != null && StrUtil.isBlank(obj.getStr("coach_action")) && StrUtil.isNotBlank(obj.getStr("name"))
                            && obj.get("arguments") != null && tools != null && !tools.isEmpty()) {
                        String name = obj.getStr("name");
                        JSONObject args = new JSONObject();
                        try {
                            Object argObj = obj.get("arguments");
                            if (argObj instanceof JSONObject) {
                                args = (JSONObject) argObj;
                            } else {
                                args = JSONUtil.parseObj(String.valueOf(argObj));
                            }
                        } catch (Exception ignore) {
                        }
                        String result = executeTool(name, args, userId, sessionId, scenarioId);
                        Map<String, String> toolMsg = new HashMap<>();
                        toolMsg.put("role", "assistant");
                        toolMsg.put("content", result);
                        messages.add(toolMsg);

                        toolsUsed = true;
                        JSONObject follow = new JSONObject();
                        follow.put("model", dashscopeModel != null ? dashscopeModel : DEFAULT_MODEL);
                        follow.put("messages", applyContextWindow(messages, dashscopeMaxContextChars));
                        follow.put("temperature", dashscopeTemperature);
                        follow.put("presence_penalty", dashscopePresencePenalty);
                        follow.put("response_format", responseFormat);
                        if (!toolsUsed) {
                            follow.put("tools", tools);
                            follow.put("tool_choice", "auto");
                        }
                        response = doAliReq(follow);
                        continue;
                    }
                    updateSessionFromModel(state, content, userMessage);
                    return AppRestResult.success((Object) content);
                } catch (Exception ignore) {
                    JSONObject wrapper = new JSONObject();
                    wrapper.put("coach_action", "feedback_and_ask");
                    wrapper.put("message", content);
                    wrapper.put("question", "");
                    wrapper.put("topic", "");
                    wrapper.put("focus", "");
                    wrapper.put("difficulty", "");
                    updateSessionFromModel(state, wrapper.toString(), userMessage);
                    return AppRestResult.success((Object) wrapper.toString());
                }
            }
            JSONObject finalReq = new JSONObject();
            finalReq.put("model", dashscopeModel != null ? dashscopeModel : DEFAULT_MODEL);
            finalReq.put("messages", applyContextWindow(messages, dashscopeMaxContextChars));
            finalReq.put("temperature", dashscopeTemperature);
            finalReq.put("presence_penalty", dashscopePresencePenalty);
            finalReq.put("response_format", responseFormat);
            JSONObject finalResp = doAliReq(finalReq);
            JSONArray c2 = finalResp.getJSONArray("choices");
            if (c2 != null && !c2.isEmpty()) {
                JSONObject m2 = c2.getJSONObject(0).getJSONObject("message");
                String content2 = m2 != null ? m2.getStr("content") : "";
                if (StrUtil.isNotBlank(content2)) {
                    try {
                        JSONUtil.parseObj(content2);
                        updateSessionFromModel(state, content2, userMessage);
                        return AppRestResult.success((Object) content2);
                    } catch (Exception ignore) {
                        JSONObject wrapper = new JSONObject();
                        wrapper.put("coach_action", "feedback_and_ask");
                        wrapper.put("message", content2);
                        wrapper.put("question", "");
                        wrapper.put("topic", "");
                        wrapper.put("focus", "");
                        wrapper.put("difficulty", "");
                        updateSessionFromModel(state, wrapper.toString(), userMessage);
                        return AppRestResult.success((Object) wrapper.toString());
                    }
                }
            }
            return AppRestResult.error("Tool loop exceeded limit");
        } catch (Exception e) {
            e.printStackTrace();
            return AppRestResult.error("Internal Server Error: " + e.getMessage());
        }
    }

    private AppRestResult callAliyunApi(JSONObject aliyunRequest) {
        try {
            String apiKey = getApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                return AppRestResult.error("Missing DASHSCOPE_API_KEY");
            }
            HttpResponse response = HttpRequest.post(ALIYUN_API_URL)
                    .header("Authorization", "Bearer " + apiKey)
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
                    // Best-effort JSON validation; fallback to plaintext wrapper
                    try {
                        JSONUtil.parseObj(content);
                        return AppRestResult.success((Object) content);
                    } catch (Exception ignored) {
                        JSONObject wrapper = new JSONObject();
                        wrapper.put("coach_action", "feedback_and_ask");
                        wrapper.put("message", content);
                        wrapper.put("question", "");
                        wrapper.put("topic", "");
                        wrapper.put("focus", "");
                        wrapper.put("difficulty", "");
                        return AppRestResult.success((Object) wrapper.toString());
                    }
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

    private AppRestResult callAliyunApiPlain(JSONObject aliyunRequest) {
        try {
            String apiKey = getApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                return AppRestResult.error("Missing DASHSCOPE_API_KEY");
            }
            HttpResponse response = HttpRequest.post(ALIYUN_API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(aliyunRequest.toString())
                    .execute();

            if (response.isOk()) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    String content = message != null ? message.getStr("content") : "";
                    return AppRestResult.success((Object) (content == null ? "" : content));
                }
                return AppRestResult.error("Empty response from AI provider");
            }
            return AppRestResult.error("AI Provider Error: " + response.getStatus() + " " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
            return AppRestResult.error("Internal Server Error: " + e.getMessage());
        }
    }

    private JSONObject doAliReq(JSONObject body) {
        String apiKey = getApiKey();
        Semaphore sem = getSemaphore();
        try {
            sem.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted");
        }
        HttpResponse response;
        try {
            response = HttpRequest.post(ALIYUN_API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .execute();
        } finally {
            sem.release();
        }
        if (!response.isOk()) {
            throw new RuntimeException("AI Provider Error: " + response.getStatus() + " " + response.body());
        }
        return JSONUtil.parseObj(response.body());
    }

    private JSONArray buildToolsSchema() {
        JSONArray tools = new JSONArray();
        JSONObject t1 = new JSONObject();
        t1.put("type", "function");
        JSONObject f1 = new JSONObject();
        f1.put("name", "get_user_profile");
        f1.put("description", "获取用户画像（水平、兴趣、近期目标）");
        JSONObject p1 = new JSONObject();
        JSONObject p1props = new JSONObject();
        p1props.put("user_id", new JSONObject().putOnce("type", "string"));
        p1.put("type", "object");
        p1.put("properties", p1props);
        f1.put("parameters", p1);
        t1.put("function", f1);
        tools.add(t1);

        JSONObject t2 = new JSONObject();
        t2.put("type", "function");
        JSONObject f2 = new JSONObject();
        f2.put("name", "load_scenario");
        f2.put("description", "加载练习场景配置");
        JSONObject p2 = new JSONObject();
        JSONObject p2props = new JSONObject();
        p2props.put("scenario_id", new JSONObject().putOnce("type", "string"));
        p2.put("type", "object");
        p2.put("properties", p2props);
        f2.put("parameters", p2);
        t2.put("function", f2);
        tools.add(t2);

        JSONObject t4 = new JSONObject();
        t4.put("type", "function");
        JSONObject f4 = new JSONObject();
        f4.put("name", "get_standard_exercise");
        f4.put("description", "获取标准英语练习题（用于约束讲解与示例）");
        JSONObject p4 = new JSONObject();
        JSONObject p4props = new JSONObject();
        p4props.put("topic", new JSONObject().putOnce("type", "string"));
        p4props.put("focus", new JSONObject().putOnce("type", "string"));
        p4.put("type", "object");
        p4.put("properties", p4props);
        f4.put("parameters", p4);
        t4.put("function", f4);
        tools.add(t4);
        return tools;
    }

    private String executeTool(String name, JSONObject args, String userId, String sessionId, String scenarioId) {
        if ("get_user_profile".equals(name)) {
            JSONObject out = new JSONObject();
            out.put("user_id", args.getStr("user_id", userId));
            out.put("cefr", "A2");
            JSONArray interests = new JSONArray();
            interests.add("Travel");
            interests.add("Work");
            out.put("interests", interests);
            out.put("recent_goal", "Practice past simple in daily topics");
            return out.toString();
        }
        if ("load_scenario".equals(name)) {
            String sid = args.getStr("scenario_id", scenarioId);
            JSONObject out = new JSONObject();
            out.put("scenario_id", sid);
            if (StrUtil.isNotBlank(sid)) {
                try {
                    LyhScenario sc = scenarioService.selectLyhScenarioByScenarioId(Long.valueOf(sid));
                    if (sc != null) {
                        out.put("title", sc.getTitle());
                        out.put("persona_prompt", sc.getPersonaPrompt());
                    }
                } catch (Exception ignore) {
                }
            }
            return out.toString();
        }
        if ("log_dialogue".equals(name)) {
            JSONObject out = new JSONObject();
            out.put("status", "ok");
            out.put("session_id", args.getStr("session_id", sessionId));
            out.put("role", args.getStr("role"));
            out.put("length", args.getStr("content", "").length());
            return out.toString();
        }
        if ("get_standard_exercise".equals(name)) {
            String topic = args.getStr("topic", "Daily");
            String focus = args.getStr("focus", "Past simple");
            JSONObject out = new JSONObject();
            out.put("topic", topic);
            out.put("focus", focus);
            out.put("type", "fill_in_blank");
            JSONArray items = new JSONArray();
            JSONObject it1 = new JSONObject();
            it1.put("q", "Yesterday I ____ (go) to the gym.");
            it1.put("a", "went");
            items.add(it1);
            JSONObject it2 = new JSONObject();
            it2.put("q", "She ____ (not watch) TV last night.");
            it2.put("a", "did not watch");
            items.add(it2);
            out.put("items", items);
            return out.toString();
        }
        return new JSONObject().putOnce("status", "unknown_tool").toString();
    }

    private static class SessionState {
        String summary;
        String difficulty;
        String lastFocus;
        long updatedAt;
    }

    private List<Map<String, String>> applyContextWindow(List<Map<String, String>> messages, int maxChars) {
        if (messages == null || messages.isEmpty() || maxChars <= 0)
            return messages;
        int total = 0;
        for (Map<String, String> m : messages) {
            String c = m.get("content");
            total += c == null ? 0 : c.length();
        }
        if (total <= maxChars)
            return messages;
        List<Map<String, String>> out = new ArrayList<>(messages);
        int idx = 0;
        while (idx < out.size() && total > maxChars) {
            Map<String, String> m = out.get(idx);
            String role = m.get("role");
            if (!"system".equals(role)) {
                String c = m.get("content");
                total -= c == null ? 0 : c.length();
                out.remove(idx);
                continue;
            }
            idx++;
            if (idx >= out.size())
                break;
        }
        return out;
    }
}
