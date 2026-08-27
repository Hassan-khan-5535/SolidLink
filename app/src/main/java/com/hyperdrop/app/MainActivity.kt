package com.hyperdrop.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hyperdrop.app.presentation.receive.ReceivePairingScreen
import com.hyperdrop.app.presentation.receive.ReceivePairingViewModel
import com.hyperdrop.app.presentation.send.SendPairingScreen
import com.hyperdrop.app.presentation.send.SendPairingViewModel
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
                    var mode by remember { mutableStateOf("MENU") } // MENU, SEND, RECEIVE

                    when (mode) {
                        "MENU" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "HyperDrop",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "P2P File Transfer",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(48.dp))

                                Button(
                                    onClick = { mode = "SEND" },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Send (Show QR)")
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = { mode = "RECEIVE" },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Receive (Scan QR)")
                                }
                            }
                        }
                        "SEND" -> {
                            val viewModel: SendPairingViewModel = hiltViewModel()
                            SendPairingScreen(
                                viewModel = viewModel,
                                onBack = { mode = "MENU" },
                                onPairingComplete = { endpointId, peerName ->
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Paired with $peerName — ready for transfer (Prompt 7+)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    // Transfer flow will be wired in Prompt 7-8
                                }
                            )
                        }
                        "RECEIVE" -> {
                            val viewModel: ReceivePairingViewModel = hiltViewModel()
                            ReceivePairingScreen(
                                viewModel = viewModel,
                                onBack = { mode = "MENU" },
                                onPairingComplete = { endpointId, peerName ->
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Paired with $peerName — ready to receive (Prompt 7+)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    // Transfer flow will be wired in Prompt 7-8
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
