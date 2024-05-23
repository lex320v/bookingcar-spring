package com.bookingcar.entity;

import com.bookingcar.entity.enums.RequestStatus;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

import java.time.Instant;

@ToString(exclude = {"car", "client", "feedback"}, callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@OptimisticLocking(type = OptimisticLockType.VERSION)
public class Request extends BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private Instant dateTimeFrom;

    @Column(nullable = false)
    private Instant dateTimeTo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private String comment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    private Car car;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private User client;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "feedback_id")
    private Feedback feedback;
}
