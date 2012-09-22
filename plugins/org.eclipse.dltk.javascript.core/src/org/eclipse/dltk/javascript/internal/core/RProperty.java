package org.eclipse.dltk.javascript.internal.core;

import java.util.Set;

import org.eclipse.dltk.compiler.problem.IProblemCategory;
import org.eclipse.dltk.javascript.typeinfo.IRType;
import org.eclipse.dltk.javascript.typeinfo.IRTypeDeclaration;
import org.eclipse.dltk.javascript.typeinfo.IRVariable;
import org.eclipse.dltk.javascript.typeinfo.model.Property;

public class RProperty extends RMember<Property> implements IRVariable {

	public RProperty(Property member, IRType type,
			Set<IProblemCategory> suppressedWarnings,
			IRTypeDeclaration typeDeclaration) {
		super(member, type, suppressedWarnings, typeDeclaration);
	}

}
