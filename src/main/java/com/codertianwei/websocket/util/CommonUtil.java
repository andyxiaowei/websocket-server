package com.codertianwei.websocket.util;

import java.util.UUID;

public final class CommonUtil {
    public static final String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
