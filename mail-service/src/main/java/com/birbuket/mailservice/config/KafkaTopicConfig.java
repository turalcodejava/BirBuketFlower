package com.birbuket.mailservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic passwordResetTopic(@Value("${app.kafka.password-reset-topic}") String name) {
        return TopicBuilder.name(name).partitions(1).build();
    }

    @Bean
    public NewTopic welcomeTopic(@Value("${app.kafka.welcome-topic}") String name) {
        return TopicBuilder.name(name).partitions(1).build();
    }
}
