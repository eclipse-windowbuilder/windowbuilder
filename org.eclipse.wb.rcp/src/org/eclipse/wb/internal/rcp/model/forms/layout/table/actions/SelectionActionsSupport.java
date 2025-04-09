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
package org.eclipse.wb.internal.rcp.model.forms.layout.table.actions;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.rcp.model.ModelMessages;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.ITableWrapDataInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.ITableWrapLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.forms.widgets.TableWrapData;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for adding selection actions for {@link ITableWrapLayout_Info<C>}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class SelectionActionsSupport<C extends IControlInfo> extends ObjectEventListener {
	private final ITableWrapLayoutInfo<C> m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SelectionActionsSupport(ITableWrapLayoutInfo<C> layout) {
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
		List<ITableWrapDataInfo> dataInfos = new ArrayList<>();
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
				dataInfos.add(m_layout.getTableWrapData2(control));
			}
		}
		// create horizontal actions
		actions.add(new Separator());
		addAlignmentAction(
				actions,
				dataInfos,
				true,
				CoreImages.ALIGNMENT_H_MENU_LEFT,
				ModelMessages.SelectionActionsSupport_haLeft,
				TableWrapData.LEFT);
		addAlignmentAction(
				actions,
				dataInfos,
				true,
				CoreImages.ALIGNMENT_H_MENU_CENTER,
				ModelMessages.SelectionActionsSupport_haCenter,
				TableWrapData.CENTER);
		addAlignmentAction(
				actions,
				dataInfos,
				true,
				CoreImages.ALIGNMENT_H_MENU_RIGHT,
				ModelMessages.SelectionActionsSupport_haRight,
				TableWrapData.RIGHT);
		addAlignmentAction(
				actions,
				dataInfos,
				true,
				CoreImages.ALIGNMENT_H_MENU_FILL,
				ModelMessages.SelectionActionsSupport_haFill,
				TableWrapData.FILL);
		// create vertical actions
		actions.add(new Separator());
		addAlignmentAction(
				actions,
				dataInfos,
				false,
				CoreImages.ALIGNMENT_V_MENU_TOP,
				ModelMessages.SelectionActionsSupport_vaTop,
				TableWrapData.TOP);
		addAlignmentAction(
				actions,
				dataInfos,
				false,
				CoreImages.ALIGNMENT_V_MENU_CENTER,
				ModelMessages.SelectionActionsSupport_vaMiddle,
				TableWrapData.MIDDLE);
		addAlignmentAction(
				actions,
				dataInfos,
				false,
				CoreImages.ALIGNMENT_V_MENU_BOTTOM,
				ModelMessages.SelectionActionsSupport_vaBottom,
				TableWrapData.BOTTOM);
		addAlignmentAction(
				actions,
				dataInfos,
				false,
				CoreImages.ALIGNMENT_V_MENU_FILL,
				ModelMessages.SelectionActionsSupport_vaFill,
				TableWrapData.FILL);
		// create grab actions
		actions.add(new Separator());
		addGrabAction(
				actions,
				dataInfos,
				true,
				CoreImages.ALIGNMENT_H_MENU_GROW,
				ModelMessages.SelectionActionsSupport_haGrab);
		addGrabAction(
				actions,
				dataInfos,
				false,
				CoreImages.ALIGNMENT_V_MENU_GROW,
				ModelMessages.SelectionActionsSupport_vaGrab);
	}

	private void addAlignmentAction(List<Object> actions,
			List<ITableWrapDataInfo> dataInfos,
			boolean horizontal,
			ImageDescriptor icon,
			String tooltip,
			int alignment) {
		boolean isChecked = true;
		// prepare select current value
		for (ITableWrapDataInfo layoutData : dataInfos) {
			if (horizontal) {
				if (layoutData.getHorizontalAlignment() != alignment) {
					isChecked = false;
					break;
				}
			} else {
				if (layoutData.getVerticalAlignment() != alignment) {
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
			List<ITableWrapDataInfo> dataInfos,
			boolean horizontal,
			ImageDescriptor icon,
			String tooltip) {
		boolean isChecked = true;
		// prepare select current value
		for (ITableWrapDataInfo layoutData : dataInfos) {
			if (horizontal) {
				if (!layoutData.getHorizontalGrab()) {
					isChecked = false;
					break;
				}
			} else {
				if (!layoutData.getVerticalGrab()) {
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
		private final List<ITableWrapDataInfo> m_dataInfos;
		private final boolean m_horizontal;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AbstractAction(int style,
				List<ITableWrapDataInfo> dataInfos,
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
			for (ITableWrapDataInfo layoutData : m_dataInfos) {
				if (m_horizontal) {
					handleHorizontal(layoutData);
				} else {
					handleVertical(layoutData);
				}
			}
		}

		protected abstract void handleHorizontal(ITableWrapDataInfo layoutData) throws Exception;

		protected abstract void handleVertical(ITableWrapDataInfo layoutData) throws Exception;
	}
	private class AlignmentAction extends AbstractAction {
		private final int m_alignment;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AlignmentAction(List<ITableWrapDataInfo> dataInfos,
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
		protected void handleHorizontal(ITableWrapDataInfo layoutData) throws Exception {
			layoutData.setHorizontalAlignment(m_alignment);
		}

		@Override
		protected void handleVertical(ITableWrapDataInfo layoutData) throws Exception {
			layoutData.setVerticalAlignment(m_alignment);
		}
	}
	private class GrabAction extends AbstractAction {
		private final boolean m_grab;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public GrabAction(List<ITableWrapDataInfo> dataInfos,
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
		protected void handleHorizontal(ITableWrapDataInfo layoutData) throws Exception {
			layoutData.setHorizontalGrab(m_grab);
		}

		@Override
		protected void handleVertical(ITableWrapDataInfo layoutData) throws Exception {
			layoutData.setVerticalGrab(m_grab);
		}
	}
}