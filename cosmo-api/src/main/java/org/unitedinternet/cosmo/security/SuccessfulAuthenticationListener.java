package org.unitedinternet.cosmo.security;

import org.springframework.security.core.Authentication;

public interface SuccessfulAuthenticationListener {
	void onSuccessfulAuthentication(Authentication authentication);
}
