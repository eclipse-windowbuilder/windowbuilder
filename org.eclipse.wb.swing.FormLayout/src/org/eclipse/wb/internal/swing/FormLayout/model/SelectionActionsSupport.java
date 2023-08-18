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
package org.eclipse.wb.internal.swing.FormLayout.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swing.FormLayout.Activator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jface.action.Separator;

import com.jgoodies.forms.layout.CellConstraints;

import java.util.List;

/**
 * Helper for adding selection actions for {@link FormLayoutInfo}.
 *
 * @author lobas_av
 * @coverage swing.FormLayout.model
 */
public class SelectionActionsSupport extends ObjectEventListener {
	private final FormLayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SelectionActionsSupport(FormLayoutInfo layout) {
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
		List<CellConstraintsSupport> constraints = Lists.newArrayList();
		for (ObjectInfo object : objects) {
			// check object
			if (!(object instanceof ComponentInfo component) || object.getParent() != m_layout.getContainer()) {
				return;
			}
			constraints.add(FormLayoutInfo.getConstraints(component));
		}
		// create horizontal actions
		actions.add(new Separator());
		addAlignmentAction(
				actions,
				constraints,
				true,
				"default.gif",
				ModelMessages.SelectionActionsSupport_haDefault,
				CellConstraints.DEFAULT);
		addAlignmentAction(
				actions,
				constraints,
				true,
				"left.gif",
				ModelMessages.SelectionActionsSupport_haLeft,
				CellConstraints.LEFT);
		addAlignmentAction(
				actions,
				constraints,
				true,
				"center.gif",
				ModelMessages.SelectionActionsSupport_haCenter,
				CellConstraints.CENTER);
		addAlignmentAction(
				actions,
				constraints,
				true,
				"right.gif",
				ModelMessages.SelectionActionsSupport_haRight,
				CellConstraints.RIGHT);
		addAlignmentAction(
				actions,
				constraints,
				true,
				"fill.gif",
				ModelMessages.SelectionActionsSupport_haFill,
				CellConstraints.FILL);
		// create vertical actions
		actions.add(new Separator());
		addAlignmentAction(
				actions,
				constraints,
				false,
				"default.gif",
				ModelMessages.SelectionActionsSupport_vaDefault,
				CellConstraints.DEFAULT);
		addAlignmentAction(
				actions,
				constraints,
				false,
				"top.gif",
				ModelMessages.SelectionActionsSupport_haTop,
				CellConstraints.TOP);
		addAlignmentAction(
				actions,
				constraints,
				false,
				"center.gif",
				ModelMessages.SelectionActionsSupport_vaCenter,
				CellConstraints.CENTER);
		addAlignmentAction(
				actions,
				constraints,
				false,
				"bottom.gif",
				ModelMessages.SelectionActionsSupport_vaBottom,
				CellConstraints.BOTTOM);
		addAlignmentAction(
				actions,
				constraints,
				false,
				"fill.gif",
				ModelMessages.SelectionActionsSupport_vaFill,
				CellConstraints.FILL);
	}

	private void addAlignmentAction(List<Object> actions,
			List<CellConstraintsSupport> constraints,
			boolean horizontal,
			String iconPath,
			String tooltip,
			CellConstraints.Alignment alignment) {
		boolean isChecked = true;
		// prepare select current value
		for (CellConstraintsSupport constraint : constraints) {
			if (horizontal) {
				if (constraint.alignH != alignment) {
					isChecked = false;
					break;
				}
			} else {
				if (constraint.alignV != alignment) {
					isChecked = false;
					break;
				}
			}
		}
		// create action
		AlignmentAction action =
				new AlignmentAction(constraints, horizontal, iconPath, tooltip, isChecked, alignment);
		actions.add(action);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Actions
	//
	////////////////////////////////////////////////////////////////////////////
	private abstract class AbstractAction extends ObjectInfoAction {
		private final List<CellConstraintsSupport> m_constraints;
		private final boolean m_horizontal;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AbstractAction(int style,
				List<CellConstraintsSupport> constraints,
				boolean horizontal,
				String iconPath,
				String tooltip,
				boolean checked) {
			super(m_layout, "", style);
			m_constraints = constraints;
			m_horizontal = horizontal;
			String path = "alignment/" + (m_horizontal ? "h" : "v") + "/menu/" + iconPath;
			setImageDescriptor(Activator.getImageDescriptor(path));
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
			for (CellConstraintsSupport constraint : m_constraints) {
				if (m_horizontal) {
					handleHorizontal(constraint);
				} else {
					handleVertical(constraint);
				}
				constraint.write();
			}
		}

		protected abstract void handleHorizontal(CellConstraintsSupport constraint) throws Exception;

		protected abstract void handleVertical(CellConstraintsSupport constraint) throws Exception;
	}
	private final class AlignmentAction extends AbstractAction {
		private final CellConstraints.Alignment m_alignment;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AlignmentAction(List<CellConstraintsSupport> constraints,
				boolean horizontal,
				String iconPath,
				String tooltip,
				boolean checked,
				CellConstraints.Alignment alignment) {
			super(AS_RADIO_BUTTON, constraints, horizontal, iconPath, tooltip, checked);
			m_alignment = alignment;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// AbstractAction
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void handleHorizontal(CellConstraintsSupport constraint) throws Exception {
			constraint.alignH = m_alignment;
		}

		@Override
		protected void handleVertical(CellConstraintsSupport constraint) throws Exception {
			constraint.alignV = m_alignment;
		}
	}
}