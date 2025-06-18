package DrMuhamadMubarak.TheFuture.frontend.nextjs;

import DrMuhamadMubarak.TheFuture.frontend.nextjs.metadata.EntityInfo;
import DrMuhamadMubarak.TheFuture.frontend.nextjs.service.ApiGeneratorService;
import DrMuhamadMubarak.TheFuture.frontend.nextjs.service.EntityExtractorService;
import DrMuhamadMubarak.TheFuture.frontend.nextjs.service.NextjsFileService;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Main class for generating Next.js frontend files
 * Coordinates the work of various services, each with a single responsibility
 */
public class NextjsGenerator {
    private static final String TEMPLATE_DIR = "Code Forge Dashboard";

    // Service classes
    private static final EntityExtractorService entityExtractor = new EntityExtractorService();
    private static final ApiGeneratorService apiGenerator = new ApiGeneratorService();
    private static final NextjsFileService fileService = new NextjsFileService();

    /**
     * Generates Next.js frontend files for the given project
     *
     * @param projectName the name of the project
     */
    public static void generateNextjsFiles(String projectName) {
        try {
            String outputDir = projectName + "/frontend";

            // Create output directory
            File outputDirFile = new File(outputDir);

            // Copy base structure from template
            fileService.copyTemplateStructure(TEMPLATE_DIR, outputDir);

            // Get entities and endpoints from backend
            List<EntityInfo> entities = entityExtractor.getEntitiesFromBackend(projectName);

            // Generate customized dashboard file
            fileService.generateDashboard(projectName, TEMPLATE_DIR, outputDir, entities);

            // Update package.json with project name
            fileService.updatePackageJson(projectName, outputDir);

            // Generate API utility files
            apiGenerator.generateApiUtils(outputDir, entities);

            // Generate .env.local file for backend URL
            apiGenerator.generateEnvFile(projectName, outputDir);

            printSuccessMessage(outputDirFile);

        } catch (IOException e) {
            System.err.println("Error generating Next.js files: " + e.getMessage());
        }
    }

    /**
     * Prints a success message with instructions
     */
    private static void printSuccessMessage(File outputDir) {
        System.out.println("--------------------------------------------------");
        System.out.println("Next.js frontend generation completed successfully!");
        System.out.println("Frontend files generated at: " + outputDir.getAbsolutePath());
        System.out.println("To start the application, navigate to the directory and run:");
        System.out.println("pnpm install");
        System.out.println("pnpm run dev");
        System.out.println("--------------------------------------------------");
    }
}
