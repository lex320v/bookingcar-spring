package com.bookingcar.dto.mediaItem;

import com.bookingcar.entity.enums.MediaItemType;
import lombok.Value;

@Value
public class MediaItemReadDto {
    Long id;
    MediaItemType type;
    String mimeType;
    String previewLink;
    String link;
}
