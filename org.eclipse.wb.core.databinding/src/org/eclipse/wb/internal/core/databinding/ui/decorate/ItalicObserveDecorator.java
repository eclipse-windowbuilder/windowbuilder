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
package org.eclipse.wb.internal.core.databinding.ui.decorate;

import org.eclipse.swt.graphics.Font;

/**
 * Decorator with "Italic" font.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class ItalicObserveDecorator extends ObserveDecorator {
	////////////////////////////////////////////////////////////////////////////
	//
	// IObserveDecorator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Font getFont(Font baseItalicFont, Font baseBoldFont, Font baseBoldItalicFont) {
		return baseItalicFont;
	}
}