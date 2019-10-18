package io.chirp.messenger;

public enum TransactionStatus {
    _00 {
        @Override
        String description() {
            return "Transaction Failed";
        }
    },
    _01 {
        @Override
        String description() {
            return "Transaction successful";
        }
    },
    _09 {
        @Override
        String description() {
            return "Insufficient Funds";
        }
    };

    abstract String description();
}
