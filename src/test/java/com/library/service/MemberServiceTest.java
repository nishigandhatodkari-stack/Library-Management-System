package com.library.service;

import com.library.dto.MemberDto;
import com.library.entity.Member;
import com.library.entity.enums.TransactionStatus;
import com.library.exception.BusinessValidationException;
import com.library.repository.BookTransactionRepository;
import com.library.repository.MemberRepository;
import com.library.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookTransactionRepository transactionRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private MemberDto sampleMemberDto;
    private Member sampleMember;

    @BeforeEach
    void setUp() {
        sampleMemberDto = new MemberDto();
        sampleMemberDto.setMemberId("MEM-1001");
        sampleMemberDto.setFullName("Aarav Sharma");
        sampleMemberDto.setEmail("aarav@example.com");
        sampleMemberDto.setPhone("+91 9876543210");
        sampleMemberDto.setAddress("Delhi");
        sampleMemberDto.setRegistrationDate(LocalDate.now());

        sampleMember = new Member(
                "MEM-1001",
                "Aarav Sharma",
                "aarav@example.com",
                "+91 9876543210",
                "Delhi",
                LocalDate.now()
        );
        sampleMember.setId(1L);
    }

    @Test
    @DisplayName("Should successfully register member when ID and email are unique")
    void testSaveMember_Success() {
        when(memberRepository.existsByMemberId("MEM-1001")).thenReturn(false);
        when(memberRepository.existsByEmail("aarav@example.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(sampleMember);

        Member saved = memberService.saveMember(sampleMemberDto);

        assertNotNull(saved);
        assertEquals("Aarav Sharma", saved.getFullName());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw BusinessValidationException when member ID already exists")
    void testSaveMember_DuplicateMemberId_ThrowsException() {
        when(memberRepository.existsByMemberId("MEM-1001")).thenReturn(true);

        BusinessValidationException ex = assertThrows(
                BusinessValidationException.class,
                () -> memberService.saveMember(sampleMemberDto)
        );

        assertTrue(ex.getMessage().contains("already registered"));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw BusinessValidationException when email already exists")
    void testSaveMember_DuplicateEmail_ThrowsException() {
        when(memberRepository.existsByMemberId("MEM-1001")).thenReturn(false);
        when(memberRepository.existsByEmail("aarav@example.com")).thenReturn(true);

        BusinessValidationException ex = assertThrows(
                BusinessValidationException.class,
                () -> memberService.saveMember(sampleMemberDto)
        );

        assertTrue(ex.getMessage().contains("already in use"));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should prevent deleting member who has active issued books")
    void testDeleteMember_WithActiveLoans_ThrowsException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(transactionRepository.countByMemberIdAndStatus(1L, TransactionStatus.ISSUED)).thenReturn(1L);

        BusinessValidationException ex = assertThrows(
                BusinessValidationException.class,
                () -> memberService.deleteMember(1L)
        );

        assertTrue(ex.getMessage().contains("active book(s) issued"));
        verify(memberRepository, never()).delete(any(Member.class));
    }
}
