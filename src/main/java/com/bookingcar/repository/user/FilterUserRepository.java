package com.bookingcar.repository.user;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.user.UserFilterDto;
import com.bookingcar.entity.User;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface FilterUserRepository {

    List<User> findAllByFilterCriteria(UserFilterDto userFilterDto);

    List<User> findAllByFilterQueryDsl(UserFilterDto userFilterDto);

    PageImpl<User> findAllByFilterQueryDsl(UserFilterDto userFilterDto, PageDto pageDto);
}
