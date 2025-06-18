package DrMuhamadMubarak.TheFuture.frontend.nextjs.service;

import DrMuhamadMubarak.TheFuture.frontend.nextjs.metadata.EntityInfo;
import DrMuhamadMubarak.TheFuture.frontend.nextjs.metadata.EndpointInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service responsible for generating Next.js API utility files
 */
public class ApiGeneratorService {

    /**
     * Generates API utility files for the frontend
     */
    public void generateApiUtils(String outputDir, List<EntityInfo> entities) throws IOException {
        // Create lib/api directory if it doesn't exist
        Path apiDirPath = Paths.get(outputDir, "lib", "api");
        if (!Files.exists(apiDirPath)) {
            Files.createDirectories(apiDirPath);
        }

        // Generate api-client.ts
        generateApiClient(outputDir);

        // Generate entity-specific API files
        for (EntityInfo entity : entities) {
            generateEntityApiFile(entity, outputDir);
        }
    }

    /**
     * Generates the base API client
     */
    private void generateApiClient(String outputDir) throws IOException {
        String apiClientCode = """
                // lib/api/api-client.ts
                import axios from 'axios';
                
                const apiClient = axios.create({
                  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081',
                  headers: {
                    'Content-Type': 'application/json'
                  }
                });
                
                export default apiClient;
                """;

        Path apiClientPath = Paths.get(outputDir, "lib", "api", "api-client.ts");
        Files.write(apiClientPath, apiClientCode.getBytes());
    }

    /**
     * Generates API file for a specific entity
     */
    private void generateEntityApiFile(EntityInfo entity, String outputDir) throws IOException {
        String entityName = entity.getName();
        String entityNameLower = entityName.toLowerCase();
        StringBuilder apiCode = new StringBuilder();

        apiCode.append("// lib/api/").append(entityNameLower).append(".ts\n");
        apiCode.append("import apiClient from './api-client';\n\n");
        apiCode.append("export interface ").append(entityName).append(" {\n");
        apiCode.append("  id: number;\n");
        // Add basic fields that most entities would have
        apiCode.append("  name?: string;\n");
        apiCode.append("  // Add other fields based on your entity structure\n");
        apiCode.append("}\n\n");

        apiCode.append("export const get").append(entityName).append("s = async () => {\n");
        apiCode.append("  const response = await apiClient.get<").append(entityName).append("[]>('").append(entity.getBaseEndpoint()).append("');\n");
        apiCode.append("  return response.data;\n");
        apiCode.append("};\n\n");

        apiCode.append("export const get").append(entityName).append(" = async (id: number) => {\n");
        apiCode.append("  const response = await apiClient.get<").append(entityName).append(">(`").append(entity.getBaseEndpoint()).append("/${id}`);\n");
        apiCode.append("  return response.data;\n");
        apiCode.append("};\n\n");

        apiCode.append("export const create").append(entityName).append(" = async (data: Omit<").append(entityName).append(", 'id'>) => {\n");
        apiCode.append("  const response = await apiClient.post<").append(entityName).append(">('").append(entity.getBaseEndpoint()).append("', data);\n");
        apiCode.append("  return response.data;\n");
        apiCode.append("};\n\n");

        apiCode.append("export const update").append(entityName).append(" = async (id: number, data: Partial<").append(entityName).append(">) => {\n");
        apiCode.append("  const response = await apiClient.put<").append(entityName).append(">(`").append(entity.getBaseEndpoint()).append("/${id}`, data);\n");
        apiCode.append("  return response.data;\n");
        apiCode.append("};\n\n");

        apiCode.append("export const delete").append(entityName).append(" = async (id: number) => {\n");
        apiCode.append("  await apiClient.delete(`").append(entity.getBaseEndpoint()).append("/${id}`);\n");
        apiCode.append("};\n\n");

        // Add custom behavior methods
        for (EndpointInfo endpoint : entity.getEndpoints()) {
            if (endpoint.isCustomBehavior()) {
                String methodName = extractMethodName(endpoint.getPath());
                apiCode.append("export const ").append(methodName).append(" = async (");

                switch (endpoint.getMethod()) {
                    case "GET" -> {
                        apiCode.append(") => {\n");
                        apiCode.append("  const response = await apiClient.get('").append(endpoint.getPath()).append("');\n");
                    }
                    case "POST" -> {
                        apiCode.append("data: any) => {\n");
                        apiCode.append("  const response = await apiClient.post('").append(endpoint.getPath()).append("', data);\n");
                    }
                    case "PUT" -> {
                        apiCode.append("id: number, data: any) => {\n");
                        apiCode.append("  const response = await apiClient.put(`").append(endpoint.getPath()).append("`, data);\n");
                    }
                    default -> {
                        apiCode.append(") => {\n");
                        apiCode.append("  const response = await apiClient.").append(endpoint.getMethod().toLowerCase()).append("('").append(endpoint.getPath()).append("');\n");
                    }
                }

                apiCode.append("  return response.data;\n");
                apiCode.append("};\n\n");
            }
        }

        Path entityApiPath = Paths.get(outputDir, "lib", "api", entityNameLower + ".ts");
        Files.write(entityApiPath, apiCode.toString().getBytes());
    }

    /**
     * Generates an environment file with API URLs
     */
    public void generateEnvFile(String projectName, String outputDir) throws IOException {
        String envContent = "# Backend API URL\n" +
                "NEXT_PUBLIC_API_URL=http://localhost:8081\n" +
                "NEXT_PUBLIC_APP_NAME=" + projectName + "\n";

        Path envFilePath = Paths.get(outputDir, ".env.local");
        Files.write(envFilePath, envContent.getBytes());
    }

    /**
     * Extracts a method name from an endpoint path
     */
    private String extractMethodName(String path) {
        // Extract a method name from path
        // /api/users/search -> searchUsers
        String[] parts = path.split("/");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            // Remove any path parameters
            if (lastPart.contains("{")) {
                lastPart = lastPart.substring(0, lastPart.indexOf("{"));
            }
            // Clean up and format
            if (!lastPart.isEmpty()) {
                // Convert to camelCase if it has hyphens
                if (lastPart.contains("-")) {
                    String[] words = lastPart.split("-");
                    StringBuilder methodName = new StringBuilder(words[0]);
                    for (int i = 1; i < words.length; i++) {
                        methodName.append(Character.toUpperCase(words[i].charAt(0)))
                                 .append(words[i].substring(1));
                    }
                    return methodName.toString();
                }
                return lastPart;
            }
        }
        return "customOperation";
    }
}
