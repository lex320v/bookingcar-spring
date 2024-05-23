package com.bookingcar.util;

import com.bookingcar.entity.Car;
import com.bookingcar.entity.Feedback;
import com.bookingcar.entity.Request;
import com.bookingcar.entity.User;
import com.bookingcar.entity.enums.CarType;
import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.RequestStatus;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.entity.enums.UserStatus;
import jakarta.persistence.EntityManager;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

@UtilityClass
public class TestDataImporter {

    public void importData(EntityManager entityManager) {
        saveUser(entityManager, "admin1", "Артём", "Михайлов", Gender.MALE, Role.ADMIN);
        saveUser(entityManager, "admin2", "Михаил", "Черкасов", Gender.MALE, Role.ADMIN);

        var owner1 = saveUser(entityManager, "owner1", "Константин", "Ларин", Gender.MALE, Role.OWNER);
        var owner2 = saveUser(entityManager, "owner2", "Анастасия", "Орлова", Gender.FEMALE, Role.OWNER);
        saveUser(entityManager, "yatikai", "Анна", "Кочергина", Gender.FEMALE, Role.OWNER);

        var client1 = saveUser(entityManager, "client1", "Арина", "Новикова", Gender.FEMALE, Role.CLIENT);
        var client2 = saveUser(entityManager, "client2", "Мирослава", "Суслова", Gender.FEMALE, Role.CLIENT);
        saveUser(entityManager, "rtonyoka", "Александр", "Петров", Gender.MALE, Role.CLIENT);
        saveUser(entityManager, "uniyand", "Василиса", "Сидорова", Gender.FEMALE, Role.CLIENT);

        var car1 = saveCar(entityManager, owner1, "Toyota", "Camry");
        var car2 = saveCar(entityManager, owner2, "Ford", "Focus");
        saveCar(entityManager, owner1, "Audi", "TT");
        saveCar(entityManager, owner1, "Nissan", "X-Trail");
        saveCar(entityManager, owner1, "Suzuki", "Swift");
        saveCar(entityManager, owner1, "Citroen", "C4");
        saveCar(entityManager, owner1, "Renault", "Zoe");
        saveCar(entityManager, owner1, "Jeep", "Wrangler");
        saveCar(entityManager, owner1, "Ford", "Explorer");
        saveCar(entityManager, owner1, "Lexus", "ES");
        saveCar(entityManager, owner1, "Volkswagen", "Jetta");
        saveCar(entityManager, owner1, "GMC", "Terrain");
        saveCar(entityManager, owner1, "Volkswagen", "Golf");
        saveCar(entityManager, owner2, "Land Rover", "Range Rover");
        saveCar(entityManager, owner2, "Toyota", "Tacoma");
        saveCar(entityManager, owner2, "Lincoln", "MKZ");

        saveRequest(entityManager, client1, car1, RequestStatus.OPEN);
        saveRequest(entityManager, client1, car2, RequestStatus.OPEN);
        saveRequest(entityManager, client2, car1, RequestStatus.OPEN);

        var request1 = saveRequest(entityManager, client1, car1, RequestStatus.FINISHED);
        var request2 = saveRequest(entityManager, client1, car1, RequestStatus.FINISHED);
        var request3 = saveRequest(entityManager, client1, car1, RequestStatus.FINISHED);

        var request4 = saveRequest(entityManager, client1, car2, RequestStatus.FINISHED);
        var request5 = saveRequest(entityManager, client1, car2, RequestStatus.FINISHED);
        var request6 = saveRequest(entityManager, client1, car2, RequestStatus.FINISHED);

        saveFeedback(entityManager, request1, 3);
        saveFeedback(entityManager, request2, 4);
        saveFeedback(entityManager, request3, 5);

        saveFeedback(entityManager, request4, 2);
        saveFeedback(entityManager, request5, 2);
        saveFeedback(entityManager, request6, 3);
    }

    private User saveUser(EntityManager entityManager, String username, String firstname, String lastname,
                          Gender gender, Role role) {
        var user = User.builder()
                .username(username)
                .firstname(firstname)
                .lastname(lastname)
                .password("{noop}qwerty")
                .status(UserStatus.ACTIVE)
                .gender(gender)
                .role(role)
                .birthDate(LocalDate.of(randomInt(1990, 2000), randomInt(1, 12), randomInt(1, 28)))
                .build();

        entityManager.persist(user);
        entityManager.flush();

        return user;
    }

    private Car saveCar(EntityManager entityManager, User owner, String manufacturer, String model) {
        CarType[] values = CarType.values();

        Car car = Car.builder()
                .manufacturer(manufacturer)
                .model(model)
                .year(randomInt(2005, 2022))
                .horsepower(randomInt(60, 600))
                .price(randomInt(100, 9000))
                .active(true)
                .type(values[randomInt(0, values.length - 1)])
                .owner(owner)
                .build();

        entityManager.persist(car);

        return car;
    }

    private Request saveRequest(EntityManager entityManager, User client, Car car, RequestStatus status) {
        var request = Request.builder()
                .dateTimeFrom(Instant.now())
                .dateTimeTo(Instant.now().plus(Duration.ofHours(8)))
                .client(client)
                .car(car)
                .status(status)
                .build();

        entityManager.persist(request);

        return request;
    }

    private Feedback saveFeedback(EntityManager entityManager, Request request, int rating) {
        Feedback feedback = Feedback.builder()
                .rating(rating)
                .text("text")
                .build();

        entityManager.persist(feedback);

        request.setFeedback(feedback);
        entityManager.persist(request);

        return feedback;
    }

    private int randomInt(int min, int max) {
        max -= min;
        return (int) (Math.random() * ++max) + min;
    }
}
