package io.github.wliamp.pro.pay

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.collections.get

internal class IAuthorizeNet internal constructor(
    private val props: PaymentProps.AuthorizeNetProps,
    private val webClient: WebClient
) : IPayment<AuthorizeNetClientData, AuthorizeNetSystemData> {
    private val provider = "authorizeNet"

    override fun authorize(client: AuthorizeNetClientData, system: AuthorizeNetSystemData): Mono<Any> =
        getHostedPaymentToken("authOnlyTransaction", client, system)

    override fun sale(client: AuthorizeNetClientData, system: AuthorizeNetSystemData): Mono<Any> =
        getHostedPaymentToken("authCaptureTransaction", client, system)

    override fun capture(client: AuthorizeNetClientData, system: AuthorizeNetSystemData): Mono<Any> =
        requireAuthKeys().flatMap {
            val body = mapOf(
                "createTransactionRequest" to mapOf(
                    "merchantAuthentication" to merchantAuth(),
                    "transactionRequest" to mapOf(
                        "transactionType" to "priorAuthCaptureTransaction",
                        "amount" to client.amount,
                        "refTransId" to system.refTransId
                    )
                )
            )
            callJsonApi(body).map(::mapTxnResponse)
        }

    override fun refund(client: AuthorizeNetClientData, system: AuthorizeNetSystemData): Mono<Any> =
        requireAuthKeys().flatMap {
            val body = mapOf(
                "createTransactionRequest" to mapOf(
                    "merchantAuthentication" to merchantAuth(),
                    "transactionRequest" to mapOf(
                        "transactionType" to "refundTransaction",
                        "amount" to client.amount,
                        "refTransId" to system.refTransId
                    )
                )
            )
            callJsonApi(body).map(::mapTxnResponse)
        }

    override fun void(cus: AuthorizeNetClientData, sys: AuthorizeNetSystemData): Mono<Any> =
        requireAuthKeys().flatMap {
            val body = mapOf(
                "createTransactionRequest" to mapOf(
                    "merchantAuthentication" to merchantAuth(),
                    "transactionRequest" to mapOf(
                        "transactionType" to "voidTransaction",
                        "refTransId" to sys.refTransId
                    )
                )
            )
            callJsonApi(body).map(::mapTxnResponse)
        }

    private fun getHostedPaymentToken(
        transactionType: String,
        client: AuthorizeNetClientData,
        system: AuthorizeNetSystemData
    ): Mono<Any> =
        requireAuthKeys().flatMap {
            val txnReq = mutableMapOf<String, Any?>(
                "transactionType" to transactionType,
                "amount" to client.amount
            ).apply {
                if (!system.orderId.isNullOrBlank() || !system.description.isNullOrBlank()) {
                    this["order"] = mapOf(
                        "orderId" to system.orderId,
                        "description" to (system.description ?: ("Create Payment for orderId=" + system.orderId))
                    )
                }
            }
            val body = mapOf(
                "getHostedPaymentPageRequest" to mapOf(
                    "merchantAuthentication" to merchantAuth(),
                    "transactionRequest" to txnReq,
                    "hostedPaymentSettings" to buildHostedPaymentSettings()
                )
            )
            callJsonApi(body).map { resp ->
                val token = resp["token"] as? String
                    ?: error("Authorize.Net Hosted Payment token is missing")
                mapOf(
                    "success" to true,
                    "provider" to provider,
                    "redirectUrl" to props.returnUrl,
                    "token" to token
                )
            }
        }

    private fun buildHostedPaymentSettings(): Map<String, Any> = mapOf(
        "setting" to listOf(
            mapOf(
                "settingName" to "hostedPaymentReturnOptions",
                "settingValue" to """
              {
                "showReceipt": true,
                "url": "${props.returnUrl}",
                "urlText": "Continue",
                "cancelUrl": "${props.cancelUrl}",
                "cancelUrlText": "Cancel"
              }
            """.trimIndent()
            )
        )
    )

    private fun callJsonApi(body: Any): Mono<Map<*, *>> =
        webClient.post()
            .uri(props.baseUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(body)
            .retrieve()
            .onStatus({ it.isError }) { res ->
                res.bodyToMono(String::class.java).flatMap { body ->
                    Mono.error(IllegalStateException("Authorize.Net request failed: HTTP ${res.statusCode()} - $body"))
                }
            }
            .bodyToMono(Map::class.java)

    private fun merchantAuth(): Map<String, String> =
        mapOf("name" to props.apiLoginId, "transactionKey" to props.transactionKey)

    private fun requireAuthKeys(): Mono<Unit> =
        props.takeIf {
            it.apiLoginId.isNotBlank() &&
                it.transactionKey.isNotBlank() &&
                it.returnUrl.isNotBlank() &&
                it.cancelUrl.isNotBlank()
        }?.let { Mono.just(Unit) }
            ?: Mono.error(
                IllegalStateException(
                    "Missing parameter " +
                        "'provider.payment.authorize-net.api-login-id' " +
                        "or 'provider.payment.authorize-net.cancel-url' " +
                        "or 'provider.payment.authorize-net.return-url' " +
                        "or 'provider.payment.authorize-net.transaction-key' " +
                        "for AuthorizeNet configurations"
                )
            )

    private fun mapTxnResponse(resp: Map<*, *>) =
        (resp["transactionResponse"] as? Map<*, *>).let { txn ->
            val msgs = resp["messages"] as? Map<*, *>
            val responseCode = txn?.get("responseCode")?.toString()
            val transId = txn?.get("transId")?.toString()
            val resultCode = msgs?.get("resultCode")?.toString()
            val success = responseCode == "1" && resultCode == "Ok" && !transId.isNullOrBlank()
            mapOf(
                "success" to success,
                "provider" to provider,
                "transactionId" to transId,
                "authCode" to txn?.get("authCode"),
                "responseCode" to responseCode,
                "resultCode" to resultCode,
                "raw" to resp
            )
        }
}
