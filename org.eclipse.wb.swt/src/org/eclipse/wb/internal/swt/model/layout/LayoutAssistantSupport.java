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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SWT provider for layout assistant pages.
 *
 * @author lobas_av
 * @coverage swt.assistant
 */
public abstract class LayoutAssistantSupport
extends
org.eclipse.wb.core.editor.actions.assistant.LayoutAssistantSupport {
	protected final LayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutAssistantSupport(LayoutInfo layout) {
		super(layout.getUnderlyingModel());
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// LayoutAssistantSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected final String getConstraintsPageTitle() {
		return ModelMessages.LayoutAssistantSupport_layoutDataPage;
	}

	@Override
	protected final ObjectInfo getContainer() {
		return m_layout.getComposite().getUnderlyingModel();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Converts {@link IControlInfo}s into their {@link ILayoutDataInfo}s.
	 */
	protected final List<LayoutDataInfo> getDataList(List<ObjectInfo> objects) {
		List<LayoutDataInfo> dataList = objects.stream() //
				.map(from -> m_layout.getLayoutData2((IControlInfo) from)) //
				.collect(Collectors.toList());
		return dataList;
	}
}