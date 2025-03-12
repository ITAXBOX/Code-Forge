package DrMuhamadMubarak.TheFuture.generator.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    private WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public Mono<String> generateJsonFromPrompt(String prompt) {
        System.out.println("Sending request to OpenAI API...");
        System.out.println("API URL: " + apiUrl);
        System.out.println("Prompt: " + prompt);

        Map<String, Object> requestBodyMap = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful AI that generates structured JSON."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7,
                "max_tokens", 1000
        );

        return webClient.post()
                .bodyValue(requestBodyMap)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        JsonNode rootNode = objectMapper.readTree(response);
                        JsonNode contentNode = rootNode.path("choices").path(0).path("message").path("content");

                        if (contentNode.isMissingNode()) {
                            return Mono.error(new RuntimeException("'content' field is missing in the API response"));
                        }

                        return Mono.just(contentNode.asText());
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to parse API response", e));
                    }
                })
                .doOnNext(response -> System.out.println("Extracted JSON: " + response))
                .doOnError(error -> System.err.println("API Error: " + error.getMessage()));
    }
}
