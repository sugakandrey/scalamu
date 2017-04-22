package org.scalamu.utils

import java.io.InputStream

import org.objectweb.asm.Opcodes._
import org.objectweb.asm._
import org.scalamu.core.{ClassInfo, ClassName}

import scala.collection.mutable
import scala.util.Try

trait ASMUtils {
  def loadClassFileInfo(is: InputStream): Try[ClassInfo] =
    Try(load(is, ClassInfoVisitor()))

  private def load[T](is: InputStream, visitor: CollectingVisitor[T]): T = {
    val contents = try {
      val reader = new ClassReader(is)
      reader.accept(
        visitor,
        ClassReader.SKIP_CODE & ClassReader.SKIP_DEBUG & ClassReader.SKIP_FRAMES
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
      visitor: CollectingVisitor[_ <: Traversable[R]]
    ): Set[R] =
      internalNames.collect {
        case name if visitedSuperClasses.add(name) =>
          Thread.currentThread().getContextClassLoader.getResourceAsStream(s"$name.class")
      }.flatMap(load(_, visitor))(collection.breakOut)

    private case class SuperInterfaceVisitor() extends CollectingVisitor[mutable.Set[String]] {
      private val superNames: mutable.Set[String] = mutable.Set.empty

      override def result: mutable.Set[String] =
        superNames | traverseSuperHierarchy(superNames, SuperInterfaceVisitor())

      override def visit(
        version: Int,
        access: Int,
        name: String,
        signature: String,
        superName: String,
        interfaces: Array[String]
      ): Unit = Option(interfaces).foreach(_.foreach(superNames += _))
    }

    private case class SuperClassInfo(internalName: String, annotations: mutable.Set[ClassName])

    private case class SuperClassVisitor() extends CollectingVisitor[Set[SuperClassInfo]] {
      private var selfInfo: SuperClassInfo       = _
      private var superClassName: Option[String] = None

      override def result: Set[SuperClassInfo] =
        traverseSuperHierarchy(superClassName, SuperClassVisitor()) + selfInfo

      override def visit(
        version: Int,
        access: Int,
        name: String,
        signature: String,
        superName: String,
        interfaces: Array[String]
      ): Unit = {
        superClassName = Option(superName)
        selfInfo = SuperClassInfo(name, mutable.Set.empty)
      }

      override def visitAnnotation(
        desc: String,
        visible: Boolean
      ): AnnotationVisitor = {
        selfInfo.annotations += ClassName.fromDescriptor(desc)
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

      val superInterfaces = (interfacesSet | traverseSuperHierarchy(
        interfacesSet,
        SuperInterfaceVisitor()
      )).map(ClassName.fromInternal)

      val superClasses = superClass.fold(Set.empty[SuperClassInfo])(
        cl => traverseSuperHierarchy(Seq(cl), SuperClassVisitor())
      )

      val superAnnotations = superClasses.flatMap(_.annotations)

      val wholeHierarchy =
        superClasses.map(aClass => ClassName.fromInternal(aClass.internalName)) | superInterfaces

      val className = ClassName.fromInternal(name)

      classInfo = ClassInfo(
        className,
        wholeHierarchy,
        superAnnotations,
        name.endsWith("$"),
        hasNoArgConstructor = false,
        None
      )
    }

    override def visitSource(source: String, debug: String): Unit =
      classInfo = classInfo.copy(source = Option(source))

    private def addAnnotation(desc: String): AnnotationVisitor = {
      classInfo =
        classInfo.copy(annotations = classInfo.annotations + ClassName.fromDescriptor(desc))
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
