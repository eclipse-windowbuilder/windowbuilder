/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;

/**
 * Presentation for {@link Label} with style.
 *
 * @author scheglov_ke
 * @coverage swt.model.presentation
 */
public final class LabelStylePresentation extends StylePresentation {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LabelStylePresentation(LabelInfo label) {
		super(label);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// StylePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initImages() {
		addImage(
				SWT.SEPARATOR | SWT.HORIZONTAL,
				"wbp-meta/org/eclipse/swt/widgets/Label_separatorHorizontal.gif");
		addImage(
				SWT.SEPARATOR | SWT.VERTICAL,
				"wbp-meta/org/eclipse/swt/widgets/Label_separatorVertical.gif");
	}
}