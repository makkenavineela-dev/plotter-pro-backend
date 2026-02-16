package com.plotterpro.controller;

import com.plotterpro.entity.GraphEntity;
import com.plotterpro.entity.UserEntity;
import com.plotterpro.repository.GraphRepository;
import com.plotterpro.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

@Controller
public class GraphController {

    private final GraphRepository graphRepository;
    private final UserService userService;

    public GraphController(GraphRepository graphRepository, UserService userService) {
        this.graphRepository = graphRepository;
        this.userService = userService;
    }

    // Handles form submission from plot pages
    @PostMapping("/save-graph")
    public String saveGraph(@RequestParam(required = false) Long id,
            @RequestParam String title,
            @RequestParam String equation,
            @RequestParam String mode,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserEntity user = userService.getUserByEmail(userDetails.getUsername());
        GraphEntity graph;

        if (id != null) {
            graph = graphRepository.findById(id).orElse(new GraphEntity());
            if (graph.getId() != null && !graph.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        } else {
            graph = new GraphEntity();
        }

        if (graph.getShareToken() == null) {
            graph.setShareToken(java.util.UUID.randomUUID().toString());
        }

        graph.setTitle(title);
        graph.setEquation(equation);
        graph.setMode(mode);
        graph.setUser(user);

        graphRepository.save(graph);
        return "redirect:/main";
    }

    // Get Share Link for a Graph
    @GetMapping("/api/graphs/{id}/share-link")
    @ResponseBody
    public String getShareLink(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userService.getUserByEmail(userDetails.getUsername());
        GraphEntity graph = graphRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!graph.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (graph.getShareToken() == null) {
            graph.setShareToken(java.util.UUID.randomUUID().toString());
            graphRepository.save(graph);
        }

        return "/share/" + graph.getShareToken();
    }

    // Public View for Shared Graphs
    @GetMapping("/share/{token}")
    public String viewSharedGraph(@PathVariable String token, org.springframework.ui.Model model) {
        GraphEntity graph = graphRepository.findByShareToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Graph not found"));

        model.addAttribute("graph", graph);
        model.addAttribute("readOnly", true);

        return "2d".equals(graph.getMode()) ? "plot2d" : "plot3d";
    }

    @DeleteMapping("/api/graphs/{id}")
    @ResponseBody
    public void deleteGraph(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userService.getUserByEmail(userDetails.getUsername());
        GraphEntity graph = graphRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!graph.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        graphRepository.delete(graph);
    }
}
