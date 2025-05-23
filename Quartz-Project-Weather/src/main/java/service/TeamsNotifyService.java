package service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TeamsNotifyService {

    private final RestTemplate restTemplate = new RestTemplate();
    
    // 請替換為你實際建立的 Webhook URL
    private static final String WEBHOOK_URL = "https://picmis.webhook.office.com/webhookb2/063e4e34-c438-4214-8b95-e55410cf77da@dda92dc6-2169-4d9a-acae-54019f5327f2/IncomingWebhook/d4e3d055f1a242989e0cb6ddc8484688/eb4146bd-81e5-4a9b-b5db-2084c86533b4/V2QuUJrvartxl2bPcApZoTHdAdA_mhw8cAtzc0tMqzOQc1";

    public void sendMessage(String message) {
        Map<String, String> payload = Map.of("text", message);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
        
        restTemplate.postForEntity(WEBHOOK_URL, request, String.class);
    }
}
