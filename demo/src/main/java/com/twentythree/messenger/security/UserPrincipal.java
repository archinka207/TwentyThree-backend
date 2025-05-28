package com.twentythree.messenger.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.twentythree.messenger.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
// import java.util.stream.Collectors; // If you add roles from User entity

public class UserPrincipal implements UserDetails {
    private Long id;
    private String nickname; // This will be used as the "username" for Spring Security

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String nickname, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.nickname = nickname;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        // For TwentyThree, we might not have complex roles initially.
        // Every authenticated user gets a basic "ROLE_USER".
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        // If you add roles to User entity:
        // List<GrantedAuthority> authorities = user.getRoles().stream()
        //         .map(role -> new SimpleGrantedAuthority(role.getName().name()))
        //         .collect(Collectors.toList());

        return new UserPrincipal(
                user.getId(),
                user.getNickname(),
                user.getPassword(), // Hashed password from User entity
                authorities
        );
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getUsername() {
        // Spring Security's UserDetailsService expects a "username".
        // We are using nickname for login.
        return nickname;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Add logic if accounts can expire
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Add logic for account locking
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Add logic if credentials can expire
    }

    @Override
    public boolean isEnabled() {
        return true; // Add logic for disabling accounts
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}