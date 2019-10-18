package io.chirp.messenger

import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import kotlin.test.assertFailsWith

class PaymentStatusTest {

    private var paymentId: String = ""
    private var paymentDetails: String = ""
    private var statusCode: String = ""
    private var statusDescription: String = ""

    @Before
    fun setUp() {
        statusCode = "01"
        paymentId = "0123456789"
        paymentDetails = "PaymentOk"
        statusDescription = TransactionStatus.valueOf("_$statusCode").description()

    }

    @Test
    fun toStringTest() {
        val paymentStatus = PaymentStatus("$statusCode$paymentId$paymentDetails")

        assertEquals(paymentStatus.toString(), "PaymentStatus(statusDescription=$statusDescription, paymentId=$paymentId, paymentDetails='$paymentDetails')")
    }

    @Test
    fun zeroPaymentIdTest() {
        val zeroCode = "00"
        val statusDescription = TransactionStatus.valueOf("_$zeroCode").description()
        val paymentStatus = PaymentStatus("$zeroCode$paymentId$paymentDetails")

        assertEquals(paymentStatus.toString(), "PaymentStatus(statusDescription=$statusDescription, paymentId=$paymentId, paymentDetails='$paymentDetails')")
    }

    @Test
    fun notFoundPaymentIdTest() {
        val zeroCode = "99"
        assertFailsWith<IllegalArgumentException> {
            TransactionStatus.valueOf("_$zeroCode").description()
        }
    }

    @Test
    fun formattedStatusTest() {
        val code = "Código: " + this.paymentId
        val paymentStatus = PaymentStatus("$statusCode$paymentId$paymentDetails")
        assertEquals(paymentStatus.formattedStatus(), "$statusDescription\n" + "$paymentDetails\n" + code)
    }
}