package com.library.service;

import com.library.dto.MemberDto;
import com.library.entity.Member;

import java.util.List;

public interface MemberService {
    List<Member> getAllMembers();
    List<Member> searchMembers(String keyword);
    Member getMemberById(Long id);
    Member getMemberByMemberId(String memberId);
    Member saveMember(MemberDto memberDto);
    Member updateMember(Long id, MemberDto memberDto);
    void deleteMember(Long id);
    MemberDto convertToDto(Member member);
}
