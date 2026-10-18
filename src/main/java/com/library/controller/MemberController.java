package com.library.controller;

import com.library.dto.MemberDto;
import com.library.entity.Member;
import com.library.exception.BusinessValidationException;
import com.library.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public String listMembers(@RequestParam(value = "keyword", required = false) String keyword,
                              Model model) {
        List<Member> members = memberService.searchMembers(keyword);

        model.addAttribute("members", members);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activePage", "members");

        return "members/list";
    }

    @GetMapping("/new")
    public String showAddMemberForm(Model model) {
        if (!model.containsAttribute("memberDto")) {
            model.addAttribute("memberDto", new MemberDto());
        }
        model.addAttribute("isEdit", false);
        model.addAttribute("activePage", "members");
        return "members/form";
    }

    @PostMapping("/save")
    public String saveMember(@Valid @ModelAttribute("memberDto") MemberDto memberDto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("activePage", "members");
            return "members/form";
        }

        try {
            memberService.saveMember(memberDto);
            redirectAttributes.addFlashAttribute("successMessage", "Member '" + memberDto.getFullName() + "' (" + memberDto.getMemberId() + ") was successfully registered!");
            return "redirect:/members";
        } catch (BusinessValidationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("activePage", "members");
            return "members/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditMemberForm(@PathVariable("id") Long id, Model model) {
        Member member = memberService.getMemberById(id);
        if (!model.containsAttribute("memberDto")) {
            model.addAttribute("memberDto", memberService.convertToDto(member));
        }
        model.addAttribute("memberId", id);
        model.addAttribute("isEdit", true);
        model.addAttribute("activePage", "members");
        return "members/form";
    }

    @PostMapping("/edit/{id}")
    public String updateMember(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("memberDto") MemberDto memberDto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("memberId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("activePage", "members");
            return "members/form";
        }

        try {
            memberService.updateMember(id, memberDto);
            redirectAttributes.addFlashAttribute("successMessage", "Member details for '" + memberDto.getFullName() + "' updated successfully!");
            return "redirect:/members";
        } catch (BusinessValidationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("memberId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("activePage", "members");
            return "members/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteMember(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Member member = memberService.getMemberById(id);
            memberService.deleteMember(id);
            redirectAttributes.addFlashAttribute("successMessage", "Member '" + member.getFullName() + "' was successfully deleted.");
        } catch (BusinessValidationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the member.");
        }
        return "redirect:/members";
    }
}
