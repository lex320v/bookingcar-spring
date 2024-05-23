package com.bookingcar.util;

import com.bookingcar.dto.PageResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Pagination {

    public Integer countPages(PageResponse.Metadata metadata) {
        var countPages = (int) Math.ceil((double) metadata.getTotalElements() / metadata.getSize());

        return countPages - 1;
    }

}
