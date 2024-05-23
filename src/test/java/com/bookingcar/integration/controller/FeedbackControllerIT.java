package com.bookingcar.integration.controller;

import com.bookingcar.BaseIntegrationTest;
import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.PageResponse;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.feedback.FeedbackCreateDto;
import com.bookingcar.entity.Car;
import com.bookingcar.entity.Feedback;
import com.bookingcar.entity.Request;
import com.bookingcar.entity.User;
import com.bookingcar.entity.enums.CarType;
import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.RequestStatus;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.entity.enums.UserStatus;
import com.bookingcar.integration.SecurityContextHelper;
import com.bookingcar.repository.car.CarRepository;
import com.bookingcar.repository.feedback.FeedbackRepository;
import com.bookingcar.repository.request.RequestRepository;
import com.bookingcar.repository.user.UserRepository;
import com.bookingcar.service.FeedbackService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static com.bookingcar.dto.feedback.FeedbackCreateDto.Fields.rating;
import static com.bookingcar.dto.feedback.FeedbackCreateDto.Fields.requestId;
import static com.bookingcar.dto.feedback.FeedbackCreateDto.Fields.text;
import static com.bookingcar.entity.enums.Role.CLIENT;
import static com.bookingcar.entity.enums.Role.OWNER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class FeedbackControllerIT extends BaseIntegrationTest {

    private final MockMvc mockMvc;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final RequestRepository requestRepository;
    private final FeedbackService feedbackService;
    private final FeedbackRepository feedbackRepository;
    private final EntityManager entityManager;

    private User savedClient;
    private UserPrincipalDto userPrincipal;

    @BeforeEach
    void init() {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        userPrincipal = securityContextHelper.authenticateUser(Role.CLIENT, securityContext);
        savedClient = userRepository.getReferenceById(userPrincipal.getId());
    }

    @Test
    void create() throws Exception {
        var savedOwner = userRepository.save(buildUser("owner", OWNER));
        var savedCar = carRepository.save(buildCar(savedOwner));
        var savedRequest = requestRepository.save(buildRequest(savedClient, savedCar));

        var feedbackRating = 7;
        var feedbackText = "test";

        mockMvc.perform(
                        post("/feedbacks")
                                .param(requestId, savedRequest.getId().toString())
                                .param(rating, String.valueOf(feedbackRating) )
                                .param(text, feedbackText)
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrlPattern("/requests/*")
                );

        entityManager.clear();
        var feedback = requestRepository.getReferenceById(savedRequest.getId()).getFeedback();

        assertThat(feedback.getRating()).isEqualTo(feedbackRating);
        assertThat(feedback.getText()).isEqualTo(feedbackText);
    }

    @Test
    void update() throws Exception {
        var savedOwner = userRepository.save(buildUser("owner", OWNER));
        var savedCar = carRepository.save(buildCar(savedOwner));
        var savedRequest = requestRepository.save(buildRequest(savedClient, savedCar));

        var feedbackCreateDto = FeedbackCreateDto.builder()
                .requestId(savedRequest.getId())
                .rating(7)
                .text("test")
                .build();
        feedbackService.createOrUpdate(feedbackCreateDto, userPrincipal);

        var updatedRating = 10;
        var updatedText = "updated";
        mockMvc.perform(
                        post("/feedbacks")
                                .param(requestId, feedbackCreateDto.getRequestId().toString())
                                .param(rating, String.valueOf(updatedRating))
                                .param(text, updatedText)
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrlPattern("/requests/*")
                );

        entityManager.clear();
        var feedback = requestRepository.getReferenceById(savedRequest.getId()).getFeedback();

        assertThat(feedback.getRating()).isEqualTo(updatedRating);
        assertThat(feedback.getText()).isEqualTo(updatedText);
    }

    @Test
    void findAll() throws Exception {
        var savedOwner = userRepository.save(buildUser("owner", OWNER));
        var savedCar = carRepository.save(buildCar(savedOwner));
        var client1 = userRepository.save(buildUser("client1", CLIENT));
        var client2 = userRepository.save(buildUser("client2", CLIENT));
        var client3 = userRepository.save(buildUser("client3", CLIENT));
        var savedRequest1 = requestRepository.save(buildRequest(client1, savedCar));
        var savedRequest2 = requestRepository.save(buildRequest(client2, savedCar));
        var savedRequest3 = requestRepository.save(buildRequest(client3, savedCar));

        var feedback1 = feedbackRepository.save(Feedback.builder().rating(7).text("test1").build());
        var feedback2 = feedbackRepository.save(Feedback.builder().rating(8).text("test2").build());
        var feedback3 = feedbackRepository.save(Feedback.builder().rating(9).text("test3").build());
        savedRequest1.setFeedback(feedback1);
        savedRequest2.setFeedback(feedback2);
        savedRequest3.setFeedback(feedback3);
        requestRepository.flush();

        var result = PageResponse.of(feedbackService.findAllByCarId(savedCar.getId(), new PageDto()));

        mockMvc.perform(get("/feedbacks?carId=" + savedCar.getId()))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("feedback/feedbacks"),
                        model().attributeExists("feedbacks"),
                        model().attribute("feedbacks", result)
                );
    }

    private User buildUser(String username, Role role) {
        return User.builder()
                .username(username)
                .firstname("test")
                .lastname("test")
                .password("test")
                .role(role)
                .gender(Gender.MALE)
                .status(UserStatus.ACTIVE)
                .birthDate(LocalDate.of(2001, 1, 1))
                .build();
    }

    private Car buildCar(User owner) {
        return Car.builder()
                .manufacturer("test")
                .model("test")
                .type(CarType.CROSSOVER)
                .active(true)
                .price(100)
                .horsepower(100)
                .year(2010)
                .owner(owner)
                .build();
    }

    private Request buildRequest(User client, Car car) {
        var currentTime = Instant.now();

        return Request.builder()
                .dateTimeFrom(currentTime)
                .dateTimeTo(currentTime.plus(Duration.ofHours(4)))
                .status(RequestStatus.FINISHED)
                .car(car)
                .client(client)
                .build();
    }
}
