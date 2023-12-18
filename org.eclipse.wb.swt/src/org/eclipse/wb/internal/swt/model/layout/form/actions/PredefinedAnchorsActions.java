/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.layout.form.actions;

import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.layout.form.IFormLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;

import java.util.Arrays;
import java.util.List;

/**
 * Adds various pre-defined anchors to controls. Ex., 'top-left', 'top-bottom-right', etc.
 *
 * @author mitin_aa
 * @coverage swt.model.layout.form
 */
public class PredefinedAnchorsActions<C extends IControlInfo> {
	private static final String IMAGE_PREFIX = "info/layout/FormLayout/assistant/";
	private final IFormLayoutInfo<C> m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PredefinedAnchorsActions(IFormLayoutInfo<C> layout) {
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Fill
	//
	////////////////////////////////////////////////////////////////////////////
	public void contributeActions(C widget, IContributionManager manager) {
		List<C> selection = Arrays.asList(widget);
		contributeActions(selection, manager);
	}

	public void contributeActions(List<C> selection, IContributionManager manager) {
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_topLeft,
				"top_left.gif",
				PositionConstants.LEFT | PositionConstants.TOP));
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_topRight,
				"top_right.gif",
				PositionConstants.TOP | PositionConstants.RIGHT));
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_bottomLeft,
				"bottom_left.gif",
				PositionConstants.LEFT | PositionConstants.BOTTOM));
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_bottomRight,
				"bottom_right.gif",
				PositionConstants.BOTTOM | PositionConstants.RIGHT));
		manager.add(new Separator());
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_leftRightTop,
				"top_left_right.gif",
				PositionConstants.LEFT | PositionConstants.TOP | PositionConstants.RIGHT));
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_leftRightBottom,
				"bottom_left_right.gif",
				PositionConstants.LEFT | PositionConstants.BOTTOM | PositionConstants.RIGHT));
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_topBottomLeft,
				"top_bottom_left.gif",
				PositionConstants.TOP | PositionConstants.LEFT | PositionConstants.BOTTOM));
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_topBottomRight,
				"top_bottom_right.gif",
				PositionConstants.BOTTOM | PositionConstants.RIGHT | PositionConstants.TOP));
		manager.add(new Separator());
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_TopBottomLeftRight,
				"top_bottom_left_right.gif",
				PositionConstants.BOTTOM
				| PositionConstants.RIGHT
				| PositionConstants.TOP
				| PositionConstants.LEFT));
		manager.add(new Separator());
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_topLeftRelative,
				"top_left_relative.gif",
				PositionConstants.TOP | PositionConstants.LEFT,
				true));
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_topRightRelative,
				"top_right_relative.gif",
				PositionConstants.TOP | PositionConstants.RIGHT,
				true));
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_bottomLeftRelative,
				"bottom_left_relative.gif",
				PositionConstants.LEFT | PositionConstants.BOTTOM,
				true));
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_bottomRightRelative,
				"bottom_right_relative.gif",
				PositionConstants.BOTTOM | PositionConstants.RIGHT,
				true));
		manager.add(new Separator());
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_topLeftRightRelative,
				"top_left_right_relative.gif",
				PositionConstants.TOP | PositionConstants.LEFT | PositionConstants.RIGHT,
				true));
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_bottomLeftRightRelative,
				"bottom_left_right_relative.gif",
				PositionConstants.BOTTOM | PositionConstants.LEFT | PositionConstants.RIGHT,
				true));
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_topBottomRightRelative,
				"top_bottom_right_relative.gif",
				PositionConstants.BOTTOM | PositionConstants.TOP | PositionConstants.RIGHT,
				true));
		manager.add(new SetCornerAnchorsAction<>(selection,
				ModelMessages.PredefinedAnchorsActions_topBottomLeftRelative,
				"top_bottom_left_relative.gif",
				PositionConstants.BOTTOM | PositionConstants.TOP | PositionConstants.LEFT,
				true));
	}

	private final class SetCornerAnchorsAction<C1 extends IControlInfo> extends ObjectInfoAction {
		private final int m_alignment;
		private final List<C> m_widgets;
		private final boolean m_relative;

		private SetCornerAnchorsAction(List<C> selection, String text, String imageName, int alignment) {
			this(selection, text, imageName, alignment, false);
		}

		private SetCornerAnchorsAction(List<C> selection,
				String text,
				String imageName,
				int alignment,
				boolean relative) {
			super(m_layout.getUnderlyingModel(), text, Activator.getImageDescriptor(IMAGE_PREFIX + imageName));
			m_widgets = selection;
			m_alignment = alignment;
			m_relative = relative;
		}

		@Override
		protected void runEx() throws Exception {
			for (C widget : m_widgets) {
				m_layout.setQuickAnchors(widget, m_alignment, m_relative);
			}
		}
	}
}
