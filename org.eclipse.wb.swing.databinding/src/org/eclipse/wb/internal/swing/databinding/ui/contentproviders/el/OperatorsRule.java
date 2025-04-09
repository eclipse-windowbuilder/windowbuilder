/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * {@link IRule} for detect {@code EL} operators.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class OperatorsRule implements IRule {
	private final IToken m_token;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public OperatorsRule(ElPropertyUiConfiguration configuration) {
		m_token = new Token(new TextAttribute(configuration.getOperatorsColor()));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IRule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		CharacterScannerWrapper wrapper = new CharacterScannerWrapper(scanner);
		switch (wrapper.read()) {
		case '{' :
		case '}' :
		case '[' :
		case ']' :
		case '$' :
		case '#' :
		case '%' :
		case '?' :
		case ':' :
		case '.' :
		case '*' :
		case '+' :
		case '-' :
		case '/' :
			return m_token;
		case '>' :
		case '<' :
		case '!' :
			if (!wrapper.test('=')) {
				scanner.unread();
			}
			return m_token;
		case '=' :
			if (wrapper.test('=')) {
				return m_token;
			}
			break;
		case '&' :
			if (wrapper.test('&')) {
				return m_token;
			}
			break;
		case '|' :
			if (wrapper.test('|')) {
				return m_token;
			}
			break;
		}
		wrapper.unread();
		return Token.UNDEFINED;
	}
}