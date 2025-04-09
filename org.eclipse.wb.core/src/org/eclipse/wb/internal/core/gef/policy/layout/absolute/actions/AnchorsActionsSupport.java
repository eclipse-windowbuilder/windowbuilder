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
package org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.gef.GefMessages;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementsSupport;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.resource.ImageDescriptor;

public class AnchorsActionsSupport {
	private final PlacementsSupport m_placementsSupport;

	public AnchorsActionsSupport(PlacementsSupport placementsSupport) {
		m_placementsSupport = placementsSupport;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Actions
	//
	////////////////////////////////////////////////////////////////////////////
	public void fillAnchorsActions(IContributionManager manager,
			IAbstractComponentInfo widget,
			boolean isHorizontal) {
		if (isHorizontal) {
			manager.add(new SetAlignmentAction(widget,
					GefMessages.AnchorsActionsSupport_leftAlignment,
					CoreImages.ALIGNMENT_H_MENU_LEFT,
					PositionConstants.LEFT));
			manager.add(new SetAlignmentAction(widget,
					GefMessages.AnchorsActionsSupport_rightAlignment,
					CoreImages.ALIGNMENT_H_MENU_RIGHT,
					PositionConstants.RIGHT));
			manager.add(new MakeResizeableAction(widget,
					GefMessages.AnchorsActionsSupport_makeResizableHorizontal,
					CoreImages.ALIGNMENT_H_MENU_FILL,
					isHorizontal));
		} else {
			manager.add(new SetAlignmentAction(widget,
					GefMessages.AnchorsActionsSupport_topAlignment,
					CoreImages.ALIGNMENT_V_MENU_TOP,
					PositionConstants.TOP));
			manager.add(new SetAlignmentAction(widget,
					GefMessages.AnchorsActionsSupport_bottomAlignment,
					CoreImages.ALIGNMENT_V_MENU_BOTTOM,
					PositionConstants.BOTTOM));
			manager.add(new MakeResizeableAction(widget,
					GefMessages.AnchorsActionsSupport_makeResizableVertical,
					CoreImages.ALIGNMENT_V_MENU_FILL,
					isHorizontal));
		}
	}

	private final class SetAlignmentAction extends ObjectInfoAction {
		private final int m_alignment;
		private final IAbstractComponentInfo m_widget;

		private SetAlignmentAction(IAbstractComponentInfo widget,
				String text,
				ImageDescriptor icon,
				int alignment) {
			super(widget.getUnderlyingModel(), text, icon);
			m_widget = widget;
			m_alignment = alignment;
		}

		@Override
		protected void runEx() throws Exception {
			m_placementsSupport.setAlignment(m_widget, m_alignment);
		}
	}
	private final class MakeResizeableAction extends ObjectInfoAction {
		private final boolean m_isHorizontal;
		private final IAbstractComponentInfo m_widget;

		private MakeResizeableAction(IAbstractComponentInfo widget,
				String text,
				ImageDescriptor icon,
				boolean isHorizontal) {
			super(widget.getUnderlyingModel(), text, icon);
			m_widget = widget;
			m_isHorizontal = isHorizontal;
		}

		@Override
		protected void runEx() throws Exception {
			m_placementsSupport.setResizeable(m_widget, m_isHorizontal);
		}
	}
}
