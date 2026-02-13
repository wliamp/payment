package io.github.wliamp.pro.pay

data class ZaloPayClientData(
    val amount: String? = null,
    val description: String? = null,
    val deviceInfo: String? = null,
    val embedData: String? = null,
    val item: String? = null,
    val bankCode: String? = null
) : OClient() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var amount: String? = null
        private var description: String? = null
        private var deviceInfo: String? = null
        private var embedData: String? = null
        private var item: String? = null
        private var bankCode: String? = null

        fun amount(amount: String?) = apply { this.amount = amount }
        fun description(description: String?) = apply { this.description = description }
        fun deviceInfo(deviceInfo: String?) = apply { this.deviceInfo = deviceInfo }
        fun embedData(embedData: String?) = apply { this.embedData = embedData }
        fun item(item: String?) = apply { this.item = item }
        fun bankCode(bankCode: String?) = apply { this.bankCode = bankCode }

        fun build() = ZaloPayClientData(
            amount, description,
            deviceInfo, embedData,
            item, bankCode
        )
    }
}

data class ZaloPaySystemData(
    val appUser: String? = null,
    val appTransId: String? = null,
    val callbackUrl: String? = null,
    val refundFeeAmount: String? = null,
    val zpTransId: String? = null,
) : OSystem() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var appUser: String? = null
        private var appTransId: String? = null
        private var callbackUrl: String? = null
        private var refundFeeAmount: String? = null
        private var zpTransId: String? = null

        fun appUser(appUser: String?) = apply { this.appUser = appUser }
        fun appTransId(appTransId: String?) = apply { this.appTransId = appTransId }
        fun callbackUrl(callbackUrl: String?) = apply { this.callbackUrl = callbackUrl }
        fun refundFeeAmount(refundFeeAmount: String?) = apply { this.refundFeeAmount = refundFeeAmount }
        fun zpTransId(zpTransId: String?) = apply { this.zpTransId = zpTransId }

        fun build() = ZaloPaySystemData(
            appUser, appTransId,
            callbackUrl, refundFeeAmount,
            zpTransId
        )
    }
}
