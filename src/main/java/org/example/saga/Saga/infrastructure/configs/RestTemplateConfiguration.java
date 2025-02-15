package org.example.saga.Saga.infrastructure.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplateConfiguration {
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        converters.add(new FormHttpMessageConverter()); // Добавляем поддержку multipart/form-data
        converters.add(new MappingJackson2HttpMessageConverter()); // Добавляем поддержку JSON
        restTemplate.setMessageConverters(converters);
        return restTemplate;
    }
}
