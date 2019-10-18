package io.chirp.messenger

enum class ItemMessages {
    ITEM1 {
        override fun message(): String {
            return "1111111110000012990003509455555"
        }
    },
    ITEM2 {
        override fun message(): String {
            return "111111111000008990003509355555"
        }
    },
    ITEM3 {
        override fun message(): String {
            return "111111111000006990003509355555"
        }
    };

    internal abstract fun message(): String
}