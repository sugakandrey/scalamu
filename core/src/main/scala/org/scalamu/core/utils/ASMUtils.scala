package org.scalamu.core.utils

import java.io.InputStream

import org.objectweb.asm.Opcodes._
import org.objectweb.asm._
import org.scalamu.core.api.{ClassInfo, ClassName}

import scala.collection.{breakOut, mutable}
import scala.util.Try

trait ASMUtils {
  def loadClassFileInfo(is: InputStream): Try[ClassInfo] =
    Try(load(is, ClassInfoVisitor()))

  private def load[T](is: InputStream, visitor: CollectingVisitor[T]): T = {
    val contents = try {
      val reader = new ClassReader(is)
      reader.accept(
        visitor,
        ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES
      )
      visitor
    } finally is.close()
    contents.result
  }

  private abstract class CollectingVisitor[T] extends ClassVisitor(ASM5) {
    def result: T
  }

  private case class ClassInfoVisitor() extends CollectingVisitor[ClassInfo] {
    private val visitedSuperClasses = mutable.HashSet.empty[String]

    private var classInfo: ClassInfo = _

    override def result: ClassInfo = classInfo

    private def traverseSuperHierarchy[R](
      internalNames: Traversable[String],
      collectAnnotations: Boolean
    ): mutable.Set[SuperClassInfo] =
      internalNames.collect {
        case name if visitedSuperClasses.add(name) =>
          ClassLoadingUtils.contextClassLoader.getResourceAsStream(s"$name.class")
      }.flatMap(load(_, SuperClassVisitor(collectAnnotations)))(breakOut)

    private case class SuperClassInfo(
      internalName: String,
      annotations: mutable.Set[ClassName]
    )

    private case class SuperClassVisitor(collectAnnotations: Boolean)
        extends CollectingVisitor[mutable.Set[SuperClassInfo]] {

      private var selfInfo: SuperClassInfo             = _
      private var superClass: Option[String]           = None
      private val superInterfaces: mutable.Set[String] = mutable.Set.empty

      override def result: mutable.Set[SuperClassInfo] =
        traverseSuperHierarchy(
          superClass,
          collectAnnotations
        ) ++= traverseSuperHierarchy(
          superInterfaces,
          collectAnnotations = false
        ) += selfInfo

      override def visit(
        version: Int,
        access: Int,
        name: String,
        signature: String,
        superName: String,
        interfaces: Array[String]
      ): Unit = {
        selfInfo   = SuperClassInfo(name, mutable.Set.empty)
        superClass = Option(superName)
        Option(interfaces).foreach(_.foreach(superInterfaces += _))
      }

      override def visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor = {
        if (collectAnnotations) {
          selfInfo.annotations += ClassName.fromDescriptor(desc)
        }
        null
      }
    }

    override def visit(
      version: Int,
      access: Int,
      name: String,
      signature: String,
      superName: String,
      interfaces: Array[String]
    ): Unit = {
      val superClass    = Option(superName)
      val interfacesSet = Option(interfaces).fold(Set.empty[String])(_.toSet)

      val wholeHierarchy = traverseSuperHierarchy(
        superClass,
        collectAnnotations = true
      ) ++= traverseSuperHierarchy(interfacesSet, collectAnnotations = false)

      val superAnnotations: Set[ClassName] = wholeHierarchy.flatMap(_.annotations)(breakOut)

      val className  = ClassName.fromInternal(name)
      val isAbstract = ((access & ACC_ABSTRACT) | (access & ACC_INTERFACE)) != 0

      classInfo = ClassInfo(
        className,
        wholeHierarchy.map(aClass => ClassName.fromInternal(aClass.internalName))(breakOut),
        superAnnotations,
        name.endsWith("$"),
        hasNoArgConstructor = false,
        isAbstract = isAbstract,
        None
      )
    }

    override def visitSource(source: String, debug: String): Unit =
      classInfo = classInfo.copy(source = Option(source))

    private def addAnnotation(desc: String): AnnotationVisitor = {
      classInfo = classInfo.copy(annotations = classInfo.annotations + ClassName.fromDescriptor(desc))
      null
    }

    override def visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor =
      addAnnotation(desc)

    override def visitMethod(
      access: Int,
      name: String,
      desc: String,
      signature: String,
      exceptions: Array[String]
    ): MethodVisitor = {
      if (name == "<init>" && desc == "()V") {
        classInfo = classInfo.copy(hasNoArgConstructor = true)
      }
      new MethodVisitor(ASM5) {
        override def visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor =
          addAnnotation(desc)
      }
    }
  }
}

object ASMUtils extends ASMUtils
