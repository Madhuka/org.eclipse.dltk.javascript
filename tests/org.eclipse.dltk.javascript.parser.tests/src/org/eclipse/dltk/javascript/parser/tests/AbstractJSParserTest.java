/*******************************************************************************
 * Copyright (c) 2010 xored software, Inc.  
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html  
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.javascript.parser.tests;

import org.eclipse.dltk.compiler.problem.IProblem;
import org.eclipse.dltk.compiler.problem.ProblemCollector;
import org.eclipse.dltk.javascript.ast.Script;
import org.eclipse.dltk.javascript.parser.JSProblem;
import org.eclipse.dltk.javascript.parser.JavaScriptParser;

import junit.framework.TestCase;

public abstract class AbstractJSParserTest extends TestCase {

	protected final ProblemCollector reporter = new ProblemCollector();
	protected final JavaScriptParser parser = new JavaScriptParser();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		reporter.reset();
		parser.setTypeInformationEnabled(false);
	}

	protected Script parse(final String source) {
		final Script script = parser.parse(source, reporter);
		assertNotNull(script);
		for (IProblem problem : reporter.getProblems()) {
			if (problem instanceof JSProblem) {
				rethrow(((JSProblem) problem).getCause());
			}
		}
		return script;
	}

	protected static void rethrow(final Throwable cause) {
		if (cause instanceof RuntimeException) {
			throw (RuntimeException) cause;
		} else if (cause instanceof Error) {
			throw (Error) cause;
		} else {
			throw new RuntimeException(cause);
		}
	}

}