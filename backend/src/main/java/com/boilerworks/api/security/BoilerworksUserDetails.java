package com.boilerworks.api.security;

import com.boilerworks.api.model.AppUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class BoilerworksUserDetails implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final boolean active;
    private final boolean staff;
    private final Collection<? extends GrantedAuthority> authorities;

    public BoilerworksUserDetails(AppUser user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.active = user.isActive();
        this.staff = user.isStaff();
        this.authorities = user.getGroups().stream()
            .flatMap(group -> group.getPermissions().stream())
            .map(perm -> new SimpleGrantedAuthority(perm.getCodename()))
            .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
