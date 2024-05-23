package com.bookingcar.service;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.car.CarCreateUpdateDto;
import com.bookingcar.dto.car.CarFilterDto;
import com.bookingcar.dto.car.CarReadDto;
import com.bookingcar.dto.car.CarReadWithStatsDto;
import com.bookingcar.dto.car.ChangePositionDto;
import com.bookingcar.entity.Car;
import com.bookingcar.entity.CarToMediaItem;
import com.bookingcar.entity.CarToMediaItemId;
import com.bookingcar.entity.enums.MediaItemType;
import com.bookingcar.exceptions.AccessException;
import com.bookingcar.mapper.CarMapper;
import com.bookingcar.repository.CarToMediaItemRepository;
import com.bookingcar.repository.MediaItemRepository;
import com.bookingcar.repository.car.CarRepository;
import com.bookingcar.repository.user.UserRepository;
import com.google.common.collect.Streams;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarService {

    private final int MAX_NUMBER_CAR_IMAGES = 4;
    private final int MAX_NUMBER_CAR_VIDEO = 1;

    private final CarRepository carRepository;
    private final MediaItemService mediaItemService;
    private final CarToMediaItemRepository carToMediaItemRepository;
    private final MediaItemRepository mediaItemRepository;
    private final UserRepository userRepository;
    private final CarMapper carMapper;

    public Page<CarReadWithStatsDto> findAll(CarFilterDto carFilterDto,
                                             PageDto pageDto,
                                             UserPrincipalDto userPrincipal) {
        return carRepository.findAllByFilter(carFilterDto, pageDto, userPrincipal);
    }

    public CarReadDto findById(Long id) {
        var car = carRepository.findByIdWithMediaAndOwner(id)
                .orElseThrow(() -> new ValidationException("car with id: " + id + " does not exist"));

        return carMapper.carToReadDto(car);
    }

    @Transactional
    public CarReadDto create(CarCreateUpdateDto carCreateDto, UserPrincipalDto userPrincipal) {
        var car = carMapper.createDtoToCar(carCreateDto);
        car.setOwner(userRepository.getReferenceById(userPrincipal.getId()));
        var savedCar = carRepository.save(car);

        return carMapper.carToReadDto(savedCar);
    }

    @Transactional
    public void update(Long carId, CarCreateUpdateDto carUpdateDto, UserPrincipalDto userPrincipal) {
        var car = carRepository.findById(carId)
                .orElseThrow(() -> new ValidationException("car with id: " + carId + " does not exist"));
        checkPermissionToEdit(car, userPrincipal);

        var updatedCar = carMapper.updateDtoToCar(carUpdateDto, car);

        carRepository.saveAndFlush(updatedCar);
    }

    @Transactional
    public void delete(Long carId, UserPrincipalDto userPrincipal) {
        var car = carRepository.findById(carId)
                .orElseThrow(() -> new ValidationException("car with id: " + carId + " does not exist"));
        checkPermissionToEdit(car, userPrincipal);

        carRepository.delete(car);
        carRepository.flush();
    }

    @Transactional
    public void attachMedia(Long carId, MultipartFile file, UserPrincipalDto userPrincipal) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("empty file");
        }

        var car = carRepository.findByIdWithMediaAndOwner(carId)
                .orElseThrow(() -> new ValidationException("car with id: " + carId + "does not exist"));
        checkPermissionToEdit(car, userPrincipal);

        var attachedImages = car.getCarToMediaItems();
        checkingImagesLimits(attachedImages);

        var savedMedia = mediaItemService.create(
                file,
                MediaItemType.CAR_IMAGE,
                userRepository.getReferenceById(userPrincipal.getId())
        );
        var carToMediaItemId = CarToMediaItemId.builder()
                .carId(car.getId())
                .mediaItemId(savedMedia.getId())
                .build();
        var lastPosition = attachedImages.isEmpty()
                ? 0
                : attachedImages.get(attachedImages.size() - 1).getPosition();
        var carToMediaItem = CarToMediaItem.builder()
                .id(carToMediaItemId)
                .car(car)
                .mediaItem(savedMedia)
                .position(++lastPosition)
                .build();

        carToMediaItemRepository.saveAndFlush(carToMediaItem);
    }

    @Transactional
    public void detachMedia(Long carId, Long mediaItemId, UserPrincipalDto userPrincipal) {
        var car = carRepository.findByIdWithMediaAndOwner(carId)
                .orElseThrow(() -> new ValidationException("car with id: " + carId + "does not exist"));
        checkPermissionToEdit(car, userPrincipal);

        var mediaItem = mediaItemRepository.findById(mediaItemId)
                .orElseThrow(() -> new ValidationException("media item with id: " + mediaItemId + " does not exist"));

        var carToMediaItemsUpdatedPositions = updateCarToMediaPositions(car.getCarToMediaItems(), mediaItem.getId());

        mediaItemRepository.delete(mediaItem);
        mediaItemRepository.flush();
        if (!carToMediaItemsUpdatedPositions.isEmpty()) {
            carToMediaItemRepository.saveAllAndFlush(carToMediaItemsUpdatedPositions);
        }
    }

    @Transactional
    public void changeMediaItemPosition(Long carId,
                                        ChangePositionDto changePositionDto,
                                        UserPrincipalDto userPrincipal) {
        var car = carRepository.findByIdWithMediaAndOwner(carId)
                .orElseThrow(() -> new ValidationException("car with id: " + carId + "does not exist"));
        checkPermissionToEdit(car, userPrincipal);
        var carToMediaItemsSwappedPositions = swapPositions(
                car.getCarToMediaItems(),
                changePositionDto.getMediaItemId(),
                changePositionDto.getPosition()
        );

        carToMediaItemRepository.saveAllAndFlush(carToMediaItemsSwappedPositions);
    }

    private void checkPermissionToEdit(Car car, UserPrincipalDto userPrincipal) {
        if (!Objects.equals(car.getOwner().getId(), userPrincipal.getId())) {
            throw new AccessException("you are not owner of car with id: " + car.getId());
        }
    }

    private void checkingImagesLimits(List<CarToMediaItem> carToMediaItems) {
        var attachedImages = carToMediaItems
                .stream()
                .filter(carToMediaItem -> carToMediaItem.getMediaItem().getType() == MediaItemType.CAR_IMAGE)
                .toList();
        if (attachedImages.size() >= MAX_NUMBER_CAR_IMAGES) {
            throw new ValidationException("reached maximum number of images: " + MAX_NUMBER_CAR_IMAGES);
        }
    }

    private List<CarToMediaItem> updateCarToMediaPositions(List<CarToMediaItem> carToMediaItems,
                                                           Long mediaItemId) {
        return Streams.mapWithIndex(
                carToMediaItems.stream().filter(item -> !item.getMediaItem().getId().equals(mediaItemId)),
                (item, index) -> {
                    item.setPosition((int) ++index);

                    return item;
                }
        ).toList();
    }

    private List<CarToMediaItem> swapPositions(List<CarToMediaItem> carToMediaItems,
                                               Long mediaItemId,
                                               Integer position) {
        var carToMediaItemFrom = carToMediaItems.stream()
                .filter(item -> item.getMediaItem().getId().equals(mediaItemId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("incorrect mediaItemId: " + mediaItemId));

        var carToMediaItemTo = carToMediaItems.stream()
                .filter(item -> item.getPosition().equals(position))
                .findFirst()
                .orElseThrow(() -> new ValidationException("incorrect position: " + position));

        carToMediaItemTo.setPosition(carToMediaItemFrom.getPosition());
        carToMediaItemFrom.setPosition(position);

        return List.of(carToMediaItemTo, carToMediaItemFrom);
    }
}
