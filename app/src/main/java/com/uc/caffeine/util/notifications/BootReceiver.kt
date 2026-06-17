package com.uc.caffeine.util.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.uc.caffeine.data.CaffeineDatabase
import com.uc.caffeine.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = SettingsRepository(context)
                val settings = repo.settingsFlow.first()
                val lastOpenedAt = repo.getLastAppOpenedAt()
                NotificationScheduler.scheduleAllFromSettings(context, settings, lastOpenedAt)

                // Alarms don't survive reboot — re-arm reminders for future-dated drinks.
                val futureEntries = CaffeineDatabase.getDatabase(context)
                    .consumptionLogDao()
                    .getFutureEntriesOnce(System.currentTimeMillis())
                futureEntries.forEach { entry ->
                    NotificationScheduler.scheduleDrinkReminder(
                        context = context,
                        entryId = entry.id,
                        drinkName = entry.drinkName,
                        quantity = entry.quantity,
                        triggerAtMillis = entry.startedAtMillis,
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
