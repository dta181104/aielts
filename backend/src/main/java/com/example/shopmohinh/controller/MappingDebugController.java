package com.example.shopmohinh.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class MappingDebugController {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @GetMapping("/debug/mappings")
    public Map<String, Set<String>> mappings() {
        return handlerMapping.getHandlerMethods().entrySet().stream()
                .filter(e -> {
                    try {
                        var pc = e.getKey().getPatternsCondition();
                        if (pc == null) return false;
                        return pc.getPatterns().stream().anyMatch(p -> p != null && p.contains("identity"));
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .collect(Collectors.toMap(
                        e -> String.join("|", e.getKey().getPatternsCondition().getPatterns()),
                        e -> e.getKey().getMethodsCondition().getMethods().stream().map(Object::toString).collect(Collectors.toSet())
                ));
    }
}
