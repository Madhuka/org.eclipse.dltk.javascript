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
package org.eclipse.dltk.internal.javascript.typeinference;

import java.util.Stack;

public class CompletionString {

	private static class Bracket {
		final char ch;
		final int position;

		public Bracket(char ch, int position) {
			this.ch = ch;
			this.position = position;
		}

	}

	public static String parse(String id, boolean dotBeforeBrackets) {
		StringBuffer sb = new StringBuffer();
		int start = 0;
		int current = id.length();
		final Stack<Bracket> inBrackStack = new Stack<Bracket>();
		boolean inStringSingle = false;
		boolean inStringDouble = false;
		for (int i = id.length(); --i >= 0;) {
			char c = id.charAt(i);
			if (c == '\'') {
				if (inStringSingle) {
					inStringSingle = false;
					continue;
				}
				// end of a string try to skip this.
				if (!inStringDouble)
					inStringSingle = true;
			}
			if (c == '\"') {
				if (inStringDouble) {
					inStringDouble = false;
					continue;
				}
				// end of a string try to skip this.
				if (!inStringSingle)
					inStringDouble = true;
			}
			if (inStringSingle || inStringDouble)
				continue;

			if (c == ']') {
				if (inBrackStack.isEmpty()) {
					String brackets = "[]";
					if (dotBeforeBrackets && i > 0
							&& ((i - 2) < 0 || id.charAt(i - 2) != '.')) {
						brackets = ".[]";
					}
					sb.insert(0, brackets + id.substring(i + 1, current));
				}
				inBrackStack.push(new Bracket('[', i));
				continue;
			}
			if (c == ')') {
				if (inBrackStack.isEmpty()) {
					sb.insert(0, id.substring(i + 1, current));
				}
				inBrackStack.push(new Bracket('(', i));
				continue;
			}
			if (c == '[' || c == '(') {
				if (inBrackStack.isEmpty()) {
					if (i + 1 < id.length() && id.charAt(i + 1) == c) {
						// illegal code like [[xx]. try best guess
						id = id.substring(0, i) + id.substring(i + 1);
						return parse(id, dotBeforeBrackets);
					}
					return id.substring(i + 1, current) + sb.toString();
				}
				if (c == inBrackStack.peek().ch) {
					current = i;
					inBrackStack.pop();
				}
				continue;
			}
			if (c != '.'
					&& c != ':'
					&& inBrackStack.isEmpty()
					&& (Character.isWhitespace(c) || !Character
							.isJavaIdentifierPart(c))) {
				start = i + 1;
				break;
			}
		}
		if (start == 0 && current == id.length() && inBrackStack.isEmpty())
			return id;
		if (!inBrackStack.isEmpty()) { // illegal code like []]
			Bracket last = inBrackStack.pop();
			id = id.substring(start, last.position)
					+ id.substring(last.position + 1, id.length());
			return parse(id, dotBeforeBrackets);
		}
		sb.insert(0, id.substring(start, current));
		return sb.toString();
	}

}