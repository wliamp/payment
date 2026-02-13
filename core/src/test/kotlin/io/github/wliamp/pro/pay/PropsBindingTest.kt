package io.github.wliamp.pro.pay

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class PropsBindingTest {
    private val baseRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(PaymentAutoConfig::class.java))

    @Test
    fun `props binding should map values correctly`() {
        baseRunner.withPropertyValues(
            "provider.payment.authorize-net.api-login-id=test-login",
            "provider.payment.authorize-net.transaction-key=test-key",
            "provider.payment.vn-pay.secret-key=test-vnpay",
            "provider.payment.vn-pay.tmn-code=tmn123",
            "provider.payment.zalo-pay.app-id=1001",
            "provider.payment.zalo-pay.key1=zalo-secret"
        ).run { ctx ->
            val props = ctx.getBean(PaymentProps::class.java)

            assertThat(props.authorizeNet.apiLoginId).isEqualTo("test-login")
            assertThat(props.authorizeNet.transactionKey).isEqualTo("test-key")
            assertThat(props.vnPay.secretKey).isEqualTo("test-vnpay")
            assertThat(props.vnPay.tmnCode).isEqualTo("tmn123")
            assertThat(props.zaloPay.appId).isEqualTo(1001)
            assertThat(props.zaloPay.key1).isEqualTo("zalo-secret")
        }
    }

    @Test
    fun `context loads without any config (defaults)`() {
        baseRunner.run { ctx ->
            assertThat(ctx).hasSingleBean(PaymentProps::class.java)

            val props = ctx.getBean(PaymentProps::class.java)
            assertThat(props.authorizeNet.baseUrl)
                .isEqualTo("https://api2.authorize.net/xml/v1/request.api")
            assertThat(props.vnPay.expiredMinutes).isEqualTo(15)
            assertThat(props.zaloPay.expireDurationSeconds).isEqualTo(86400)
        }
    }
}
