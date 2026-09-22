package com.app.YouthCompass.service.utils;

import com.app.YouthCompass.domain.vo.member.MemberVO;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final MemberVO member; // MemberVO를 통째로 보유

    public CustomUserDetails(MemberVO member) {
        this.member = member;
    }

    @Override
    public Collection getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(member.getMemberRole().name()));
    }

    @Override
    public String getPassword() {
        return member.getMemberPassword();
    }

    @Override
    public String getUsername() {
        return member.getMemberLoginId();
    }

    // 시큐리티 필수 계정 상태 메서드들
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}