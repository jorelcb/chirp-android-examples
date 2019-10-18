package io.chirp.messenger

enum class TransactionStatus {
    _00 {
        override fun description(): String {
            return "Transaction Failed"
        }
    },
    _01 {
        override fun description(): String {
            return "Transaction successful"
        }
    },
    _09 {
        override fun description(): String {
            return "Insufficient Funds"
        }
    };

    internal abstract fun description(): String
}
