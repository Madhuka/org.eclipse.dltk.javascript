package org.eclipse.dltk.javascript.internal.core;

import java.util.List;
import java.util.Set;

import org.eclipse.dltk.compiler.problem.IProblemCategory;
import org.eclipse.dltk.javascript.typeinfo.IRMethod;
import org.eclipse.dltk.javascript.typeinfo.IRParameter;
import org.eclipse.dltk.javascript.typeinfo.IRType;
import org.eclipse.dltk.javascript.typeinfo.IRTypeDeclaration;
import org.eclipse.dltk.javascript.typeinfo.RTypes;
import org.eclipse.dltk.javascript.typeinfo.model.Method;
import org.eclipse.dltk.javascript.typeinfo.model.ParameterKind;

public class RMethod extends RMember<Method> implements IRMethod {

	private final List<IRParameter> parameters;

	public RMethod(Method method, IRType type,
			Set<IProblemCategory> suppressedWarnings,
			List<IRParameter> parameters, IRTypeDeclaration typeDeclaration) {
		super(method, type, suppressedWarnings, typeDeclaration);
		this.parameters = parameters;
	}

	public int getParameterCount() {
		return parameters.size();
	}

	public List<IRParameter> getParameters() {
		return parameters;
	}

	public boolean isTyped() {
		if (type != null) {
			return true;
		}
		for (int i = 0; i < parameters.size(); ++i) {
			final IRParameter parameter = parameters.get(i);
			if (parameter.getType() != RTypes.any()
					|| parameter.getKind() != ParameterKind.NORMAL) {
				return true;
			}
		}
		return false;
	}

	public boolean isConstructor() {
		// TODO
		return false;
	}

}
