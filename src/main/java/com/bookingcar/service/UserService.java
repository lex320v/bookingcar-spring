package com.bookingcar.service;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.user.ChangePasswordDto;
import com.bookingcar.dto.user.UserCreateDto;
import com.bookingcar.dto.user.UserFilterDto;
import com.bookingcar.dto.user.UserReadDto;
import com.bookingcar.dto.user.UserUpdateDto;
import com.bookingcar.entity.enums.MediaItemType;
import com.bookingcar.entity.enums.UserStatus;
import com.bookingcar.exceptions.AccessException;
import com.bookingcar.mapper.UserMapper;
import com.bookingcar.repository.MediaItemRepository;
import com.bookingcar.repository.user.UserRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.bookingcar.entity.enums.Role.ADMIN;
import static com.bookingcar.entity.enums.Role.CLIENT;
import static com.bookingcar.entity.enums.Role.OWNER;
import static com.bookingcar.entity.enums.Role.SUPER_ADMIN;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MediaItemRepository mediaItemRepository;
    private final MediaItemService mediaItemService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    public Page<UserReadDto> findAll(UserFilterDto userFilterDto, PageDto pageDto) {
        return userRepository.findAllByFilterQueryDsl(userFilterDto, pageDto)
                .map(userMapper::userToReadDto);
    }

    public UserReadDto findById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("user with id: " + id + " does not exist"));

        return userMapper.userToReadDto(user);
    }

    @Transactional
    public UserReadDto create(UserCreateDto userCreateDto) {
        var user = userMapper.createDtoToUser(userCreateDto);
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        var savedUser = userRepository.save(user);

        return userMapper.userToReadDto(savedUser);
    }

    @Transactional
    public void update(Long id, UserUpdateDto userUpdateDto) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("user with id: " + id + " does not exist"));

        var updatedUser = userMapper.updateDtoToUser(userUpdateDto, user);
        if (userUpdateDto.getImage() != null && !userUpdateDto.getImage().isEmpty()) {
            var savedAvatar = mediaItemService.create(
                    userUpdateDto.getImage(),
                    MediaItemType.AVATAR,
                    user
            );
            user.setAvatar(savedAvatar);
        }

        userRepository.saveAndFlush(updatedUser);
    }

    @Transactional
    public void editStatus(Long id, UserStatus status, UserPrincipalDto userPrincipal) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("user with id: " + id + " does not exist"));
        if (userPrincipal.getAuthorities().contains(ADMIN)
                && (user.getRole() == ADMIN || user.getRole() == SUPER_ADMIN)) {
            throw new AccessException("you can not change user with id: " + id);
        }

        user.setStatus(status);
        userRepository.saveAndFlush(user);
    }

    @Transactional
    public void delete(Long id, UserPrincipalDto userPrincipal) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("user with id: " + id + " does not exist"));
        var deleteOtherUser = !user.getId().equals(userPrincipal.getId());
        if (deleteOtherUser
                && (userPrincipal.getAuthorities().contains(OWNER) || userPrincipal.getAuthorities().contains(CLIENT))) {
            throw new AccessException("you can not delete other user");
        }
        if (deleteOtherUser
                && !userPrincipal.getAuthorities().contains(SUPER_ADMIN)
                && (user.getRole() == ADMIN || user.getRole() == SUPER_ADMIN)) {
            throw new AccessException("you can not delete other admin");
        }

        userRepository.delete(user);
        userRepository.flush();
    }

    @Transactional
    public void deleteAvatar(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("user with id: " + id + " does not exist"));

        mediaItemRepository.delete(user.getAvatarMediaItem());
        mediaItemRepository.flush();
    }

    @Override
    public UserPrincipalDto loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Failed to retrieve user: " + username));

        return new UserPrincipalDto(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == UserStatus.ACTIVE,
                Collections.singleton(user.getRole())
        );
    }

    public void changePassword(ChangePasswordDto changePasswordDto, UserPrincipalDto userPrincipal) {
        var user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ValidationException("user with id: " + userPrincipal.getId() + " does not exist"));

        var correct = passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword());
        if (!correct) {
            throw new ValidationException("incorrect current password");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.saveAndFlush(user);
    }
}
