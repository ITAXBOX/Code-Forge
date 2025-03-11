package DrMuhamadMubarak.TheFuture.generator.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder().baseUrl(apiUrl).build();
    }

    public Mono<String> generateJsonFromPrompt(String prompt) {
        System.out.println("Sending request to OpenAI API...");
        System.out.println("API URL: " + apiUrl);
        System.out.println("Prompt: " + prompt);

        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("model", "gpt-3.5-turbo-instruct");
        requestBodyMap.put("prompt", prompt);
        requestBodyMap.put("max_tokens", 500);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(requestBodyMap);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to create request body", e));
        }

        return webClient.post()
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        JsonNode rootNode = objectMapper.readTree(response);
                        JsonNode textNode = rootNode.path("choices").path(0).path("text");

                        if (textNode.isMissingNode()) {
                            return Mono.error(new RuntimeException("'text' field is missing in the API response"));
                        }

                        return Mono.just(textNode.asText());
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to parse API response", e));
                    }
                })
                .doOnNext(response -> System.out.println("Extracted JSON: " + response))
                .doOnError(error -> System.err.println("API Error: " + error.getMessage()));
    }
}