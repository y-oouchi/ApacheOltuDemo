package com.example.ssoDemo.bean;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceBean {
	private final String username;
	private final String password;
	private final String dispName;
}
