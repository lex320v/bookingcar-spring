package com.bookingcar.integration.controller;


import com.bookingcar.BaseIntegrationTest;
import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.PageResponse;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.request.RequestCreateDto;
import com.bookingcar.dto.request.RequestFilterDto;
import com.bookingcar.entity.Car;
import com.bookingcar.entity.Request;
import com.bookingcar.entity.User;
import com.bookingcar.entity.enums.CarType;
import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.RequestStatus;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.entity.enums.UserStatus;
import com.bookingcar.integration.SecurityContextHelper;
import com.bookingcar.mapper.RequestMapper;
import com.bookingcar.repository.car.CarRepository;
import com.bookingcar.repository.request.RequestRepository;
import com.bookingcar.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static com.bookingcar.dto.request.RequestCreateDto.Fields.carId;
import static com.bookingcar.dto.request.RequestCreateDto.Fields.dateTimeFrom;
import static com.bookingcar.dto.request.RequestCreateDto.Fields.dateTimeTo;
import static com.bookingcar.entity.enums.Role.OWNER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class RequestControllerIT extends BaseIntegrationTest {

    private final MockMvc mockMvc;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final SecurityContextHelper securityContextHelper;
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
    void createRequestView() throws Exception {
        var savedOwner = userRepository.save(buildUser("owner", OWNER));
        var savedCar = carRepository.save(buildCar(savedOwner));

        var timeFrom = Instant.now();
        var timeTo = timeFrom.plus(Duration.ofHours(4));

        mockMvc.perform(
                        get("/requests/create?carId=" + savedCar.getId()
                                + "&dateTimeFrom=" + timeFrom
                                + "&dateTimeTo=" + timeTo)
                )
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("request/create")
                );
    }

    @Test
    void findAllClient() throws Exception {
        var savedOwner = userRepository.save(buildUser("owner", OWNER));
        var savedCar = carRepository.save(buildCar(savedOwner));

        var request1 = buildRequest(savedClient, savedCar);
        var request2 = buildRequest(savedClient, savedCar);

        requestRepository.save(request1);
        requestRepository.save(request2);

        var filter = RequestFilterDto.builder().build();
        var pageDto = new PageDto();
        pageDto.setSortDirection(Sort.Direction.DESC);
        var result = PageResponse.of(requestRepository.findAllWithCar(filter, pageDto, userPrincipal));

        mockMvc.perform(
                        get("/requests")
                )
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("request/requests"),
                        model().attribute("requests", result)
                );
    }

    @Test
    void findAllOwner() throws Exception {
        var savedOwner = userRepository.save(buildUser("owner", OWNER));
        var savedCar1 = carRepository.save(buildCar(savedOwner));
        var savedCar2 = carRepository.save(buildCar(savedOwner));

        var request1 = buildRequest(savedClient, savedCar1);
        var request2 = buildRequest(savedClient, savedCar2);

        requestRepository.save(request1);
        requestRepository.save(request2);

        var filter = RequestFilterDto.builder().build();
        var pageDto = new PageDto();
        pageDto.setSortDirection(Sort.Direction.DESC);
        var result = PageResponse.of(requestRepository.findAllWithCar(filter, pageDto, userPrincipal));

        mockMvc.perform(
                        get("/requests")
                )
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("request/requests"),
                        model().attribute("requests", result)
                );
    }

    @Test
    void findById() throws Exception {
        var savedOwner = userRepository.save(buildUser("owner", OWNER));
        var savedCar = carRepository.save(buildCar(savedOwner));
        var currentDateTime = Instant.now();

        var request = Request.builder()
                .dateTimeFrom(currentDateTime)
                .dateTimeTo(currentDateTime.plus(Duration.ofHours(4)))
                .status(RequestStatus.OPEN)
                .car(savedCar)
                .client(savedClient)
                .build();

        var createdRequest = requestRepository.save(request);

        var expectedRequestReadDto = requestMapper.requestToReadDto(request);

        mockMvc.perform(get("/requests/" + createdRequest.getId()))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        model().attribute("request", expectedRequestReadDto)
                );
    }

    @Test
    void create() throws Exception {
        var savedOwner = userRepository.save(buildUser("owner", OWNER));
        var savedCar = carRepository.save(buildCar(savedOwner));

        var currentDate = Instant.now();

        var requestCreateDto = new RequestCreateDto(
                currentDate,
                currentDate.plus(Duration.ofHours(4)),
                savedCar.getId()
        );

        var mvcResult = mockMvc.perform(
                        post("/requests")
                                .param(dateTimeFrom, requestCreateDto.getDateTimeFrom().toString())
                                .param(dateTimeTo, requestCreateDto.getDateTimeTo().toString())
                                .param(carId, savedCar.getId().toString())
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrlPattern("/requests/*")
                )
                .andReturn();

        var splitRedirectionUrl = mvcResult.getResponse().getHeader("Location").split("/");
        Long requestId = Long.parseLong(splitRedirectionUrl[2]);

        var createdRequest = requestRepository.findById(requestId);

        assertTrue(createdRequest.isPresent());
        assertThat(createdRequest.get().getDateTimeFrom()).isEqualTo(requestCreateDto.getDateTimeFrom());
        assertThat(createdRequest.get().getDateTimeTo()).isEqualTo(requestCreateDto.getDateTimeTo());
        assertThat(createdRequest.get().getStatus()).isEqualTo(RequestStatus.OPEN);
    }

    @Test
    void acceptRequest() throws Exception {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        var savedOwner = userRepository.save(buildUser("owner", OWNER));
        securityContextHelper.authenticateUser(savedOwner, securityContext);

        var savedCar = carRepository.save(buildCar(savedOwner));
        var request = buildRequest(savedClient, savedCar);
        var savedRequest = requestRepository.save(request);

        mockMvc.perform(
                        post("/requests/" + savedRequest.getId() + "/update")
                                .param("status", RequestStatus.PROCESSING.toString())
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrlPattern("/requests/*")
                );


        entityManager.detach(savedRequest);
        var canceledRequest = requestRepository.getReferenceById(savedRequest.getId());

        assertThat(canceledRequest.getStatus()).isEqualTo(RequestStatus.PROCESSING);
    }

    @Test
    void cancelRequest() throws Exception {
        var savedOwner = userRepository.save(buildUser("owner", OWNER));
        var savedCar = carRepository.save(buildCar(savedOwner));
        var savedRequest = requestRepository.save(buildRequest(savedClient, savedCar));

        mockMvc.perform(
                        post("/requests/" + savedRequest.getId() + "/update")
                                .param("status", RequestStatus.CANCELED.toString())
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrlPattern("/requests/*")
                );


        entityManager.detach(savedRequest);
        var canceledRequest = requestRepository.getReferenceById(savedRequest.getId());

        assertThat(canceledRequest.getStatus()).isEqualTo(RequestStatus.CANCELED);
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
                .status(RequestStatus.OPEN)
                .car(car)
                .client(client)
                .build();
    }
}
