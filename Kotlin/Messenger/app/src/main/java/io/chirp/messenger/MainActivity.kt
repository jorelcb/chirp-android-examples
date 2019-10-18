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

private const val CHIRP_APP_KEY = "a7d694f9dF55fD4e511fb64e1";
private const val CHIRP_APP_SECRET = "C6F271A258CBA5Ee3E5eD89c603C738F4D682A65F9FeDC19d9";
private const val CHIRP_APP_CONFIG = "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";

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
                        val message = String(data, Charsets.UTF_8)
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
