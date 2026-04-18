package me.bchieu.messaging.common.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SecurityEndpointProperties.class)
public class SecurityConfig {}
