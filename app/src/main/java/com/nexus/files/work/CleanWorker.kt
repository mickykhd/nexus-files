package com.nexus.files.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nexus.files.data.CleanerRepository
import com.nexus.files.data.FileRepository
import com.nexus.files.data.VaultRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Real scheduled housekeeping: purges junk/cache older than 7 days and
 * enforces vault auto-destruct deadlines. Runs weekly (Sunday 03:00 local).
 */
class CleanWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val files = FileRepository(applicationContext)
        val cleaner = CleanerRepository(files)
        val vault = VaultRepository(applicationContext)

        val purgedVault = runCatching { vault.purgeExpired() }.getOrDefault(0)

        val weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val report = runCatching { cleaner.scan() }.getOrNull()
        val staleJunk = report?.cacheFiles.orEmpty().filter { it.modified < weekAgo }
        val purgedJunk = runCatching { cleaner.cleanCache(staleJunk) }.getOrDefault(0)

        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "nexus-deep-clean"

        /** Next Sunday 03:00 local time. */
        fun nextRunAt(): Long {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 3)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1)
                while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            return cal.timeInMillis
        }

        fun schedule(context: Context): Long {
            val firstRun = nextRunAt()
            val delay = (firstRun - System.currentTimeMillis()).coerceAtLeast(60_000L)
            val request = PeriodicWorkRequestBuilder<CleanWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
            return firstRun
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)
        }

        suspend fun isScheduled(context: Context): Boolean = runCatching {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(UNIQUE_NAME)
                .get()
                .any { !it.state.isFinished }
        }.getOrDefault(false)
    }
}
