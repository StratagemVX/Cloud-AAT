package com.chatapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for CORS and static-resource serving.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:" + CorsOriginUtils.DEFAULT_ALLOWED_ORIGINS + "}")
    private String allowedOriginsRaw = CorsOriginUtils.DEFAULT_ALLOWED_ORIGINS;

    /** Allow cross-origin requests only from explicitly configured origins. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = CorsOriginUtils.parseAllowedOrigins(allowedOriginsRaw);
        if (origins.length > 0) {
            registry.addMapping("/api/**")
                    .allowedOrigins(origins)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("Content-Type", "Authorization", "X-Admin-Token", "X-Requested-With")
                    .allowCredentials(true);
        }
    }

    /** Serve static assets (HTML / CSS / JS) from classpath:/static/. */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
