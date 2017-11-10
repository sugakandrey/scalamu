package org.scalamu.core.process

import java.lang.management.{ManagementFactory, MemoryNotificationInfo, MemoryPoolMXBean}
import javax.management.openmbean.CompositeData
import javax.management.{Notification, NotificationEmitter, NotificationListener}

import org.scalamu.core.die
import org.scalamu.core.api.OutOfMemory

object MemoryWatcher {
  def startMemoryWatcher(thresholdPercentage: Int): Unit = {
    val memoryBean = ManagementFactory.getMemoryMXBean
    val emitter    = memoryBean.asInstanceOf[NotificationEmitter]
    val listener: NotificationListener = (notification: Notification, _: Any) =>
      notification.getType match {
        case MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED =>
          val data            = notification.getUserData.asInstanceOf[CompositeData]
          val memoryUsageInfo = MemoryNotificationInfo.from(data)
          scribe.warn(
            s"Memory pool ${memoryUsageInfo.getPoolName} has exceeded configured threshold " +
              s"of $thresholdPercentage%. Memory usage: ${memoryUsageInfo.getUsage}."
          )
          die(OutOfMemory)
        case _ => scribe.warn(s"Unknown notification type in $notification")
    }
    emitter.addNotificationListener(listener, null, null)
    watchAllPools(thresholdPercentage)
  }

  private def watchAllPools(thresholdPercentage: Int): Unit = {
    val pools = ManagementFactory.getMemoryPoolMXBeans
    pools.forEach(watchPool(thresholdPercentage))
  }

  private def watchPool(thresholdPercentage: Int)(pool: MemoryPoolMXBean): Unit =
    if (pool.isUsageThresholdSupported) {
      val usage     = pool.getUsage
      val max       = usage.getMax
      val threshold = (max * thresholdPercentage) / 100
      pool.setUsageThreshold(threshold)
    }
}
