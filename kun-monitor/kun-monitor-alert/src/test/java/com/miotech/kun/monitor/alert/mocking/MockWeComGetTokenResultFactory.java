package com.miotech.kun.monitor.alert.mocking;

import com.miotech.kun.dataplatform.facade.backfill.Backfill;
import com.miotech.kun.monitor.alert.model.WeComGetTokenResult;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class MockWeComGetTokenResultFactory {

    private MockWeComGetTokenResultFactory() {
    }

    public static WeComGetTokenResult create() {
        return create(0, "ok", "abc", 7200);
    }

    public static WeComGetTokenResult create(int errcode, String errmsg, String token, int expiresIn) {
        WeComGetTokenResult weComGetTokenResult = new WeComGetTokenResult();
        weComGetTokenResult.setErrcode(errcode);
        weComGetTokenResult.setErrmsg(errmsg);
        weComGetTokenResult.setAccessToken(token);
        weComGetTokenResult.setExpires_in(expiresIn);

        return weComGetTokenResult;
    }

}
