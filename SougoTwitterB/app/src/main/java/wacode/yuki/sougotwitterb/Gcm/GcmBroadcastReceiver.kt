package wacode.yuki.sougotwitterb.Gcm

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v4.content.WakefulBroadcastReceiver

/**
 * Created by yuki on 2016/05/16.
 */
class GcmBroadcastReceiver : WakefulBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val comp = ComponentName(context.packageName,GcmIntentService::class.java.name)
        startWakefulService(context,(intent.setComponent(comp)))
        resultCode = Activity.RESULT_OK
    }
}