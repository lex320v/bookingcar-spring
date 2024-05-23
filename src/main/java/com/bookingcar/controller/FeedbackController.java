package com.bookingcar.controller;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.PageResponse;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.feedback.FeedbackCreateDto;
import com.bookingcar.service.CarService;
import com.bookingcar.service.FeedbackService;
import com.bookingcar.util.Pagination;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final CarService carService;

    @GetMapping
    public String findAll(Model model, @NotNull Long carId, PageDto pageDto) {
        var car = carService.findById(carId);
        var result = PageResponse.of(feedbackService.findAllByCarId(carId, pageDto));

        model.addAttribute("car", car);
        model.addAttribute("feedbacks", result);
        model.addAttribute("page", pageDto);
        model.addAttribute("countPages", Pagination.countPages(result.getMetadata()));

        return "feedback/feedbacks";
    }

    @PostMapping
    public String createOrUpdate(@Validated FeedbackCreateDto feedbackCreateDto,
                                 @AuthenticationPrincipal UserPrincipalDto userPrincipalDto) {
        feedbackService.createOrUpdate(feedbackCreateDto, userPrincipalDto);

        return "redirect:/requests/" + feedbackCreateDto.getRequestId();
    }
}
