package com.example.ssoDemo.service;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ssoDemo.bean.AccessTokenBean;
import com.example.ssoDemo.bean.ResourceBean;
import com.example.ssoDemo.client.BasicAuthorizationOAuthClient;
import com.example.ssoDemo.config.SsoDemoConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SsoDemoService {
	@Autowired
	SsoDemoConfig config;
	
	public String createAuthorizationUrl(String state) throws OAuthSystemException {
		OAuthClientRequest clientRequest = OAuthClientRequest
				.authorizationLocation(config.getAuthorizationUri())
				.setClientId(config.getClientId())
				.setRedirectURI("http://localhost:10001/auth_callback")
				.setResponseType(ResponseType.CODE.toString())
				.setState(state)
				.setScope("resource.read resource.write")
				.buildQueryMessage();

		return clientRequest.getLocationUri();
	}
	
	public AccessTokenBean getAccessToken(String code) throws OAuthSystemException, OAuthProblemException {
		OAuthClientRequest oauthRequest = OAuthClientRequest
				.tokenLocation(config.getAccessTokenUri())
				.setRedirectURI("http://localhost:10001/auth_callback")
				.setGrantType(GrantType.AUTHORIZATION_CODE)
				.setCode(code)
				.buildBodyMessage();

		BasicAuthorizationOAuthClient oAuthClient = new BasicAuthorizationOAuthClient(new URLConnectionClient());
		oAuthClient.setClientId(config.getClientId());
		oAuthClient.setClientSecret(config.getClientSecret());
		OAuthAccessTokenResponse oAuthResponse = oAuthClient.accessTokenWithBasicAuthorization(oauthRequest, OAuth.HttpMethod.POST);
		
		AccessTokenBean bean = new AccessTokenBean();
		bean.setAccessToken(oAuthResponse.getAccessToken());
		bean.setScope(oAuthResponse.getScope());
		bean.setExpiresIn(oAuthResponse.getExpiresIn());
		
		return bean;
	}
	
	public ResourceBean getResource(String accessToken) throws OAuthSystemException, OAuthProblemException {
		OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(config.getUserInfoUri())
				.setAccessToken(accessToken)
				.buildQueryMessage();

		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
		OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
		
		log.info("json:{}", resourceResponse.getBody());
		
		JSONObject json = new JSONObject(resourceResponse.getBody());
		String username = json.optString("username");
		String password = json.optString("password");
		String dispName = json.optString("dispName");
		
		return new ResourceBean(username, password, dispName);
	}
}
