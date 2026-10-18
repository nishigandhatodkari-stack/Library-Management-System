package com.library.service.impl;

import com.library.dto.MemberDto;
import com.library.entity.Member;
import com.library.entity.enums.TransactionStatus;
import com.library.exception.BusinessValidationException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookTransactionRepository;
import com.library.repository.MemberRepository;
import com.library.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BookTransactionRepository transactionRepository;

    public MemberServiceImpl(MemberRepository memberRepository, BookTransactionRepository transactionRepository) {
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Member> searchMembers(String keyword) {
        return memberRepository.searchMembers(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Member getMemberByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with Member ID: " + memberId));
    }

    @Override
    public Member saveMember(MemberDto memberDto) {
        String cleanMemberId = memberDto.getMemberId().trim().toUpperCase();
        String cleanEmail = memberDto.getEmail().trim().toLowerCase();

        if (memberRepository.existsByMemberId(cleanMemberId)) {
            throw new BusinessValidationException("Member ID '" + cleanMemberId + "' is already registered.");
        }

        if (memberRepository.existsByEmail(cleanEmail)) {
            throw new BusinessValidationException("Email '" + cleanEmail + "' is already in use by another member.");
        }

        Member member = new Member();
        member.setMemberId(cleanMemberId);
        member.setFullName(memberDto.getFullName().trim());
        member.setEmail(cleanEmail);
        member.setPhone(memberDto.getPhone().trim());
        member.setAddress(memberDto.getAddress() != null ? memberDto.getAddress().trim() : null);
        member.setRegistrationDate(memberDto.getRegistrationDate() != null ? memberDto.getRegistrationDate() : LocalDate.now());
        member.setStatus(memberDto.getStatus() != null ? memberDto.getStatus() : "ACTIVE");

        return memberRepository.save(member);
    }

    @Override
    public Member updateMember(Long id, MemberDto memberDto) {
        Member existingMember = getMemberById(id);

        String cleanMemberId = memberDto.getMemberId().trim().toUpperCase();
        String cleanEmail = memberDto.getEmail().trim().toLowerCase();

        if (memberRepository.existsByMemberIdAndIdNot(cleanMemberId, id)) {
            throw new BusinessValidationException("Another member is already using Member ID '" + cleanMemberId + "'.");
        }

        if (memberRepository.existsByEmailAndIdNot(cleanEmail, id)) {
            throw new BusinessValidationException("Another member is already using Email '" + cleanEmail + "'.");
        }

        existingMember.setMemberId(cleanMemberId);
        existingMember.setFullName(memberDto.getFullName().trim());
        existingMember.setEmail(cleanEmail);
        existingMember.setPhone(memberDto.getPhone().trim());
        existingMember.setAddress(memberDto.getAddress() != null ? memberDto.getAddress().trim() : null);
        if (memberDto.getRegistrationDate() != null) {
            existingMember.setRegistrationDate(memberDto.getRegistrationDate());
        }
        if (memberDto.getStatus() != null) {
            existingMember.setStatus(memberDto.getStatus());
        }

        return memberRepository.save(existingMember);
    }

    @Override
    public void deleteMember(Long id) {
        Member member = getMemberById(id);
        long activeIssues = transactionRepository.countByMemberIdAndStatus(id, TransactionStatus.ISSUED);
        if (activeIssues > 0) {
            throw new BusinessValidationException("Cannot delete member '" + member.getFullName() +
                    "' (" + member.getMemberId() + ") because they currently have " + activeIssues + " active book(s) issued.");
        }
        memberRepository.delete(member);
    }

    @Override
    public MemberDto convertToDto(Member member) {
        MemberDto dto = new MemberDto();
        dto.setId(member.getId());
        dto.setMemberId(member.getMemberId());
        dto.setFullName(member.getFullName());
        dto.setEmail(member.getEmail());
        dto.setPhone(member.getPhone());
        dto.setAddress(member.getAddress());
        dto.setRegistrationDate(member.getRegistrationDate());
        dto.setStatus(member.getStatus());
        return dto;
    }
}
