package io.github.wliamp.pro.vrf

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.util.concurrent.TimeUnit

internal interface ITestSetup<P : Any, T> {
    val server: MockWebServer
    var client: WebClient
    var props: P
    var provider: T
    val mapper: ObjectMapper

    fun buildProps(): P
    fun buildProvider(props: P, client: WebClient): T

    fun initServerAndClient() =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 200)
            .doOnConnected {
                it.addHandlerLast(ReadTimeoutHandler(1, TimeUnit.SECONDS))
                it.addHandlerLast(WriteTimeoutHandler(1, TimeUnit.SECONDS))
            }
            .let {
                WebClient.builder()
                    .baseUrl(server.url("/").toString().removeSuffix("/"))
                    .clientConnector(ReactorClientHttpConnector(it))
                    .build()
            }
            .also { client = it }
            .also { props = buildProps() }
            .also { provider = buildProvider(props, client) }

    fun enqueueJson(server: MockWebServer, body: Any, code: Int = 200) =
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(body))
        )
}
