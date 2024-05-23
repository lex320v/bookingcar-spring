package com.bookingcar.dto.mediaItem;

import com.bookingcar.entity.enums.MediaItemType;
import lombok.Value;

@Value
public class MediaItemCreateDto {
    MediaItemType type;
    String mimeType;
    String previewLink;
    String link;
}
