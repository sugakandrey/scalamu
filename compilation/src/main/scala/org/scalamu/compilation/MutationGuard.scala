package org.scalamu.compilation

object MutationGuard {
  private var activeId = 0

  def enabledMutation: Int        = activeId
  def enableForId(mId: Int): Unit = activeId = mId
}
