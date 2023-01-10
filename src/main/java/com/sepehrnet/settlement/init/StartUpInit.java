package com.sepehrnet.settlement.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class StartUpInit {

    public Map<String, Object> cache;

    @PostConstruct
    private void init() {
        log.debug("Init... ");
        cache = new LinkedHashMap<>();
    }
}
