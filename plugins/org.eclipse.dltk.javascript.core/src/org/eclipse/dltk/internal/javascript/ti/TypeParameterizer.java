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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dltk.javascript.typeinfo.IRSimpleType;
import org.eclipse.dltk.javascript.typeinfo.IRType;
import org.eclipse.dltk.javascript.typeinfo.RTypes;
import org.eclipse.dltk.javascript.typeinfo.TypeUtil;
import org.eclipse.dltk.javascript.typeinfo.model.GenericType;
import org.eclipse.dltk.javascript.typeinfo.model.Type;
import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelFactory;
import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelPackage;
import org.eclipse.dltk.javascript.typeinfo.model.TypeVariable;
import org.eclipse.dltk.javascript.typeinfo.model.TypeVariableClassType;
import org.eclipse.dltk.javascript.typeinfo.model.TypeVariableReference;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

@SuppressWarnings("serial")
class TypeParameterizer extends EcoreUtil.Copier {

	final GenericType genericType;
	final Map<TypeVariable, IRType> parameters = new HashMap<TypeVariable, IRType>();
	final IRType[] actualParameters;

	public TypeParameterizer(GenericType genericType, List<IRType> parameters) {
		super(false);
		this.genericType = genericType;
		final EList<TypeVariable> variables = genericType.getTypeParameters();
		this.actualParameters = new IRType[variables.size()];
		for (int i = 0; i < variables.size(); ++i) {
			final TypeVariable variable = variables.get(i);
			IRType variableType = null;
			if (i < parameters.size()) {
				variableType = parameters.get(i);
			}
			if (variableType == null) {
				variableType = RTypes.any();
			}
			actualParameters[i] = variableType;
			this.parameters.put(variable, variableType);
		}
	}

	public Type copy() {
		return (Type) copy(genericType);
	}

	@Override
	public EObject copy(EObject eObject) {
		if (eObject == null) {
			return null;
		} else {
			final EObject copyEObject;
			final EClass eClass;
			if (eObject == genericType) {
				final EClass genericClass = genericType.eClass();
				final String parametererizedType = EcoreUtil.getAnnotation(
						genericClass, TypeInfoModelPackage.eNS_URI,
						"parameterizedType");
				if (parametererizedType != null) {
					final EClass parametererizedClass = (EClass) genericClass
							.getEPackage().getEClassifier(parametererizedType);
					copyEObject = EcoreUtil.create(parametererizedClass);
				} else {
					copyEObject = TypeInfoModelFactory.eINSTANCE.createType();
				}
				eClass = copyEObject.eClass();
			} else if (eObject instanceof TypeVariableReference) {
				final IRType source = parameters
						.get(((TypeVariableReference) eObject).getVariable());
				if (source != null) {
					return TypeUtil.createRType(source);
				}
				// probably generic static method in generic class.
				copyEObject = createCopy(eObject);
				eClass = eObject.eClass();
			} else if (eObject instanceof TypeVariableClassType) {
				final IRType source = parameters
						.get(((TypeVariableClassType) eObject).getVariable());
				return TypeUtil
						.createRType(RTypes
								.classType(source instanceof IRSimpleType ? ((IRSimpleType) source)
										.getTarget() : null));
			} else {
				copyEObject = createCopy(eObject);
				eClass = eObject.eClass();
			}
			put(eObject, copyEObject);
			for (int i = 0, size = eClass.getFeatureCount(); i < size; ++i) {
				EStructuralFeature eStructuralFeature = eClass
						.getEStructuralFeature(i);
				if (eStructuralFeature.isChangeable()
						&& !eStructuralFeature.isDerived()) {
					if (eStructuralFeature instanceof EAttribute) {
						copyAttribute((EAttribute) eStructuralFeature, eObject,
								copyEObject);
					} else {
						EReference eReference = (EReference) eStructuralFeature;
						if (eReference.isContainment()) {
							copyContainment(eReference, eObject, copyEObject);
						}
					}
				}
			}
			copyProxyURI(eObject, copyEObject);
			return copyEObject;
		}
	}

	@Override
	protected void copyReference(EReference eReference, EObject eObject,
			EObject copyEObject) {
		if (eObject instanceof TypeVariableReference
				|| eObject instanceof TypeVariableClassType)
			return;
		if (eReference == TypeInfoModelPackage.Literals.TYPE__SUPER_TYPE
				&& eObject == genericType) {
			// superType is handled specially
			return;
		}
		super.copyReference(eReference, eObject, copyEObject);
	}
}
