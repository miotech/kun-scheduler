package com.miotech.kun.monitor.sla.mocking;

import com.miotech.kun.monitor.facade.model.sla.SlaConfig;

public class MockSlaFactory {

    private MockSlaFactory() {
    }

    public static SlaConfig create() {
        return new SlaConfig(1, 0, 0, "0 0 0 * * ? *");
    }

}
