package com.bookingcar.service;

import com.bookingcar.dto.mediaItem.MediaItemReadDto;
import com.bookingcar.entity.MediaItem;
import com.bookingcar.entity.User;
import com.bookingcar.entity.enums.MediaItemType;
import com.bookingcar.mapper.MediaItemMapper;
import com.bookingcar.repository.MediaItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaItemService {

    private final MediaItemRepository mediaItemRepository;
    private final ImageService imageService;
    private final MediaItemMapper mediaItemMapper;

    public Optional<MediaItemReadDto> findById(Long id) {
        return mediaItemRepository.findById(id)
                .map(mediaItemMapper::mediaItemToReadDto);
    }

    public Optional<byte[]> getMediaItem(Long id) {
        return mediaItemRepository.findById(id)
                .flatMap(mediaItem -> imageService.get(mediaItem.getLink()));
    }

    @Transactional
    @SneakyThrows
    public MediaItem create(MultipartFile file, MediaItemType mediaItemType, User currentUser) {
        var fileName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
        imageService.upload(fileName, file);

        var mediaItem = MediaItem.builder()
                .type(mediaItemType)
                .mimeType(file.getContentType())
                .link(fileName)
                .previewLink(fileName)
                .uploader(currentUser)
                .build();

        return mediaItemRepository.save(mediaItem);
    }

    @Transactional
    public boolean delete(Long id) {
        return mediaItemRepository.findById(id)
                .map(entity -> {
                    mediaItemRepository.delete(entity);
                    mediaItemRepository.flush();

                    return true;
                })
                .orElse(false);
    }
}
