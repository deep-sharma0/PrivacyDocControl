package com.privacydoccontrol.security;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.privacydoccontrol.model.User;

import java.util.*;

public class CustomUserDetails implements UserDetails {

    private User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // can be enhanced later
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // can be enhanced later
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // can be enhanced later
    }

    @Override
    public boolean isEnabled() {
        return true; // can be enhanced later
    }

    public User getUser() {
        return user;
    }
}
