package io.github.wliamp.pro.pay

import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import kotlin.collections.get

internal class IZaloPayment internal constructor(
    private val props: PaymentProps.ZaloPayProps,
    private val webClient: WebClient
) : IPayment<ZaloPayClientData, ZaloPaySystemData> {
    private val provider = "zaloPay"

    @Deprecated(
        message = "Not supported by ZaloPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun authorize(client: ZaloPayClientData, system: ZaloPaySystemData): Mono<Any> =
        Mono.error(UnsupportedOperationException("ZaloPay AUTHORIZE unsupported"))

    @Deprecated(
        message = "Not supported by ZaloPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun capture(client: ZaloPayClientData, system: ZaloPaySystemData): Mono<Any> =
        Mono.error(UnsupportedOperationException("ZaloPay CAPTURE unsupported"))

    override fun sale(client: ZaloPayClientData, system: ZaloPaySystemData): Mono<Any> =
        props.takeIf {
            (it.appId > 0) &&
                it.key1.isNotBlank()
        }?.let { p ->
            val appId = p.appId
            val appUser = system.appUser ?: ""
            val appTransId = system.appTransId ?: ""
            val appTime = System.currentTimeMillis().toString()
            val amount: Long = client.amount?.toNumber() ?: 0
            val item = client.item ?: "[]"
            val description = client.description ?: "Payment for the order #${appTransId}"
            val embedData = client.embedData ?: "{}"
            val body = mutableMapOf<String, Any>(
                "app_id" to appId,
                "app_user" to appUser,
                "app_trans_id" to appTransId,
                "app_time" to appTime,
                "expire_duration_seconds" to p.expireDurationSeconds,
                "amount" to amount,
                "item" to item,
                "description" to description,
                "embed_data" to embedData,
                "mac" to hmacSHA256(p.key1, listOf(appId, appTransId, appUser, amount, appTime, embedData, item).joinToString("|"))
            )
            body.optional("bank_code", client.bankCode)
            body.optional("device_info", client.deviceInfo)
            body.optional("sub_app_id", p.subAppId)
            body.optional("callback_url", system.callbackUrl)
            webClient.post()
                .uri("${p.baseUrl}${p.saleUri}")
                .bodyValue(body)
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    Mono.error(IllegalStateException("ZaloPay sale failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { resp ->
                    mapOf(
                        "purl" to resp["order_url"],
                        "resp" to resp
                    )
                }
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.zalo-pay.app-id' " +
                    "or 'provider.payment.zalo-pay.key-1' " +
                    "or 'provider.payment.zalo-pay.return-url' " +
                    "or 'provider.payment.zalo-pay.cancel-url' " +
                    "for ZaloPay configuration"
            )
        )

    override fun refund(client: ZaloPayClientData, system: ZaloPaySystemData): Mono<Any> =
        props.takeIf {
            (it.appId > 0) &&
                it.key1.isNotBlank()
        }?.let { p ->
            val key1 = p.key1
            val appId = p.appId
            val mRefundId =
                "${formatDate(LocalDateTime.now(), "yyMMdd")}_" +
                    "${appId}_" +
                    generateCode((37 - "$appId".length).coerceAtLeast(0))
            val zpTransId = system.zpTransId ?: ""
            val amount = client.amount ?: 0
            val refundFeeAmount: Long = system.refundFeeAmount?.toNumber() ?: 0
            val timestamp = System.currentTimeMillis()
            val description = client.description ?: "Refund for order ${system.appTransId}"
            val body = mutableMapOf<String, Any>(
                "m_refund_id" to mRefundId,
                "app_id" to appId,
                "zp_trans_id" to zpTransId,
                "amount" to amount,
                "timestamp" to timestamp
            )
            body.optional("refund_fee_amount", refundFeeAmount)
            body["mac"] = body["refund_fee_amount"]?.let {
                hmacSHA256(key1, listOf(appId, zpTransId, amount, description, timestamp).joinToString("|"))
            } ?: hmacSHA256(key1, listOf(appId, zpTransId, amount, refundFeeAmount, description, timestamp).joinToString("|"))
            body["description"] = description
            webClient.post()
                .uri("${p.baseUrl}${p.refundUri}")
                .bodyValue(body)
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    Mono.error(IllegalStateException("ZaloPay refund failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { response ->
                    mapOf(
                        "success" to (response["return_code"] == 1),
                        "refundId" to response["refund_id"]
                    )
                }
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.zalo-pay.app-id' " +
                    "or 'provider.payment.zalo-pay.key-1' " +
                    "for ZaloPay configuration"
            )
        )

    @Deprecated(
        message = "Not supported by ZaloPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun void(client: ZaloPayClientData, system: ZaloPaySystemData): Mono<Any> =
        Mono.error(UnsupportedOperationException("ZaloPay VOID unsupported"))

    private fun hmacSHA256(key: String, data: String): String = hmac("SHA256", key, data)
}
