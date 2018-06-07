package com.example.ssoDemo.controller;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class SsoTestController {
	@RequestMapping("/requestCheck")
	public void requestCheck(HttpServletRequest request) {
		Enumeration<?> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = (String)headerNames.nextElement();
			String headerValue = request.getHeader(headerName);
			log.info("header " + headerName + ":" + headerValue);
		}
		
		for(String key : request.getParameterMap().keySet()) {
			log.info("parameter " + key + ":" + request.getParameter(key));
		}
	}
}
