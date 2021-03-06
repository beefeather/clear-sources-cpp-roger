/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Container for include export patterns, for example "IWYU pragma: export",
 * "IWYU pragma: begin_exports" and "IWYU pragma: end_exports".
 * @see "https://code.google.com/p/include-what-you-use/wiki/IWYUPragmas"
 *
 * @since 5.5
 */
public class IncludeExportPatterns {
	private final Pattern includeExportPattern;
	private final Pattern includeBeginExportsPattern;
	private final Pattern includeEndExportsPattern;

	public IncludeExportPatterns(String exportPattern, String beginExportsPattern,
			String endExportsPattern) {
		this.includeExportPattern = compilePattern(exportPattern);
		this.includeBeginExportsPattern = compilePattern(beginExportsPattern);
		this.includeEndExportsPattern = compilePattern(endExportsPattern);
	}

	private Pattern compilePattern(String pattern) {
		try {
			return pattern == null ? null : Pattern.compile(pattern);
		} catch (PatternSyntaxException e) {
			return null;
		}
	}

	/**
	 * Returns the include export pattern, e.g. "IWYU pragma: export".
	 */
	public Pattern getIncludeExportPattern() {
		return includeExportPattern;
	}

	/**
	 * Returns the include export pattern, e.g. "IWYU pragma: begin_exports".
	 */
	public Pattern getIncludeBeginExportsPattern() {
		return includeBeginExportsPattern;
	}

	/**
	 * Returns the include export pattern, e.g. "IWYU pragma: end_exports".
	 */
	public Pattern getIncludeEndExportsPattern() {
		return includeEndExportsPattern;
	}
}
