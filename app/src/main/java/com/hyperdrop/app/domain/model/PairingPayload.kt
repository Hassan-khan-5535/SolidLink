package com.hyperdrop.app.domain.model

import org.json.JSONObject
import java.util.UUID

/**
 * Payload encoded in the QR code or matched via numeric code during pairing.
 * Contains enough info for the receiver to identify and connect to the sender.
 */
data class PairingPayload(
    val sessionId: String = UUID.randomUUID().toString(),
    val deviceName: String,
    val endpointId: String,
    val numericCode: String = generateNumericCode(),
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): String {
        return JSONObject().apply {
            put(KEY_SESSION_ID, sessionId)
            put(KEY_DEVICE_NAME, deviceName)
            put(KEY_ENDPOINT_ID, endpointId)
            put(KEY_NUMERIC_CODE, numericCode)
            put(KEY_TIMESTAMP, timestamp)
        }.toString()
    }

    companion object {
        private const val KEY_SESSION_ID = "sid"
        private const val KEY_DEVICE_NAME = "dn"
        private const val KEY_ENDPOINT_ID = "eid"
        private const val KEY_NUMERIC_CODE = "nc"
        private const val KEY_TIMESTAMP = "ts"

        fun fromJson(json: String): PairingPayload? {
            return try {
                val obj = JSONObject(json)
                PairingPayload(
                    sessionId = obj.getString(KEY_SESSION_ID),
                    deviceName = obj.getString(KEY_DEVICE_NAME),
                    endpointId = obj.getString(KEY_ENDPOINT_ID),
                    numericCode = obj.getString(KEY_NUMERIC_CODE),
                    timestamp = obj.getLong(KEY_TIMESTAMP)
                )
            } catch (e: Exception) {
                null
            }
        }

        private fun generateNumericCode(): String {
            return (100000..999999).random().toString()
        }
    }
}
