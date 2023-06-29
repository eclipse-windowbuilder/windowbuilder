/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.layout.grid;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.swt.model.layout.ILayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.ILayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutAssistantSupport;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import java.util.List;

/**
 * Assistant pages provider for {@link GridLayout}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class GridLayoutAssistant extends LayoutAssistantSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridLayoutAssistant(ILayoutInfo<?> layout) {
		super(layout);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Pages
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractAssistantPage createLayoutPage(Composite parent) {
		return new GridLayoutAssistantPage(parent, m_layout);
	}

	@Override
	protected AbstractAssistantPage createConstraintsPage(Composite parent, List<ObjectInfo> objects) {
		List<ILayoutDataInfo> dataList = getDataList(objects);
		return new GridLayoutDataAssistantPage(parent, dataList);
	}
}
