package org.eclipse.dltk.internal.javascript.ti;

import org.eclipse.dltk.javascript.typeinference.IValueParent;
import org.eclipse.dltk.javascript.typeinference.IValueReference;
import org.eclipse.dltk.javascript.typeinfo.ITypeInfoContext;
import org.eclipse.dltk.javascript.typeinfo.ITypeNames;
import org.eclipse.dltk.javascript.typeinfo.RTypes;

public class XMLListValue extends AbstractReference {

	private final Value value = new Value() {
		public IValue getChild(String name, boolean resolve) {
			IValue value = super.getChild(name, resolve);
			if (value == null) {
				// if the value is not found (not a know existing child) then
				// just return this.
				// so that expression are always resolved to a XMLList
				value = this;
			}
			return value;
		};
	};

	private final IValueParent parent;

	public XMLListValue(IValueParent parent, ITypeInfoContext context) {
		this.parent = parent;
		value.setDeclaredType(RTypes.simple(context.getType(ITypeNames.XML)));
	}

	public IValueReference getParent() {
		if (parent instanceof IValueReference)
			return (IValueReference) parent;
		return null;
	}

	public String getName() {
		return "";
	}

	public void delete() {
	}

	public boolean isReference() {
		return false;
	}

	@Override
	public IValue getValue() {
		return value;
	}

	@Override
	public IValue createValue() {
		return getValue();
	}

}
