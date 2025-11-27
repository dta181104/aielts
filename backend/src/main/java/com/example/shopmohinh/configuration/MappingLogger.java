package com.example.shopmohinh.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
@RequiredArgsConstructor
@Slf4j
public class MappingLogger {
    private final RequestMappingHandlerMapping mapping;

    @EventListener(ApplicationReadyEvent.class)
    public void logMappings() {
        log.info("--- Listing all request mappings ---");
        mapping.getHandlerMethods().forEach((info, method) -> {
            log.info("{} -> {}#{}", info.getPatternsCondition(), method.getBeanType().getSimpleName(), method.getMethod().getName());
        });
        log.info("--- End of request mappings ---");
    }
}

