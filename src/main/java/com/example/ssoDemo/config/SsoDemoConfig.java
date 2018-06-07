package com.example.ssoDemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "sso.demo")
public class SsoDemoConfig {
	private String clientId;
	private String clientSecret;
	private String authorizationUri;
	private String accessTokenUri;
	private String userInfoUri;
	private String authorizationLogoutUri;
}
