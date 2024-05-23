package com.bookingcar.service;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.request.RequestCreateDto;
import com.bookingcar.dto.request.RequestFilterDto;
import com.bookingcar.dto.request.RequestReadDto;
import com.bookingcar.entity.Request;
import com.bookingcar.entity.enums.RequestStatus;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.exceptions.AccessException;
import com.bookingcar.mapper.RequestMapper;
import com.bookingcar.repository.car.CarRepository;
import com.bookingcar.repository.request.RequestRepository;
import com.bookingcar.repository.user.UserRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final RequestMapper requestMapper;

    public RequestReadDto findById(Long requestId, UserPrincipalDto userPrincipal) {
        var request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ValidationException("request with id: " + requestId + " does not exist"));
        checkPermissionToFindById(request, userPrincipal);

        return requestMapper.requestToReadDto(request);
    }

    public Page<RequestReadDto> findAll(RequestFilterDto requestFilterDto, PageDto pageDto, UserPrincipalDto userPrincipal) {
        return requestRepository.findAllWithCar(requestFilterDto, pageDto, userPrincipal);
    }

    @Transactional
    public RequestReadDto create(RequestCreateDto requestCreateDto, UserPrincipalDto userPrincipalDto) {
        var car = carRepository.findById(requestCreateDto.getCarId())
                .orElseThrow(() -> new ValidationException("car with id: " + requestCreateDto.getCarId() + " does not exist"));

        var activeRequest = requestRepository.findActiveRequestByCarId(
                requestCreateDto.getCarId(),
                requestCreateDto.getDateTimeFrom(),
                requestCreateDto.getDateTimeTo(),
                List.of(RequestStatus.OPEN, RequestStatus.PROCESSING)
        );
        if (activeRequest.isPresent()) {
            throw new ValidationException("car with id: " + requestCreateDto.getCarId() + " booked for selected dates");
        }

        var currentUser = userRepository.getReferenceById(userPrincipalDto.getId());

        var request = requestMapper.createDtoToRequest(requestCreateDto);
        request.setStatus(RequestStatus.OPEN);
        request.setClient(currentUser);
        request.setCar(car);
        var savedRequest = requestRepository.save(request);

        return requestMapper.requestToReadDto(savedRequest);
    }

    @Transactional
    public void update(Long requestId, RequestStatus status, UserPrincipalDto userPrincipal) {
        var request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ValidationException("request with id: " + requestId + " does not exist"));
        checkPermissionToEdit(request, status, userPrincipal);

        request.setStatus(status);
        requestRepository.saveAndFlush(request);
    }

    private void checkPermissionToFindById(Request request, UserPrincipalDto userPrincipal) {
        var creatorRequest = request.getClient().getId().equals(userPrincipal.getId());
        var ownerOfRequestCar = request.getCar().getOwner().getId().equals(userPrincipal.getId());

        if (userPrincipal.getAuthorities().contains(Role.CLIENT) && !creatorRequest) {
            throw new AccessException("you are not creator of request with id: " + request.getId());
        }
        if (userPrincipal.getAuthorities().contains(Role.OWNER) && !ownerOfRequestCar) {
            throw new AccessException("you are not owner of car with id: " + request.getCar().getId());
        }
    }

    private void checkPermissionToEdit(Request request, RequestStatus status, UserPrincipalDto userPrincipal) {
        var car = request.getCar();
        var prohibitRequestStatus =
                request.getStatus() == RequestStatus.FINISHED ||
                        request.getStatus() == RequestStatus.CANCELED ||
                        request.getStatus() == RequestStatus.REJECTED;
        if (prohibitRequestStatus) {
            throw new AccessException("prohibited to change status: " + request.getStatus());
        }

        var changeToProcessingOrRejected = status == RequestStatus.PROCESSING || status == RequestStatus.REJECTED;
        if (changeToProcessingOrRejected && userPrincipal.getAuthorities().contains(Role.CLIENT)) {
            throw new AccessException("only owner can set status: " + status);
        }
        if (changeToProcessingOrRejected && !Objects.equals(car.getOwner().getId(), userPrincipal.getId())) {
            throw new AccessException("you are not owner of car with id: " + car.getId());
        }
        var changeToCancelOrFinished = status == RequestStatus.CANCELED || status == RequestStatus.FINISHED;
        if (changeToCancelOrFinished && userPrincipal.getAuthorities().contains(Role.OWNER)) {
            throw new ValidationException("only client can set status: " + status);
        }
    }
}
