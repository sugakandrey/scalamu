package org.scalamu.utils

import java.io.InputStream

import org.objectweb.asm.Opcodes._
import org.objectweb.asm._
import org.scalamu.core.{ClassFileInfo, ClassName}
import com.typesafe.scalalogging.Logger

import scala.collection.breakOut
import scala.util.Try

trait ASMUtils {
  private val log = Logger[ASMUtils]

  type ClassLoadingResult[T] = Either[ClassNotFoundException, T]

  def loadClassFileInfo(is: InputStream): Try[ClassFileInfo] =
    Try(load(is, TestClassVisitor()))

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

  private case class TestClassVisitor() extends CollectingVisitor[ClassFileInfo] {
    private var classInfo: ClassFileInfo = _

    override def result: ClassFileInfo = classInfo

    private def traverseSuperHierarchy(internalNames: Set[String]): Set[String] =
      internalNames
        .map(
          name => Thread.currentThread().getContextClassLoader.getResourceAsStream(s"$name.class")
        )
        .flatMap(load(_, SuperClassVisitor()))

    private case class SuperClassVisitor() extends CollectingVisitor[Set[String]] {
      private var superNames: Set[String] = _

      override def result: Set[String] =
        superNames | traverseSuperHierarchy(superNames)

      override def visit(
        version: Int,
        access: Int,
        name: String,
        signature: String,
        superName: String,
        interfaces: Array[String]
      ): Unit = {
        superNames = Option(interfaces).fold(Set.empty[String])(_.toSet)
        Option(superName).foreach(name => superNames = superNames + name)
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
      val superClass         = Option(superName)
      val interfacesSet      = Option(interfaces).fold(Set.empty[String])(_.toSet)
      val directSuperClasses = superClass.fold(interfacesSet)(interfacesSet + _)
      val allSuperClasses    = directSuperClasses | traverseSuperHierarchy(directSuperClasses)

      val className = ClassName.fromInternalName(name)

      classInfo = ClassFileInfo(
        className,
        allSuperClasses.map(ClassName.fromInternalName)(breakOut),
        Set.empty,
        name.endsWith("$"),
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
    ): MethodVisitor = new MethodVisitor(ASM5) {
      override def visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor =
        addAnnotation(desc)
    }
  }
}

object ASMUtils extends ASMUtils
