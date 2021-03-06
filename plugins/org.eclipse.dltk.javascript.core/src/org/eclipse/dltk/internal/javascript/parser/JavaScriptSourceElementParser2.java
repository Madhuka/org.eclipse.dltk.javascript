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
package org.eclipse.dltk.internal.javascript.parser;

import org.eclipse.dltk.compiler.ISourceElementRequestor;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.compiler.problem.IProblemReporter;
import org.eclipse.dltk.core.ISourceElementParser;
import org.eclipse.dltk.internal.javascript.parser.structure.StructureReporter2;
import org.eclipse.dltk.internal.javascript.parser.structure.StructureRequestor;
import org.eclipse.dltk.internal.javascript.ti.TypeInferencer2;
import org.eclipse.dltk.javascript.ast.Script;
import org.eclipse.dltk.javascript.parser.JavaScriptParserUtil;

public class JavaScriptSourceElementParser2 implements ISourceElementParser {

	protected ISourceElementRequestor fRequestor = null;
	protected IProblemReporter fReporter = null;

	public void setRequestor(ISourceElementRequestor requestor) {
		this.fRequestor = requestor;
	}

	public void setReporter(IProblemReporter reporter) {
		this.fReporter = reporter;
	}

	public void parseSourceModule(IModuleSource module) {
		final Script script = parse(module);

		final TypeInferencer2 inferencer2 = new TypeInferencer2();
		StructureReporter2 sr = new StructureReporter2(inferencer2,
				new StructureRequestor(fRequestor));
		inferencer2.setVisitor(sr);
		fRequestor.enterModule();
		inferencer2.setModelElement(module.getModelElement());
		inferencer2.doInferencing(script);
		fRequestor.exitModule(script.sourceEnd());
	}

	protected Script parse(IModuleSource module) {
		return JavaScriptParserUtil.parse(module, fReporter);
	}
}
