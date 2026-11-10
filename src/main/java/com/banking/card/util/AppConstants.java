package com.banking.card.util;

public final class AppConstants {

    private AppConstants() {
    }

    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    public static final String HEADER_CHANNEL_ID = "X-Channel-ID";
    public static final String HEADER_TRANSACTION_ID = "X-Transaction-ID";

    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final String DEFAULT_CHANNEL = "MS-CARD-SERVICE";
}