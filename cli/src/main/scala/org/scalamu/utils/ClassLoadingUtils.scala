package org.scalamu.utils

import java.net.{URL, URLClassLoader}
import java.nio.file.Path

trait ClassLoadingUtils {
  def withContextClassLoader[T](loader: ClassLoader)(f: => T): T = {
    val oldClassLoader = Thread.currentThread.getContextClassLoader
    Thread.currentThread.setContextClassLoader(loader)
    try { f } finally { Thread.currentThread.setContextClassLoader(oldClassLoader) }
  }

  def loaderForPaths(paths: Set[Path]): ClassLoader = {
    val containerURLs: Array[URL] = paths.map(_.toUri.toURL)(collection.breakOut)
    new URLClassLoader(containerURLs, getClass.getClassLoader)
  }
}

object ClassLoadingUtils extends ClassLoadingUtils
