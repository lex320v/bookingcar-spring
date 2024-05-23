package com.bookingcar.controller;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.PageResponse;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.feedback.FeedbackCreateDto;
import com.bookingcar.dto.request.LocalBookingDatesDto;
import com.bookingcar.dto.request.RequestCreateDto;
import com.bookingcar.dto.request.RequestFilterDto;
import com.bookingcar.entity.enums.RequestStatus;
import com.bookingcar.service.CarService;
import com.bookingcar.service.RequestService;
import com.bookingcar.util.Pagination;
import com.bookingcar.validation.RequestStatusValidator;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.bookingcar.entity.enums.RequestStatus.FINISHED;
import static com.bookingcar.entity.enums.RequestStatus.OPEN;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final CarService carService;

    @GetMapping
    public String findAll(Model model,
                          RequestFilterDto requestFilterDto,
                          PageDto pageDto,
                          @AuthenticationPrincipal UserPrincipalDto userPrincipal) {
        pageDto.setSortDirection(Sort.Direction.DESC);
        var result = PageResponse.of(requestService.findAll(requestFilterDto, pageDto, userPrincipal));

        var requestStatusesType = Stream.concat(
                Stream.of((String) null),
                Arrays.stream(RequestStatus.values()).map(String::valueOf)
        ).toList();

        model.addAttribute("requests", result);
        model.addAttribute("filter", requestFilterDto);
        model.addAttribute("requestStatuses", requestStatusesType);
        model.addAttribute("page", pageDto);
        model.addAttribute("sortDirections", Sort.Direction.values());
        model.addAttribute("countPages", Pagination.countPages(result.getMetadata()));

        return "request/requests";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable("id") Long id,
                           Model model,
                           @AuthenticationPrincipal UserPrincipalDto userPrincipal) {
        var requestReadDto = requestService.findById(id, userPrincipal);

        model.addAttribute("request", requestReadDto);
        model.addAttribute("minRating", FeedbackCreateDto.MIN_RATING);
        model.addAttribute("maxRating", FeedbackCreateDto.MAX_RATING);

        return "request/request";
    }

    @GetMapping("/create")
    public String createView(Model model, @NotNull Long carId, LocalBookingDatesDto localBookingDatesDto) {
        var car = carService.findById(carId);

        model.addAttribute("car", car);
        model.addAttribute("localBookingDatesDto", localBookingDatesDto);

        return "request/create";
    }

    @PostMapping
    public String create(@Validated RequestCreateDto requestCreateDto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         @AuthenticationPrincipal UserPrincipalDto userPrincipalDto) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("carId", requestCreateDto.getCarId());
            redirectAttributes.addFlashAttribute("request", requestCreateDto);
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());

            return "redirect:/requests/create";
        }
        var createdRequest = requestService.create(requestCreateDto, userPrincipalDto);

        return "redirect:/requests/" + createdRequest.getId();
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Long id,
                         @RequestStatusValidator(prohibitedValues = {OPEN, FINISHED}) RequestStatus status,
                         @AuthenticationPrincipal UserPrincipalDto userPrincipal) {
        requestService.update(id, status, userPrincipal);

        return "redirect:/requests/" + id;
    }
}
