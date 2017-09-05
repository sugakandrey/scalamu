package org.scalamu.utils

class InMemoryClassLoader(
  classes: Map[String, Array[Byte]],
  parent: ClassLoader = ClassLoadingUtils.contextClassLoader
) extends ClassLoader(parent) {

  private def getClassBytes(name: String): Array[Byte] = classes.get(name) match {
    case Some(aClass) => aClass
    case None =>
      val is = parent.getResourceAsStream(name.replaceAll("\\.", "/") + ".class")
      if (is == null) throw new ClassNotFoundException()
      Iterator.continually(is.read).takeWhile(-1 !=).map(_.toByte).toArray
  }

  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    val loaded: Option[Class[_]] = Option(findLoadedClass(name))
    loaded match {
      case Some(aClass)                    => aClass
      case None if name.startsWith("java") => parent.loadClass(name)
      case None =>
        val bytes   = getClassBytes(name)
        val defined = defineClass(name, bytes, 0, bytes.length)
        if (resolve) resolveClass(defined)
        defined
    }
  }
}
