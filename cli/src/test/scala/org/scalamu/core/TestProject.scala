package org.scalamu.core

import java.nio.file.Path

final case class TestProject(
  name: String,
  path: Path,
  dependencies: Set[Path],
  target: Path
)
