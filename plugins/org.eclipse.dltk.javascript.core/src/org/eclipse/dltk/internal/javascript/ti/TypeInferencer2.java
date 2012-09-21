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
package org.eclipse.dltk.internal.javascript.ti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.javascript.ast.Script;
import org.eclipse.dltk.javascript.core.JavaScriptPlugin;
import org.eclipse.dltk.javascript.internal.core.ThreadTypeSystemImpl;
import org.eclipse.dltk.javascript.parser.JSProblem;
import org.eclipse.dltk.javascript.parser.JSProblemReporter;
import org.eclipse.dltk.javascript.typeinference.IValueCollection;
import org.eclipse.dltk.javascript.typeinference.IValueReference;
import org.eclipse.dltk.javascript.typeinference.ReferenceKind;
import org.eclipse.dltk.javascript.typeinfo.AttributeKey;
import org.eclipse.dltk.javascript.typeinfo.IElementResolver;
import org.eclipse.dltk.javascript.typeinfo.IMemberEvaluator;
import org.eclipse.dltk.javascript.typeinfo.IModelBuilder;
import org.eclipse.dltk.javascript.typeinfo.ITypeProvider;
import org.eclipse.dltk.javascript.typeinfo.ITypeSystem;
import org.eclipse.dltk.javascript.typeinfo.RTypes;
import org.eclipse.dltk.javascript.typeinfo.ReferenceSource;
import org.eclipse.dltk.javascript.typeinfo.TypeInfoManager;
import org.eclipse.dltk.javascript.typeinfo.TypeMode;
import org.eclipse.dltk.javascript.typeinfo.TypeUtil;
import org.eclipse.dltk.javascript.typeinfo.model.Member;
import org.eclipse.dltk.javascript.typeinfo.model.Property;
import org.eclipse.dltk.javascript.typeinfo.model.SimpleType;
import org.eclipse.dltk.javascript.typeinfo.model.Type;
import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelFactory;
import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelLoader;
import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

public class TypeInferencer2 extends TypeSystemImpl implements
		ITypeInferenceContext {

	private TypeInferencerVisitor visitor;
	private ReferenceSource source;

	private void initializeVisitor() {
		if (visitor == null) {
			visitor = new TypeInferencerVisitor(this);
		}
		visitor.initialize();
	}

	public void setVisitor(TypeInferencerVisitor visitor) {
		this.visitor = visitor;
	}

	public void setModelElement(IModelElement modelElement) {
		setSource(new ReferenceSource(modelElement));
	}

	public void setSource(ReferenceSource source) {
		this.source = source;
	}

	private static final boolean DEBUG = false;

	public void doInferencing(Script script) {
		if (DEBUG)
			System.out.println("Visiting "
					+ source
					+ " with "
					+ (visitor != null ? visitor.getClass().getName()
							: "Default") + " in "
					+ Thread.currentThread().getName());
		final ITypeSystem saved = CURRENT.get();
		try {
			((ThreadTypeSystemImpl) CURRENT).set(this);
			elements.clear();
			modelBuilders = null;
			typeProviders = null;
			initializeVisitor();
			visitor.visit(script);
			visitor.done();
			// IValueCollection collection = visitor.getCollection();
			// visitor = null;
			// return collection;
		} catch (PositionReachedException e) {
			// visitor = null;
			throw e;
		} catch (RuntimeException e) {
			log(e);
		} catch (AssertionError e) {
			log(e);
		} finally {
			((ThreadTypeSystemImpl) CURRENT).set(saved);
		}
		// return null;
	}

	protected void log(Throwable e) {
		final JSProblemReporter reporter = visitor.getProblemReporter();
		if (reporter != null) {
			reporter.reportProblem(new JSProblem(e));
		}
		JavaScriptPlugin.error(e);
	}

	public IValueReference evaluate(ASTNode node) {
		initializeVisitor();
		return visitor.visit(node);
	}

	public IValueCollection getCollection() {
		return visitor.getCollection();
	}

	public IValueCollection currentCollection() {
		return visitor.peekContext();
	}

	private final Map<String, Type> types = new ConcurrentHashMap<String, Type>();

	public Type getType(String typeName) {
		if (typeName == null || typeName.length() == 0) {
			return null;
		}
		final boolean queryProviders = canQueryTypeProviders();
		return getType(typeName, null, queryProviders, true, !queryProviders,
				true);
	}

	public SimpleType getTypeRef(String typeName) {
		return TypeUtil.ref(getType(typeName));
	}

	public Type getKnownType(String typeName, TypeMode mode) {
		if (typeName == null || typeName.length() == 0) {
			return null;
		}
		final boolean queryProviders = canQueryTypeProviders();
		return getType(typeName, mode, queryProviders, true, !queryProviders,
				false);
	}

	@Override
	public Type doResolveType(Type type) {
		final String typeName = URI.decode(((InternalEObject) type).eProxyURI()
				.fragment());
		final Type resolved = getType(typeName, null, true, true, false, true);
		if (resolved != null) {
			return resolved;
		}
		return type;
	}

	@Override
	protected void notifyTypeCreated(Type type) {
		typeRS.addToResource(type);
	}

	public Set<String> listTypes(TypeMode mode, String prefix) {
		Set<String> result = new HashSet<String>();
		final TypeInfoModelLoader loader = TypeInfoModelLoader.getInstance();
		Set<String> typeNames = mode == TypeMode.CODE ? loader
				.listTypeLiterals(prefix) : loader.listTypes(prefix);
		if (typeNames != null) {
			result.addAll(typeNames);
		}
		for (ITypeProvider provider : getTypeProviders()) {
			typeNames = provider.listTypes(this, mode, prefix);
			if (typeNames != null) {
				result.addAll(typeNames);
			}
		}
		return result;
	}

	/**
	 * @return the source
	 */
	public ReferenceSource getSource() {
		return source;
	}

	public IValueCollection getTopValueCollection() {
		if (resolve) {
			for (IMemberEvaluator evaluator : TypeInfoManager
					.getMemberEvaluators()) {
				final IValueCollection collection = evaluator
						.getTopValueCollection(this);
				if (collection != null) {
					return collection;
				}
			}
		}
		return null;
	}

	public IModelElement getModelElement() {
		return source != null ? source.getModelElement() : null;
	}

	private enum TypeResolveMode {
		SIMPLE, PROXY, UNKNOWN
	}

	private Type getType(String typeName, TypeMode mode,
			boolean queryProviders, boolean queryPredefined,
			boolean allowProxy, boolean allowUnknown) {
		Type type = types.get(typeName);
		if (type != null) {
			if (!allowUnknown && type.getKind() == TypeKind.UNKNOWN) {
				return null;
			}
			return type;
		}
		type = loadType(typeName, mode, queryProviders, queryPredefined);
		if (type != null) {
			validateTypeInfo(type);
			types.put(typeName, type);
			typeRS.addToResource(type);
			return type;
		}
		if (allowProxy) {
			type = TypeUtil.createProxy(typeName);
			return type;
		}
		if (allowUnknown) {
			type = createUnknown(typeName);
			typeRS.addToResource(type);
			types.put(typeName, type);
			return type;
		}
		return null;
	}

	private void validateTypeInfo(Type type) {
		final Resource resource = ((EObject) type).eResource();
		if (resource != null) {
			final ResourceSet resourceSet = resource.getResourceSet();
			if (resourceSet != null) {
				Assert.isLegal(!(resourceSet instanceof TypeResourceSet),
						"Type " + type.getName() + " has invalid resource: "
								+ resource + " (" + resourceSet.getClass()
								+ ")");
			}
		}
		// TODO check that member referenced types are contained or proxy
	}

	protected static Type createUnknown(String typeName) {
		final Type type = TypeInfoModelFactory.eINSTANCE.createType();
		type.setName(typeName);
		type.setKind(TypeKind.UNKNOWN);
		return type;
	}

	private final Map<String, Boolean> activeTypeRequests = new HashMap<String, Boolean>();

	private boolean canQueryTypeProviders() {
		return activeTypeRequests.isEmpty();
	}

	private Type loadType(String typeName, TypeMode mode,
			boolean queryProviders, boolean queryPredefined) {
		if (queryProviders
				&& activeTypeRequests.put(typeName, Boolean.FALSE) == null) {
			try {
				for (ITypeProvider provider : getTypeProviders()) {
					final Type type = provider.getType(this, mode, typeName);
					if (type != null && !type.eIsProxy()) {
						return type;
					}
				}
			} finally {
				activeTypeRequests.remove(typeName);
			}
		}
		if (queryPredefined) {
			final TypeInfoModelLoader loader = TypeInfoModelLoader
					.getInstance();
			final Type type;
			if (mode == TypeMode.CODE) {
				type = loader.getTypeLiteral(typeName);
			} else {
				type = loader.getType(typeName);
			}
			if (type != null) {
				return type;
			}
		}
		return null;
	}

	private ITypeProvider[] typeProviders = null;

	public ITypeProvider[] getTypeProviders() {
		if (typeProviders == null) {
			typeProviders = createTypeProviders();
		}
		return typeProviders;
	}

	protected ITypeProvider[] createTypeProviders() {
		return TypeInfoManager.createTypeProviders(this);
	}

	static abstract class TypeResourceSet extends ResourceSetImpl {

		public TypeResourceSet() {
			TypeInfoModelLoader.getInstance().initializeURIMap(this);
		}

		@Override
		public EObject getEObject(URI uri, boolean loadOnDemand) {
			if (TypeUtil.isTypeProxy(uri)) {
				final String typeName = URI.decode(uri.fragment());
				final Type type = resolveTypeProxy(typeName);
				if (type == null) {
					return createUnknown(typeName);
				} else {
					return type;
				}
			}
			return super.getEObject(uri, loadOnDemand);
		}

		protected abstract Type resolveTypeProxy(String typeName);

		public synchronized Resource getResource() {
			if (typesResource == null) {
				typesResource = new ResourceImpl(
						TypeUtil.createProxyResourceURI());
				getResources().add(typesResource);
			}
			return typesResource;
		}

		private Resource typesResource = null;

		public void addToResource(final Type type) {
			final EObject object = type;
			if (object.eResource() == null) {
				add(type);
			}
		}

		protected synchronized void add(Type type) {
			getResource().getContents().add(type);
		}

	}

	private final TypeResourceSet typeRS = new TypeResourceSet() {
		@Override
		protected Type resolveTypeProxy(String typeName) {
			return getType(typeName, null, true, false, false, false);
		}
	};

	private IValueTypeFactory factory = new ValueTypeFactoryImpl(this);

	public IValueTypeFactory getFactory() {
		return factory;
	}

	private boolean resolve = true;

	private Map<String, Member> elements = new HashMap<String, Member>();

	public Member resolve(String name) {
		if (name == null)
			return null;
		Member element = elements.get(name);
		if (element != null) {
			return element;
		}
		element = TypeInfoModelLoader.getInstance().getMember(name);
		if (element != null) {
			elements.put(name, element);
			return element;
		}
		if (resolve) {
			for (IElementResolver resolver : TypeInfoManager
					.getElementResolvers()) {
				element = resolver.resolveElement(this, name);
				if (element != null) {
					elements.put(name, element);
					return element;
				}
			}
		}
		return null;
	}

	public IValue valueOf(Member member) {
		for (IMemberEvaluator evaluator : TypeInfoManager.getMemberEvaluators()) {
			final IValueCollection collection = evaluator.valueOf(this, member);
			if (collection != null) {
				if (collection instanceof IValueProvider) {
					IValue value = ((IValueProvider) collection).getValue();
					if (member.getType() != null) {
						value.setDeclaredType(RTypes.create(this,
								member.getType()));
					}
					if (value.getKind() == ReferenceKind.UNKNOWN) {
						if (member instanceof Property) {
							value.setKind(ReferenceKind.PROPERTY);
						} else {
							value.setKind(ReferenceKind.METHOD);
						}
					}
					if (value instanceof ImmutableValue) {
						ElementValue elementValue = ElementValue.createFor(
								member, this);
						return new ValueWithElementValue(
								(ImmutableValue) value, elementValue);
					}
					return value;
				} else {
					break;
				}
			}
		}
		return null;
	}

	public Set<String> listGlobals(String prefix) {
		final Set<String> result = new HashSet<String>();
		for (Member member : TypeInfoModelLoader.getInstance().listMembers(
				prefix)) {
			result.add(member.getName());
		}
		for (IElementResolver resolver : TypeInfoManager.getElementResolvers()) {
			Set<String> globals = resolver.listGlobals(this, prefix);
			if (globals != null) {
				result.addAll(globals);
			}
		}
		return result;
	}

	public void setDoResolve(boolean resolve) {
		this.resolve = resolve;
	}

	private IModelBuilder[] modelBuilders = null;

	public IModelBuilder[] getModelBuilders() {
		if (modelBuilders == null) {
			modelBuilders = TypeInfoManager.getModelBuilders(this);
		}
		return modelBuilders;
	}

	private final Map<AttributeKey<?>, List<Object>> attributes = new HashMap<AttributeKey<?>, List<Object>>();

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(AttributeKey<T> key) {
		final List<Object> values = attributes.get(key);
		return values != null && !values.isEmpty() ? (T) values.get(values
				.size() - 1) : null;
	}

	public <T> void pushAttribute(AttributeKey<T> key, T value) {
		List<Object> values = attributes.get(key);
		if (values == null) {
			values = new ArrayList<Object>(4);
			attributes.put(key, values);
		}
		values.add(value);
	}

	@SuppressWarnings("unchecked")
	public <T> T popAttribute(AttributeKey<T> key) {
		final List<Object> values = attributes.get(key);
		if (values != null && !values.isEmpty()) {
			return (T) values.remove(values.size() - 1);
		} else {
			return null;
		}
	}

}
