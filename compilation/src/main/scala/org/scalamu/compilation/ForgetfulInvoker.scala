package org.scalamu.compilation

import scoverage.Invoker.measurementFile
import scoverage.Platform.{FileWriter, ThreadSafeMap}

object ForgetfulInvoker {

  private val threadFiles = new ThreadLocal[ThreadSafeMap[String, FileWriter]]
  private val ids = ThreadSafeMap.empty[(String, Int), Any]
  
  def invoked(id: Int, dataDir: String): Unit = {
    if (!ids.contains((dataDir, id))) {
      var files = threadFiles.get()
      if (files == null) {
        files = ThreadSafeMap.empty[String, FileWriter]
        threadFiles.set(files)
      }
      val writer = files.getOrElseUpdate(dataDir, new FileWriter(measurementFile(dataDir), true))
      writer.append(id.toString + '\n').flush()

      ids.put((dataDir, id), ())
    }
  }
  
  def forget(): Unit = ids.clear()
}
