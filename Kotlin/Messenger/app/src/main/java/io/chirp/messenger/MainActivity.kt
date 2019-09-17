package io.chirp.messenger

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import io.chirp.chirpsdk.ChirpSDK
import io.chirp.chirpsdk.models.ChirpSDKState
import io.chirp.chirpsdk.models.ChirpError
import io.chirp.chirpsdk.models.ChirpErrorCode
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.graphics.Typeface


private const val REQUEST_RECORD_AUDIO = 1

private const val CHIRP_APP_KEY = "YOUR_APP_KEY"
private const val CHIRP_APP_SECRET = "YOUR_APP_SECRET"
private const val CHIRP_APP_CONFIG = "YOUR_APP_CONFIG"

private const val TAG = "ChirpMessenger"

class MainActivity : AppCompatActivity() {

    private lateinit var chirpConnect: ChirpSDK

    private lateinit var messageReceived: TextView
    private lateinit var messageToSend: EditText
    private lateinit var sendMessageBtn: Button
    private lateinit var context: Context
    private var maxPayloadLength = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.messageToSend = findViewById(R.id.messageToSend)
        this.messageReceived = findViewById(R.id.messageRreceived)
        this.sendMessageBtn = findViewById(R.id.sendMessage)
        this.context = this

        val calibreLight = Typeface.createFromAsset(assets, "fonts/calibre_light.ttf")
        val calibreMedium = Typeface.createFromAsset(assets, "fonts/calibre_medium.ttf")
        messageToSend.typeface = calibreLight
        messageReceived.typeface = calibreLight
        sendMessageBtn.typeface = calibreMedium

        if (CHIRP_APP_KEY == "" || CHIRP_APP_SECRET == "") {
            Log.e(TAG, "CHIRP_APP_KEY or CHIRP_APP_SECRET is not set. " +
                    "Please update with your CHIRP_APP_KEY/CHIRP_APP_SECRET from developers.chirp.io")
            return
        }

        /**
         * Instantiate SDK with key secret and local config string
         */
        chirpConnect = ChirpSDK(this, CHIRP_APP_KEY, CHIRP_APP_SECRET)
        Log.v(TAG, "Connect Version: " + chirpConnect.version)

        sendMessageBtn.setOnClickListener(sendClickListener)
        messageToSend.addTextChangedListener(textChangedListener)

        val configError = chirpConnect.setConfig(CHIRP_APP_CONFIG)
        if (configError.code > 0) {
            Log.e(TAG, "ChirpError" + configError.message)
        } else {
            maxPayloadLength = chirpConnect.maxPayloadLength()
            val startError = chirpConnect.start()

            if (startError.code > 0) {
                Log.e(TAG, "ChirpError: " + startError.message)
            } else {

                chirpConnect.onSent { data: ByteArray, channel: Int ->
                    /**
                     * onSent is called when a send event has completed.
                     * The data argument contains the payload that was sent.
                     */
                    setButtonStyle("SEND", R.color.send_button_default_bg, true)
                    displayToast("Message sent.")
                }

                chirpConnect.onReceiving { channel: Int ->
                    /**
                     * onReceiving is called when a receive event begins.
                     * No data has yet been received.
                     */
                    setButtonStyle("RECEIVING", R.color.send_button_gray_bg, false)
                    Log.v(TAG, "ConnectCallback: onReceiving on channel: $channel")        }

                chirpConnect.onSending { data: ByteArray, channel: Int ->
                    /**
                     * onSending is called when a send event begins.
                     * The data argument contains the payload being sent.
                     */
                    setButtonStyle("SENDING", R.color.send_button_gray_bg, false)
                    val message = String(data, Charsets.UTF_8)
                    Log.v(TAG, "ConnectCallback: onSending: $message on channel: $channel")
                }

                chirpConnect.onReceived { data: ByteArray?, channel: Int ->
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
                        val message = String(data, Charsets.UTF_8)
                        Log.v(TAG, "ConnectCallback: onReceived: $message on channel: $channel")
                        updateReceivedMessage(message)
                    }
                }

                chirpConnect.onStateChanged { oldState: ChirpSDKState, newState: ChirpSDKState ->
                    /**
                     * onStateChanged is called when the SDK changes state.
                     */
                    Log.v(TAG, "ConnectCallback: onStateChanged $oldState -> $newState")
                }
            }
        }
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

    /**
     * Fired when sent EditText input is changed
     */
    private val textChangedListener = object : TextWatcher {
        var messageToSendText = ""
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val i = s.toString().toByteArray().size
            if (i <= maxPayloadLength) {
                Log.d(TAG, "text size: $i")
                messageToSendText = s.toString()
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
    fun Context.toast(message: CharSequence) =
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
        chirpConnect.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            chirpConnect.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        stopSdk()
    }

    private fun setButtonStyle(title: String, background: Int, isClickable: Boolean) {
        runOnUiThread{
            sendMessageBtn.text = title
            sendMessageBtn.setBackgroundColor(resources.getColor(background))
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
        val error = chirpConnect.stop()
        if (error.code > 0) {
            Log.e(TAG, "ConnectError: " + error.message)
            return
        }
    }

    private fun startSdk() {
        val error = chirpConnect.start()
        if (error.code > 0) {
            Log.e(TAG, "ConnectError: " + error.message)
            return
        }
    }

    private fun sendPayload(payload: String) {
        /**
         * A payload is a byte array dynamic size with a maximum size defined by the config string.
         *
         * Convert String payload to  a byte array, and send it.
         */
        val payload = payload.toByteArray(Charsets.UTF_8)
        val maxPayloadLength = chirpConnect.maxPayloadLength()
        if (payload.size > maxPayloadLength) {
            Log.e("ConnectError: ", "Payload too long")
            return;
        }
        val error = chirpConnect.send(payload)
        if (error.code > 0) {
            val volumeError = ChirpError(ChirpErrorCode.CHIRP_SDK_INVALID_VOLUME, "Volume too low. Please increase volume!")
            if (error.code == volumeError.code) {
                context.toast(volumeError.message)
            }
            Log.e("ConnectError: ", error.message)
        }
    }
}
