package com.bookingcar.rest;

import com.bookingcar.dto.mediaItem.MediaItemReadDto;
import com.bookingcar.service.MediaItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
@RequestMapping("/api/v1/media-items")
@RequiredArgsConstructor
public class MediaItemRestController {

    private final MediaItemService mediaItemService;

    @GetMapping(value = "/file/{id}", produces = APPLICATION_OCTET_STREAM_VALUE)
    public byte[] getMediaById(@PathVariable("id") Long id) {
        return mediaItemService.getMediaItem(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public MediaItemReadDto findById(@PathVariable("id") Long id) {
        return mediaItemService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        if (!mediaItemService.delete(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
