package io.github.wliamp.pro.pay

internal sealed class PaymentException(
    provider: String,
    message: String,
    cause: Throwable? = null
) : RuntimeException("[$provider] $message", cause)

internal class PaymentConfigException(
    provider: String,
    message: String
) : PaymentException(provider, message)

internal class PaymentHttpException(
    provider: String,
    status: Int,
    responseBody: String
) : PaymentException(provider, "HTTP $status: $responseBody")

internal class PaymentParseException(
    provider: String,
    message: String,
    cause: Throwable? = null
) : PaymentException(provider, message, cause)

internal class PaymentNetworkException(
    provider: String,
    cause: Throwable
) : PaymentException(provider, "Network error", cause)

internal class PaymentUnexpectedException(
    provider: String,
    cause: Throwable
) : PaymentException(provider, "Unexpected error", cause)
