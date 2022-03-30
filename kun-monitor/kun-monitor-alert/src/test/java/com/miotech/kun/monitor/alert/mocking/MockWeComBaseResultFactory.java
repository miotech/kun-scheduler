package com.miotech.kun.monitor.alert.mocking;

import com.miotech.kun.monitor.alert.model.WeComBaseResult;

public class MockWeComBaseResultFactory {

    private MockWeComBaseResultFactory() {
    }

    public static WeComBaseResult create() {
        return create(0, "ok");
    }

    public static WeComBaseResult create(int errcode, String errmsg) {
        WeComBaseResult weComBaseResult = new WeComBaseResult();
        weComBaseResult.setErrcode(errcode);
        weComBaseResult.setErrmsg(errmsg);
        return weComBaseResult;
    }

}
