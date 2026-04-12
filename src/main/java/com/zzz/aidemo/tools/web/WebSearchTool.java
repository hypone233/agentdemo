package com.zzz.aidemo.tools.web;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WebSearchTool {

    // 搜索 API 地址（固定不变）
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    // API 密钥（通过构造函数注入）
    private final String apiKey;

    /**
     * 构造函数：传入 API 密钥初始化工具
     * @param apiKey searchapi.io 平台的 API Key
     */
    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 执行百度搜索
     * @param query 搜索关键词
     * @return 前 5 条搜索结果拼接成的字符串
     */
    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(
            @ToolParam(description = "Search query keyword") String query
    ) {
        // 构建请求参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");

        try {
            // 发起 HTTP GET 请求
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);

            // 解析 JSON 响应
            JSONObject jsonObject = JSONUtil.parseObj(response);

            // 提取 organic_results 字段（搜索结果列表）
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");

            // 截取前 5 条结果
            List<Object> objects = organicResults.subList(0, 5);

            // 将结果对象拼接为字符串（逗号分隔）
            String result = objects.stream()
                    .map(obj -> {
                        JSONObject tmpJSONObject = (JSONObject) obj;
                        return tmpJSONObject.toString();
                    })
                    .collect(Collectors.joining(","));

            return result;
        } catch (Exception e) {
            // 异常捕获，返回错误信息
            return "Error searching Baidu: " + e.getMessage();
        }
    }
}