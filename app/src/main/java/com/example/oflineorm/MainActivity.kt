package com.example.oflineorm

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.oflineorm.ui.MainScreen
import com.example.oflineorm.ui.theme.OflineormTheme
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    private val TAG = "RECEIPT_TRACKER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- 1. Robustly Handle Shared Intent Data ---
        val imageUri: Uri? = when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("image/") == true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
                    }
                } else {
                    null
                }
            }
            else -> null
        }

        // This is the correct robust ID extraction logic
        val sourcePackageId = imageUri?.authority?.substringBefore(".fileprovider")
            ?: intent.getStringExtra("android.intent.extra.PACKAGE_NAME")
            ?: "UNKNOWN"

        logIntentDetails(intent, imageUri, sourcePackageId)

        setContent {
            OflineormTheme {
                MainScreen(
                    imageUri = imageUri,
                    sourcePackageId = sourcePackageId,
                    context = this
                )
            }
        }
    }

    private fun logIntentDetails(intent: Intent, imageUri: Uri?, sourcePackageId: String) {
        val json = JSONObject().apply {
            put("action", intent.action ?: "N/A")
            put("type", intent.type ?: "N/A")
            put("source_package_id", sourcePackageId)
            put("image_uri", imageUri?.toString() ?: "N/A")
            val extras = intent.extras
            if (extras != null) {
                val extrasJson = JSONObject()
                for (key in extras.keySet()) {
                    val value = extras.get(key)
                    if (value is String || value is Int || value is Boolean) {
                        extrasJson.put(key, value)
                    } else if (key == Intent.EXTRA_TEXT && value is CharSequence) {
                        extrasJson.put(key, value.toString())
                    } else {
                        extrasJson.put(key, value?.javaClass?.simpleName ?: "null")
                    }
                }
                put("extras", extrasJson)
            }
        }
        Log.d(TAG, "Intent Details (JSON): \n" + json.toString(4))
    }
}
