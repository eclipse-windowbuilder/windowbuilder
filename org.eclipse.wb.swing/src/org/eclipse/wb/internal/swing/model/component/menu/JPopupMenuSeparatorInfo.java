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
package org.eclipse.wb.internal.swing.model.component.menu;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.variable.VoidInvocationVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jdt.core.dom.MethodInvocation;

import javax.swing.JMenu;

/**
 * Implementation of {@link ComponentInfo} for {@link JMenu#addSeparator()}.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage swing.model.menu
 */
public final class JPopupMenuSeparatorInfo extends JMenuItemInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JPopupMenuSeparatorInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		if (creationSupport.getNode() instanceof MethodInvocation) {
			setVariableSupport(new VoidInvocationVariableSupport(this));
		}
	}
}
