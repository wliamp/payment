package io.github.wliamp.pro.pay

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(PaymentProps::class)
internal class PaymentAutoConfig private constructor(
    private val props: PaymentProps
) {
    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.authorize-net",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun an(): IPayment<AuthorizeNetClientData, AuthorizeNetSystemData> = IAuthorizeNet(props.authorizeNet, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.vn-pay",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun vp(): IPayment<VnPayClientData, VnPaySystemData> = IVnPayment(props.vnPay, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.zalo-pay",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun zp(): IPayment<ZaloPayClientData, ZaloPaySystemData> = IZaloPayment(props.zaloPay, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun pay(
        an: ObjectProvider<IPayment<AuthorizeNetClientData, AuthorizeNetSystemData>>,
        vp: ObjectProvider<IPayment<VnPayClientData, VnPaySystemData>>,
        zp: ObjectProvider<IPayment<ZaloPayClientData, ZaloPaySystemData>>
    ): PaymentProvider = PaymentProvider(
        an.ifAvailable,
        vp.ifAvailable,
        zp.ifAvailable
    )
}
