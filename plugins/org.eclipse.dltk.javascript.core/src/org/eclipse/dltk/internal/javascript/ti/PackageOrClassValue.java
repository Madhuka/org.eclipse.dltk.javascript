package org.eclipse.dltk.internal.javascript.ti;

import org.eclipse.dltk.javascript.typeinfo.ITypeSystem;
import org.eclipse.dltk.javascript.typeinfo.RTypes;
import org.eclipse.dltk.javascript.typeinfo.model.Type;

public class PackageOrClassValue extends Value {

	private final String name;
	private final ITypeInferenceContext context;

	public PackageOrClassValue(String name, ITypeInferenceContext context) {
		this.name = name;
		this.context = context;
		// Try to resolve it with the name with the Packages prefix.
		Type type = context.getKnownType("Packages." + name, null);
		if (type != null) {
			types.add(RTypes.classType(type));
		}
	}

	@Override
	protected ITypeSystem getTypeSystem() {
		return context;
	}

	@Override
	public IValue getChild(String childName, boolean resolve) {
		IValue child = super.getChild(childName, resolve);
		if (child == null) {
			child = createChildImpl(childName);
		}
		return child;
	}

	@Override
	public IValue createChild(String childName, int flags) {
		Value child = createChildImpl(childName);
		putDirectChild(childName, child);
		return child;
	}

	private Value createChildImpl(String childName) {
		return new PackageOrClassValue(name + "." + childName, context);
	}

}
