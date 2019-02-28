package io.chirp.sdkdemoapp

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import io.chirp.connect.ChirpConnect
import io.chirp.connect.models.ChirpConnectState

private const val REQUEST_RECORD_AUDIO = 1
internal var CHIRP_APP_KEY = ""
internal var CHIRP_APP_SECRET = ""
internal var CHIRP_APP_CONFIG = ""


private const val TAG = "ConnectDemoApp"

class MainActivity : AppCompatActivity() {

    private lateinit var chirpConnect: ChirpConnect

    private lateinit var status: TextView
    private lateinit var lastChirp: TextView
    private lateinit var versionView: TextView
    private lateinit var startStopSdkBtn: Button
    private lateinit var sendPayloadBtn: Button
    private lateinit var parentLayout: View

    private var startStopSdkBtnPressed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.parentLayout = findViewById<View>(android.R.id.content)

        this.status = findViewById(R.id.stateValue)
        this.lastChirp = findViewById(R.id.lastChirp)
        this.versionView = findViewById(R.id.versionView)
        this.startStopSdkBtn = findViewById(R.id.startStopSdkBtn)
        this.sendPayloadBtn = findViewById(R.id.sendPayloadBtn)

        sendPayloadBtn.alpha = .4f
        sendPayloadBtn.isClickable = false
        startStopSdkBtn.alpha = .4f
        startStopSdkBtn.isClickable = false

        if (CHIRP_APP_KEY == "" || CHIRP_APP_SECRET == "") {
            Log.e(TAG, "CHIRP_APP_KEY or CHIRP_APP_SECRET is not set. " +
                    "Please update with your CHIRP_APP_KEY/CHIRP_APP_SECRET from developers.chirp.io")
            return
        }

        /**
         * Key and secret initialisation
         */
        this.chirpConnect = ChirpConnect(this, CHIRP_APP_KEY, CHIRP_APP_SECRET)
        Log.v(TAG, "Connect Version: " + chirpConnect.version)
        versionView.text = chirpConnect.version
        val setConfigError = chirpConnect.setConfig(CHIRP_APP_CONFIG)
        if (setConfigError.code > 0) {
            Log.e(TAG, setConfigError.message)
            return
        }
        sendPayloadBtn.setOnClickListener { sendPayload() }
        startStopSdkBtn.alpha = 1f
        startStopSdkBtn.isClickable = true
        startStopSdkBtn.setOnClickListener { startStopSdk() }
        val versionText = chirpConnect.version + "\n" +
                chirpConnect.getProtocolName() + " v" +
                chirpConnect.getProtocolVersion()
        versionView.text = versionText

        chirpConnect.onSent { payload: ByteArray, channel: Int ->
            /**
             * onSent is called when a send event has completed.
             * The payload argument contains the payload data that was sent.
             */
            val hexData: String = payload.toHex()
            updateLastPayload(hexData)
            Log.v(TAG, "ConnectCallback: onSent: $hexData on channel: $channel")
        }

        chirpConnect.onReceived { payload: ByteArray?, channel: Int ->
            /**
             * onReceived is called when a receive event has completed.
             * If the payload was decoded successfully, it is passed in payload.
             * Otherwise, payload is null.
             */
            var hexData = "null"
            if (payload != null) {
                hexData = payload.toHex()
            }
            Log.v(TAG, "ConnectCallback: onReceived: $hexData on channel: $channel")
            updateLastPayload(hexData)
        }

        chirpConnect.onSending { payload: ByteArray, channel: Int ->
            /**
             * onSending is called when a send event begins.
             * The data argument contains the payload being sent.
             */
            val hexData: String = payload.toHex()
            Log.v(TAG, "ConnectCallback: onSending: $hexData on channel: $channel")
            updateLastPayload(hexData)
        }

        chirpConnect.onReceiving { channel: Int ->
            /**
             * onReceiving is called when a receive event begins.
             * No data has yet been received.
             */
            Log.v(TAG, "ConnectCallback: onReceiving on channel: $channel")
        }

        chirpConnect.onStateChanged { oldState: ChirpConnectState, newState: ChirpConnectState ->
            /**
             * onStateChanged is called when the SDK changes state.
             */
            Log.v(TAG, "ConnectCallback: onStateChanged $oldState -> $newState")
            when (newState) {
                ChirpConnectState.CHIRP_CONNECT_STATE_NOT_CREATED -> updateStatus("NotCreated")
                ChirpConnectState.CHIRP_CONNECT_STATE_STOPPED  -> updateStatus("Stopped")
                ChirpConnectState.CHIRP_CONNECT_STATE_PAUSED  -> updateStatus("Paused")
                ChirpConnectState.CHIRP_CONNECT_STATE_RUNNING  -> updateStatus("Running")
                ChirpConnectState.CHIRP_CONNECT_STATE_SENDING  -> updateStatus("Sending")
                ChirpConnectState.CHIRP_CONNECT_STATE_RECEIVING  -> updateStatus("Receiving")
                else -> updateStatus(newState.toString())
            }
        }

        chirpConnect.onSystemVolumeChanged { oldVolume: Int, newVolume: Int ->
            /**
             * onSystemVolumeChanged is called when the system volume is changed.
             *
            */
            val snackBar = Snackbar.make(parentLayout, "System volume has been changed to: $newVolume", Snackbar.LENGTH_LONG)
            snackBar.setAction("CLOSE") { }
                    .setActionTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
                    .show()
            Log.v(TAG, "System volume has been changed, notify user to increase the volume when sending data")
        }
    }


    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
        } else {
            if (startStopSdkBtnPressed) startSdk()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (startStopSdkBtnPressed) stopSdk()
                }
                return
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (!::chirpConnect.isInitialized) return
        chirpConnect.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!::chirpConnect.isInitialized) return
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

    private fun updateStatus(newStatus: String) {
        runOnUiThread { status.text = newStatus }
    }

    private fun updateLastPayload(newPayload: String) {
        runOnUiThread { lastChirp.text = newPayload }
    }

    private fun stopSdk() {
        if (!::chirpConnect.isInitialized) return
        val error = chirpConnect.stop()
        if (error.code > 0) {
            Log.e(TAG, "ConnectError: " + error.message)
            return
        }
        sendPayloadBtn.alpha = .4f
        sendPayloadBtn.isClickable = false
        startStopSdkBtn.text = "Start Sdk"
    }

    private fun startSdk() {
        if (!::chirpConnect.isInitialized) return
        val error = chirpConnect.start()
        if (error.code > 0) {
            Log.e(TAG, "ConnectError: " + error.message)
            return
        }
        sendPayloadBtn.alpha = 1f
        sendPayloadBtn.isClickable = true
        startStopSdkBtn.text = "Stop Sdk"
    }

    private fun startStopSdk() {
        /**
         * Start or stop the SDK.
         * Audio is only processed when the SDK is running.
         */
        startStopSdkBtnPressed = true
        if (chirpConnect.getState() === ChirpConnectState.CHIRP_CONNECT_STATE_STOPPED ) {
            startSdk()
        } else {
            stopSdk()
        }
    }

    private fun sendPayload() {
        /**
         * A payload is a byte array dynamic size with a maximum size defined by the config string.
         *
         * Generate a random payload, and send it.
         */
        val payload = chirpConnect.randomPayload(0)     //size = 0 will generate a random payload size
        val error = chirpConnect.send(payload)
        if (error.code > 0) {
            Log.e("ConnectError: ", error.message)
        }
    }

    fun ByteArray.toHex() = this.joinToString(separator = "") {
        it.toInt().and(0xff).toString(16).padStart(2, '0')
    }

    fun String.hexStringToByteArray() = ByteArray(this.length / 2) {
        this.substring(it * 2, it * 2 + 2).toInt(16).toByte()
    }

}
