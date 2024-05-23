package com.bookingcar.repository.request;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.request.RequestFilterDto;
import com.bookingcar.dto.request.RequestReadDto;
import org.springframework.data.domain.PageImpl;

public interface FilterRequestRepository {

    PageImpl<RequestReadDto> findAllWithCar(RequestFilterDto requestFilterDto,
                                            PageDto pageDto,
                                            UserPrincipalDto userPrincipal);
}
