package com.bookingcar.controller;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.PageResponse;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.car.BookingStatus;
import com.bookingcar.dto.car.CarCreateUpdateDto;
import com.bookingcar.dto.car.CarFilterDto;
import com.bookingcar.dto.car.CarSortFields;
import com.bookingcar.dto.car.ChangePositionDto;
import com.bookingcar.dto.request.LocalBookingDatesDto;
import com.bookingcar.entity.enums.CarType;
import com.bookingcar.service.CarService;
import com.bookingcar.util.Pagination;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.stream.Stream;

@Controller
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public String findAll(Model model,
                          @Validated CarFilterDto carFilterDto,
                          BindingResult bindingResult,
                          LocalBookingDatesDto localBookingDatesDto,
                          RedirectAttributes redirectAttributes,
                          PageDto pageDto,
                          @AuthenticationPrincipal UserPrincipalDto userPrincipalDto) {
        var carTypeValues = Stream.concat(
                Stream.of((String) null),
                Arrays.stream(CarType.values()).map(String::valueOf)
        ).toList();

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("filter", CarFilterDto.builder().build());
            redirectAttributes.addFlashAttribute("carTypes", carTypeValues);
            redirectAttributes.addFlashAttribute("page", pageDto);
            redirectAttributes.addFlashAttribute("bookingStatuses", BookingStatus.values());
            redirectAttributes.addFlashAttribute("sortFields", CarSortFields.values());
            redirectAttributes.addFlashAttribute("sortDirections", Sort.Direction.values());
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());

            return "redirect:/cars";
        }

        var result = PageResponse.of(carService.findAll(carFilterDto, pageDto, userPrincipalDto));

        model.addAttribute("cars", result);
        model.addAttribute("filter", carFilterDto);
        model.addAttribute("carTypes", carTypeValues);
        model.addAttribute("page", pageDto);
        model.addAttribute("bookingStatuses", BookingStatus.values());
        model.addAttribute("sortFields", CarSortFields.values());
        model.addAttribute("sortDirections", Sort.Direction.values());
        model.addAttribute("countPages", Pagination.countPages(result.getMetadata()));
        model.addAttribute("localBookingDatesDto", localBookingDatesDto);

        return "car/cars";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable("id") Long id,
                           Model model,
                           @AuthenticationPrincipal UserPrincipalDto userPrincipal) {
        var car = carService.findById(id);

        model.addAttribute("car", car);
        model.addAttribute("carTypeValues", CarType.values());

        return car.getOwner().getId().equals(userPrincipal.getId())
                ? "car/car-owner"
                : "car/car-client";
    }

    @GetMapping("/create")
    public String createCarView(Model model, @ModelAttribute("car") CarCreateUpdateDto carCreateUpdateDto) {
        model.addAttribute("car", carCreateUpdateDto);
        model.addAttribute("carTypeValues", CarType.values());

        return "car/create";
    }

    @PostMapping
    public String createCar(@Validated CarCreateUpdateDto carCreateDto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal UserPrincipalDto userPrincipalDto) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("car", carCreateDto);
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());

            return "redirect:/cars/create";
        }
        var createdCar = carService.create(carCreateDto, userPrincipalDto);

        return "redirect:/cars/" + createdCar.getId();
    }

    @PostMapping("/{id}/update")
    public String updateCar(@PathVariable("id") Long id,
                            @Validated CarCreateUpdateDto carUpdateDto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal UserPrincipalDto userPrincipalDto) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("car", carUpdateDto);
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());

            return "redirect:/cars/{id}";
        }
        carService.update(id, carUpdateDto, userPrincipalDto);

        return "redirect:/cars/{id}";
    }

    @PostMapping("/{id}/attach-media")
    public String attachMedia(@PathVariable("id") Long id,
                              MultipartFile file,
                              @AuthenticationPrincipal UserPrincipalDto userPrincipalDto) {
        carService.attachMedia(id, file, userPrincipalDto);

        return "redirect:/cars/{id}";
    }

    @PostMapping("/{id}/detach-media")
    public String detachMedia(@PathVariable("id") Long id,
                              Long mediaItemId,
                              @AuthenticationPrincipal UserPrincipalDto userPrincipalDto) {
        carService.detachMedia(id, mediaItemId, userPrincipalDto);

        return "redirect:/cars/{id}";
    }

    @PostMapping("/{id}/change-position")
    public String changePosition(@PathVariable("id") Long id,
                                 @Validated ChangePositionDto changePositionDto,
                                 @AuthenticationPrincipal UserPrincipalDto userPrincipalDto) {
        carService.changeMediaItemPosition(id, changePositionDto, userPrincipalDto);

        return "redirect:/cars/{id}";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, @AuthenticationPrincipal UserPrincipalDto userPrincipalDto) {
        carService.delete(id, userPrincipalDto);

        return "redirect:/cars";
    }
}
