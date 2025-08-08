/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.model.layout.gbl.actions;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagConstraintsInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;
import org.eclipse.wb.swing.SwingImages;

import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for adding selection actions for {@link AbstractGridBagLayoutInfo}.
 *
 * @author lobas_av
 * @coverage swing.model.layout
 */
public class SelectionActionsSupport extends ObjectEventListener {
	private final AbstractGridBagLayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SelectionActionsSupport(AbstractGridBagLayoutInfo layout) {
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
		// prepare constraints
		List<AbstractGridBagConstraintsInfo> constraints = new ArrayList<>();
		for (ObjectInfo object : objects) {
			// check object
			if (!m_layout.isManagedObject(object)) {
				return;
			}
			// add data info
			ComponentInfo component = (ComponentInfo) object;
			constraints.add(m_layout.getConstraints(component));
		}
		// create horizontal actions
		actions.add(new Separator());
		addAlignmentAction(
				actions,
				constraints,
				true,
				CoreImages.ALIGNMENT_H_MENU_LEFT,
				ModelMessages.SelectionActionsSupport_haLeft,
				ColumnInfo.Alignment.LEFT);
		addAlignmentAction(
				actions,
				constraints,
				true,
				CoreImages.ALIGNMENT_H_MENU_CENTER,
				ModelMessages.SelectionActionsSupport_haCenter,
				ColumnInfo.Alignment.CENTER);
		addAlignmentAction(
				actions,
				constraints,
				true,
				CoreImages.ALIGNMENT_H_MENU_RIGHT,
				ModelMessages.SelectionActionsSupport_haRight,
				ColumnInfo.Alignment.RIGHT);
		addAlignmentAction(
				actions,
				constraints,
				true,
				CoreImages.ALIGNMENT_H_MENU_FILL,
				ModelMessages.SelectionActionsSupport_haFill,
				ColumnInfo.Alignment.FILL);
		// create vertical actions
		actions.add(new Separator());
		addAlignmentAction(
				actions,
				constraints,
				false,
				CoreImages.ALIGNMENT_V_MENU_TOP,
				ModelMessages.SelectionActionsSupport_vaTop,
				RowInfo.Alignment.TOP);
		addAlignmentAction(
				actions,
				constraints,
				false,
				CoreImages.ALIGNMENT_V_MENU_CENTER,
				ModelMessages.SelectionActionsSupport_vaCenter,
				RowInfo.Alignment.CENTER);
		addAlignmentAction(
				actions,
				constraints,
				false,
				CoreImages.ALIGNMENT_V_MENU_BOTTOM,
				ModelMessages.SelectionActionsSupport_vaBottom,
				RowInfo.Alignment.BOTTOM);
		addAlignmentAction(
				actions,
				constraints,
				false,
				CoreImages.ALIGNMENT_V_MENU_FILL,
				ModelMessages.SelectionActionsSupport_vaFill,
				RowInfo.Alignment.FILL);
		addAlignmentAction(
				actions,
				constraints,
				false,
				SwingImages.ALIGNMENT_V_MENU_BASELINE,
				ModelMessages.SelectionActionsSupport_vaBaseline,
				RowInfo.Alignment.BASELINE);
		addAlignmentAction(
				actions,
				constraints,
				false,
				SwingImages.ALIGNMENT_V_MENU_BASELINE_ABOVE,
				ModelMessages.SelectionActionsSupport_vaAboveBaseline,
				RowInfo.Alignment.BASELINE_ABOVE);
		addAlignmentAction(
				actions,
				constraints,
				false,
				SwingImages.ALIGNMENT_V_MENU_BASELINE_BELOW,
				ModelMessages.SelectionActionsSupport_vaBelowBaseline,
				RowInfo.Alignment.BASELINE_BELOW);
		// create grow actions
		actions.add(new Separator());
		addGrowAction(
				actions,
				constraints,
				true,
				CoreImages.ALIGNMENT_H_MENU_GROW,
				ModelMessages.SelectionActionsSupport_haGrow);
		addGrowAction(
				actions,
				constraints,
				false,
				CoreImages.ALIGNMENT_V_MENU_GROW,
				ModelMessages.SelectionActionsSupport_vaGrow);
	}

	private void addAlignmentAction(List<Object> actions,
			List<AbstractGridBagConstraintsInfo> constraints,
			boolean horizontal,
			ImageDescriptor icon,
			String tooltip,
			Object alignment) {
		boolean isChecked = true;
		// prepare select current value
		for (AbstractGridBagConstraintsInfo constraint : constraints) {
			if (horizontal) {
				if (constraint.getHorizontalAlignment() != alignment) {
					isChecked = false;
					break;
				}
			} else {
				if (constraint.getVerticalAlignment() != alignment) {
					isChecked = false;
					break;
				}
			}
		}
		// create action
		AlignmentAction action =
				new AlignmentAction(constraints, horizontal, icon, tooltip, isChecked, alignment);
		actions.add(action);
	}

	private void addGrowAction(List<Object> actions,
			List<AbstractGridBagConstraintsInfo> constraints,
			boolean horizontal,
			ImageDescriptor icon,
			String tooltip) {
		boolean isChecked = true;
		// prepare select current value
		for (AbstractGridBagConstraintsInfo constraint : constraints) {
			if (horizontal) {
				if (!constraint.getColumn().hasWeight()) {
					isChecked = false;
					break;
				}
			} else {
				if (!constraint.getRow().hasWeight()) {
					isChecked = false;
					break;
				}
			}
		}
		// create action
		GrowAction action =
				new GrowAction(constraints, horizontal, icon, tooltip, isChecked, !isChecked);
		actions.add(action);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Actions
	//
	////////////////////////////////////////////////////////////////////////////
	private abstract class AbstractAction extends ObjectInfoAction {
		private final List<AbstractGridBagConstraintsInfo> m_constraints;
		private final boolean m_horizontal;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AbstractAction(int style,
				List<AbstractGridBagConstraintsInfo> constraints,
				boolean horizontal,
				ImageDescriptor icon,
				String tooltip,
				boolean checked) {
			super(m_layout, "", style);
			m_constraints = constraints;
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
			for (AbstractGridBagConstraintsInfo constraint : m_constraints) {
				if (m_horizontal) {
					handleHorizontal(constraint);
				} else {
					handleVertical(constraint);
				}
			}
		}

		protected abstract void handleHorizontal(AbstractGridBagConstraintsInfo constraint)
				throws Exception;

		protected abstract void handleVertical(AbstractGridBagConstraintsInfo constraint)
				throws Exception;
	}
	private final class AlignmentAction extends AbstractAction {
		private final Object m_alignment;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AlignmentAction(List<AbstractGridBagConstraintsInfo> constraints,
				boolean horizontal,
				ImageDescriptor icon,
				String tooltip,
				boolean checked,
				Object alignment) {
			super(AS_RADIO_BUTTON, constraints, horizontal, icon, tooltip, checked);
			m_alignment = alignment;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// AbstractAction
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void handleHorizontal(AbstractGridBagConstraintsInfo constraint) throws Exception {
			constraint.setHorizontalAlignment((ColumnInfo.Alignment) m_alignment);
		}

		@Override
		protected void handleVertical(AbstractGridBagConstraintsInfo constraint) throws Exception {
			constraint.setVerticalAlignment((RowInfo.Alignment) m_alignment);
		}
	}
	private final class GrowAction extends AbstractAction {
		private final boolean m_grow;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public GrowAction(List<AbstractGridBagConstraintsInfo> constraints,
				boolean horizontal,
				ImageDescriptor icon,
				String tooltip,
				boolean checked,
				boolean grow) {
			super(AS_CHECK_BOX, constraints, horizontal, icon, tooltip, checked);
			m_grow = grow;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// AbstractAction
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void handleHorizontal(AbstractGridBagConstraintsInfo constraint) throws Exception {
			ColumnInfo column = constraint.getColumn();
			column.setWeight(m_grow ? 1 : 0);
		}

		@Override
		protected void handleVertical(AbstractGridBagConstraintsInfo constraint) throws Exception {
			RowInfo row = constraint.getRow();
			row.setWeight(m_grow ? 1 : 0);
		}
	}
}