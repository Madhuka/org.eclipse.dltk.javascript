/*******************************************************************************
 * Copyright (c) 2011 NumberFour AG
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     NumberFour AG - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.javascript.typeinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.dltk.compiler.problem.IProblemCategory;
import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IMember;
import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IMethod;
import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IParameter;
import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IVariable;
import org.eclipse.dltk.javascript.typeinfo.model.Element;
import org.eclipse.dltk.javascript.typeinfo.model.Parameter;
import org.eclipse.dltk.javascript.typeinfo.model.ParameterKind;
import org.eclipse.dltk.javascript.typeinfo.model.Visibility;

public class RModelBuilder {

	private static class RElement implements IRElement {

		private final String name;
		protected final IRType type;
		// nullable
		private final Set<IProblemCategory> suppressedWarnings;
		// nullable
		private final Object source;

		public RElement(String name, IRType type,
				Set<IProblemCategory> suppressedWarnings, Object source) {
			this.name = name;
			this.type = type;
			this.suppressedWarnings = suppressedWarnings;
			this.source = source;
		}

		public String getName() {
			return name;
		}

		public IRType getType() {
			return type;
		}

		public Set<IProblemCategory> getSuppressedWarnings() {
			return suppressedWarnings != null ? suppressedWarnings
					: Collections.<IProblemCategory> emptySet();
		}

		public boolean isSuppressed(IProblemIdentifier problemIdentifier) {
			if (suppressedWarnings != null) {
				for (IProblemCategory category : suppressedWarnings) {
					if (category.contents().contains(problemIdentifier)) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean isDeprecated() {
			if (source instanceof IMember) {
				return ((IMember) source).isDeprecated();
			} else if (source instanceof Element) {
				return ((Element) source).isDeprecated();
			} else {
				return false;
			}
		}

		public Object getSource() {
			return source;
		}

	}

	private static class RMember extends RElement implements IRMember {
		final Visibility visibility;
		final IRTypeDeclaration typeDeclaration;

		public RMember(String name, IRType type,
				Set<IProblemCategory> suppressedWarnings,
				Visibility visibility, Object source,
				IRTypeDeclaration typeDeclaration) {
			super(name, type, suppressedWarnings, source);
			this.visibility = visibility;
			this.typeDeclaration = typeDeclaration;
		}

		public Visibility getVisibility() {
			return visibility;
		}

		public IRTypeDeclaration getDeclaringType() {
			return typeDeclaration;
		}
	}

	private static class RMethod extends RMember implements IRMethod {

		final boolean _constructor;
		final List<IRParameter> parameters;

		public RMethod(String name, IRType type,
				Set<IProblemCategory> suppressedWarnings,
				Visibility visibility, boolean _constructor,
				List<IRParameter> parameters, Object source) {
			super(name, type, suppressedWarnings, visibility, source, null);
			this._constructor = _constructor;
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
			return _constructor;
		}

	}

	private static class RParameter implements IRParameter {

		private final String name;
		private final IRType type;
		private final ParameterKind kind;

		public RParameter(String name, IRType type, ParameterKind kind) {
			Assert.isNotNull(type);
			this.name = name;
			this.type = type;
			this.kind = kind;
		}

		public String getName() {
			return name;
		}

		public IRType getType() {
			return type;
		}

		public ParameterKind getKind() {
			return kind;
		}

		@Override
		public int hashCode() {
			return type.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof RParameter) {
				final RParameter other = (RParameter) obj;
				if (name == null) {
					if (other.name != null)
						return false;
				} else if (!name.equals(other.name))
					return false;
				if (!type.equals(other.type))
					return false;
				if (kind != other.kind)
					return false;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return name + ":" + type;
		}

		public boolean isOptional() {
			return kind == ParameterKind.OPTIONAL;
		}

		public boolean isVarargs() {
			return kind == ParameterKind.VARARGS;
		}

	}

	private static class RVariable extends RMember implements IRVariable {

		public RVariable(String name, IRType type,
				Set<IProblemCategory> suppressedWarnings,
				Visibility visibility, Object source) {
			super(name, type, suppressedWarnings, visibility, source, null);
		}

	}

	public static IRMethod create(ITypeSystem context, IMethod method) {
		return new RMethod(method.getName(), RTypes.create(context,
				method.getType()), method.getSuppressedWarnings(),
				method.getVisibility(), method.isConstructor(), convertParams0(
						context, method.getParameters()), method);
	}

	public static IRVariable create(ITypeSystem context, IVariable variable) {
		return new RVariable(variable.getName(), RTypes.create(context,
				variable.getType()), variable.getSuppressedWarnings(),
				variable.getVisibility(), variable);
	}

	public static List<IRParameter> convert(ITypeSystem context,
			List<Parameter> parameters) {
		if (parameters.isEmpty()) {
			return Collections.emptyList();
		} else {
			final List<IRParameter> params = new ArrayList<IRParameter>(
					parameters.size());
			for (Parameter parameter : parameters) {
				final IRType paramType = parameter.getType() != null ? RTypes
						.create(context, parameter.getType()) : RTypes.any();
				params.add(new RParameter(parameter.getName(), paramType,
						parameter.getKind()));
			}
			return params;
		}
	}

	private static List<IRParameter> convertParams0(ITypeSystem context,
			List<IParameter> parameters) {
		if (parameters.isEmpty()) {
			return Collections.emptyList();
		} else {
			final List<IRParameter> params = new ArrayList<IRParameter>();
			for (IParameter parameter : parameters) {
				final IRType paramType = parameter.getType() != null ? RTypes
						.create(context, parameter.getType()) : RTypes.any();
				params.add(new RParameter(parameter.getName(), paramType,
						parameter.getKind()));
			}
			return params;
		}
	}

}
