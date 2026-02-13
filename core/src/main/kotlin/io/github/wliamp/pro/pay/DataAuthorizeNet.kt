package io.github.wliamp.pro.pay

data class AuthorizeNetClientData(
    val amount: String? = null,
) : OClient() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var amount: String? = null

        fun amount(amount: String?) = apply { this.amount = amount }

        fun build() = AuthorizeNetClientData(
            amount
        )
    }
}

data class AuthorizeNetSystemData(
    val description: String? = null,
    val orderId: String? = null,
    val refTransId: String? = null,
) : OSystem() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var description: String? = null
        private var orderId: String? = null
        private var refTransId: String? = null

        fun description(description: String?) = apply { this.description = description }
        fun orderId(orderId: String?) = apply { this.orderId = orderId }
        fun refTransId(refTransId: String?) = apply { this.refTransId = refTransId }

        fun build() = AuthorizeNetSystemData(
            description,
            orderId,
            refTransId
        )
    }
}
