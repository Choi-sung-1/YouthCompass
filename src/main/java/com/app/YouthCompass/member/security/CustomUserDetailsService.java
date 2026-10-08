package com.app.YouthCompass.member.security;


import com.app.YouthCompass.member.domain.MemberVO;
import com.app.YouthCompass.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String memberLoginId) throws UsernameNotFoundException {
        MemberVO member = memberMapper.selectMemberByMemberLoginId(memberLoginId).orElseThrow(()->new UsernameNotFoundException("사용자를"+memberLoginId+"를 찾을 수 없습니다."));
        return new CustomUserDetails(member);
    }
}
