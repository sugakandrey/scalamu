package org.scalamu.utils.bytecode

import java.util.concurrent.atomic.AtomicInteger

import org.objectweb.asm.Opcodes._
import org.objectweb.asm._

object GuardsClassFactory {
  def increment(className: String, method: MethodVisitor): Unit = {
    method.visitMaxs(1, 0)
    method.visitCode()
    method.visitFieldInsn(
      GETSTATIC,
      className,
      "mutationId",
      Type.getDescriptor(classOf[AtomicInteger])
    )
    method.visitMethodInsn(
      INVOKEVIRTUAL,
      Type.getInternalName(classOf[AtomicInteger]),
      "incrementAndGet",
      "()I",
      false
    )
    method.visitInsn(IRETURN)
    method.visitEnd()
  }

  def increaseBy(delta: Int)(className: String, method: MethodVisitor): Unit = {
    method.visitMaxs(2, 0)
    method.visitCode()
    method.visitFieldInsn(
      GETSTATIC,
      className,
      "mutationId",
      Type.getDescriptor(classOf[AtomicInteger])
    )
    method.visitLdcInsn(delta)
    method.visitMethodInsn(
      INVOKEVIRTUAL,
      Type.getInternalName(classOf[AtomicInteger]),
      "addAndGet",
      "(I)I",
      false
    )
    method.visitInsn(IRETURN)
    method.visitEnd()
  }

  def forClassNames(
    names: Seq[String],
    idUpdateStrategy: (String, MethodVisitor) => Unit = increment
  ): Map[String, Array[Byte]] =
    names.map(name => name -> forClassName(name, idUpdateStrategy))(collection.breakOut)

  def forClassName(
    name: String,
    idUpdateStrategy: (String, MethodVisitor) => Unit = increment
  ): Array[Byte] = {
    val cw = new ClassWriter(0)
    cw.visit(
      V1_8,
      ACC_PUBLIC,
      name,
      null,
      Type.getInternalName(classOf[Object]),
      null
    )

    val field = cw.visitField(
      ACC_PRIVATE + ACC_FINAL + ACC_STATIC,
      "mutationId",
      Type.getDescriptor(classOf[AtomicInteger]),
      null,
      null
    )
    field.visitEnd()

    val init = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
    init.visitMaxs(1, 1)
    init.visitCode()
    init.visitVarInsn(ALOAD, 0)
    init.visitMethodInsn(
      INVOKESPECIAL,
      Type.getInternalName(classOf[Object]),
      "<init>",
      "()V",
      false
    )
    init.visitInsn(RETURN)
    init.visitEnd()

    val method = cw.visitMethod(
      ACC_PUBLIC + ACC_STATIC,
      "enabledMutation",
      "()I",
      null,
      null
    )
    idUpdateStrategy(name, method)

    val clinit = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null)
    clinit.visitMaxs(3, 0)
    clinit.visitCode()
    clinit.visitTypeInsn(NEW, Type.getInternalName(classOf[AtomicInteger]))
    clinit.visitInsn(DUP)
    clinit.visitInsn(ICONST_0)
    clinit.visitMethodInsn(
      INVOKESPECIAL,
      Type.getInternalName(classOf[AtomicInteger]),
      "<init>",
      "(I)V",
      false
    )
    clinit.visitFieldInsn(
      PUTSTATIC,
      name,
      "mutationId",
      Type.getDescriptor(classOf[AtomicInteger])
    )
    clinit.visitInsn(RETURN)
    clinit.visitEnd()

    cw.visitEnd()
    cw.toByteArray
  }
}
