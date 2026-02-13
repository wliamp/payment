package io.github.wliamp.pro.pay

import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

internal class IVnPayment internal constructor(
    private val props: PaymentProps.VnPayProps,
    private val webClient: WebClient
) : IPayment<VnPayClientData, VnPaySystemData> {
    private val provider = "vnPay"

    @Deprecated(
        message = "Not supported by VNPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun authorize(client: VnPayClientData, system: VnPaySystemData): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay AUTHORIZE unsupported"))

    @Deprecated(
        message = "Not supported by VNPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun capture(client: VnPayClientData, system: VnPaySystemData): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay CAPTURE unsupported"))

    override fun sale(client: VnPayClientData, system: VnPaySystemData): Mono<Any> =
        props.takeIf {
            it.returnUrl.isNotBlank() &&
                it.secretKey.isNotBlank() &&
                it.tmnCode.isNotBlank()
        }?.let { p ->
            val vnpTxnRef = system.vnpTxnRef ?: generateCode(100)
            val now = LocalDateTime.now()
            val pattern = "yyyyMMddHHmmss"
            val body = mutableMapOf<String, Any>(
                "vnp_Version" to "2.1.0",
                "vnp_Command" to "pay",
                "vnp_TmnCode" to p.tmnCode,
                "vnp_Amount" to (client.vnpAmount?.toInt()?.times(100)).toString(),
                "vnp_CreateDate" to formatDate(now, pattern),
                "vnp_CurrCode" to "VND",
                "vnp_IpAddr" to (client.vnpIpAddr ?: "127.0.0.1"),
                "vnp_Locale" to (client.vnpLocale ?: "vn"),
                "vnp_OrderInfo" to (system.vnpOrderInfo ?: "Create Order for TxnRef=${vnpTxnRef}"),
                "vnp_OrderType" to (system.vnpOrderType ?: "other"),
                "vnp_ReturnUrl" to p.returnUrl,
                "vnp_ExpireDate" to formatDate(now.plusMinutes(p.expiredMinutes), pattern),
                "vnp_TxnRef" to vnpTxnRef,
            )
            body.optional("vnp_BankCode", client.vnpBankCode)
            body.optional("vnp_Bill_Mobile", client.vnpBillMobile)
            body.optional("vnp_Bill_Email", client.vnpBillEmail)
            body.optional("vnp_Bill_FirstName", client.vnpBillFirstName)
            body.optional("vnp_Bill_LastName", client.vnpBillLastName)
            body.optional("vnp_Bill_Address", client.vnpBillAddress)
            body.optional("vnp_Bill_City", client.vnpBillCity)
            body.optional("vnp_Bill_Country", client.vnpBillCountry)
            body.optional("vnp_Bill_State", client.vnpBillState)
            body.optional("vnp_Inv_Phone", client.vnpInvPhone)
            body.optional("vnp_Inv_Email", client.vnpInvEmail)
            body.optional("vnp_Inv_Customer", client.vnpInvCustomer)
            body.optional("vnp_Inv_Address", client.vnpInvAddress)
            body.optional("vnp_Inv_Company", client.vnpInvCompany)
            body.optional("vnp_Inv_Taxcode", client.vnpInvTaxcode)
            body.optional("vnp_Inv_Type", client.vnpInvType)
            val query = querySale(body)
            Mono.just(
                mapOf(
                    "purl" to
                        "${p.baseUrl}${p.saleUri}?$query&vnp_SecureHash=${hmacSHA512(p.secretKey, query)}"
                )
            )
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.vn-pay.return-url' " +
                    "or 'provider.payment.vn-pay.secret-key' " +
                    "or 'provider.payment.vn-pay.tmn-code' " +
                    "for VNPay configurations"
            )
        )

    override fun refund(client: VnPayClientData, system: VnPaySystemData): Mono<Any> =
        props.takeIf {
            it.secretKey.isNotBlank() &&
                it.tmnCode.isNotBlank()
        }?.let { p ->
            val pattern = "yyyyMMddHHmmss"
            val requestId = system.vnpRequestId ?: generateCode(32)
            val version = "2.1.0"
            val command = "refund"
            val tmnCode = p.tmnCode
            val transactionType = system.vnpTransactionType ?: "02"
            val txnRef = system.vnpTxnRef ?: generateCode(100)
            val amount = (client.vnpAmount?.toInt()?.times(100)).toString()
            val transactionNo = system.vnpTransactionNo ?: ""
            val transactionDate = formatDate(system.vnpTransactionDate, pattern)
            val createBy = system.vnpCreateBy ?: ""
            val createDate = formatDate(LocalDateTime.now(), pattern)
            val ipAddr = system.vnpIpAddr ?: "127.0.0.1"
            val orderInfo = system.vnpOrderInfo ?: "Refund order $txnRef"
            val body = mutableMapOf<String, Any>(
                "vnp_RequestId" to requestId,
                "vnp_Version" to version,
                "vnp_Command" to command,
                "vnp_TmnCode" to tmnCode,
                "vnp_TransactionType" to transactionType,
                "vnp_TxnRef" to txnRef,
                "vnp_Amount" to amount,
                "vnp_OrderInfo" to orderInfo,
                "vnp_TransactionNo" to transactionNo,
                "vnp_TransactionDate" to transactionDate,
                "vnp_CreateBy" to createBy,
                "vnp_CreateDate" to createDate,
                "vnp_IpAddr" to ipAddr,
                "vnp_SecureHash" to hmacSHA512(
                    p.secretKey,
                    listOf(
                        requestId, version, command, tmnCode,
                        transactionType, txnRef, amount, transactionNo,
                        transactionDate, createBy, createDate, ipAddr, orderInfo
                    ).joinToString("|")
                )
            )
            webClient.post()
                .uri("${p.baseUrl}${p.refundUri}")
                .bodyValue(body)
                .retrieve()
                .onStatus({ status -> status.isError }) { response ->
                    Mono.error(IllegalStateException("VNPay payment failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { resp -> mapOf("resp" to resp) }
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.vn-pay.secret-key' " +
                    "or 'provider.payment.vn-pay.tmn-code' " +
                    "for VNPay configuration"
            )
        )

    @Deprecated(
        message = "Not supported by VNPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun void(client: VnPayClientData, system: VnPaySystemData): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay VOID unsupported"))

    private fun querySale(body: Map<String, Any>): String =
        body.entries
            .sortedBy { it.key }
            .joinToString("&")
            { "${it.key}=${URLEncoder.encode(it.value.toString(), StandardCharsets.UTF_8.toString())}" }

    private fun hmacSHA512(key: String, data: String): String = hmac("SHA512", key, data)
}
