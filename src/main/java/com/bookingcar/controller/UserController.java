package com.bookingcar.controller;

import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.user.ChangePasswordDto;
import com.bookingcar.dto.user.UserCreateDto;
import com.bookingcar.dto.user.UserUpdateDto;
import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.service.UserService;
import com.bookingcar.validation.group.CreateUser;
import jakarta.validation.ValidationException;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

import static com.bookingcar.entity.enums.Role.ADMIN;
import static com.bookingcar.entity.enums.Role.CLIENT;
import static com.bookingcar.entity.enums.Role.OWNER;
import static com.bookingcar.entity.enums.Role.SUPER_ADMIN;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal UserPrincipalDto userPrincipal) {
        var userReadDto = userService.findById(userPrincipal.getId());
        model.addAttribute("user", userReadDto);
        model.addAttribute("genders", Gender.values());

        return "user/profile";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable("id") Long id, Model model) {
        var userReadDto = userService.findById(id);
        model.addAttribute("user", userReadDto);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("roles", Role.values());

        return "user/user";
    }

    @GetMapping("/registration")
    public String registration(Model model, @ModelAttribute("user") UserCreateDto userCreateDto) {
        model.addAttribute("user", userCreateDto);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("roles", List.of(OWNER, CLIENT));

        return "user/registration";
    }

    @PostMapping("/sign-up")
    public String createUser(@Validated({Default.class, CreateUser.class}) UserCreateDto userCreateDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("user", userCreateDto);
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());

            return "redirect:/users/registration";
        }
        userService.create(userCreateDto);

        return "redirect:/login";
    }

    @PostMapping("/update")
    public String update(@Validated UserUpdateDto userUpdateDto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         @AuthenticationPrincipal UserPrincipalDto userPrincipal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("user", userUpdateDto);
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());

            return "redirect:/users/profile";
        }
        userService.update(userPrincipal.getId(), userUpdateDto);

        return "redirect:/users/profile";

    }

    @PostMapping("/{id}/avatar/delete")
    public String deleteAvatar(@PathVariable("id") Long id) {
        userService.deleteAvatar(id);

        return "redirect:/users/profile";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, @AuthenticationPrincipal UserPrincipalDto userPrincipal) {
        userService.delete(id, userPrincipal);

        if (userPrincipal.getAuthorities().contains(SUPER_ADMIN) || userPrincipal.getAuthorities().contains(ADMIN)) {
            return "redirect:/admins/users";
        }

        return "redirect:/login";
    }

    @PostMapping("/change-password")
    public String changePassword(@Validated ChangePasswordDto changePasswordDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal UserPrincipalDto userPrincipal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());

            return "redirect:/users/profile";
        }
        try {
            userService.changePassword(changePasswordDto, userPrincipal);
        } catch (ValidationException exception) {
            redirectAttributes.addFlashAttribute("passwordError", exception.getMessage());
        }


        return "redirect:/users/profile";
    }
}
