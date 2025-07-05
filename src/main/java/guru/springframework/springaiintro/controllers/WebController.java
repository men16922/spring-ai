package guru.springframework.springaiintro.controllers;

import guru.springframework.springaiintro.model.Answer;
import guru.springframework.springaiintro.model.Question;
import guru.springframework.springaiintro.services.OpenAIService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web")
public class WebController {
    private final OpenAIService openAIService;

    public WebController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @GetMapping("/ask")
    public String askForm(Model model) {
        model.addAttribute("question", new Question(""));
        return "ask";
    }

    @PostMapping("/ask")
    public String askSubmit(@ModelAttribute Question question, Model model) {
        Answer answer = openAIService.getAnswer(question);
        model.addAttribute("answer", answer);
        return "ask";
    }
}
