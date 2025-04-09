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
package org.eclipse.wb.internal.swing.model.component;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.variable.VoidInvocationVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import javax.swing.JToolBar;

/**
 * Implementation of {@link ComponentInfo} for {@link JToolBar#addSeparator()} and
 * {@link JToolBar#addSeparator(java.awt.Dimension)}.
 *
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class JToolBarSeparatorInfo extends ComponentInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JToolBarSeparatorInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		setVariableSupport(new VoidInvocationVariableSupport(this));
	}
}
