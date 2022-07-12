package com.vaca.bleserveryes

import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var gattServiceConn: GattServiceConn? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BleServer.dataScope.launch {
            delay(10000);
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter.name = "TY_M0111581886"
        }

        val gattCharacteristicValue = findViewById<EditText>(R.id.editTextGattCharacteristicValue)
        gattCharacteristicValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                gattServiceConn?.binding?.setMyCharacteristicValue(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        // Startup our Bluetooth GATT service explicitly so it continues to run even if
        // this activity is not in focus
        startForegroundService(Intent(this, GattService::class.java))
    }

    override fun onStart() {
        super.onStart()

        val latestGattServiceConn = GattServiceConn()
        if (bindService(Intent(this, GattService::class.java), latestGattServiceConn, 0)) {
            gattServiceConn = latestGattServiceConn
        }
    }

    override fun onStop() {
        super.onStop()

        if (gattServiceConn != null) {
            unbindService(gattServiceConn!!)
            gattServiceConn = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // We only want the service around for as long as our app is being run on the device
        stopService(Intent(this, GattService::class.java))
    }


    private class GattServiceConn : ServiceConnection {
        var binding: DeviceAPI? = null

        override fun onServiceDisconnected(name: ComponentName?) {
            binding = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binding = service as? DeviceAPI
        }
    }
}