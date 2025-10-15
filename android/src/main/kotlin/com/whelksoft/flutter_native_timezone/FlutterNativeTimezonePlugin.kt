package com.whelksoft.flutter_native_timezone

import android.os.Build
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import java.time.ZoneId
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.*
import kotlin.collections.ArrayList

class FlutterNativeTimezonePlugin : FlutterPlugin, MethodCallHandler {

    private lateinit var channel: MethodChannel

    // backward compatibility with flutter api v1
    companion object {
        @JvmStatic
        @Suppress("DEPRECATION", "UNCHECKED_CAST")
        fun registerWith(registrar: Any) {
            try {
                val plugin = FlutterNativeTimezonePlugin()
                // Use reflection to access messenger() method for backward compatibility
                val messenger = registrar.javaClass.getMethod("messenger").invoke(registrar) as BinaryMessenger
                plugin.setupMethodChannel(messenger)
            } catch (e: Exception) {
                // Fallback for newer Flutter versions where Registrar might not be available
                // This ensures the plugin doesn't crash on newer Flutter versions
            }
        }
    }

    override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        setupMethodChannel(binding.binaryMessenger)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getLocalTimezone" -> result.success(getLocalTimezone())

            "getAvailableTimezones" -> result.success(getAvailableTimezones())

            else -> result.notImplemented()
        }
    }

    private fun getLocalTimezone(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ZoneId.systemDefault().id
        } else {
            TimeZone.getDefault().id
        }
    }

    private fun getAvailableTimezones(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ZoneId.getAvailableZoneIds().toCollection(ArrayList())
        } else {
            TimeZone.getAvailableIDs().toCollection(ArrayList())
        }
    }

    private fun setupMethodChannel(messenger: BinaryMessenger) {
        channel = MethodChannel(messenger, "flutter_native_timezone")
        channel.setMethodCallHandler(this)
    }
}
