
package com.example.demo.controller.admin;

import com.example.demo.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;

    // Constructor Injection
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Endpoint nhận tin nhắn từ frontend và trả về phản hồi từ Gemini.
     * @param requestPayload Payload chứa tin nhắn và lịch sử chat.
     *                       Ví dụ: { "message": "...", "history": [ {"role": "user", "parts": [{"text":"..."}]}, {"role": "model", "parts": [{"text":"..."}]} ] }
     * @return ResponseEntity chứa phản hồi dạng text.
     */
    @PostMapping("/gemini")
    public ResponseEntity<Map<String, String>> handleChatMessage(@RequestBody Map<String, Object> requestPayload) {
        String userMessage = (String) requestPayload.get("message");
        // Lấy history, đảm bảo đúng kiểu List<Map<String, Object>>
        List<Map<String, Object>> history = null;
        if (requestPayload.get("history") instanceof List) {
            try {
                // Cẩn thận khi ép kiểu generic
                history = (List<Map<String, Object>>) requestPayload.get("history");
            } catch (ClassCastException e) {
                logger.warn("Could not cast history payload to List<Map<String, Object>>");
                // Có thể bỏ qua history nếu sai định dạng
            }
        }


        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("reply", "Tin nhắn không được để trống."));
        }

        logger.info("Received chat message: {}", userMessage);
        String botReply = chatService.getGeminiResponse(userMessage, history);

        // Trả về response dạng JSON {"reply": "..."}
        return ResponseEntity.ok(Map.of("reply", botReply));
    }
}