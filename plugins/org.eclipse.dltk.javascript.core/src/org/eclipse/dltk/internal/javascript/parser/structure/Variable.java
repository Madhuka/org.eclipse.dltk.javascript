package org.eclipse.dltk.internal.javascript.parser.structure;

import java.util.Collections;
import java.util.List;

public class Variable extends StructureNode implements IDeclaration {

	private final String name;
	private IStructureNode value;

	public Variable(IScope parent, String name) {
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

	@Override
	public List<IStructureNode> getChildren() {
		return value != null ? Collections.singletonList(value) : Collections
				.<IStructureNode> emptyList();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(":variable");
		if (value != null) {
			sb.append("=");
			sb.append(value);
		}
		return sb.toString();
	}

}
