/*******************************************************************************
 * Copyright (c) 2012 NumberFour AG
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     NumberFour AG - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.javascript.internal.core;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.dltk.compiler.problem.IProblemCategory;
import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
import org.eclipse.dltk.javascript.typeinfo.IRMember;
import org.eclipse.dltk.javascript.typeinfo.IRTypeDeclaration;
import org.eclipse.dltk.javascript.typeinfo.model.Type;

public class RTypeDeclaration implements IRTypeDeclaration {

	private final Type type;
	private final IRTypeDeclaration superType;
	private final List<IRTypeDeclaration> traits;
	private final List<IRMember> members;

	public RTypeDeclaration(Type type, IRTypeDeclaration superType,
			List<IRTypeDeclaration> traits, List<IRMember> members) {
		this.type = type;
		this.superType = superType;
		this.traits = traits;
		this.members = members;
	}

	public String getName() {
		return type.getName();
	}

	public Set<IProblemCategory> getSuppressedWarnings() {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}

	public boolean isSuppressed(IProblemIdentifier problemIdentifier) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDeprecated() {
		return type.isDeprecated();
	}

	public Object getSource() {
		return type;
	}

	public IRTypeDeclaration getSuperType() {
		return superType;
	}

	public List<IRTypeDeclaration> getTraits() {
		return traits;
	}

	public List<IRMember> getMembers() {
		return members;
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RTypeDeclaration) {
			final RTypeDeclaration other = (RTypeDeclaration) obj;
			return type == other.type;
		}
		return false;
	}

}
