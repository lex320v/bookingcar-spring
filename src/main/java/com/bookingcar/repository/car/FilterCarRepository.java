package com.bookingcar.repository.car;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.car.CarFilterDto;
import com.bookingcar.dto.car.CarReadWithStatsDto;
import org.springframework.data.domain.PageImpl;

public interface FilterCarRepository {

    PageImpl<CarReadWithStatsDto> findAllByFilter(CarFilterDto carFilterDto,
                                                  PageDto pageDto,
                                                  UserPrincipalDto userPrincipal);
}
