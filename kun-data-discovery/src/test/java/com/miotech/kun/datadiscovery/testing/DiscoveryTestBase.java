package com.miotech.kun.datadiscovery.testing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

/**
 * @program: kun
 * @description: testing
 * @author: zemin  huang
 * @create: 2022-01-24 09:18
 **/

@SpringBootTest(classes = DiscoveryTestBase.Configuration.class)
@ActiveProfiles("test")
@ComponentScan(basePackages = {
        "com.miotech.kun.common",
        "com.miotech.kun.datadiscovery"
})
@AutoConfigureMockMvc
@Slf4j
public class DiscoveryTestBase {



    @org.springframework.context.annotation.Configuration
    @EnableAutoConfiguration
    public static class Configuration {

    }

}
