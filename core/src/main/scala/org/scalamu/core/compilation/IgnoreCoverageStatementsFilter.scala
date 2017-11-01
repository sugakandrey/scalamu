package org.scalamu.core.compilation

import org.scalamu.common.filtering.InverseRegexFilter

object IgnoreCoverageStatementsFilter extends InverseRegexFilter(Seq("org.scalamu.compilation.ForgetfulInvoker.*".r))
