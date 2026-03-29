package com.horzits.business.controller;

import com.horzits.common.core.domain.AppRestResult;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import com.horzits.common.config.RuoYiConfig;
import com.aliyun.oss.model.CannedAccessControlList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.hutool.json.JSONArray;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/business/lyh/coach")
public class LyhCoachController {

    private static final Logger log = LoggerFactory.getLogger(LyhCoachController.class);
    @Value("${speech.asr.provider:}")
    private String asrProvider;
    @Value("${speech.asr.appKey:}")
    private String asrAppKey;
    @Value("${speech.asr.accessKeyId:}")
    private String asrAccessKeyId;
    @Value("${speech.asr.accessKeySecret:}")
    private String asrAccessKeySecret;
    @Value("${speech.asr.regionId:cn-shanghai}")
    private String asrRegionId;
    @Value("${oss.endpoint:}")
    private String ossEndpoint;
    @Value("${oss.bucket:}")
    private String ossBucket;
    @Value("${oss.publicDomain:}")
    private String ossPublicDomain;
    @Value("${oss.dirPrefix:audio/}")
    private String ossDirPrefix;
    @Value("${ruoyi.profile:D:/ruoyi/uploadPath}")
    private String uploadRoot;
    @Value("${dashscope.apiKey:}")
    private String dashscopeApiKey;
    @Value("${dashscope.asrModel:qwen3-asr-flash-filetrans}")
    private String dashscopeAsrModel;

    @PostMapping("/voice")
    public AppRestResult voice(@RequestBody Map<String, Object> body, HttpServletRequest httpReq) {
        String audioUrl = body.get("audioUrl") == null ? "" : String.valueOf(body.get("audioUrl"));
        String questionId = body.get("questionId") == null ? "" : String.valueOf(body.get("questionId"));
        String difficulty = body.get("difficulty") == null ? "" : String.valueOf(body.get("difficulty"));

        String ensured = ensurePublicAudio(audioUrl);
        log.info("voice input audioUrl={}, ensured={}", audioUrl, ensured);
        String userText = asr(ensured);
        log.info("voice asr userText={}", userText);
        String fallbackReply = userText != null && !userText.equals("(voice)") && !userText.isEmpty()
                ? ("Good job. I heard: " + userText)
                : "Good job. Try to speak more clearly and add one more detail.";
        String reply = fallbackReply;
        boolean hasText = userText != null && userText.trim().length() > 0 && !"(voice)".equals(userText.trim());
        if (!hasText) {
            List<String> tips = new ArrayList<>();
            tips.add("靠近麦克风，避免环境噪音");
            tips.add("放慢语速，清楚说出关键单词");
            tips.add("尝试用一个简短句子开始");
            List<String> drills = new ArrayList<>();
            drills.add("请用一句话回答：Where are you?");
            drills.add("再换一句：What are you doing now?");
            Map<String, Object> out = new HashMap<>();
            out.put("userText", userText);
            out.put("textReply", fallbackReply);
            out.put("tips", tips);
            out.put("drills", drills);
            out.put("replyAudioUrl", "");
            out.put("questionId", questionId);
            out.put("audioUrl", ensured);
            out.put("difficulty", difficulty);
            out.put("userText", userText);
            return AppRestResult.success(out);
        }
        try {
            JSONObject coachReq = new JSONObject();
            coachReq.put("message", userText);
            String auth = httpReq != null ? httpReq.getHeader("Authorization") : null;
            HttpRequest coachPost = HttpRequest.post("http://127.0.0.1:8089/business/lyh/chat/coach")
                    .header("Content-Type", "application/json")
                    .body(coachReq.toString());
            if (auth != null && !auth.isEmpty()) {
                coachPost.header("Authorization", auth);
            }
            HttpResponse coachResp = coachPost.execute();
            if (coachResp.isOk()) {
                log.info("coach resp ok: {}", coachResp.body());
                JSONObject coachJson = JSONUtil.parseObj(coachResp.body());
                Integer code = coachJson.getInt("code");
                if (code != null && code != 200) {
                    Map<String, Object> out = new HashMap<>();
                    List<String> tips = new ArrayList<>();
                    tips.add("放慢语速，句尾更清晰");
                    tips.add("使用更具体的词汇与短语");
                    tips.add("加入连接词提升连贯性");
                    List<String> drills = new ArrayList<>();
                    drills.add("请用两句话回答该题，并加入一个具体细节");
                    drills.add("换一种表达方式再说一遍，尝试使用连接词 therefore 或 moreover");
                    Map<String, Object> result = new HashMap<>();
                    result.put("userText", userText);
                    result.put("textReply", reply);
                    result.put("tips", tips);
                    result.put("drills", drills);
                    result.put("replyAudioUrl", "");
                    result.put("questionId", questionId);
                    result.put("audioUrl", ensured);
                    result.put("difficulty", difficulty);
                    result.put("userText", userText);
                    return AppRestResult.success(result);
                }
                Object data = coachJson.get("data");
                if (data != null) {
                    String content = String.valueOf(data);
                    JSONObject obj = parseCoachContent(content);
                    if (obj != null) {
                        String msg = obj.getStr("message", "");
                        String q = obj.getStr("question", "");
                        String combined = msg;
                        if (q != null && !q.isEmpty()) {
                            combined = (combined == null || combined.isEmpty()) ? q : (combined + " " + q);
                        }
                        if (combined != null && !combined.isEmpty()) {
                            reply = combined;
                        } else if (content != null && !content.isEmpty()) {
                            reply = content;
                        }
                    } else if (content != null && !content.isEmpty()) {
                        reply = content;
                    }
                }
            }
        } catch (Exception ignore) {
            log.warn("coach call failed", ignore);
        }

        List<String> tips = new ArrayList<>();
        tips.add("放慢语速，句尾更清晰");
        tips.add("使用更具体的词汇与短语");
        tips.add("加入连接词提升连贯性");

        List<String> drills = new ArrayList<>();
        drills.add("请用两句话回答该题，并加入一个具体细节");
        drills.add("换一种表达方式再说一遍，尝试使用连接词 therefore 或 moreover");

        Map<String, Object> out = new HashMap<>();
        out.put("userText", userText);
        out.put("textReply", reply);
        out.put("tips", tips);
        out.put("drills", drills);
        out.put("replyAudioUrl", "");
        out.put("questionId", questionId);
        out.put("audioUrl", ensured);
        out.put("difficulty", difficulty);
        out.put("userText", userText);
        return AppRestResult.success(out);
    }

    private JSONObject parseCoachContent(String content) {
        if (content == null)
            return null;
        String s = content.trim();
        if (s.isEmpty())
            return null;
        try {
            if (s.startsWith("{") && s.endsWith("}")) {
                return JSONUtil.parseObj(s);
            }
        } catch (Exception ignore) {
        }
        try {
            if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
                String unquoted = s.substring(1, s.length() - 1);
                unquoted = unquoted.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\"",
                        "\"");
                if (unquoted.trim().startsWith("{")) {
                    return JSONUtil.parseObj(unquoted);
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    /**
     * 将本地/内网音频转存到 OSS，生成公网 URL；若已是公网 URL 则直接返回
     */
    private String ensurePublicAudio(String url) {
        if (url == null || url.isEmpty())
            return url;
        try {
            String lower = url.toLowerCase();
            boolean local = lower.contains("localhost") || lower.contains("127.0.0.1");
            if (!local)
                return url;
            if (ossEndpoint == null || ossEndpoint.isEmpty() || ossBucket == null || ossBucket.isEmpty()) {
                return url;
            }
            // 解析 /profile/ 之后的相对路径
            int idx = url.indexOf("/profile/");
            if (idx < 0)
                return url;
            String suffix = url.substring(idx + "/profile/".length());
            String localPath = uploadRoot + File.separator + suffix.replace("/", File.separator);
            File f = new File(localPath);
            if (!f.exists())
                return url;
            String objectKeyBase = (ossDirPrefix == null ? "" : ossDirPrefix);
            String objectKey = objectKeyBase + suffix.replace("\\", "/");
            OSS client = new OSSClientBuilder().build("https://" + ossEndpoint, asrAccessKeyId, asrAccessKeySecret);
            try {
                client.putObject(ossBucket, objectKey, f);
                // 设置对象为公共读，便于阿里云 ASR 通过 file_link 拉取
                client.setObjectAcl(ossBucket, objectKey, CannedAccessControlList.PublicRead);
            } finally {
                client.shutdown();
            }
            if (ossPublicDomain != null && !ossPublicDomain.isEmpty()) {
                String domain = ossPublicDomain.endsWith("/")
                        ? ossPublicDomain.substring(0, ossPublicDomain.length() - 1)
                        : ossPublicDomain;
                return domain + "/" + objectKey;
            }
            return "https://" + ossBucket + "." + ossEndpoint + "/" + objectKey;
        } catch (Exception e) {
            return url;
        }
    }

    /**
     * 接入阿里云智能语音 FileTrans（短语音识别）- 简化实现
     * 注意：生产中请按官方文档完善鉴权与异常处理
     */
    private String asr(String audioUrl) {
        if (audioUrl == null || audioUrl.isEmpty())
            return "(voice)";
        if (!"aliyun".equalsIgnoreCase(asrProvider))
            return "(voice)";
        try {
            String popText = asrViaPop(audioUrl);
            if (popText != null && !popText.isEmpty())
                return popText;
            String dsText = asrViaDashscope(audioUrl);
            if (dsText != null && !dsText.isEmpty())
                return dsText;
        } catch (Exception ignore) {
            log.warn("asr exception", ignore);
        }
        return "(voice)";
    }

    private String asrViaDashscope(String audioUrl) {
        if (dashscopeApiKey == null || dashscopeApiKey.isEmpty())
            return null;
        try {
            String submitUrl = "https://dashscope.aliyuncs.com/api/v1/services/audio/asr/transcription";
            JSONObject payload = new JSONObject();
            payload.put("model", dashscopeAsrModel == null || dashscopeAsrModel.isEmpty() ? "qwen3-asr-flash-filetrans"
                    : dashscopeAsrModel);
            JSONObject input = new JSONObject();
            input.put("file_url", audioUrl);
            payload.put("input", input);
            JSONObject params = new JSONObject();
            params.put("channel_id", new JSONArray().put(0));
            params.put("enable_itn", false);
            params.put("enable_words", false);
            payload.put("parameters", params);

            HttpResponse submitResp = HttpRequest.post(submitUrl)
                    .header("Authorization", "Bearer " + dashscopeApiKey)
                    .header("Content-Type", "application/json")
                    .header("X-DashScope-Async", "enable")
                    .body(payload.toString())
                    .execute();
            if (!submitResp.isOk()) {
                log.warn("dashscope submit not ok: code={}, body={}", submitResp.getStatus(), submitResp.body());
                return null;
            }
            JSONObject submitObj = JSONUtil.parseObj(submitResp.body());
            JSONObject out = submitObj.getJSONObject("output");
            String taskId = out != null ? out.getStr("task_id") : null;
            if (taskId == null || taskId.isEmpty()) {
                log.warn("dashscope submit no task_id: {}", submitResp.body());
                return null;
            }

            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 60000) {
                HttpResponse q = HttpRequest.get("https://dashscope.aliyuncs.com/api/v1/tasks/" + taskId)
                        .header("Authorization", "Bearer " + dashscopeApiKey)
                        .header("X-DashScope-Async", "enable")
                        .header("Content-Type", "application/json")
                        .execute();
                if (!q.isOk()) {
                    Thread.sleep(1200);
                    continue;
                }
                JSONObject qObj = JSONUtil.parseObj(q.body());
                JSONObject qOut = qObj.getJSONObject("output");
                String status = qOut != null ? qOut.getStr("task_status") : null;
                if ("SUCCEEDED".equalsIgnoreCase(status)) {
                    String transcriptionUrl = null;
                    JSONObject result = qObj.getJSONObject("result");
                    if (result == null && qOut != null) {
                        result = qOut.getJSONObject("result");
                    }
                    if (result != null) {
                        transcriptionUrl = result.getStr("transcription_url", result.getStr("transcriptionUrl"));
                    }
                    if ((transcriptionUrl == null || transcriptionUrl.isEmpty()) && qOut != null) {
                        JSONArray results = qOut.getJSONArray("results");
                        if (results != null && results.size() > 0) {
                            JSONObject r0 = results.getJSONObject(0);
                            transcriptionUrl = r0.getStr("transcription_url", r0.getStr("transcriptionUrl"));
                        }
                    }
                    if (transcriptionUrl != null && !transcriptionUrl.isEmpty()) {
                        HttpResponse tr = HttpRequest.get(transcriptionUrl).execute();
                        if (tr.isOk()) {
                            String text = extractDashscopeText(tr.body());
                            if (text != null && !text.isEmpty()) {
                                return text;
                            }
                        }
                    }
                    return null;
                }
                if ("FAILED".equalsIgnoreCase(status) || "UNKNOWN".equalsIgnoreCase(status)) {
                    log.warn("dashscope task status={}, body={}", status, q.body());
                    return null;
                }
                Thread.sleep(1200);
            }
        } catch (Exception e) {
            log.warn("dashscope asr exception", e);
        }
        return null;
    }

    private String extractDashscopeText(String body) {
        try {
            JSONObject obj = JSONUtil.parseObj(body);
            JSONArray transcripts = obj.getJSONArray("transcripts");
            if (transcripts != null && transcripts.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < transcripts.size(); i++) {
                    JSONObject one = transcripts.getJSONObject(i);
                    String t = one.getStr("text", one.getStr("Text"));
                    if (t != null && !t.isEmpty()) {
                        if (sb.length() > 0)
                            sb.append(" ");
                        sb.append(t);
                    }
                }
                if (sb.length() > 0)
                    return sb.toString();
            }
            String text = obj.getStr("text");
            if (text != null && !text.isEmpty())
                return text;
        } catch (Exception ignore) {
        }
        return null;
    }

    private String getAliyunToken() {
        try {
            String endpoint = "https://nls-meta." + asrRegionId + ".aliyuncs.com";
            TreeMap<String, String> params = new TreeMap<>();
            params.put("AccessKeyId", asrAccessKeyId);
            params.put("Action", "CreateToken");
            params.put("Format", "JSON");
            params.put("RegionId", asrRegionId);
            params.put("SignatureMethod", "HMAC-SHA1");
            params.put("SignatureNonce", UUID.randomUUID().toString());
            params.put("SignatureVersion", "1.0");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            params.put("Timestamp", sdf.format(new Date()));
            params.put("Version", "2019-02-28");
            String canonicalized = buildCanonicalizedQuery(params);
            String stringToSign = "GET&" + percentEncode("/") + "&" + percentEncode(canonicalized);
            String secret = asrAccessKeySecret + "&";
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            String signature = Base64.getEncoder()
                    .encodeToString(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
            String url = endpoint + "/?Signature=" + percentEncode(signature) + "&" + canonicalized;
            HttpResponse resp = HttpRequest.get(url).execute();
            if (resp.isOk()) {
                JSONObject obj = JSONUtil.parseObj(resp.body());
                JSONObject tokenObj = obj.getJSONObject("Token");
                if (tokenObj != null) {
                    String id = tokenObj.getStr("Id");
                    if (id != null && !id.isEmpty()) {
                        return id;
                    }
                }
            } else {
                log.warn("CreateToken httpStatus={}, body={}", resp.getStatus(), resp.body());
            }
        } catch (Exception e) {
            log.warn("CreateToken exception", e);
        }
        return null;
    }

    // POP RPC 兜底：SubmitTask/GetTaskResult
    private String asrViaPop(String audioUrl) {
        try {
            String endpoint = "https://filetrans." + asrRegionId + ".aliyuncs.com";
            // SubmitTask
            TreeMap<String, String> submitParams = new TreeMap<>();
            submitParams.put("AccessKeyId", asrAccessKeyId);
            submitParams.put("Action", "SubmitTask");
            submitParams.put("Format", "JSON");
            submitParams.put("RegionId", asrRegionId);
            submitParams.put("SignatureMethod", "HMAC-SHA1");
            submitParams.put("SignatureNonce", UUID.randomUUID().toString());
            submitParams.put("SignatureVersion", "1.0");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            submitParams.put("Timestamp", sdf.format(new Date()));
            submitParams.put("Version", "2018-08-17");
            JSONObject task = new JSONObject();
            task.put("appkey", asrAppKey);
            task.put("file_link", audioUrl);
            task.put("version", "2.0");
            submitParams.put("Task", task.toString());
            String canonicalized = buildCanonicalizedQuery(submitParams);
            String stringToSign = "POST&" + percentEncode("/") + "&" + percentEncode(canonicalized);
            String secret = asrAccessKeySecret + "&";
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            String signature = Base64.getEncoder()
                    .encodeToString(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
            String bodyForm = "Signature=" + percentEncode(signature) + "&" + canonicalized;
            HttpResponse submitResp = HttpRequest.post(endpoint + "/")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(bodyForm)
                    .execute();
            if (!submitResp.isOk()) {
                log.warn("POP SubmitTask httpStatus={}, body={}", submitResp.getStatus(), submitResp.body());
                return null;
            }
            JSONObject submitObj = JSONUtil.parseObj(submitResp.body());
            String taskId = submitObj.getStr("TaskId", submitObj.getStr("task_id"));
            if (taskId == null || taskId.isEmpty()) {
                log.warn("POP SubmitTask no TaskId: {}", submitResp.body());
                return null;
            }
            // GetTaskResult
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 60000) {
                TreeMap<String, String> getParams = new TreeMap<>();
                getParams.put("AccessKeyId", asrAccessKeyId);
                getParams.put("Action", "GetTaskResult");
                getParams.put("Format", "JSON");
                getParams.put("RegionId", asrRegionId);
                getParams.put("SignatureMethod", "HMAC-SHA1");
                getParams.put("SignatureNonce", UUID.randomUUID().toString());
                getParams.put("SignatureVersion", "1.0");
                getParams.put("Timestamp", sdf.format(new Date()));
                getParams.put("Version", "2018-08-17");
                getParams.put("TaskId", taskId);
                String canGet = buildCanonicalizedQuery(getParams);
                String sts = "GET&" + percentEncode("/") + "&" + percentEncode(canGet);
                Mac macGet = Mac.getInstance("HmacSHA1");
                macGet.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
                String sig = Base64.getEncoder().encodeToString(macGet.doFinal(sts.getBytes(StandardCharsets.UTF_8)));
                String getUrl = endpoint + "/?Signature=" + percentEncode(sig) + "&" + canGet;
                HttpResponse getResp = HttpRequest.get(getUrl).execute();
                if (!getResp.isOk()) {
                    Thread.sleep(1200);
                    continue;
                }
                JSONObject rj = JSONUtil.parseObj(getResp.body());
                String status = rj.getStr("StatusText", rj.getStr("status"));
                if ("SUCCEEDED".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
                    JSONObject result = rj.getJSONObject("Result");
                    if (result != null) {
                        String text = result.getStr("Transcription", result.getStr("Text"));
                        if ((text == null || text.isEmpty())) {
                            JSONArray sents = result.getJSONArray("Sentences");
                            if (sents != null && sents.size() > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < sents.size(); i++) {
                                    JSONObject one = sents.getJSONObject(i);
                                    String t = one.getStr("Text", one.getStr("text"));
                                    if (t != null && !t.isEmpty()) {
                                        if (sb.length() > 0)
                                            sb.append(" ");
                                        sb.append(t);
                                    }
                                }
                                if (sb.length() > 0)
                                    text = sb.toString();
                            }
                        }
                        return text;
                    }
                    return null;
                } else if ("RUNNING".equalsIgnoreCase(status) || "QUEUEING".equalsIgnoreCase(status)
                        || "PROCESSING".equalsIgnoreCase(status)) {
                    Thread.sleep(1200);
                } else {
                    log.warn("POP GetTaskResult status={}, body={}", status, getResp.body());
                    return null;
                }
            }
        } catch (Exception e) {
            log.warn("asrViaPop exception", e);
        }
        return null;
    }

    private static String buildCanonicalizedQuery(TreeMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first)
                sb.append("&");
            first = false;
            sb.append(percentEncode(e.getKey())).append("=").append(percentEncode(e.getValue()));
        }
        return sb.toString();
    }

    private static String percentEncode(String s) {
        try {
            String enc = URLEncoder.encode(s, "UTF-8");
            enc = enc.replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
            return enc;
        } catch (Exception e) {
            return s;
        }
    }
}
