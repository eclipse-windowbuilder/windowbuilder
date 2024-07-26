/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.layout.grid.actions;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.layout.grid.IGridDataInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.IGridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for adding selection actions for {@link IGridLayoutInfo}.
 *
 * @author lobas_av
 * @coverage swt.model.layout
 */
public final class SelectionActionsSupport<C extends IControlInfo> extends ObjectEventListener {
	private final IGridLayoutInfo<C> m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SelectionActionsSupport(IGridLayoutInfo<C> layout) {
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
		List<IGridDataInfo> dataInfos = new ArrayList<>();
		{
			List<C> controls = m_layout.getControls();
			for (ObjectInfo object : objects) {
				// check object
				if (!controls.contains(object)) {
					return;
				}
				// add data info
				@SuppressWarnings("unchecked")
				C control = (C) object;
				dataInfos.add(m_layout.getGridData2(control));
			}
		}
		// create horizontal actions
		actions.add(new Separator());
		addAlignmentAction(
				actions,
				dataInfos,
				true,
				CoreImages.ALIGNMENT_H_MENU_LEFT,
				ModelMessages.SelectionActionsSupport_horLeft,
				SWT.LEFT);
		addAlignmentAction(
				actions,
				dataInfos,
				true,
				CoreImages.ALIGNMENT_H_MENU_CENTER,
				ModelMessages.SelectionActionsSupport_horCenter,
				SWT.CENTER);
		addAlignmentAction(
				actions,
				dataInfos,
				true,
				CoreImages.ALIGNMENT_H_MENU_RIGHT,
				ModelMessages.SelectionActionsSupport_horRight,
				SWT.RIGHT);
		addAlignmentAction(
				actions,
				dataInfos,
				true,
				CoreImages.ALIGNMENT_H_MENU_FILL,
				ModelMessages.SelectionActionsSupport_horFill,
				SWT.FILL);
		// create vertical actions
		actions.add(new Separator());
		addAlignmentAction(
				actions,
				dataInfos,
				false,
				CoreImages.ALIGNMENT_V_MENU_TOP,
				ModelMessages.SelectionActionsSupport_verTop,
				SWT.TOP);
		addAlignmentAction(
				actions,
				dataInfos,
				false,
				CoreImages.ALIGNMENT_V_MENU_CENTER,
				ModelMessages.SelectionActionsSupport_verCenter,
				SWT.CENTER);
		addAlignmentAction(
				actions,
				dataInfos,
				false,
				CoreImages.ALIGNMENT_V_MENU_BOTTOM,
				ModelMessages.SelectionActionsSupport_verBottom,
				SWT.BOTTOM);
		addAlignmentAction(
				actions,
				dataInfos,
				false,
				CoreImages.ALIGNMENT_V_MENU_FILL,
				ModelMessages.SelectionActionsSupport_verFill,
				SWT.FILL);
		// create grab actions
		actions.add(new Separator());
		addGrabAction(
				actions,
				dataInfos,
				true,
				CoreImages.ALIGNMENT_H_MENU_GROW,
				ModelMessages.SelectionActionsSupport_horGrab);
		addGrabAction(
				actions,
				dataInfos,
				false,
				CoreImages.ALIGNMENT_V_MENU_GROW,
				ModelMessages.SelectionActionsSupport_verGrab);
	}

	private void addAlignmentAction(List<Object> actions,
			List<IGridDataInfo> dataInfos,
			boolean horizontal,
			ImageDescriptor icon,
			String tooltip,
			int alignment) {
		boolean isChecked = true;
		// prepare select current value
		for (IGridDataInfo gridData : dataInfos) {
			if (horizontal) {
				if (gridData.getHorizontalAlignment() != alignment) {
					isChecked = false;
					break;
				}
			} else {
				if (gridData.getVerticalAlignment() != alignment) {
					isChecked = false;
					break;
				}
			}
		}
		// create action
		AlignmentAction action =
				new AlignmentAction(dataInfos, horizontal, icon, tooltip, isChecked, alignment);
		actions.add(action);
	}

	private void addGrabAction(List<Object> actions,
			List<IGridDataInfo> dataInfos,
			boolean horizontal,
			ImageDescriptor icon,
			String tooltip) {
		boolean isChecked = true;
		// prepare select current value
		for (IGridDataInfo gridData : dataInfos) {
			if (horizontal) {
				if (!gridData.getHorizontalGrab()) {
					isChecked = false;
					break;
				}
			} else {
				if (!gridData.getVerticalGrab()) {
					isChecked = false;
					break;
				}
			}
		}
		// create action
		GrabAction action =
				new GrabAction(dataInfos, horizontal, icon, tooltip, isChecked, !isChecked);
		actions.add(action);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Actions
	//
	////////////////////////////////////////////////////////////////////////////
	private abstract class AbstractAction extends ObjectInfoAction {
		private final List<IGridDataInfo> m_dataInfos;
		private final boolean m_horizontal;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AbstractAction(int style,
				List<IGridDataInfo> dataInfos,
				boolean horizontal,
				ImageDescriptor icon,
				String tooltip,
				boolean checked) {
			super(m_layout.getUnderlyingModel(), "", style);
			m_dataInfos = dataInfos;
			m_horizontal = horizontal;
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
			for (IGridDataInfo gridData : m_dataInfos) {
				if (m_horizontal) {
					handleHorizontal(gridData);
				} else {
					handleVertical(gridData);
				}
			}
		}

		protected abstract void handleHorizontal(IGridDataInfo gridData) throws Exception;

		protected abstract void handleVertical(IGridDataInfo gridData) throws Exception;
	}
	private class AlignmentAction extends AbstractAction {
		private final int m_alignment;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AlignmentAction(List<IGridDataInfo> dataInfos,
				boolean horizontal,
				ImageDescriptor icon,
				String tooltip,
				boolean checked,
				int alignment) {
			super(AS_RADIO_BUTTON, dataInfos, horizontal, icon, tooltip, checked);
			m_alignment = alignment;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// AbstractAction
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void handleHorizontal(IGridDataInfo gridData) throws Exception {
			gridData.setHorizontalAlignment(m_alignment);
		}

		@Override
		protected void handleVertical(IGridDataInfo gridData) throws Exception {
			gridData.setVerticalAlignment(m_alignment);
		}
	}
	private class GrabAction extends AbstractAction {
		private final boolean m_grab;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public GrabAction(List<IGridDataInfo> dataInfos,
				boolean horizontal,
				ImageDescriptor icon,
				String tooltip,
				boolean checked,
				boolean grab) {
			super(AS_CHECK_BOX, dataInfos, horizontal, icon, tooltip, checked);
			m_grab = grab;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// AbstractAction
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void handleHorizontal(IGridDataInfo gridData) throws Exception {
			gridData.setHorizontalGrab(m_grab);
		}

		@Override
		protected void handleVertical(IGridDataInfo gridData) throws Exception {
			gridData.setVerticalGrab(m_grab);
		}
	}
}