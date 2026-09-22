package com.app.YouthCompass.service.utils;


import com.app.YouthCompass.domain.vo.member.MemberVO;
import com.app.YouthCompass.repository.member.MemberDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberDAO memberDAO;

    @Override
    public UserDetails loadUserByUsername(String memberLoginId) throws UsernameNotFoundException {
        MemberVO member = memberDAO.findMember(memberLoginId).orElseThrow(()->new UsernameNotFoundException("사용자를"+memberLoginId+"를 찾을 수 없습니다."));
        return new CustomUserDetails(member);
    }
}
