package com.example.monitorpharmacyapp

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class MonitorInterface : AppCompatActivity(), View.OnClickListener {
    private var stream_thread: HandlerThread? = null
    private var flash_thread: HandlerThread? = null
    private var rssi_thread: HandlerThread? = null
    private var stream_handler: Handler? = null
    private var flash_handler: Handler? = null
    private var rssi_handler: Handler? = null
    private var flash_button: Button? = null
    private var monitor: ImageView? = null
    private var rssi_text: TextView? = null
    lateinit var ip_text: EditText
    private val ID_CONNECT = 200
    private val ID_FLASH = 201
    private val ID_RSSI = 202
    private var flash_on_off = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor_interface)

        findViewById<View>(R.id.connect).setOnClickListener(this)
        findViewById<View>(R.id.flash).setOnClickListener(this)
        flash_button = findViewById(R.id.flash)
        monitor = findViewById(R.id.monitor)
        rssi_text = findViewById(R.id.rssi)
        ip_text = findViewById(R.id.ip)
        ip_text.setText("192.168.1.3")
        stream_thread = HandlerThread("http")
        stream_thread!!.start()
        stream_handler = HttpHandler(stream_thread!!.looper)
        flash_thread = HandlerThread("http")
        flash_thread!!.start()
        flash_handler = HttpHandler(flash_thread!!.looper)
        rssi_thread = HandlerThread("http")
        rssi_thread!!.start()
        rssi_handler = HttpHandler(rssi_thread!!.looper)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.connect -> {
                stream_handler!!.sendEmptyMessage(ID_CONNECT)
                rssi_handler!!.sendEmptyMessage(ID_RSSI)
                Toast.makeText(this,"Loading...",Toast.LENGTH_SHORT).show()
            }
            R.id.flash -> flash_handler!!.sendEmptyMessage(ID_FLASH)
            else -> {}
        }
    }

    private inner class HttpHandler(looper: Looper?) : Handler(looper!!) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                ID_CONNECT -> VideoStream()
                ID_FLASH -> SetFlash()
                ID_RSSI -> GetRSSI()
                else -> {}
            }
        }
    }

    private fun SetFlash() {
        flash_on_off = flash_on_off xor true
        val flash_url: String
        flash_url = if (flash_on_off) {
            "http://" + ip_text!!.text + ":80/led?var=flash&val=1"
        } else {
            "http://" + ip_text!!.text + ":80/led?var=flash&val=0"
        }
        try {
            val url = URL(flash_url)
            val huc = url.openConnection() as HttpURLConnection
            huc.requestMethod = "GET"
            huc.connectTimeout = 1000 * 5
            huc.readTimeout = 1000 * 5
            huc.doInput = true
            huc.connect()
            if (huc.responseCode == 200) {
                val `in` = huc.inputStream
                val isr = InputStreamReader(`in`)
                val br = BufferedReader(isr)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun GetRSSI() {
        rssi_handler!!.sendEmptyMessageDelayed(ID_RSSI, 500)
        val rssi_url = "http://" + ip_text!!.text + ":80/RSSI"
        try {
            val url = URL(rssi_url)
            try {
                val huc = url.openConnection() as HttpURLConnection
                huc.requestMethod = "GET"
                huc.connectTimeout = 1000 * 5
                huc.readTimeout = 1000 * 5
                huc.doInput = true
                huc.connect()
                if (huc.responseCode == 200) {
                    val `in` = huc.inputStream
                    val isr = InputStreamReader(`in`)
                    val br = BufferedReader(isr)
                    val data = br.readLine()
                    if (!data.isEmpty()) {
                        runOnUiThread { rssi_text!!.text = data }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }

    private fun VideoStream() {
        val stream_url = "http://" + ip_text!!.text + ":81/stream"
        var bis: BufferedInputStream? = null
        val fos: FileOutputStream? = null
        try {
            val url = URL(stream_url)
            try {
                val huc = url.openConnection() as HttpURLConnection
                huc.requestMethod = "GET"
                huc.connectTimeout = 1000 * 5
                huc.readTimeout = 1000 * 5
                huc.doInput = true
                huc.connect()
                if (huc.responseCode == 200) {
                    val `in` = huc.inputStream
                    val isr = InputStreamReader(`in`)
                    val br = BufferedReader(isr)
                    var data: String
                    var len: Int
                    var buffer: ByteArray
                    while (br.readLine().also { data = it } != null) {
                        if (data.contains("Content-Type:")) {
                            data = br.readLine()
                            len = data.split(":").toTypedArray()[1].trim { it <= ' ' }.toInt()
                            bis = BufferedInputStream(`in`)
                            buffer = ByteArray(len)
                            var t = 0
                            while (t < len) {
                                t += bis.read(buffer, t, len - t)
                            }
                            Bytes2ImageFile(buffer,
                                getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/0A.jpg")
                            val bitmap =
                                BitmapFactory.decodeFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/0A.jpg")
                            runOnUiThread { monitor!!.setImageBitmap(bitmap) }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } finally {
            try {
                bis?.close()
                fos?.close()
                stream_handler!!.sendEmptyMessageDelayed(ID_CONNECT, 3000)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun Bytes2ImageFile(bytes: ByteArray, fileName: String) {
        try {
            val file = File(fileName)
            val fos = FileOutputStream(file)
            fos.write(bytes, 0, bytes.size)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "MonitorInterface::"
    }
}