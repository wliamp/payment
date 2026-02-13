package io.github.wliamp.pro.pay

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.wliamp.pro.vrf.ITestSetup
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

internal class VnPayTest : ITestSetup<PaymentProps.VnPayProps, IPayment<VnPayClientData, VnPaySystemData>> {
    override lateinit var server: MockWebServer
    override lateinit var client: WebClient
    override lateinit var props: PaymentProps.VnPayProps
    override lateinit var provider: IPayment<VnPayClientData, VnPaySystemData>
    override val mapper = ObjectMapper()

    override fun buildProps(): PaymentProps.VnPayProps =
        PaymentProps.VnPayProps().apply {
            baseUrl = ""
            returnUrl = "http://test-return-url"
            secretKey = "test-secret-key"
            tmnCode = "test-tmn-code"
            saleUri = "/sale"
            refundUri = "/refund"
            expiredMinutes = 15
        }

    override fun buildProvider(
        props: PaymentProps.VnPayProps,
        client: WebClient
    ) = IVnPayment(props, client)

    @BeforeEach
    fun setup() {
        server = MockWebServer()
        server.start()
        initServerAndClient()
    }

    @AfterEach
    fun tearDown() = server.shutdown()

    // ---------- Unsupported Operations ----------
    @Test
    fun `authorize should throw UnsupportedOperationException`() {
        val clientData = VnPayClientData(vnpAmount = "1000")
        val systemData = VnPaySystemData()
        StepVerifier.create(provider.authorize(clientData, systemData))
            .expectError(UnsupportedOperationException::class.java)
            .verify()
    }

    @Test
    fun `capture should throw UnsupportedOperationException`() {
        val clientData = VnPayClientData(vnpAmount = "1000")
        val systemData = VnPaySystemData()
        StepVerifier.create(provider.capture(clientData, systemData))
            .expectError(UnsupportedOperationException::class.java)
            .verify()
    }

    @Test
    fun `void should throw UnsupportedOperationException`() {
        val clientData = VnPayClientData(vnpAmount = "1000")
        val systemData = VnPaySystemData()
        StepVerifier.create(provider.void(clientData, systemData))
            .expectError(UnsupportedOperationException::class.java)
            .verify()
    }

    // ---------- Sale ----------
    @Test
    fun `sale should build correct purl`() {
        val clientData = VnPayClientData(vnpAmount = "1000")
        val systemData = VnPaySystemData()
        StepVerifier.create(provider.sale(clientData, systemData))
            .expectNextMatches {
                val mapResult = it as? Map<*, *>
                mapResult?.containsKey("purl") == true &&
                    (mapResult["purl"] as? String)?.contains("vnp_SecureHash") == true
            }
            .verifyComplete()
    }

    @Test
    fun `sale should error if missing config`() {
        val badProps = PaymentProps.VnPayProps().apply {
            baseUrl = ""
            returnUrl = ""
            secretKey = ""
            tmnCode = ""
        }
        val badProvider = IVnPayment(badProps, client)
        val clientData = VnPayClientData(vnpAmount = "1000")
        val systemData = VnPaySystemData()
        StepVerifier.create(badProvider.sale(clientData, systemData))
            .expectError(IllegalStateException::class.java)
            .verify()
    }

    // ---------- Refund ----------
    @Test
    fun `refund should call endpoint and return response`() {
        enqueueJson(server, mapOf("status" to "success"))

        val clientData = VnPayClientData(vnpAmount = "1000")
        val systemData = VnPaySystemData(
            vnpTransactionDate = "20250911123045",
            vnpTxnRef = "TXN123",
            vnpTransactionNo = "TRX456"
        )

        StepVerifier.create(provider.refund(clientData, systemData))
            .expectNextMatches {
                ((it as? Map<*, *>)
                    ?.get("resp") as? Map<*, *>
                    )?.get("status") == "success"
            }
            .verifyComplete()
    }

    @Test
    fun `refund should error if missing config`() {
        val badProps = PaymentProps.VnPayProps().apply {
            baseUrl = ""
            secretKey = ""
            tmnCode = ""
        }
        val badProvider = IVnPayment(badProps, client)
        val clientData = VnPayClientData(vnpAmount = "1000")
        val systemData = VnPaySystemData()
        StepVerifier.create(badProvider.refund(clientData, systemData))
            .expectError(IllegalStateException::class.java)
            .verify()
    }

    @Test
    fun `sale should include optional fields`() {
        val clientData = VnPayClientData(
            vnpBillCity = "Hanoi",
            vnpBankCode = "VCB"
        )
        val systemData = VnPaySystemData()
        StepVerifier.create(provider.sale(clientData, systemData))
            .expectNextMatches {
                val purl = (it as Map<*, *>)["purl"] as String
                purl.contains("vnp_BankCode=VCB")
            }
            .verifyComplete()
    }
}
