package org.scalamu.core.compilation

import org.scalamu.common.MutantId

object MutationGuard {
  private var activeId = 0

  def enabledMutation: Int = activeId

  def enableForId(mId: MutantId): Unit = activeId = mId.id
}
