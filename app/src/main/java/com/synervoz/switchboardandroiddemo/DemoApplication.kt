package com.synervoz.switchboardandroiddemo

import android.app.Application
import com.synervoz.switchboard.sdk.Switchboard
import com.synervoz.switchboardsherpa.SherpaExtension
import com.synervoz.switchboardsilerovad.SileroVADExtension
import com.synervoz.switchboardwhisper.WhisperExtension

/**
 * Loads the Switchboard extensions and initializes the SDK exactly once per process.
 *
 * Initialization must happen a single time for the whole app — re-registering the
 * extensions (e.g. by initializing again for each example screen) makes
 * [Switchboard.initialize] return an error. Doing it here, in Application.onCreate,
 * guarantees a single initialization even if the activity is recreated.
 */
class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        WhisperExtension.load()
        SileroVADExtension.load()
        SherpaExtension.load()

        val initResult = Switchboard.initialize(
            context = this,
            appId = "demo",
            appSecret = "demo",
            extensions = mapOf(
                "Whisper" to emptyMap<String, Any>(),
                "Silero" to emptyMap<String, Any>(),
                "Sherpa" to emptyMap<String, Any>(),
            ),
        )

        if (initResult.isError) {
            throw RuntimeException("Failed to initialize Switchboard SDK: ${initResult.error}")
        }
    }
}
