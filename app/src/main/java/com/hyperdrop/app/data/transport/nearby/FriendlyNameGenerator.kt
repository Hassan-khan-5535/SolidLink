package com.hyperdrop.app.data.transport.nearby

import android.os.Build
import java.util.UUID

object FriendlyNameGenerator {
    fun generateName(): String {
        val model = Build.MODEL
        // Append a short random 4-character suffix to handle identical models
        val suffix = UUID.randomUUID().toString().substring(0, 4).uppercase()
        return "$model-$suffix"
    }
}
