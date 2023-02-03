package com.xplore.paymobile.interactiondetection

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import javax.inject.Inject

class AppLifecycleCallbacks @Inject constructor(private val userInteractionDetector: UserInteractionDetector) :
    ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        //No implementation
    }

    override fun onActivityStarted(activity: Activity) {
        //No implementation
    }

    override fun onActivityResumed(activity: Activity) {
        activity.window.decorView.setOnClickListener {
            userInteractionDetector.onUserInteraction()
        }
    }

    override fun onActivityPaused(activity: Activity) {
        //No implementation
    }

    override fun onActivityStopped(activity: Activity) {
        //No implementation
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
        //No implementation
    }

    override fun onActivityDestroyed(activity: Activity) {
        //No implementation
    }
}