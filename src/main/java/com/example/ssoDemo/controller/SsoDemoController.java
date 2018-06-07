package com.example.ssoDemo.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.ssoDemo.bean.AccessTokenBean;
import com.example.ssoDemo.bean.ResourceBean;
import com.example.ssoDemo.config.SsoDemoConfig;
import com.example.ssoDemo.service.SsoDemoService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class SsoDemoController {

	@Autowired
	HttpSession ses;
	@Autowired
	SsoDemoConfig config;
	@Autowired
	SsoDemoService service;
	
	private String state;
	
	@RequestMapping("/top")
	public String top() {
		return "top";
	}
	
	@RequestMapping("/index")
	public String index(HttpServletRequest request, Model model) {
		// TODO:ログイン判定
		if(ses.getAttribute("accessToken") == null) {
			String redirectUrl = request.getRequestURL().toString();
			if(!StringUtils.isEmpty(request.getQueryString())) {
				redirectUrl += "?" + request.getQueryString();
			}
			ses.setAttribute("redirectUrl", redirectUrl);
			
			return "redirect:login";
		} else {
			if(log.isDebugEnabled()) {
				Enumeration<?> headerNames = request.getHeaderNames();
				while (headerNames.hasMoreElements()) {
					// ヘッダ名と値を取得
					String headerName = (String)headerNames.nextElement();
					String headerValue = request.getHeader(headerName);
					log.debug("header " + headerName + ":" + headerValue);
				}
			}
			
			model.addAttribute("accessToken", ses.getAttribute("accessToken"));
			model.addAttribute("scope", ses.getAttribute("scope"));
			model.addAttribute("expiresIn", ses.getAttribute("expiresIn"));
			model.addAttribute("userInfo", ses.getAttribute("userInfo"));
			
			return "index";
		}
	}
	
	@RequestMapping("/login")
	public String login(final Model model) throws OAuthSystemException {
		state = Base64.getEncoder().encodeToString(ses.getId().getBytes());

		String redirectURL = service.createAuthorizationUrl(state);
		log.debug("locationURL:{}", redirectURL);

		return "redirect:" + redirectURL;
	}
	
	@GetMapping("/auth_callback")
	public String authCallback(HttpServletRequest request, HttpServletResponse response, Model model)
												throws OAuthProblemException, OAuthSystemException {
		OAuthAuthzResponse oauthResponse = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
		String code = oauthResponse.getCode();
		String resState = oauthResponse.getState();
		
		if(!state.equals(resState)) {
			log.error("state is invalid. state:{}, resState:{}", state, resState);
			throw new RuntimeException("state is invalid");
		}
	
		AccessTokenBean tokenBean = service.getAccessToken(code);
		ResourceBean resource = service.getResource(tokenBean.getAccessToken());
		
		ses.setAttribute("accessToken", tokenBean.getAccessToken());
		ses.setAttribute("scope", tokenBean.getScope());
		ses.setAttribute("expiresIn", tokenBean.getExpiresIn());
		ses.setAttribute("userInfo", resource);

		if(ses.getAttribute("redirectUrl") != null) {
			String redirectUrl = (String) ses.getAttribute("redirectUrl");
			ses.removeAttribute("redirectUrl");
			
			return "redirect:" + redirectUrl;
		}
		
		return "redirect:index";
	}
	
	@PostMapping("/logout")
	public String logout() throws UnsupportedEncodingException {
		if(ses.getAttribute("accessToken") != null) {
			ses.removeAttribute("accessToken");
			ses.removeAttribute("scope");
			ses.removeAttribute("expiresIn");
			ses.removeAttribute("userInfo");
			
			// 認証サーバのログアウト
			String redirectUrl = URLEncoder.encode("http://localhost:10001/top", "UTF-8");
			return "redirect:" + config.getAuthorizationLogoutUri() + "?redirect_uri=" + redirectUrl;
		}
		return "redirect:top";
	}
}
