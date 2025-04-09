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

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditorActionContributor;
import org.eclipse.ui.IEditorPart;

/**
 * Contributor for {@link DesignerEditor}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
@SuppressWarnings("restriction")
public class DesignerEditorContributor extends CompilationUnitEditorActionContributor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		if (part != null) {
			((DesignerEditor) part).activated();
		}
	}
}