package io.github.wliamp.pro.pay

import reactor.core.publisher.Mono

interface IPayment<C : OClient, S : OSystem> {
    fun authorize(client: C, system: S): Mono<Any>
    fun capture(client: C, system: S): Mono<Any>
    fun sale(client: C, system: S): Mono<Any>
    fun refund(client: C, system: S): Mono<Any>
    fun void(client: C, system: S): Mono<Any>
}
