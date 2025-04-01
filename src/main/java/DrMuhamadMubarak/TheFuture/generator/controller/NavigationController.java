package DrMuhamadMubarak.TheFuture.generator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import static DrMuhamadMubarak.TheFuture.utils.FileUtils.deleteProjectDirectory;

@Controller
@SessionAttributes("projectName")
public class NavigationController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/with-delete")
    public String indexWithDelete(Model model) {
        String projectName = (String) model.getAttribute("projectName");

        deleteProjectDirectory(projectName);

        return "index";
    }

    @GetMapping("/start")
    public String start() {
        return "start";
    }

    @GetMapping("/start-with-delete")
    public String startWithDelete(Model model) {
        String projectName = (String) model.getAttribute("projectName");

        deleteProjectDirectory(projectName);

        return "start";
    }

    @GetMapping("/support")
    public String support() {
        return "resources/support";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "legal/privacy";
    }

    @GetMapping("/terms-of-service")
    public String termsOfService() {
        return "legal/terms";
    }

    @GetMapping("/licenses")
    public String licenses() {
        return "legal/licenses";
    }

    @GetMapping("/loading-page")
    public String loadingPage() {
        return "loading-page";
    }
}