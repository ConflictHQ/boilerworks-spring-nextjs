package com.boilerworks.api.dto;

import com.boilerworks.api.model.AppUser;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class UserResponse {

    private final UUID id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String fullName;
    private final boolean active;
    private final boolean staff;
    private final Instant lastLogin;
    private final Set<String> groups;
    private final Set<String> permissions;

    public UserResponse(AppUser user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.fullName = user.getFullName();
        this.active = user.isActive();
        this.staff = user.isStaff();
        this.lastLogin = user.getLastLogin();
        this.groups = user.getGroups().stream()
            .map(g -> g.getName())
            .collect(Collectors.toSet());
        this.permissions = user.getGroups().stream()
            .flatMap(g -> g.getPermissions().stream())
            .map(p -> p.getCodename())
            .collect(Collectors.toSet());
    }
}
