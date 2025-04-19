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
package org.eclipse.wb.internal.swing.MigLayout.gef.header.actions;

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.edit.ColumnHeaderEditPart;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.MigLayout.model.MigDimensionInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.resource.ImageDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract action for manipulating selected {@link MigDimensionInfo}'s.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
public abstract class DimensionHeaderAction<T extends MigDimensionInfo> extends ObjectInfoAction {
	private final boolean m_horizontal;
	private final IEditPartViewer m_viewer;
	private final MigLayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DimensionHeaderAction(DimensionHeaderEditPart<T> editPart, String text) {
		this(editPart, text, null);
	}

	public DimensionHeaderAction(DimensionHeaderEditPart<T> editPart,
			String text,
			ImageDescriptor imageDescriptor) {
		this(editPart, text, imageDescriptor, AS_PUSH_BUTTON);
	}

	public DimensionHeaderAction(DimensionHeaderEditPart<T> editPart,
			String text,
			ImageDescriptor imageDescriptor,
			int style) {
		super(editPart.getLayout(), text, style);
		m_horizontal = editPart instanceof ColumnHeaderEditPart;
		m_viewer = editPart.getViewer();
		m_layout = editPart.getLayout();
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
		List<T> dimensions = new ArrayList<>();
		{
			List<? extends EditPart> editParts = m_viewer.getSelectedEditParts();
			for (EditPart editPart : editParts) {
				if (editPart instanceof DimensionHeaderEditPart) {
					@SuppressWarnings("unchecked")
					DimensionHeaderEditPart<T> headerEditPart = (DimensionHeaderEditPart<T>) editPart;
					dimensions.add(headerEditPart.getDimension());
				}
			}
		}
		// run over them
		run(dimensions);
	}

	/**
	 * Does some operation on {@link List} of selected {@link MigDimensionInfo}'s.
	 */
	protected void run(List<T> dimensions) throws Exception {
		List<?> allDimensions = m_horizontal ? m_layout.getColumns() : m_layout.getRows();
		for (T dimension : dimensions) {
			int index = allDimensions.indexOf(dimension);
			run(dimension, index);
		}
		m_layout.writeDimensions();
	}

	/**
	 * Does some operation on selected {@link MigDimensionInfo}'s.
	 */
	protected void run(T dimension, int index) throws Exception {
	}
}
