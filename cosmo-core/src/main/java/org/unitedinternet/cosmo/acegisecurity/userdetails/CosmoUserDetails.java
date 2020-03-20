/*
 * Copyright 2005-2006 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.acegisecurity.userdetails;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.unitedinternet.cosmo.model.User;

/**
 * Wraps a Cosmo <code>User</code> to provide Acegi Security with
 * access to user account information.
 * <p>
 * If the associated user is an administrator, contains an authority
 * named <code>ROLE_ROOT</code>.
 * <p>
 * If the associated user is not an administrator, contains an
 * authority named <code>ROLE_USER</code>.
 *
 * @see UserDetails
 * @see GrantedAuthority
 */
public class CosmoUserDetails implements UserDetails {
    
   private static final long serialVersionUID = 3034617040424768102L;

    private User user;
    private List<GrantedAuthority> authorities;

    /**
     * @param user the wrapped <code>User</code>
     * @see User
     */
    public CosmoUserDetails(User user) {
        this.user = user;

        ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        if (user.getAdmin().booleanValue()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ROOT"));
        }
        if (! user.isOverlord()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        this.authorities = authorities;
    }

    // UserDetails methods

    /**
     * Indicates whether the user's account has expired. An expired
     * account can not be authenticated.
     *
     * Note: since account expiration has not been implemented in
     * Cosmo, this method always returns <code>true</code>.
     *
     * @return <code>true</code> if the user's account is valid (ie
     * non-expired), <code>false</code> if no longer valid (ie
     * expired)
     */
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user account is locked or unlocked. A
     * locked user can not be authenticated.
     *
     * @return <code>true</code> if the user is not locked,
     * <code>false</code> otherwise
     */
    public boolean isAccountNonLocked() {
        if (user.isOverlord()) {
            return true;
        }
        return ! user.isLocked().booleanValue();
    }

    /**
     * Returns the authorities granted to the user. Cannot return
     * <code>null</code>.
     *
     * @return the authorities (never <code>null</code>)
     */
    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Indicates whether the users's credentials (password) has
     * expired. Expired credentials prevent authentication.
     *
     * Note: since credential expiration has not been implemented in
     * Cosmo, this method always returns <code>true</code>.
     *
     * @return <code>true</code> if the user's credentials are
     * valid (ie non-expired), <code>false</code> if no longer
     * valid (ie expired)
     */
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user account has been activated. A
     * disabled user cannot be authenticated.
     *
     * @return <code>true</code> if the user is enabled,
     * <code>false</code> otherwise
     */
    public boolean isEnabled() {
        return user.isActivated();
    }

    /**
     * Returns the password used to authenticate the user. Cannot
     * return <code>null</code>.
     *
     * @return the password (never <code>null</code>)
     */
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the username used to authenticate the user. Cannot
     * return <code>null</code>.
     *
     * @return the username (never <code>null</code>)
     */
    public String getUsername() {
        return user.getUsername();
    }

    // oru methods

    /**
     * Returns the underlying <code>User</code>.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }
}
