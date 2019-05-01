package io.chirp.messenger

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import io.chirp.connect.ChirpConnect
import io.chirp.connect.models.ChirpError
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import io.chirp.connect.models.ChirpErrorCode
import android.graphics.Typeface
import android.widget.*
import io.chirp.connect.models.ChirpConnectState
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.security.Security
import java.util.*

import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


private const val REQUEST_RECORD_AUDIO = 1

/**
 * TODO: Copy and paste your Chirp app key, secret and config
 * below from https://developers.chirp.io
 */
const val CHIRP_APP_KEY = "YOUR_CHIRP_APP_KEY"
const val CHIRP_APP_SECRET = "YOUR_CHIRP_APP_SECRET"
const val CHIRP_APP_CONFIG = "YOUR_CHIRP_APP_CONFIG"

private const val TAG = "ChirpSecureMessenger"

class MainActivity : AppCompatActivity() {

    private lateinit var chirp: ChirpConnect

    private val keyBytes = byteArrayOf(0x43, 0x68, 0x69, 0x72, 0x70, 0x20, 0x48, 0x61, 0x63, 0x6b, 0x61, 0x74, 0x68, 0x6f, 0x6e, 0x21)
    private val ivBytes = generateIvBytes(keyBytes)
    private val key = SecretKeySpec(keyBytes, "AES")
    private val ivSpec = IvParameterSpec(ivBytes)
    private val cipher: Cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC")

    private lateinit var messageReceived: TextView
    private lateinit var messageToSend: EditText
    private lateinit var sendMessageBtn: Button
    private lateinit var context: Context
    private var maxPayloadLength = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Security.addProvider(BouncyCastleProvider())

        this.messageToSend = findViewById(R.id.messageToSend)
        this.messageReceived = findViewById(R.id.messageRreceived)
        this.sendMessageBtn = findViewById(R.id.sendMessage)
        this.context = this

        val calibreLight = Typeface.createFromAsset(assets, "fonts/calibre_light.ttf")
        val calibreMedium = Typeface.createFromAsset(assets, "fonts/calibre_medium.ttf")
        messageToSend.typeface = calibreLight
        messageReceived.typeface = calibreLight
        sendMessageBtn.typeface = calibreMedium

        /**
         * Instantiate SDK with key secret and local config string
         */
        chirp = ChirpConnect(this, CHIRP_APP_KEY, CHIRP_APP_SECRET)
        Log.v(TAG, "ChirpSDK Version: " + chirp.version)

        val setConfigError = chirp.setConfig(CHIRP_APP_CONFIG)
        if (setConfigError.code > 0) {
            Log.e("ChirpError: ", setConfigError.message)
            return

        }

        maxPayloadLength = chirp.maxPayloadLength()
        sendMessageBtn.setOnClickListener(sendClickListener)
        messageToSend.addTextChangedListener(textChangedListener)

        chirp.onSending {
            data: ByteArray, channel: Int ->
            /**
             * onSending is called when a send event begins.
             * The data argument contains the payload being sent.
             */
            setButtonStyle("SENDING", R.color.send_button_gray_bg, false)
            val message = String(data, Charsets.UTF_8)
            Log.v(TAG, "ConnectCallback: onSending: $message on channel: $channel")
        }

        chirp.onSent {
            data: ByteArray, channel: Int ->
            /**
             * onSent is called when a send event has completed.
             * The data argument contains the payload that was sent.
             */
            setButtonStyle("SEND", R.color.send_button_default_bg, true)
            displayToast("Message sent.")
        }

        chirp.onReceiving {
            channel: Int ->
            /**
             * onReceiving is called when a receive event begins.
             * No data has yet been received.
             */
            setButtonStyle("RECEIVING", R.color.send_button_gray_bg, false)
            Log.v(TAG, "ConnectCallback: onReceiving on channel: $channel")
        }

        chirp.onReceived { data: ByteArray?, channel: Int ->
            /**
             * onReceived is called when a receive event has completed.
             * If the payload was decoded successfully, it is passed in data.
             * Otherwise, data is null.
             */
            setButtonStyle("SEND", R.color.send_button_default_bg, true)
            if (data == null) {
                displayToast("Receiving failed.")
            } else {
                displayToast("Received message.")

                val message = String(decryptBytes(data), Charsets.UTF_8)
                Log.v(TAG, "ConnectCallback: onReceived: $message on channel: $channel")
                updateReceivedMessage(message)
            }
        }
        startSdk()
    }

    /**
     * Fired when sent button is clicked
     */
    private val sendClickListener = fun(view: View) {
        view.hideKeyboard()
        val message = messageToSend.text.toString()
        if (message.isEmpty()) {
            displayToast("Please enter a message first.")
        } else {
            sendPayload(messageToSend.text.toString())
        }
    }

    fun encryptBytes(bytes: ByteArray) : ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        val bIn = ByteArrayInputStream(bytes)
        val cIn = CipherInputStream(bIn, cipher)
        val bOut = ByteArrayOutputStream()

        var ch: Int = cIn.read()
        while (ch >= 0) {
            bOut.write(ch)
            ch = cIn.read()
        }

        return bOut.toByteArray()
    }

    private fun decryptBytes(bytes: ByteArray?) : ByteArray {
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        val bOut = ByteArrayOutputStream()
        val cOut = CipherOutputStream(bOut, cipher)
        cOut.write(bytes)
        cOut.close()
       return bOut.toByteArray()
    }

    private fun generateIvBytes(key: ByteArray): ByteArray {
        val now = Date()
        val calendar = Calendar.getInstance()
        calendar.time = now
        val minutes = calendar.get(Calendar.MINUTE)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        var keyBytes = key.plus(hour.toByte())
        keyBytes = keyBytes.plus(minutes.toByte())
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(keyBytes)
        return hash.slice(0..15).toByteArray()
    }

    /**
     * Fired when sent EditText input is changed
     */
    private val textChangedListener = object : TextWatcher {
        var messageToSendText = ""
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val encryptedBytes = encryptBytes(s.toString().toByteArray())
            if (encryptedBytes.size <= maxPayloadLength) {
                Log.d(TAG, "text size: ${encryptedBytes.size}")
                messageToSendText = encryptedBytes.toString()
            } else {
                displayToast("Message too long! Max size is $maxPayloadLength bytes.")
                messageToSend.setText(messageToSendText)
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

    /**
     * Used to hide keyboard when send button is clicked
     */
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    /**
     * Used to display toast popup messages
     */
    private fun Context.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
        } else {
            startSdk()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSdk()
                }
                return
            }
        }
    }

    override fun onPause() {
        super.onPause()
        chirp.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        chirp.close()
    }

    override fun onStop() {
        super.onStop()
        stopSdk()
    }

    private fun setButtonStyle(title: String, background: Int, isClickable: Boolean) {
        runOnUiThread{
            sendMessageBtn.text = title
            val bgColor = ContextCompat.getColor(context, background)
            sendMessageBtn.setBackgroundColor(bgColor)
            sendMessageBtn.isClickable = isClickable
        }
    }

    private fun displayToast(message: String) {
        runOnUiThread{
            context.toast(message)
        }
    }

    private fun updateReceivedMessage(newPayload: String) {
        runOnUiThread { messageReceived.text = newPayload }
    }

    private fun stopSdk() {
        if (chirp.getState() > ChirpConnectState.CHIRP_CONNECT_STATE_STOPPED) {
            val error = chirp.stop()
            if (error.code > 0) {
                Log.e(TAG, "ConnectError: " + error.message)
                return
            }
        }
    }

    private fun startSdk() {
        if (chirp.getState() < ChirpConnectState.CHIRP_CONNECT_STATE_RUNNING) {
            val error = chirp.start()
            if (error.code > 0) {
                Log.e(TAG, "ConnectError: " + error.message)
                return
            }
        }
    }

    private fun sendPayload(payload: String) {
        /**
         * A payload is a byte array dynamic size with a maximum size defined by the config string.
         *
         * Convert String payload to  a byte array, and send it.
         */
        val encryptedPayload = encryptBytes(payload.toByteArray(Charsets.UTF_8))
        val maxPayloadLength = chirp.maxPayloadLength()
        if (encryptedPayload.size > maxPayloadLength) {
            Log.e("ConnectError: ", "Payload too long")
            return
        }
        val error = chirp.send(encryptedPayload)
        if (error.code > 0) {
            val volumeError = ChirpError(ChirpErrorCode.CHIRP_CONNECT_INVALID_VOLUME, "Volume too low. Please increase volume!")
            if (error.code == volumeError.code) {
                context.toast(volumeError.message)
            }
            Log.e("ConnectError: ", error.message)
        }
    }
}
