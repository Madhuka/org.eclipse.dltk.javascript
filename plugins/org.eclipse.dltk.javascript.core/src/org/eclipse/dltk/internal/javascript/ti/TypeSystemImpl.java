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
package org.eclipse.dltk.internal.javascript.ti;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dltk.javascript.internal.core.RTypeDeclaration;
import org.eclipse.dltk.javascript.typeinfo.AttributeKey;
import org.eclipse.dltk.javascript.typeinfo.IRMember;
import org.eclipse.dltk.javascript.typeinfo.IRType;
import org.eclipse.dltk.javascript.typeinfo.IRTypeDeclaration;
import org.eclipse.dltk.javascript.typeinfo.ITypeSystem;
import org.eclipse.dltk.javascript.typeinfo.OriginReference;
import org.eclipse.dltk.javascript.typeinfo.RTypes;
import org.eclipse.dltk.javascript.typeinfo.model.GenericType;
import org.eclipse.dltk.javascript.typeinfo.model.Member;
import org.eclipse.dltk.javascript.typeinfo.model.Method;
import org.eclipse.dltk.javascript.typeinfo.model.Property;
import org.eclipse.dltk.javascript.typeinfo.model.Type;
import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelLoader;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;

public class TypeSystemImpl implements ITypeSystem {

	/*
	 * @see ITypeSystem#resolveType(Type)
	 */
	public Type resolveType(Type type) {
		if (type != null && type.isProxy()) {
			return doResolveType(type);
		} else {
			return type;
		}
	}

	public Type doResolveType(Type type) {
		final String typeName = URI.decode(((InternalEObject) type).eProxyURI()
				.fragment());
		final Type resolved = TypeInfoModelLoader.getInstance().getType(
				typeName);
		if (resolved != null) {
			return resolved;
		}
		return type;
	}

	/*
	 * @see ITypeSystem#valueOf(Member)
	 */
	public IValue valueOf(Member member) {
		return null;
	}

	private final Map<Type, IRTypeDeclaration> typeDeclarations = new HashMap<Type, IRTypeDeclaration>();

	public IRTypeDeclaration convert(Type type) {
		return convertType(type);
	}

	private IRTypeDeclaration _convert(final Type type) {
		return type != null ? convertType(type) : null;
	}

	private IRTypeDeclaration convertType(Type type) {
		IRTypeDeclaration declaration = typeDeclarations.get(type);
		if (declaration != null) {
			return declaration;
		}
		final IRTypeDeclaration superType = _convert(type.getSuperType());
		final List<IRTypeDeclaration> traits = new ArrayList<IRTypeDeclaration>();
		for (Type trait : type.getTraits()) {
			traits.add(convertType(trait));
		}
		final List<IRMember> members = new ArrayList<IRMember>(type
				.getMembers().size());
		for (Member member : type.getMembers()) {
			if (member instanceof Method) {
				members.add(convertMethod((Method) member));
			} else {
				assert member instanceof Property;
				members.add(convertProperty((Property) member));
			}
		}
		// TODO members
		declaration = new RTypeDeclaration(type, superType, traits, members);
		typeDeclarations.put(type, declaration);
		return declaration;
	}

	private IRMember convertProperty(Property member) {
		// TODO Auto-generated method stub
		return null;
	}

	private IRMember convertMethod(Method member) {
		// TODO Auto-generated method stub
		return null;
	}

	private static class ParameterizedTypeKey {
		final GenericType type;
		final IRType[] parameters;

		public ParameterizedTypeKey(GenericType type, List<IRType> parameters) {
			this.type = type;
			final int expectedParamCount = type.getTypeParameters().size();
			this.parameters = new IRType[expectedParamCount];
			for (int i = 0; i < expectedParamCount; ++i) {
				this.parameters[i] = i < parameters.size() ? parameters.get(i)
						: RTypes.none();
			}
		}

		@Override
		public int hashCode() {
			return type.hashCode() ^ Arrays.hashCode(parameters);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ParameterizedTypeKey) {
				final ParameterizedTypeKey other = (ParameterizedTypeKey) obj;
				return type == other.type
						&& Arrays.equals(parameters, other.parameters);
			}
			return false;
		}

		@Override
		public String toString() {
			final StringBuilder parameterizedName = new StringBuilder(type
					.getName().length() + parameters.length * 16);
			parameterizedName.append(type.getName());
			parameterizedName.append("<");
			for (int i = 0; i < parameters.length; ++i) {
				if (i > 0) {
					parameterizedName.append(",");
				}
				parameterizedName.append(parameters[i].getName());
			}
			parameterizedName.append(">");
			return parameterizedName.toString();
		}

	}

	private final Map<ParameterizedTypeKey, Type> types = new HashMap<ParameterizedTypeKey, Type>();

	/*
	 * @see ITypeSystem#parameterize(Type, java.util.List)
	 */
	public Type parameterize(Type target, List<IRType> parameters) {
		target = resolveType(target);
		if (target instanceof GenericType) {
			final GenericType genericType = (GenericType) target;
			final ParameterizedTypeKey key = new ParameterizedTypeKey(
					genericType, parameters);
			Type type = types.get(key);
			if (type != null) {
				if (DEBUG) {
					System.out.println("Returning " + type.getName());
				}
				return type;
			}
			if (DEBUG) {
				System.out.println("Creating " + key);
			}
			final TypeParameterizer parameterizer = new TypeParameterizer(
					genericType, parameters);
			type = parameterizer.copy();
			parameterizer.copyReferences();
			type.setName(key.toString());
			type.eAdapters().add(
					new OriginReference(genericType,
							parameterizer.actualParameters));
			types.put(key, type);
			notifyTypeCreated(type);
			final Type superType = target.getSuperType();
			if (superType != null) {
				if (superType instanceof GenericType) {
					type.setSuperType(parameterize(superType, parameters));
				} else {
					type.setSuperType(superType);
				}
			}
			return type;
		}
		return target;
	}

	protected void notifyTypeCreated(Type type) {
	}

	/*
	 * @see ITypeSystem#getAttribute(AttributeKey)
	 */
	public <T> T getAttribute(AttributeKey<T> key) {
		return null;
	}

	private Map<Object, Object> values;

	/*
	 * @see ITypeSystem#getValue(java.lang.Object )
	 */
	public Object getValue(Object key) {
		assert key != null;
		return values != null ? values.get(key) : null;
	}

	/*
	 * @see ITypeSystem#setValue(java.lang.Object , java.lang.Object)
	 */
	public void setValue(Object key, Object value) {
		assert key != null;
		if (values == null) {
			values = new HashMap<Object, Object>();
		}
		values.put(key, value);
	}

	private static final boolean DEBUG = false;

}
