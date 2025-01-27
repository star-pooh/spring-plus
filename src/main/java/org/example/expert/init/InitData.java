package org.example.expert.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitData {

    private final InitDataRepository initDataRepository;

    @PostConstruct
    @Transactional
    public void init() {
        long startTime = System.currentTimeMillis();

        final int MAX_DATA_SIZE = 1000000;
        final int BATCH_SIZE = 10000;
        final String EMAIL_SUFFIX = "@test.com";
        final String PASSWORD = "Abc1234!";

        List<User> userList = new ArrayList<>();
        Faker faker = new Faker();

        for (int i = 0; i < MAX_DATA_SIZE; i++) {
            userList.add(new User(
                    faker.name().firstName() + i + EMAIL_SUFFIX,
                    PASSWORD,
                    UserRole.ROLE_USER,
                    faker.color().name() + faker.name().lastName() + faker.name().firstName()
            ));
        }

        initDataRepository.saveAll(userList, BATCH_SIZE);

        long endTime = System.currentTimeMillis();

        log.info("processing Time : {}", endTime - startTime);
    }
}
