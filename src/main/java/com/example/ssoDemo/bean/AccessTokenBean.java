package com.example.ssoDemo.bean;

import lombok.Data;

@Data
public class AccessTokenBean {
	private String accessToken;
	private String scope;
	private long expiresIn;
}
