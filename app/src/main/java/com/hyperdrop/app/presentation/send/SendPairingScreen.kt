package com.hyperdrop.app.presentation.send

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyperdrop.app.presentation.qrcode.QrCodeDisplay

/**
 * Sender pairing screen. Displays QR code and numeric code for the receiver
 * to scan/enter. When a peer connects, shows an approval screen requiring
 * explicit accept before any bytes move.
 */
@Composable
fun SendPairingScreen(
    viewModel: SendPairingViewModel,
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
            text = "Send — Pair via QR",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        when (val currentState = state) {
            is PairingState.GeneratingPayload -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Preparing pairing code…", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            is PairingState.ShowingQrAndCode -> {
                QrCodeAndCodeDisplay(
                    qrPayload = currentState.payload.toJson(),
                    numericCode = currentState.payload.numericCode,
                    deviceName = currentState.payload.deviceName
                )
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel")
                }
            }

            is PairingState.PeerConnected -> {
                ApprovalCard(
                    peerName = currentState.peerName,
                    authDigits = currentState.authenticationDigits,
                    onAccept = { viewModel.acceptPairing() },
                    onReject = { viewModel.rejectPairing() }
                )
            }

            is PairingState.WaitingForPeerApproval -> {
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

            is PairingState.PairingComplete -> {
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
                            "Ready to transfer files.",
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

            is PairingState.PairingRejected -> {
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

            is PairingState.Error -> {
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
 * Displays the QR code and the 6-digit numeric fallback code.
 * Uses the Prompt 5 [QrCodeDisplay] component — no camera/ZXing duplication.
 */
@Composable
private fun QrCodeAndCodeDisplay(
    qrPayload: String,
    numericCode: String,
    deviceName: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Show this to the receiver",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // QR code — reuses Prompt 5 component
        QrCodeDisplay(
            payload = qrPayload,
            size = 240.dp
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(0.6f))
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Or enter this code manually",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 6-digit numeric code in large monospace
        Text(
            text = numericCode.chunked(3).joinToString(" "),
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Advertising as: $deviceName",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Accept/Reject card shown when a peer initiates a connection.
 * No bytes move until the user explicitly taps Accept.
 */
@Composable
private fun ApprovalCard(
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
                    text = "Connection Request",
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
                    text = "wants to pair with you",
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
