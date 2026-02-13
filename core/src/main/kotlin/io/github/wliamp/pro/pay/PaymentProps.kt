package io.github.wliamp.pro.pay

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "provider.payment")
internal data class PaymentProps(
    var authorizeNet: AuthorizeNetProps = AuthorizeNetProps(),
    var vnPay: VnPayProps = VnPayProps(),
    var zaloPay: ZaloPayProps = ZaloPayProps()
) {
    data class AuthorizeNetProps(
        var baseUrl: String = "https://api2.authorize.net/xml/v1/request.api",
        var redirectUrl: String = "https://accept.authorize.net/payment/payment",
        var apiLoginId: String = "",
        var transactionKey: String = "",
        var returnUrl: String = "",
        var cancelUrl: String = "",
    )

    data class VnPayProps(
        var baseUrl: String = "https://sandbox.vnpayment.vn",
        var saleUri: String = "/paymentv2/vpcpay.html",
        var refundUri: String = "/merchant_webapi/api/transaction",
        var expiredMinutes: Long = 15,
        var returnUrl: String = "",
        var secretKey: String = "",
        var tmnCode: String = ""
    )

    data class ZaloPayProps(
        var baseUrl: String = "https://sb-openapi.zalopay.vn",
        var saleUri: String = "/v2/create",
        var refundUri: String = "/v2/refund",
        var expireDurationSeconds: Long = 86400,
        var appId: Int = 0,
        var key1: String = "",
        var subAppId: String = ""
    )
}
