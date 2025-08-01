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
package org.eclipse.wb.internal.swing.gef.policy.menu;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator.LayoutRequestValidatorStubFalse;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.GefMessages;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuBarInfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * {@link LayoutEditPolicy} allowing drop {@link JMenuBarInfo} on {@link JFrame}, {@link JDialog} or
 * {@link JApplet}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class MenuBarDropLayoutEditPolicy extends LayoutEditPolicy {
	private final ContainerInfo m_container;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuBarDropLayoutEditPolicy(ContainerInfo container) {
		m_container = container;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return VALIDATOR;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedback
	//
	////////////////////////////////////////////////////////////////////////////
	private IFigure m_feedback;

	@Override
	protected void showLayoutTargetFeedback(Request request) {
		if (m_feedback == null) {
			// create figure
			m_feedback = new Figure() {
				@Override
				protected void paintClientArea(Graphics graphics) {
					// draw placeholder text
					Rectangle bounds = getBounds();
					graphics.setForegroundColor(ColorConstants.darkGreen);
					String menuBarText = GefMessages.MenuBarDropLayoutEditPolicy_feedbackText;
					Dimension textExtent = TextUtilities.INSTANCE.getTextExtents(menuBarText, graphics.getFont());
					//
					int x = bounds.width / 2 - textExtent.width / 2;
					int y = bounds.height / 2 - textExtent.height / 2;
					graphics.drawString(menuBarText, x, y);
				}
			};
			m_feedback.setOpaque(true);
			m_feedback.setBackgroundColor(ColorConstants.menuBackground);
			// set figure bounds
			Insets clientAreaInsets = m_container.getInsets();
			Rectangle bounds = getHostFigure().getBounds().getCopy();
			bounds.x += clientAreaInsets.left;
			bounds.y += clientAreaInsets.top;
			bounds.width -= clientAreaInsets.getWidth();
			bounds.height = 27;
			m_feedback.setBounds(bounds);
			// add some border
			m_feedback.setBorder(new LineBorder(ColorConstants.menuBackgroundSelected, 1));
			addFeedback(m_feedback);
		}
	}

	@Override
	protected void eraseLayoutTargetFeedback(Request request) {
		if (m_feedback != null) {
			removeFeedback(m_feedback);
			m_feedback = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		final JMenuBarInfo menu = (JMenuBarInfo) request.getNewObject();
		return new EditCommand(m_container) {
			@Override
			protected void executeEdit() throws Exception {
				menu.command_CREATE(m_container);
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validator instance
	//
	////////////////////////////////////////////////////////////////////////////
	private final ILayoutRequestValidator VALIDATOR = new LayoutRequestValidatorStubFalse() {
		@Override
		public boolean validateCreateRequest(EditPart host, CreateRequest request) {
			// only one JMenuBar_Info
			if (!m_container.getChildren(JMenuBarInfo.class).isEmpty()) {
				return false;
			}
			// check object
			return request.getNewObject() instanceof JMenuBarInfo;
		}
	};
}
