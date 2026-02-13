package io.github.wliamp.pro.pay

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class AutoConfigTest {
    private val baseRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(PaymentAutoConfig::class.java))

    @Test
    fun `when enabled then all beans should be created`() {
        baseRunner.withPropertyValues(
            "provider.payment.authorize-net.enabled=true",
            "provider.payment.vn-pay.enabled=true",
            "provider.payment.zalo-pay.enabled=true",
            "provider.payment.authorize-net.api-login-id=test-login",
            "provider.payment.authorize-net.transaction-key=test-key",
            "provider.payment.vn-pay.secret-key=test-vnpay",
            "provider.payment.vn-pay.tmn-code=tmn123",
            "provider.payment.zalo-pay.app-id=1001",
            "provider.payment.zalo-pay.key1=zalo-secret"
        ).run { ctx ->
            assertThat(ctx).hasSingleBean(PaymentProps::class.java)
            assertThat(ctx).hasSingleBean(PaymentProvider::class.java)

            assertThat(ctx).hasSingleBean(IAuthorizeNet::class.java)
            assertThat(ctx).hasSingleBean(IVnPayment::class.java)
            assertThat(ctx).hasSingleBean(IZaloPayment::class.java)
        }
    }

    @Test
    fun `when authorizeNet disabled then bean should not exist`() {
        baseRunner.withPropertyValues(
            "provider.payment.authorize-net.enabled=false",
            "provider.payment.vn-pay.enabled=true",
            "provider.payment.zalo-pay.enabled=true"
        ).run { ctx ->
            assertThat(ctx).doesNotHaveBean(IAuthorizeNet::class.java)
            assertThat(ctx).hasSingleBean(IVnPayment::class.java)
            assertThat(ctx).hasSingleBean(IZaloPayment::class.java)
        }
    }

    @Test
    fun `when vnPay disabled then bean should not exist`() {
        baseRunner.withPropertyValues(
            "provider.payment.authorize-net.enabled=true",
            "provider.payment.vn-pay.enabled=false",
            "provider.payment.zalo-pay.enabled=true"
        ).run { ctx ->
            assertThat(ctx).hasSingleBean(IAuthorizeNet::class.java)
            assertThat(ctx).doesNotHaveBean(IVnPayment::class.java)
            assertThat(ctx).hasSingleBean(IZaloPayment::class.java)
        }
    }

    @Test
    fun `when zaloPay disabled then bean should not exist`() {
        baseRunner.withPropertyValues(
            "provider.payment.authorize-net.enabled=true",
            "provider.payment.vn-pay.enabled=true",
            "provider.payment.zalo-pay.enabled=false"
        ).run { ctx ->
            assertThat(ctx).hasSingleBean(IAuthorizeNet::class.java)
            assertThat(ctx).hasSingleBean(IVnPayment::class.java)
            assertThat(ctx).doesNotHaveBean(IZaloPayment::class.java)
        }
    }
}
