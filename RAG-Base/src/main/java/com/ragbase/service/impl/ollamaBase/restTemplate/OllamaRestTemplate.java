package com.ragbase.service.impl.ollamaBase.restTemplate;

import com.ragbase.service.impl.ollamaBase.OllamaClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "spring.ai.ollama.type", havingValue = "restTemplate")
public class OllamaRestTemplate extends OllamaClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public String chatWithOllama(String prompt) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", OLLAMA_MODEL); // 替换为你本地的模型名
        request.put("prompt", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(OLLAMA_URL + "/api/chat", entity, String.class);
        return response.getBody();
    }

}
