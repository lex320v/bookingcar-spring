package com.bookingcar.entity;

import com.bookingcar.entity.enums.CarType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"owner", "carToMediaItems", "requests"}, callSuper = true)
@EqualsAndHashCode(exclude = {"owner","carToMediaItems", "requests"}, callSuper = true)
@Entity
public class Car extends BaseEntitySoftDelete<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer horsepower;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarType type;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Builder.Default
    @OneToMany(mappedBy = "car")
    private List<Request> requests = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "car")
    private List<CarToMediaItem> carToMediaItems = new ArrayList<>();

    public void addRequest(Request request, User creator) {
        request.setClient(creator);
        request.setCar(this);
        requests.add(request);
    }
}
