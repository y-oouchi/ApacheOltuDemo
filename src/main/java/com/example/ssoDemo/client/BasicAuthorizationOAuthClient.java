package com.example.ssoDemo.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.oltu.oauth2.client.HttpClient;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import lombok.Setter;

public class BasicAuthorizationOAuthClient extends OAuthClient {

	@Setter
	private String clientId;
	@Setter
	private String clientSecret;
	public BasicAuthorizationOAuthClient(HttpClient oauthClient) {
		super(oauthClient);
	}

	public OAuthJSONAccessTokenResponse accessTokenWithBasicAuthorization(OAuthClientRequest request, String requestMethod)
																			throws OAuthSystemException, OAuthProblemException {
		return accessTokenWithBasicAuthorization(request, requestMethod, OAuthJSONAccessTokenResponse.class);
	}
	
	public <T extends OAuthAccessTokenResponse> T accessTokenWithBasicAuthorization(OAuthClientRequest request, String requestMethod, Class<T> responseClass)
																					throws OAuthSystemException, OAuthProblemException {

		String plainCredentials = clientId + ":" + clientSecret;
		String base64Credentials = Base64.getEncoder().encodeToString(plainCredentials.getBytes(StandardCharsets.UTF_8));
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Basic " + base64Credentials);
		headers.put(OAuth.HeaderType.CONTENT_TYPE, OAuth.ContentType.URL_ENCODED);

		return httpClient.execute(request, headers, requestMethod, responseClass);
	}
}
