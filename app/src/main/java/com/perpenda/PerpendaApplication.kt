package com.perpenda

import android.app.Application
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid

class PerpendaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Crash diagnostics — release builds only. Debug builds stay off so
        // dev iterations don't fill the dashboard; logcat covers local work.
        if (BuildConfig.DEBUG) return
        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            // isSendDefaultPii=false only gates IP address. Sentry Android still
            // attaches a generated per-install user.id by default, which would
            // contradict the privacy policy's "no personal info" claim. Strip
            // it (and any user object) at send time.
            options.isSendDefaultPii = false
            options.beforeSend = SentryOptions.BeforeSendCallback { event, _ ->
                event.user = null
                event
            }
            // No performance tracing — only crash + error reporting for now.
            options.tracesSampleRate = 0.0
        }
    }
}
