package com.perpenda

import android.app.Application
import io.sentry.android.core.SentryAndroid

class PerpendaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Crash diagnostics — release builds only. Debug builds stay off so
        // dev iterations don't fill the dashboard; logcat covers local work.
        if (BuildConfig.DEBUG) return
        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            // Never attach PII to events; crash diagnosis doesn't need it.
            options.isSendDefaultPii = false
            // No performance tracing — only crash + error reporting for now.
            options.tracesSampleRate = 0.0
        }
    }
}
