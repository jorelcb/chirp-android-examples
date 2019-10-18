package io.chirp.messenger

class Payment {

    private val payerId: String
    private val amount: Int
    private val productId: Int
    private var security = ""

    constructor(payerId: String, amount: Int, productId: Int) {
        this.payerId = payerId
        this.amount = amount
        this.productId = productId
    }

    override fun toString(): String {
        return "$payerId$amount$productId$security"
    }
}
