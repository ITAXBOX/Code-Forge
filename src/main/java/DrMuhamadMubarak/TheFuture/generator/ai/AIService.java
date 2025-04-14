package DrMuhamadMubarak.TheFuture.generator.ai;

import DrMuhamadMubarak.TheFuture.generator.dto.BehaviorGenerationResult;
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

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
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

    public Mono<BehaviorGenerationResult> generateBehaviorCode(String prompt) {
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(createDeepSeekRequest(prompt))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        JsonNode rootNode = objectMapper.readTree(response);

                        // Parse token usage
                        JsonNode usageNode = rootNode.path("usage");
                        int promptTokens = usageNode.path("prompt_tokens").asInt(0);
                        int completionTokens = usageNode.path("completion_tokens").asInt(0);
                        int totalTokens = usageNode.path("total_tokens").asInt(0);

                        System.out.printf("DeepSeek Token Usage - Prompt: %d, Completion: %d, Total: %d%n",
                                promptTokens, completionTokens, totalTokens);

                        // Parse the response
                        JsonNode choicesNode = rootNode.path("choices");
                        if (choicesNode.isEmpty() || !choicesNode.isArray()) {
                            return Mono.error(new RuntimeException("Invalid choices in DeepSeek API response"));
                        }

                        JsonNode messageNode = choicesNode.get(0).path("message");
                        String methods = messageNode.path("content").asText("").trim();

                        // Clean up the response
                        methods = methods.replaceAll("```java|```", "").trim();

                        return Mono.just(new BehaviorGenerationResult(methods));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to parse DeepSeek API response", e));
                    }
                })
                .onErrorResume(e -> {
                    System.err.println("DeepSeek behavior generation error: " + e.getMessage());
                    return Mono.just(new BehaviorGenerationResult(""));
                });
    }

    private Map<String, Object> createDeepSeekRequest(String prompt) {
        return Map.of(
                "model", "deepseek-chat", // or "deepseek-coder" for code-specific tasks
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "You are a Java Spring expert that generates business logic methods. " +
                                           "Return only the method implementations with JavaDoc comments."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", 0.3,
                "max_tokens", 2000,
                "stream", false
        );
    }

    public Mono<String> generateJsonFromPrompt(String prompt) {
        System.out.println("Sending request to DeepSeek API...");
        System.out.println("API URL: " + apiUrl);
        System.out.println("Prompt: " + prompt);

        Map<String, Object> requestBodyMap = Map.of(
                "model", "deepseek-chat",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful AI that generates structured JSON."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.5,
                "max_tokens", 2000,
                "stream", false
        );

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBodyMap)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        JsonNode rootNode = objectMapper.readTree(response);

                        JsonNode contentNode = rootNode.path("choices").path(0).path("message").path("content");
                        if (contentNode.isMissingNode()) {
                            return Mono.error(new RuntimeException("'content' field is missing in DeepSeek API response"));
                        }

                        JsonNode usageNode = rootNode.path("usage");
                        int promptTokens = usageNode.path("prompt_tokens").asInt();
                        int completionTokens = usageNode.path("completion_tokens").asInt();
                        int totalTokens = usageNode.path("total_tokens").asInt();

                        System.out.println("DeepSeek Token Usage: Prompt Tokens = " + promptTokens +
                                           ", Completion Tokens = " + completionTokens +
                                           ", Total Tokens = " + totalTokens);

                        String content = contentNode.asText();
                        String cleanedContent = content.replaceAll("```json|```", "").trim();

                        return Mono.just(cleanedContent);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to parse DeepSeek API response", e));
                    }
                })
                .doOnNext(response -> System.out.println("Extracted JSON: " + response))
                .doOnError(error -> System.err.println("DeepSeek API Error: " + error.getMessage()));
    }
}