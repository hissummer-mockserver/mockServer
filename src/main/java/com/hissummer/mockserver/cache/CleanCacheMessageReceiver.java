package com.hissummer.mockserver.cache;

import com.hissummer.mockserver.mgmt.entity.HttpMockRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CleanCacheMessageReceiver {

    @JmsListener(destination = "cleanCache", containerFactory = "jmsListenerFactory")
    public void receiveMessage(HttpMockRule mockRule) {

        List<String> tobeCleanedCache = new ArrayList<>();
        for (Map.Entry<String, String> entry : MockRuleCacheService.ruleCache.asMap().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (entry.getKey().contains(mockRule.getUri())) {
                log.info("clean   key: " + key + ", value: " + value);
                tobeCleanedCache.add(entry.getKey());
            }
        }
        log.debug("clean cache");
        for (String key : tobeCleanedCache) {
            MockRuleCacheService.ruleCache.invalidate(key);
        }

    }

}
