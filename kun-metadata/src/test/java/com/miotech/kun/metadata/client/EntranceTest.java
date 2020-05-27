package com.miotech.kun.metadata.client;

import com.miotech.kun.metadata.entrance.Entrance;
import org.junit.Test;

public class EntranceTest {

    @Test
    public void testStart() {
        Entrance entrance = new Entrance("jdbc:mysql://127.0.0.1:3306/unden?useSSL=false", "root", "123456");
        entrance.start();
    }

    @Test
    public void testStart_id() {
        Entrance entrance = new Entrance("jdbc:mysql://127.0.0.1:3306/unden?useSSL=false", "root", "123456");
        entrance.start(2L);
    }

}
