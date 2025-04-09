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
package org.eclipse.wb.internal.core.editor.multi;

import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of {@link MultiMode} for without "Design" page, only with "Source" page.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
final class MultiSourceMode extends DefaultMultiMode {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MultiSourceMode(DesignerEditor editor) {
		super(editor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	void setFocus() {
		m_sourcePage.setFocus();
	}

	@Override
	public void showSource() {
		showPage(m_sourcePage);
	}

	@Override
	public void showDesign() {
	}

	@Override
	public void switchSourceDesign() {
	}

	@Override
	public void onSetInput() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void create(Composite parent) {
		m_sourcePage.initialize(m_editor);
		m_sourcePage.createControl(parent);
	}

	@Override
	void editorActivatedFirstTime() {
	}

	@Override
	void dispose() {
		m_sourcePage.dispose();
	}
}
