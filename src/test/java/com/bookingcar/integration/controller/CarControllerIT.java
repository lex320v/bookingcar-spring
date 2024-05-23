package com.bookingcar.integration.controller;

import com.bookingcar.BaseIntegrationTest;
import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.PageResponse;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.car.CarCreateUpdateDto;
import com.bookingcar.dto.car.CarFilterDto;
import com.bookingcar.dto.car.CarReadDto;
import com.bookingcar.entity.Car;
import com.bookingcar.entity.CarToMediaItem;
import com.bookingcar.entity.CarToMediaItemId;
import com.bookingcar.entity.MediaItem;
import com.bookingcar.entity.User;
import com.bookingcar.entity.enums.CarType;
import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.MediaItemType;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.entity.enums.UserStatus;
import com.bookingcar.integration.SecurityContextHelper;
import com.bookingcar.mapper.UserMapper;
import com.bookingcar.repository.CarToMediaItemRepository;
import com.bookingcar.repository.MediaItemRepository;
import com.bookingcar.repository.car.CarRepository;
import com.bookingcar.repository.user.UserRepository;
import com.bookingcar.service.CarService;
import com.bookingcar.util.TestDataImporter;
import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;

import static com.bookingcar.dto.car.CarCreateUpdateDto.Fields.active;
import static com.bookingcar.dto.car.CarCreateUpdateDto.Fields.horsepower;
import static com.bookingcar.dto.car.CarCreateUpdateDto.Fields.manufacturer;
import static com.bookingcar.dto.car.CarCreateUpdateDto.Fields.model;
import static com.bookingcar.dto.car.CarCreateUpdateDto.Fields.price;
import static com.bookingcar.dto.car.CarCreateUpdateDto.Fields.type;
import static com.bookingcar.dto.car.CarCreateUpdateDto.Fields.year;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class CarControllerIT extends BaseIntegrationTest {

    private final MockMvc mockMvc;
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final CarToMediaItemRepository carToMediaItemRepository;
    private final MediaItemRepository mediaItemRepository;
    private final CarService carService;
    private final UserMapper userMapper;
    private final SecurityContextHelper securityContextHelper;

    private User savedOwner;
    private UserPrincipalDto userPrincipal;

    @BeforeEach
    void init() {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        userPrincipal = securityContextHelper.authenticateUser(Role.OWNER, securityContext);
        savedOwner = userRepository.getReferenceById(userPrincipal.getId());
    }

    @Test
    void createCarView() throws Exception {
        mockMvc.perform(get("/cars/create"))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("car/create")
                );
    }

    @Test
    void findById() throws Exception {
        var createdCar = carRepository.save(buildCar(savedOwner));

        var expectedCarReadDto = new CarReadDto(
                createdCar.getId(),
                createdCar.getManufacturer(),
                createdCar.getModel(),
                createdCar.getYear(),
                createdCar.getHorsepower(),
                createdCar.getPrice(),
                createdCar.isActive(),
                createdCar.getType(),
                userMapper.userToReadDto(createdCar.getOwner()),
                new ArrayList<>()
        );

        mockMvc.perform(
                        get("/cars/" + createdCar.getId())
                )
                .andExpectAll(
                        status().is2xxSuccessful(),
                        model().attribute("car", expectedCarReadDto)
                );
    }

    @Test
    void findAll() throws Exception {
        TestDataImporter.importData(entityManager);

        var filter = CarFilterDto.builder().build();
        var result = PageResponse.of(carService.findAll(filter, new PageDto(), userPrincipal));

        mockMvc.perform(get("/cars"))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("car/cars"),
                        model().attributeExists("cars"),
                        model().attribute("cars", result)
                );
    }

    @Test
    void create() throws Exception {
        var carCreateDto = new CarCreateUpdateDto(
                "test",
                "test",
                2005,
                100,
                200,
                true,
                CarType.CROSSOVER
        );

        var mvcResult = mockMvc.perform(
                        post("/cars")
                                .param(manufacturer, carCreateDto.getManufacturer())
                                .param(model, carCreateDto.getModel())
                                .param(year, carCreateDto.getYear().toString())
                                .param(horsepower, carCreateDto.getHorsepower().toString())
                                .param(price, carCreateDto.getPrice().toString())
                                .param(active, String.valueOf(carCreateDto.isActive()))
                                .param(type, carCreateDto.getType().toString())
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrlPattern("/cars/*")
                )
                .andReturn();

        var splitRedirectionUrl = mvcResult.getResponse().getHeader("Location").split("/");
        Long carId = Long.parseLong(splitRedirectionUrl[2]);

        var expectedReadDto = new CarReadDto(
                carId,
                carCreateDto.getManufacturer(),
                carCreateDto.getModel(),
                carCreateDto.getYear(),
                carCreateDto.getHorsepower(),
                carCreateDto.getPrice(),
                carCreateDto.isActive(),
                carCreateDto.getType(),
                userMapper.userToReadDto(savedOwner),
                new ArrayList<>()
        );

        var createdCar = carService.findById(carId);

        assertThat(createdCar).isEqualTo(expectedReadDto);
    }

    @Test
    void update() throws Exception {
        var createdCar = carRepository.save(buildCar(savedOwner));

        var carUpdateDto = new CarCreateUpdateDto(
                "updated",
                "updated",
                2010,
                100,
                100,
                true,
                CarType.CROSSOVER
        );

        mockMvc.perform(
                        post("/cars/" + createdCar.getId() + "/update")
                                .param(manufacturer, carUpdateDto.getManufacturer())
                                .param(model, carUpdateDto.getModel())
                                .param(year, carUpdateDto.getYear().toString())
                                .param(horsepower, carUpdateDto.getHorsepower().toString())
                                .param(price, carUpdateDto.getPrice().toString())
                                .param(active, String.valueOf(carUpdateDto.isActive()))
                                .param(type, carUpdateDto.getType().toString())
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrlPattern("/cars/*")
                );

        var expectedCarReadDto = new CarReadDto(
                createdCar.getId(),
                carUpdateDto.getManufacturer(),
                carUpdateDto.getModel(),
                carUpdateDto.getYear(),
                carUpdateDto.getHorsepower(),
                carUpdateDto.getPrice(),
                carUpdateDto.isActive(),
                carUpdateDto.getType(),
                userMapper.userToReadDto(savedOwner),
                new ArrayList<>()
        );
        var updatedCar = carService.findById(createdCar.getId());

        assertThat(expectedCarReadDto).isEqualTo(updatedCar);
    }

    @Test
    void attachMedia() throws Exception {
        var createdCar = carRepository.save(buildCar(savedOwner));

        mockMvc.perform(
                        multipart("/cars/" + createdCar.getId() + "/attach-media")
                                .file(getMultipartFile())
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrlPattern("/cars/*")
                );

        entityManager.detach(createdCar);
        var carWithMedia = carRepository.getReferenceById(createdCar.getId());

        assertThat(carWithMedia.getCarToMediaItems().size()).isEqualTo(1);
    }

    @Test
    void detachMedia() throws Exception {
        var createdCar = carRepository.save(buildCar(savedOwner));
        var savedMediaItem = mediaItemRepository.save(buildMediaItem(savedOwner));
        carToMediaItemRepository.saveAndFlush(buildCarToMediaItem(createdCar, savedMediaItem));

        mockMvc.perform(
                        post("/cars/" + createdCar.getId() + "/detach-media")
                                .param("mediaItemId", savedMediaItem.getId().toString())
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrlPattern("/cars/*")
                );

        entityManager.detach(createdCar);
        var carWithoutMedia = carRepository.getReferenceById(createdCar.getId());

        assertThat(carWithoutMedia.getCarToMediaItems().size()).isEqualTo(0);
    }

    @Test
    void changePositionMedia() throws Exception {
        var createdCar = carRepository.save(buildCar(savedOwner));
        var file = getMultipartFile();
        carService.attachMedia(createdCar.getId(), file, userPrincipal);

        entityManager.clear();
        carService.attachMedia(createdCar.getId(), file, userPrincipal);

        entityManager.clear();
        carService.attachMedia(createdCar.getId(), file, userPrincipal);

        entityManager.clear();
        var mediaItems = carService.findById(createdCar.getId()).getCarToMediaItems();

        var firstMedia = mediaItems.get(0);
        var secondMedia = mediaItems.get(1);
        var thirdMedia = mediaItems.get(2);
        var newPosition = 1;

        mockMvc.perform(
                        post("/cars/" + createdCar.getId() + "/change-position")
                                .param("mediaItemId", secondMedia.getMediaItem().getId().toString())
                                .param("position", String.valueOf(newPosition))
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrlPattern("/cars/*")
                );

        entityManager.clear();
        var updatedMediaItems = carService.findById(createdCar.getId()).getCarToMediaItems();

        var updatedFirstMedia = updatedMediaItems.get(0);
        var updatedSecondMedia = updatedMediaItems.get(1);
        var updatedThirdMedia = updatedMediaItems.get(2);

        assertThat(updatedFirstMedia.getMediaItem().getId()).isEqualTo(secondMedia.getMediaItem().getId());
        assertThat(updatedSecondMedia.getMediaItem().getId()).isEqualTo(firstMedia.getMediaItem().getId());
        assertThat(updatedThirdMedia.getMediaItem().getId()).isEqualTo(thirdMedia.getMediaItem().getId());

    }

    @Test
    void delete() throws Exception {
        var createdCar = carRepository.save(buildCar(savedOwner));

        mockMvc.perform(
                        post("/cars/" + createdCar.getId() + "/delete")
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/cars")
                );


        var exception = assertThrows(ValidationException.class, () -> carService.findById(createdCar.getId()));
        assertEquals(exception.getMessage(), "car with id: " + createdCar.getId() + " does not exist");
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

    private MockMultipartFile getMultipartFile() throws IOException {
        var fileBytes = Files.readAllBytes(Path.of("src", "test", "resources", "test-image.png"));
        return new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                fileBytes);
    }

    private MediaItem buildMediaItem(User uploader) {
        return MediaItem.builder()
                .mimeType("test")
                .link("test")
                .previewLink("test")
                .uploader(uploader)
                .type(MediaItemType.CAR_IMAGE)
                .build();
    }

    private CarToMediaItem buildCarToMediaItem(Car car, MediaItem mediaItem) {
        var carToMediaItemId = CarToMediaItemId.builder()
                .mediaItemId(mediaItem.getId())
                .carId(car.getId())
                .build();
        return CarToMediaItem.builder()
                .id(carToMediaItemId)
                .car(car)
                .mediaItem(mediaItem)
                .position(1)
                .build();
    }
}
