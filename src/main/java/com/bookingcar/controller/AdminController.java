package com.bookingcar.controller;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.PageResponse;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.user.UserCreateDto;
import com.bookingcar.dto.user.UserFilterDto;
import com.bookingcar.dto.user.UserFilterSortField;
import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.entity.enums.UserStatus;
import com.bookingcar.service.UserService;
import com.bookingcar.util.Pagination;
import com.bookingcar.validation.group.CreateAdmin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.stream.Stream;

@Controller
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/create")
    public String createAdminView(Model model, @ModelAttribute("user") UserCreateDto userCreateDto) {
        model.addAttribute("user", userCreateDto);
        model.addAttribute("genders", Gender.values());

        return "/admin/create";
    }

    @PostMapping
    public String createAdmin(@Validated({Default.class, CreateAdmin.class}) UserCreateDto userCreateDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("user", userCreateDto);
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());

            return "redirect:/admins/create";
        }
        userService.create(userCreateDto);

        return "redirect:/admins/users";
    }

    @GetMapping("/users")
    public String findUsers(Model model,
                            UserFilterDto userFilterDto,
                            PageDto pageDto) {
        var result = PageResponse.of(userService.findAll(userFilterDto, pageDto));

        var roleValues = Stream.concat(
                Stream.of((String) null),
                Arrays.stream(Role.values()).map(String::valueOf)
        ).toList();

        model.addAttribute("users", result);
        model.addAttribute("filter", userFilterDto);
        model.addAttribute("roles", roleValues);
        model.addAttribute("page", pageDto);
        model.addAttribute("sortFields", UserFilterSortField.values());
        model.addAttribute("sortDirections", Sort.Direction.values());
        model.addAttribute("countPages", Pagination.countPages(result.getMetadata()));

        return "admin/users";
    }

    @PostMapping("/users/edit-status/{id}")
    public String editUserStatus(@PathVariable("id") Long id,
                                 @NotNull UserStatus status,
                                 @AuthenticationPrincipal UserPrincipalDto userPrincipal) {
        userService.editStatus(id, status, userPrincipal);

        return "redirect:/admins/users";
    }
}
