package com.yo1000.s3import.presentation;

import com.yo1000.s3import.application.NodeApplicationService;
import com.yo1000.s3import.application.UserApplicationService;
import com.yo1000.s3import.domain.vo.NodeIdHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
@EnableScheduling
public class UserUpdateScheduler {
    private final UserApplicationService userApplicationService;
    private final NodeApplicationService nodeApplicationService;
    private final NodeIdHolder nodeIdHolder;
    private final Clock clock;

    private final Logger logger = LoggerFactory.getLogger(UserUpdateScheduler.class);

    public UserUpdateScheduler(
            UserApplicationService userApplicationService,
            NodeApplicationService nodeApplicationService,
            NodeIdHolder nodeIdHolder, Clock clock) {
        this.userApplicationService = userApplicationService;
        this.nodeApplicationService = nodeApplicationService;
        this.nodeIdHolder = nodeIdHolder;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        update();
    }

    @Scheduled(cron = "#{ @timeProperties.updateCron }")
    public void update() {
        long execTime = clock.millis();

        logger.info("Node={} Time={} | Starting table updates.", nodeIdHolder.value(), execTime);
        if (!nodeApplicationService.exists()) {
            nodeApplicationService.register(execTime)
                    .ifPresent(waitTime -> sleep(execTime, waitTime.millis()));
        }
        nodeApplicationService.rank(execTime)
                .ifPresent(waitTime -> sleep(execTime, waitTime.millis()));;
        userApplicationService.update(execTime);
        logger.info("Node={} Time={} | Ending table updates.", nodeIdHolder.value(), execTime);

        logger.info("Node={} Time={} | Starting table purges.", nodeIdHolder.value(), execTime);
        userApplicationService.delete();
        nodeApplicationService.cleanup(execTime);
        logger.info("Node={} Time={} | Ending table purges.", nodeIdHolder.value(), execTime);
    }

    private void sleep(long execTime, long millis) {
        try {
            logger.info("Node={} Time={} | Sleep {}-millis.", nodeIdHolder.value(), execTime, millis);
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
