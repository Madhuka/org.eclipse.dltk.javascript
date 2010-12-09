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
package org.eclipse.dltk.javascript.typeinfo;

import java.util.List;

import org.eclipse.dltk.javascript.ast.FunctionStatement;
import org.eclipse.dltk.javascript.ast.VariableStatement;
import org.eclipse.dltk.javascript.typeinference.ReferenceLocation;
import org.eclipse.dltk.javascript.typeinfo.model.Type;

public interface IModelBuilder {

	public interface IElement {
		String getName();

		void setName(String name);

		String getType();

		void setType(String type);

	}

	public interface IMember extends IElement {

		/**
		 * @since 3.0
		 */
		void setDeprecated(boolean deprecated);

		/**
		 * @since 3.0
		 */
		boolean isDeprecated();

		/**
		 * @since 3.0
		 */
		void setPrivate(boolean priv);

		/**
		 * @since 3.0
		 */
		boolean isPrivate();
	}

	public interface IParameter extends IElement {

		ReferenceLocation getLocation();

		void setLocation(ReferenceLocation location);

		/**
		 * @since 3.0
		 */
		void setOptional(boolean optional);

		/**
		 * @since 3.0
		 */
		boolean isOptional();

		/**
		 * @since 3.0
		 */
		void setVarargs(boolean varargs);

		/**
		 * @since 3.0
		 */
		boolean isVarargs();

		/**
		 * @since 3.0
		 */
		void setPropertiesType(Type type);

		/**
		 * @since 3.0
		 */
		Type getPropertiesType();
	}

	public interface IMethod extends IMember {

		int getParameterCount();

		/**
		 * Returns the list of this method parameters.
		 * 
		 * @return
		 */
		List<IParameter> getParameters();

		/**
		 * Factory method to create new parameter. The parameter is not added to
		 * the list of this method parameters.
		 * 
		 * @return
		 */
		IParameter createParameter();

		/**
		 * Finds the parameter with the specified name.
		 * 
		 * @param token
		 * @return the parameter found or <code>null</code>
		 */
		IParameter getParameter(String name);

		/**
		 * Is this a constructor method/function
		 */
		boolean isConstructor();

		/**
		 * Set to true if this method is meant to be a constructor (type)
		 */
		void setConstructor(boolean constructor);
	}

	public interface IVariable extends IMember {
	}

	void processMethod(FunctionStatement statement,
			IMethod method);

	void processVariable(VariableStatement statement,
			IVariable variabe);

}
