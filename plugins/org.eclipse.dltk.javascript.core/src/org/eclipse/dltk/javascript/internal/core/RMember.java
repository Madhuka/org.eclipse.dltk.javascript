package org.eclipse.dltk.javascript.internal.core;

import java.util.Set;

import org.eclipse.dltk.compiler.problem.IProblemCategory;
import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
import org.eclipse.dltk.javascript.typeinfo.IRMember;
import org.eclipse.dltk.javascript.typeinfo.IRType;
import org.eclipse.dltk.javascript.typeinfo.IRTypeDeclaration;
import org.eclipse.dltk.javascript.typeinfo.model.Member;
import org.eclipse.dltk.javascript.typeinfo.model.Visibility;

public class RMember<E extends Member> implements IRMember {
	protected final E member;
	private final IRTypeDeclaration typeDeclaration;
	protected final IRType type;
	private final Set<IProblemCategory> suppressedWarnings;

	public RMember(E member, IRType type,
			Set<IProblemCategory> suppressedWarnings,
			IRTypeDeclaration typeDeclaration) {
		this.member = member;
		this.typeDeclaration = typeDeclaration;
		this.type = type;
		this.suppressedWarnings = suppressedWarnings;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return member.getName();
	}

	public Visibility getVisibility() {
		return member.getVisibility();
	}

	public IRTypeDeclaration getDeclaringType() {
		return typeDeclaration;
	}

	public Set<IProblemCategory> getSuppressedWarnings() {
		return suppressedWarnings;
	}

	public boolean isSuppressed(IProblemIdentifier problemIdentifier) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDeprecated() {
		return member.isDeprecated();
	}

	public Object getSource() {
		return member;
	}

	public IRType getType() {
		return type;
	}
}
