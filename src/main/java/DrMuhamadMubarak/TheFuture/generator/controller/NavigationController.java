package DrMuhamadMubarak.TheFuture.generator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavigationController {
    @GetMapping("/start")
    public String start() {
        return "start";
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