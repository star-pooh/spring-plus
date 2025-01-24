package org.example.expert.domain.common.dto;

import lombok.Getter;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class AuthUser extends User {

    private final String email;

    public AuthUser(Long id, String password, String email, Collection<? extends GrantedAuthority> authorities) {
        super(String.valueOf(id), password, authorities);
        this.email = email;
    }

    public UserRole getUserRole() {
        return this.getAuthorities().stream()
                .map(authority -> UserRole.valueOf(authority.getAuthority()))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("유효하지 않은 권한입니다."));
    }
}
