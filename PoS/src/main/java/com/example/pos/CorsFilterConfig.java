package com.example.pos;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsFilterConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> customCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Explicitly trust your Next.js development client address
        config.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));

        // Explicitly accept all standard operational methods and OPTIONS preflights
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Accept all headers, including custom headers like sessionId
        config.setAllowedHeaders(Collections.singletonList("*"));

        // Permit cookies or localized session validation parameters if needed
        config.setAllowCredentials(true);

        // Expose the sessionId header so your frontend client can read it out if needed
        config.addExposedHeader("sessionId");

        // Apply this rule universally across all API pathways
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        // FORCE THIS FILTER TO RUN BEFORE ALL SECURITY OR INTERCEPTOR FILTERS
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}