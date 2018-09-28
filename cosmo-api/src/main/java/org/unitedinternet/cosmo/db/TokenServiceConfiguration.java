package org.unitedinternet.cosmo.db;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.springframework.security.core.token.TokenService;

@Configuration
public class TokenServiceConfiguration {

	@Value("${cosmo.tikets.serverSecret}")
	private String serverSecret;
	
	@Value("${cosmo.tikets.serverInteger}")
	private Integer serverInteger;
	
	@Bean
	public TokenService getTokenService() {

		KeyBasedPersistenceTokenService keyBasedPersistenceTokenService = new KeyBasedPersistenceTokenService();
		keyBasedPersistenceTokenService.setServerSecret(serverSecret);
		keyBasedPersistenceTokenService.setServerInteger(serverInteger);
		keyBasedPersistenceTokenService.setPseudoRandomNumberBytes(16);
		keyBasedPersistenceTokenService.setSecureRandom(new SecureRandom());
		return keyBasedPersistenceTokenService;
	}
}
