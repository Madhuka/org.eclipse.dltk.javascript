package org.eclipse.dltk.internal.javascript.parser.structure;

import java.util.Collections;
import java.util.List;

public class PropertyDeclaration extends StructureNode implements IDeclaration {

	private final String name;
	private IStructureNode value;

	public PropertyDeclaration(IScope parent, String name) {
		super(parent);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public IStructureNode getValue() {
		return value;
	}

	public void setValue(IStructureNode value) {
		this.value = value;
	}

	public List<IStructureNode> getChildren() {
		return value != null ? Collections.singletonList(value) : Collections
				.<IStructureNode> emptyList();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(":");
		if (value != null) {
			sb.append(value);
		} else {
			sb.append("<property>");
		}
		return sb.toString();
	}

}