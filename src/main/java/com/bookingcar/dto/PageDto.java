package com.bookingcar.dto;

import lombok.Data;
import org.springframework.data.domain.Sort;

/**
 * Data annotation for correct operation of default values
 */
@Data
public class PageDto {
    Integer page = 0;
    Integer size = 10;
    String sortField = "createdAt";
    Sort.Direction sortDirection = Sort.Direction.ASC;
}
