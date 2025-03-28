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

    public Mono<BehaviorGenerationResult> generateBehaviorCode(String prompt) {
        return webClient.post()
                .bodyValue(createRequest(prompt))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        JsonNode rootNode = objectMapper.readTree(response);

                        // First parse and log token usage
                        JsonNode usageNode = rootNode.path("usage");
                        int promptTokens = usageNode.path("prompt_tokens").asInt(0);
                        int completionTokens = usageNode.path("completion_tokens").asInt(0);
                        int totalTokens = usageNode.path("total_tokens").asInt(0);

                        System.out.printf("Token Usage - Prompt: %d, Completion: %d, Total: %d%n",
                                promptTokens, completionTokens, totalTokens);

                        // Then parse the actual response
                        JsonNode choicesNode = rootNode.path("choices");

                        if (choicesNode.isEmpty() || !choicesNode.isArray()) {
                            return Mono.error(new RuntimeException("Invalid choices in API response"));
                        }

                        JsonNode messageNode = choicesNode.get(0).path("message");
                        String methods = messageNode.path("content").asText("").trim();

                        // Clean up the response if needed
                        methods = methods.replaceAll("```java|```", "").trim();

                        return Mono.just(new BehaviorGenerationResult(methods));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to parse API response", e));
                    }
                })
                .onErrorResume(e -> {
                    System.err.println("Behavior generation error: " + e.getMessage());
                    return Mono.just(new BehaviorGenerationResult(""));
                });
    }

    private Map<String, Object> createRequest(String prompt) {
        return Map.of(
                "model", "gpt-4o",
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
                "response_format", Map.of("type", "text")
        );
    }

    public Mono<String> generateJsonFromPrompt(String prompt) {
        System.out.println("Sending request to OpenAI API...");
        System.out.println("API URL: " + apiUrl);
        System.out.println("Prompt: " + prompt);

        Map<String, Object> requestBodyMap = Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful AI that generates structured JSON."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.5,
                "max_tokens", 2000
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

                        JsonNode usageNode = rootNode.path("usage");
                        int promptTokens = usageNode.path("prompt_tokens").asInt();
                        int completionTokens = usageNode.path("completion_tokens").asInt();
                        int totalTokens = usageNode.path("total_tokens").asInt();

                        System.out.println("Token Usage: Prompt Tokens = " + promptTokens +
                                           ", Completion Tokens = " + completionTokens +
                                           ", Total Tokens = " + totalTokens);

                        // Extract the content and clean it
                        String content = contentNode.asText();

                        // Remove Markdown formatting (e.g., ```json and ```)
                        String cleanedContent = content.replaceAll("```json|```", "").trim();

                        return Mono.just(cleanedContent);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to parse API response", e));
                    }
                })
                .doOnNext(response -> System.out.println("Extracted JSON: " + response))
                .doOnError(error -> System.err.println("API Error: " + error.getMessage()));
    }
}
