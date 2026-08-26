package com.hyperdrop.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hyperdrop.app.presentation.qrcode.QrCodeDisplay
import com.hyperdrop.app.presentation.qrcode.QrScannerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var mode by remember { mutableStateOf("MENU") } // MENU, GENERATE, SCAN
                    var scannedData by remember { mutableStateOf("") }

                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (mode) {
                            "MENU" -> {
                                Button(onClick = { mode = "GENERATE" }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Show QR Code")
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { mode = "SCAN" }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Scan QR Code")
                                }
                                if (scannedData.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Text("Last Scanned: $scannedData", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                            "GENERATE" -> {
                                Text("Generated QR Code", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(16.dp))
                                QrCodeDisplay(payload = "HyperDrop-Test-Payload-12345")
                                Spacer(modifier = Modifier.height(32.dp))
                                Button(onClick = { mode = "MENU" }) {
                                    Text("Back")
                                }
                            }
                            "SCAN" -> {
                                Text("Scanning...", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(16.dp))
                                QrScannerView(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    onQrCodeScanned = { data ->
                                        scannedData = data
                                        Toast.makeText(this@MainActivity, "Scanned: $data", Toast.LENGTH_SHORT).show()
                                        mode = "MENU"
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { mode = "MENU" }) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
