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
 * {@link IRule} for detect digits.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class NumbersRule implements IRule {
	private final IToken m_token;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public NumbersRule(ElPropertyUiConfiguration configuration) {
		m_token = new Token(new TextAttribute(configuration.getNumbersColor()));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IRule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		if (Character.isDigit(scanner.read())) {
			while (Character.isDigit(scanner.read())) {
			}
			scanner.unread();
			return m_token;
		}
		scanner.unread();
		return Token.UNDEFINED;
	}
}