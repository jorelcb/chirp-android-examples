package io.chirp.messenger

class PaymentStatus {

    private var statusCode: String
    private var statusDescription: String
    private var responseHash: String
    private var paymentId: String
    private var paymentDetails: String

    constructor(_responseHash: String) {
        this.responseHash = _responseHash
        if (responseHash.length < 32) {
            responseHash = responseHash.padEnd(32, ' ')
        }

        this.statusCode = "_" + responseHash.subSequence(0, 2) as String
        this.statusDescription = TransactionStatus.valueOf(this.statusCode).description()
        this.paymentId = responseHash.subSequence(2, 12) as String
        this.paymentDetails = responseHash.subSequence(12, 31).trim() as String
    }

    fun formattedStatus(): String {
        var code = ""
        if (statusCode == Companion.SUCCESSFUL_STATUS) {
            code = "Código: " + this.paymentId
        }
        return "$statusDescription\n" +
                "$paymentDetails\n" + code
    }

    override fun toString(): String {
        return "PaymentStatus(statusDescription=$statusDescription, " +
                "paymentId=$paymentId, paymentDetails='$paymentDetails')"
    }

    companion object {
        const val SUCCESSFUL_STATUS = "_01"
    }
}
