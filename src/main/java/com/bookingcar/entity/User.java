package com.bookingcar.entity;

import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.entity.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"avatarMediaItem", "cars", "mediaItems", "requests"}, callSuper = true)
@EqualsAndHashCode(of = "username", callSuper = false)
@Entity(name = "users")
public class User extends BaseEntitySoftDelete<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "avatar_media_item_id")
    private MediaItem avatarMediaItem;

    @Builder.Default
    @OneToMany(mappedBy = "uploader")
    private List<MediaItem> mediaItems = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "owner")
    private List<Car> cars = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "client")
    private List<Request> requests = new ArrayList<>();

    public void setAvatar(MediaItem mediaItem) {
        if (mediaItem != null) {
            mediaItem.setUploader(this);
        }
        this.avatarMediaItem = mediaItem;
    }

    public void addMediaItem(MediaItem mediaItem) {
        mediaItems.add(mediaItem);
        mediaItem.setUploader(this);
    }

    public void addCar(Car car) {
        cars.add(car);
        car.setOwner(this);
    }

    public void addRequest(Request request) {
        requests.add(request);
        request.setClient(this);
    }
}
