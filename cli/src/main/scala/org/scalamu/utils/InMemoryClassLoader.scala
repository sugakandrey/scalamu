package org.scalamu.utils

class InMemoryClassLoader(
  classes: Map[String, Array[Byte]],
  parent: ClassLoader = ClassLoadingUtils.contextClassLoader
) extends ClassLoader(parent) {

  override def loadClass(name: String): Class[_] = {
    val loaded = Option(findLoadedClass(name))
    loaded match {
      case Some(aClass) => aClass
      case None =>
        val bytes   = classes.get(name)
        val defined = bytes.map(bs => defineClass(name, bs, 0, bs.length))
        defined.getOrElse(super.loadClass(name))
    }
  }
}
