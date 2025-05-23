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
package org.eclipse.wb.internal.rcp.model.forms.layout.column;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.rcp.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for adding selection actions for {@link IColumnLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class ColumnLayoutSelectionActionsSupport<C extends IControlInfo>
extends
ObjectEventListener {
	private final IColumnLayoutInfo<C> m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnLayoutSelectionActionsSupport(IColumnLayoutInfo<C> layout) {
		m_layout = layout;
		m_layout.addBroadcastListener(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObjectEventListener
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions) throws Exception {
		if (objects.isEmpty()) {
			return;
		}
		// prepare layout data info's
		List<IColumnLayoutDataInfo> dataInfos = new ArrayList<>();
		for (ObjectInfo object : objects) {
			// check object
			if (!(object instanceof IControlInfo) || object.getParent() != m_layout.getComposite()) {
				return;
			}
			// add data info
			@SuppressWarnings("unchecked")
			C control = (C) object;
			dataInfos.add(m_layout.getColumnData2(control));
		}
		// create horizontal actions
		actions.add(new Separator());
		addAlignmentAction(
				actions,
				dataInfos,
				CoreImages.ALIGNMENT_H_MENU_LEFT,
				ModelMessages.ColumnLayoutSelectionActionsSupport_alignmentLeft,
				ColumnLayoutData.LEFT);
		addAlignmentAction(
				actions,
				dataInfos,
				CoreImages.ALIGNMENT_H_MENU_CENTER,
				ModelMessages.ColumnLayoutSelectionActionsSupport_alignmentCenter,
				ColumnLayoutData.CENTER);
		addAlignmentAction(
				actions,
				dataInfos,
				CoreImages.ALIGNMENT_H_MENU_RIGHT,
				ModelMessages.ColumnLayoutSelectionActionsSupport_alignmentRight,
				ColumnLayoutData.RIGHT);
		addAlignmentAction(
				actions,
				dataInfos,
				CoreImages.ALIGNMENT_H_MENU_FILL,
				ModelMessages.ColumnLayoutSelectionActionsSupport_alignmentFill,
				ColumnLayoutData.FILL);
	}

	private void addAlignmentAction(List<Object> actions,
			List<IColumnLayoutDataInfo> dataInfos,
			ImageDescriptor icon,
			String tooltip,
			int alignment) throws Exception {
		boolean isChecked = true;
		// prepare select current value
		for (IColumnLayoutDataInfo columnData : dataInfos) {
			if (columnData.getHorizontalAlignment() != alignment) {
				isChecked = false;
				break;
			}
		}
		// create action
		AlignmentAction action =
				new AlignmentAction(dataInfos, icon, tooltip, isChecked, alignment);
		actions.add(action);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractAction
	//
	////////////////////////////////////////////////////////////////////////////
	private abstract class AbstractAction extends ObjectInfoAction {
		private final List<IColumnLayoutDataInfo> m_dataInfos;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AbstractAction(int style,
				List<IColumnLayoutDataInfo> dataInfos,
				ImageDescriptor icon,
				String tooltip,
				boolean checked) {
			super(m_layout.getUnderlyingModel(), "", style);
			m_dataInfos = dataInfos;
			setImageDescriptor(icon);
			setToolTipText(tooltip);
			setChecked(checked);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Run
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void runEx() throws Exception {
			for (IColumnLayoutDataInfo columnData : m_dataInfos) {
				handleHorizontal(columnData);
			}
		}

		protected abstract void handleHorizontal(IColumnLayoutDataInfo columnData) throws Exception;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// AlignmentAction
	//
	////////////////////////////////////////////////////////////////////////////
	private class AlignmentAction extends AbstractAction {
		private final int m_alignment;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AlignmentAction(List<IColumnLayoutDataInfo> dataInfos,
				ImageDescriptor icon,
				String tooltip,
				boolean checked,
				int alignment) {
			super(AS_RADIO_BUTTON, dataInfos, icon, tooltip, checked);
			m_alignment = alignment;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// AbstractAction
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void handleHorizontal(IColumnLayoutDataInfo columnData) throws Exception {
			columnData.setHorizontalAlignment(m_alignment);
		}
	}
}