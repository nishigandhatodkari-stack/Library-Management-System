package com.library.repository;

import com.library.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberId(String memberId);

    Optional<Member> findByEmail(String email);

    boolean existsByMemberId(String memberId);

    boolean existsByMemberIdAndIdNot(String memberId, Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT m FROM Member m WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(m.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.memberId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY m.fullName ASC")
    List<Member> searchMembers(@Param("keyword") String keyword);
}
