package com.hyperdrop.app.presentation.receive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyperdrop.app.presentation.qrcode.QrScannerView

/**
 * Receiver pairing screen. Shows the Prompt 5 QR scanner or a numeric code
 * entry field. On connection initiation, shows an approval card requiring
 * explicit accept before any bytes move.
 */
@Composable
fun ReceivePairingScreen(
    viewModel: ReceivePairingViewModel,
    onBack: () -> Unit,
    onPairingComplete: (endpointId: String, peerName: String) -> Unit = { _, _ -> }
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Receive — Scan QR",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (val currentState = state) {
            is ReceivePairingState.Scanning -> {
                ScanningContent(
                    onQrScanned = { viewModel.onQrScanned(it) },
                    onSwitchToCode = { viewModel.switchToCodeEntry() },
                    onBack = onBack
                )
            }

            is ReceivePairingState.EnteringCode -> {
                NumericCodeEntry(
                    onCodeSubmitted = { viewModel.onNumericCodeSubmitted(it) },
                    onSwitchToScan = { viewModel.switchToScanning() },
                    onBack = onBack
                )
            }

            is ReceivePairingState.Connecting -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Connecting to ${currentState.peerName}…",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is ReceivePairingState.WaitingForApproval -> {
                ReceiverApprovalCard(
                    peerName = currentState.peerName,
                    authDigits = currentState.authenticationDigits,
                    onAccept = { viewModel.acceptPairing() },
                    onReject = { viewModel.rejectPairing() }
                )
            }

            is ReceivePairingState.WaitingForSenderApproval -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Waiting for ${currentState.peerName} to accept…",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is ReceivePairingState.Approved -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Paired with ${currentState.peerName}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Ready to receive files.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            onPairingComplete(currentState.endpointId, currentState.peerName)
                        }) {
                            Text("Continue")
                        }
                    }
                }
            }

            is ReceivePairingState.Rejected -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❌", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Pairing Rejected",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Try Again")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onBack) {
                            Text("Go Back")
                        }
                    }
                }
            }

            is ReceivePairingState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            currentState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onBack) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
}

/**
 * QR scanner content. Reuses the Prompt 5 [QrScannerView] component —
 * no camera/ZXing duplication.
 */
@Composable
private fun ScanningContent(
    onQrScanned: (String) -> Unit,
    onSwitchToCode: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Point camera at the sender's QR code",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // QR scanner — reuses Prompt 5 component
        QrScannerView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            onQrCodeScanned = onQrScanned
        )

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onSwitchToCode) {
            Text("Enter code manually instead")
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel")
        }
    }
}

/**
 * Numeric code entry screen — fallback when scanning isn't possible.
 */
@Composable
private fun NumericCodeEntry(
    onCodeSubmitted: (String) -> Unit,
    onSwitchToScan: () -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter Pairing Code",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter the 6-digit code shown on the sender's screen",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { newValue ->
                // Only allow digits, max 6 characters
                if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                    code = newValue
                }
            },
            label = { Text("6-digit code") },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                letterSpacing = 4.sp
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onCodeSubmitted(code) },
            enabled = code.length == 6,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Connect")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onSwitchToScan) {
            Text("Scan QR code instead")
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel")
        }
    }
}

/**
 * Accept/Reject card shown when connection is initiated.
 * No bytes move until the user explicitly taps Accept.
 */
@Composable
private fun ReceiverApprovalCard(
    peerName: String,
    authDigits: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Confirm Pairing",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = peerName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pair with this device?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (authDigits.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Verification code",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = authDigits,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Confirm this code matches the sender's screen",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Reject")
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}
