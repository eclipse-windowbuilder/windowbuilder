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
package org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.actions;

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapDimensionInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.resource.ImageDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract action for manipulating selected {@link TableWrapDimensionInfo}'s.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public abstract class DimensionHeaderAction<C extends IControlInfo> extends ObjectInfoAction {
	private final IEditPartViewer m_viewer;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DimensionHeaderAction(DimensionHeaderEditPart<C> editPart, String text) {
		this(editPart, text, null);
	}

	public DimensionHeaderAction(DimensionHeaderEditPart<C> editPart,
			String text,
			ImageDescriptor imageDescriptor) {
		this(editPart, text, imageDescriptor, AS_PUSH_BUTTON);
	}

	public DimensionHeaderAction(DimensionHeaderEditPart<C> editPart,
			String text,
			ImageDescriptor imageDescriptor,
			int style) {
		super(editPart.getLayout().getUnderlyingModel(), text, style);
		m_viewer = editPart.getViewer();
		setImageDescriptor(imageDescriptor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && getClass() == obj.getClass();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected final void runEx() throws Exception {
		// prepare selection
		List<TableWrapDimensionInfo<C>> dimensions = new ArrayList<>();
		{
			for (EditPart editPart : m_viewer.getSelectedEditParts()) {
				if (editPart instanceof DimensionHeaderEditPart<?>) {
					@SuppressWarnings("unchecked")
					DimensionHeaderEditPart<C> headerEditPart = (DimensionHeaderEditPart<C>) editPart;
					dimensions.add(headerEditPart.getDimension());
				}
			}
		}
		// run over them
		run(dimensions);
	}

	/**
	 * Does some operation on {@link List} of selected {@link TableWrapDimensionInfo}'s.
	 */
	protected void run(List<TableWrapDimensionInfo<C>> dimensions) throws Exception {
		for (TableWrapDimensionInfo<C> dimension : dimensions) {
			run(dimension);
		}
	}

	/**
	 * Does some operation on selected {@link TableWrapDimensionInfo}'s.
	 */
	protected void run(TableWrapDimensionInfo<C> dimension) throws Exception {
	}
}
