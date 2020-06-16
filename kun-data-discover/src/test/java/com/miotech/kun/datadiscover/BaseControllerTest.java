package com.miotech.kun.datadiscover;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: Jie Chen
 * @created: 2020/6/12
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class BaseControllerTest {

    @Test
    public void test() {
    }
}
