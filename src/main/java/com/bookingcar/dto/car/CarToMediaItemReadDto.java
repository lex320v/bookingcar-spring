package com.bookingcar.dto.car;

import com.bookingcar.dto.mediaItem.MediaItemReadDto;
import lombok.Value;

@Value
public class CarToMediaItemReadDto {
    MediaItemReadDto mediaItem;
    Integer position;
}
