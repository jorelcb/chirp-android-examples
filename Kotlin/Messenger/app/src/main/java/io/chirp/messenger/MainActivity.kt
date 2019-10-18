package io.chirp.messenger

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import io.chirp.chirpsdk.ChirpSDK
import io.chirp.chirpsdk.models.ChirpSDKState
import io.chirp.chirpsdk.models.ChirpError
import io.chirp.chirpsdk.models.ChirpErrorCode
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.graphics.Typeface
import android.widget.*
import android.widget.Spinner
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*


private const val REQUEST_RECORD_AUDIO = 1

private const val CHIRP_APP_KEY = "FAbEE46BdbCec398C0A79ffEE"
private const val CHIRP_APP_SECRET = "C43F089D7bCda3bD3f0feF24b189d71B61d32C462dd64BB02C"
private const val CHIRP_APP_CONFIG = "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"

private const val TAG = "ChirpMessenger"

class MainActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {

    private lateinit var chirpSdk: ChirpSDK

    ///
    val buttonTexts = arrayListOf("Buttons", "Text", "Both")
    val ID = 101
    ///

    private lateinit var messageReceived: TextView
    private lateinit var messageToSend: String
    private lateinit var sendMessageBtn: Button
    private lateinit var context: Context
    private var maxPayloadLength = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ///

        val secondRg = RadioGroup(this)
        secondRg.orientation = RadioGroup.HORIZONTAL
        secondRg.weightSum = 3f
        secondRg.id = ID
        secondRg.contentDescription = "Widgets"
        secondRg.setOnCheckedChangeListener(this)
        linearLayout.firstRg.setOnCheckedChangeListener(this)


        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.weight = 0.5f

        for (i in 0 until buttonTexts.size) {
            val radioButton = RadioButton(this)
            radioButton.apply {
                text = buttonTexts[i]
                id = i
                layoutParams = p
            }
            secondRg.addView(radioButton)
        }


        ///

        // HARDCORE

        this.messageToSend = "000123451234Pago exitoso"///findViewById(R.id.messageToSend)
        this.messageReceived = findViewById(R.id.messageReceived)
        this.sendMessageBtn = findViewById(R.id.sendMessage)
        this.context = this

        val calibreLight = Typeface.createFromAsset(assets, "fonts/calibre_light.ttf")
        val calibreMedium = Typeface.createFromAsset(assets, "fonts/calibre_medium.ttf")
        //messageToSend.typeface = calibreLight
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
        chirpSdk = ChirpSDK(this, CHIRP_APP_KEY, CHIRP_APP_SECRET)
        Log.v(TAG, "ChirpSDK Version: " + chirpSdk.version)

        sendMessageBtn.setOnClickListener(sendClickListener)
        //messageToSend.addTextChangedListener(textChangedListener)

        val configError = chirpSdk.setConfig(CHIRP_APP_CONFIG)
        if (configError.code > 0) {
            Log.e(TAG, "ChirpError" + configError.message)
        } else {
            maxPayloadLength = chirpSdk.maxPayloadLength()
            val startError = chirpSdk.start()

            if (startError.code > 0) {
                Log.e(TAG, "ChirpError: " + startError.message)
            } else {

                chirpSdk.onSent { data: ByteArray, channel: Int ->
                    /**
                     * onSent is called when a send event has completed.
                     * The data argument contains the payload that was sent.
                     */
                    setButtonStyle("SEND", R.color.send_button_default_bg, true)
                    displayToast("Message sent.")
                }

                chirpSdk.onReceiving { channel: Int ->
                    /**
                     * onReceiving is called when a receive event begins.
                     * No data has yet been received.
                     */
                    setButtonStyle("RECEIVING", R.color.send_button_gray_bg, false)
                    Log.v(TAG, "ChirpSDKCallback: onReceiving on channel: $channel")        }

                chirpSdk.onSending { data: ByteArray, channel: Int ->
                    /**
                     * onSending is called when a send event begins.
                     * The data argument contains the payload being sent.
                     */
                    setButtonStyle("SENDING", R.color.send_button_gray_bg, false)
                    val message = String(data, Charsets.UTF_8)
                    Log.v(TAG, "ChirpSDKCallback: onSending: $message on channel: $channel")
                }

                chirpSdk.onReceived { data: ByteArray?, channel: Int ->
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
                        var message = String(data, Charsets.UTF_8)
                        val paymentStatus = PaymentStatus(message)
                        message = paymentStatus.formattedStatus()
                        Log.v(TAG, "ChirpSDKCallback: onReceived: $message on channel: $channel")
                        updateReceivedMessage(message, this)
                    }
                }

                chirpSdk.onStateChanged { oldState: ChirpSDKState, newState: ChirpSDKState ->
                    /**
                     * onStateChanged is called when the SDK changes state.
                     */
                    Log.v(TAG, "ChirpSDKCallback: onStateChanged $oldState -> $newState")
                }
            }
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkId: Int) {
        val checkedRadioButton = group?.findViewById(group.checkedRadioButtonId) as? RadioButton
        checkedRadioButton?.let {

            if (checkedRadioButton.isChecked)
                Toast.makeText(applicationContext, "RadioGroup: ${group?.contentDescription} RadioButton: ${checkedRadioButton?.text}", Toast.LENGTH_LONG).show()
        }

    }

    fun androidlyRadioButton(view: View) {

        val radioButton = view as RadioButton
        Toast.makeText(applicationContext, "Radio Button: ${radioButton.text}", Toast.LENGTH_LONG).show()
    }


    /**
     * Fired when sent button is clicked
     */
    private val sendClickListener = fun(view: View) {
        view.hideKeyboard()
        val message = messageToSend //messageToSend.text.toString()
        if (message.isEmpty()) {
            displayToast("Please enter a message first.")
        } else {
            sendPayload(messageToSend)//messageToSend.text.toString())
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
                //messageToSend.setText(messageToSendText)
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
        chirpSdk.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            chirpSdk.close()
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

    private fun updateReceivedMessage(newPayload: String, context: Context) {
        runOnUiThread {
            if(newPayload.contains("rechazado")){
                messageReceived.text = newPayload
                messageReceived.setBackGroundColor(0xFF000000) // NEGRO
                messageReceived.setTextColor(Color.parseColor("#F25A5A")) // ROJO
                ///val intent = Intent(context, MainActivity::class.java)
                ///context.startActivity(intent)
            } else {
                messageReceived.text = newPayload
                messageReceived.setBackGroundColor(0xFF000000) // NEGRO
                messageReceived.setTextColor(Color.parseColor("#01BC14")) // VERDE
            }

        }
    }

    fun TextView.setBackGroundColor(color: Long) = this.setBackgroundColor(color.toInt())

    private fun stopSdk() {
        val error = chirpSdk.stop()
        if (error.code > 0) {
            Log.e(TAG, "ChirpSDKError: " + error.message)
            return
        }
    }

    private fun startSdk() {
        val error = chirpSdk.start()
        if (error.code > 0) {
            Log.e(TAG, "ChirpSDKError: " + error.message)
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
        val maxPayloadLength = chirpSdk.maxPayloadLength()
        if (payload.size > maxPayloadLength) {
            Log.e("ChirpSDKError: ", "Payload too long")
            return;
        }
        val error = chirpSdk.send(payload)
        if (error.code > 0) {
            val volumeError = ChirpError(ChirpErrorCode.CHIRP_SDK_INVALID_VOLUME, "Volume too low. Please increase volume!")
            if (error.code == volumeError.code) {
                context.toast(volumeError.message)
            }
            Log.e("ChirpSDKError: ", error.message)
        }
    }
}
