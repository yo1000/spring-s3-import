package com.yo1000.s3import.application;

import com.yo1000.s3import.application.port.CsvFileLoader;
import com.yo1000.s3import.application.port.UserCsv;
import com.yo1000.s3import.config.NodeProperties;
import com.yo1000.s3import.config.TimeProperties;
import com.yo1000.s3import.domain.User;
import com.yo1000.s3import.domain.UserRepository;
import com.yo1000.s3import.domain.vo.Genders;
import com.yo1000.s3import.domain.vo.NodeIdHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Transactional
@Service
public class UserApplicationService {
    private final UserRepository userRepository;
    private final CsvFileLoader<UserCsv, User> csvFileLoader;
    private final TimeProperties timeProps;
    private final NodeProperties nodeProps;
    private final NodeIdHolder nodeIdHolder;
    private final Clock clock;

    private final Logger logger = LoggerFactory.getLogger(UserApplicationService.class);

    public UserApplicationService(
            UserRepository userRepository,
            CsvFileLoader<UserCsv, User> csvFileLoader,
            TimeProperties timeProps,
            NodeProperties nodeProps,
            NodeIdHolder nodeIdHolder,
            Clock clock) {
        this.userRepository = userRepository;
        this.csvFileLoader = csvFileLoader;
        this.timeProps = timeProps;
        this.nodeProps = nodeProps;
        this.nodeIdHolder = nodeIdHolder;
        this.clock = clock;
    }

    public User lookup(String id) {
        return userRepository.findById(id)
                .orElseThrow();
    }

    public User lookupByUsername(String username) {
        long latestUpdate = StreamSupport.stream(userRepository.findAllCreationEpochMillis().spliterator(), false)
                .mapToLong(value -> value)
                .max()
                .orElseThrow();

        return userRepository.findByUsernameAndCreationEpochMillis(username, latestUpdate)
                .orElseThrow();
    }

    public Page<User> search(
            String username,
            String email,
            String givenName,
            String familyName,
            String address,
            Pageable pageable) {
        long latestUpdate = StreamSupport.stream(userRepository.findAllCreationEpochMillis().spliterator(), false)
                .mapToLong(value -> value)
                .max()
                .orElseThrow();

        return userRepository.findAllByUsernameLikeAndEmailLikeAndGivenNameLikeAndFamilyNameLikeAndAddressLikeAndCreationEpochMillis(
                username, email, givenName, familyName, address, latestUpdate, pageable);
    }

    public void update(long execTime) {
        long start = clock.millis();

        long latestUpdate = StreamSupport.stream(userRepository.findAllCreationEpochMillis().spliterator(), false)
                .mapToLong(value -> value)
                .max()
                .orElse(0L);

        if (latestUpdate + timeProps.getMinUpdateIntervalMillis() > execTime) {
            logger.info("Node={} Time={} | Skip updates.", nodeIdHolder.value(), execTime);
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");

        try (CloseableIterator<User> iter = csvFileLoader.loadCsv((row -> new User(
                UUID.randomUUID().toString(),
                row.getUsername(),
                row.getEmail(),
                row.getGivenName(),
                row.getFamilyName(),
                Genders.ofName( row.getGender()),
                Optional.ofNullable(row.getBirthDate())
                        .map(String::trim)
                        .filter(s -> s.matches("[0-9]{4}(?:0[1-9]|1[0-2])(?:0[1-9]|[12][0-9]|3[01])"))
                        .map(s -> LocalDate.parse(s, dateFormatter))
                        .orElse(null),
                row.getAddress(),
                execTime
        )))) {
            userRepository.saveAll(() -> iter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        logger.info("Node={} Time={} | Took {}-millis to update users.",
                nodeIdHolder.value(), execTime, clock.millis() - start);
    }

    public void delete() {
        Iterable<Long> creationEpochMillis = userRepository.findAllCreationEpochMillis();
        if (StreamSupport.stream(creationEpochMillis.spliterator(), false).count() > nodeProps.getGenerations()) {
            List<Long> keepGenerations = StreamSupport.stream(creationEpochMillis.spliterator(), false)
                    .sorted((o1, o2) -> Long.compare(o2, o1))
                    .limit(nodeProps.getGenerations())
                    .toList();

            userRepository.deleteAllByCreationEpochMillis(
                    StreamSupport.stream(creationEpochMillis.spliterator(), false)
                            .filter(millis -> !keepGenerations.contains(millis))
                            .toList());
        }
    }
}
