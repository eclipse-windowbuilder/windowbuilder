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

import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.top.JPanelTopBoundsSupport;

import javax.swing.JPanel;

/**
 * Model for {@link JPanel}.
 *
 * @author scheglov_ke
 * @coverage swing.model
 */
public class JPanelInfo extends ContainerInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JPanelInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TopBoundsSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected TopBoundsSupport createTopBoundsSupport() {
		return new JPanelTopBoundsSupport(this);
	}
}
