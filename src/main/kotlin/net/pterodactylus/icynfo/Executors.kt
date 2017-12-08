package net.pterodactylus.icynfo

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

fun ScheduledExecutorService.withFixedDelay(delay: Long, unit: TimeUnit, initialDelay: Long = 0, action: () -> Unit) =
		scheduleWithFixedDelay(action, initialDelay, delay, unit)!!
