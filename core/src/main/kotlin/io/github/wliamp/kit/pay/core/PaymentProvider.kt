package io.github.wliamp.kit.pay.core

import io.github.wliamp.kit.pay.core.Payment.*

class PaymentProvider(
    val authorizeNet: IPayment<AuthorizeNetClientData, AuthorizeNetSystemData>?,
    val vnPay: IPayment<VnPayClientData, VnPaySystemData>?,
    val zaloPay: IPayment<ZaloPayClientData, ZaloPaySystemData>?
) {
    fun of(payment: Payment): IPayment<*, *>? =
        when (payment) {
            AUTHORIZE_NET -> authorizeNet
            VN_PAY -> vnPay
            ZALO_PAY -> zaloPay
        }
}
