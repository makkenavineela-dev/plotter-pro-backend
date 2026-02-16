package com.plotterpro.controller;

import com.plotterpro.entity.GraphEntity;
import com.plotterpro.entity.UserEntity;
import com.plotterpro.repository.GraphRepository;
import com.plotterpro.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class PageController {

    private final GraphRepository graphRepository;
    private final UserService userService;

    public PageController(GraphRepository graphRepository, UserService userService) {
        this.graphRepository = graphRepository;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/main";
        }
        return "headpage";
    }

    @GetMapping("/headpage")
    public String headPage() {
        return "headpage";
    }

    @GetMapping("/main")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserEntity user = userService.getUserByEmail(userDetails.getUsername());
        List<GraphEntity> graphs = graphRepository.findByUser_Id(user.getId());
        model.addAttribute("graphs", graphs);
        return "dashboard";
    }

    @GetMapping("/plot/2d")
    public String plot2d() {
        return "plot2d";
    }

    @GetMapping("/plot/3d")
    public String plot3d() {
        return "plot3d";
    }

    @GetMapping("/graph/{id}")
    public String loadGraph(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        UserEntity user = userService.getUserByEmail(userDetails.getUsername());
        GraphEntity graph = graphRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Graph not found"));

        if (!graph.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        model.addAttribute("graph", graph);
        return "2d".equalsIgnoreCase(graph.getMode()) ? "plot2d" : "plot3d";
    }
}
