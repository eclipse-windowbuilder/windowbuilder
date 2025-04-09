/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.model.forms;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.ui.forms.widgets.Section;

/**
 * Model for {@link Section}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class SectionInfo extends ExpandableCompositeInfo {
	private static final String[] POSITIONS = {
			"setTextClient",
			"setDescriptionControl",
			"setSeparatorControl",
	"setClient"};

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SectionInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport, POSITIONS);
	}

	@Override
	public Section getWidget() {
		return (Section) getObject();
	}
}
