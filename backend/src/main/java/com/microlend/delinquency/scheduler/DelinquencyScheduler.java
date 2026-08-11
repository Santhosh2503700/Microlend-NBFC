package com.microlend.delinquency.scheduler;

import com.microlend.delinquency.service.DelinquencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class DelinquencyScheduler {

    private final DelinquencyService delinquencyService;

    @Scheduled(cron = "${microlend.delinquency.scan-cron:0 0 1 * * *}")
    public void dailyScan() {
        log.info("Starting scheduled delinquency scan");
        try {
            delinquencyService.runScan(null);
        } catch (Exception e) {
            log.error("Scheduled delinquency scan failed: {}", e.getMessage(), e);
        }
    }
}
