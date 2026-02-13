package io.github.wliamp.pro.pay

import com.fasterxml.jackson.core.JsonProcessingException
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.error
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalDateTime.ofInstant
import java.time.LocalDateTime.parse
import java.time.ZoneId.systemDefault
import java.time.format.DateTimeFormatter.ofPattern
import java.util.UUID.randomUUID
import javax.crypto.Mac.getInstance
import javax.crypto.spec.SecretKeySpec

internal inline fun <reified T : Number> String.toNumber(): T? =
    when (T::class) {
        Int::class -> this.toIntOrNull() as T?
        Long::class -> this.toLongOrNull() as T?
        Float::class -> this.toFloatOrNull() as T?
        Double::class -> this.toDoubleOrNull() as T?
        else -> null
    }

internal fun MutableMap<String, Any>.optional(key: String, value: Any?) {
    when (value) {
        null -> return
        is String -> if (value.isBlank()) return
        is Long -> if (value == 0L) return
    }
    this[key] = value
}

internal fun generateCode(size: Int): String =
    randomUUID().toString().replace("-", "")
        .take(size)

internal fun formatDate(input: Any?, pattern: String): String =
    when (input) {
        is String -> parse(input, ofPattern(pattern))
        is Long -> ofInstant(Instant.ofEpochMilli(input), systemDefault())
        is LocalDateTime -> input
        else -> throw IllegalArgumentException("Unsupported date type: ${input!!::class}")
    }.format(ofPattern(pattern))

internal fun hmac(code: String, key: String, data: String): String =
    getInstance("Hmac${code}").run {
        init(SecretKeySpec(key.toByteArray(UTF_8), "Hmac${code}"))
        doFinal(data.toByteArray(UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

internal fun WebClient.fetchPayload(
    method: HttpMethod,
    uri: String,
    provider: String,
    headers: Map<String, String> = emptyMap(),
    queryParams: Map<String, String> = emptyMap(),
    body: Any? = null
): Mono<Map<String, Any>> =
    this.method(method)
        .uri {
            it.path(uri)
            queryParams.forEach { (k, v) -> it.queryParam(k, v) }
            it.build()
        }
        .apply { headers.forEach { (k, v) -> this.header(k, v) } }
        .apply { body?.let { this.bodyValue(it) } }
        .retrieve()
        .onStatus({ it.isError }) { resp ->
            resp.bodyToMono<String>()
                .flatMap { error(PaymentHttpException(provider, resp.statusCode().value(), it)) }
        }
        .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
        .onErrorMap {
            when (it) {
                is PaymentException -> it
                is ConnectException,
                is SocketTimeoutException,
                is WebClientRequestException -> PaymentNetworkException(provider, it)
                is JsonProcessingException -> PaymentParseException(provider, "Invalid JSON", it)
                is DecodingException -> {
                    val cause = it.cause
                    if (cause is JsonProcessingException)
                        PaymentParseException(provider, "Invalid JSON", cause)
                    else PaymentParseException(provider, "Invalid JSON", it)
                }
                else -> PaymentUnexpectedException(provider, it)
            }
        }
