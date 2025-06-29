// src/main/java/com/example/demo/service/ChatService.java
package com.example.demo.service;

import java.util.List;
import java.util.Map;


public interface ChatService {
    String getGeminiResponse(String userMessage, List<Map<String, Object>> history);
}