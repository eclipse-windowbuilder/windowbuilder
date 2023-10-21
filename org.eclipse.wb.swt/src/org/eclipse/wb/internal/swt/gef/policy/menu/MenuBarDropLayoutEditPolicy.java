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
package org.eclipse.wb.internal.swt.gef.policy.menu;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.core.gef.part.menu.MenuEditPartFactory;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator.LayoutRequestValidatorStubFalse;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.gef.GefMessages;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.internal.swt.support.ToolkitSupport;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.FontMetrics;

/**
 * {@link LayoutEditPolicy} allowing drop "bar" {@link MenuInfo} on <code>Shell</code>.
 *
 * @author mitin_aa
 * @coverage swt.gef.policy.menu
 */
public class MenuBarDropLayoutEditPolicy extends LayoutEditPolicy {
	private final CompositeInfo m_shell;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuBarDropLayoutEditPolicy(CompositeInfo shell) {
		m_shell = shell;
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
	private Figure m_fillFeedback;

	@Override
	protected void showLayoutTargetFeedback(Request request) {
		if (m_fillFeedback == null) {
			// create figure
			m_fillFeedback = new Figure() {
				@Override
				protected void paintClientArea(Graphics graphics) {
					// draw placeholder text
					Rectangle bounds = getBounds();
					graphics.setForegroundColor(IColorConstants.darkGreen);
					String menuBarText = GefMessages.MenuBarDropLayoutEditPolicy_dropMenuHint;
					Dimension textExtent = TextUtilities.INSTANCE.getTextExtents(menuBarText, graphics.getFont());
					FontMetrics fontMetrics = graphics.getFontMetrics();
					{
						int fontHeight = fontMetrics.getAscent() - fontMetrics.getDescent();
						int x = (bounds.width - textExtent.width) / 2;
						int y = (bounds.height - textExtent.height - fontHeight) / 2;
						graphics.drawString(menuBarText, x, y);
					}
				}
			};
			m_fillFeedback.setOpaque(true);
			m_fillFeedback.setBackgroundColor(IColorConstants.menuBackground);
			// set figure bounds
			Insets clientAreaInsets = m_shell.getClientAreaInsets();
			final Rectangle bounds = getHostFigure().getBounds().getCopy();
			bounds.width -= clientAreaInsets.getWidth();
			if (EnvironmentUtils.IS_MAC) {
				bounds.x = AbstractComponentEditPart.TOP_LOCATION.x;
				bounds.y = MenuEditPartFactory.MENU_Y_LOCATION;
			} else {
				bounds.x += clientAreaInsets.left;
				bounds.y += clientAreaInsets.top;
			}
			ExecutionUtils.runIgnore(new RunnableEx() {
				@Override
				public void run() throws Exception {
					bounds.height = ToolkitSupport.getDefaultMenuBarHeight();
				}
			});
			m_fillFeedback.setBounds(bounds);
			// add some border
			m_fillFeedback.setBorder(new LineBorder(IColorConstants.menuBackgroundSelected, 1));
			addFeedback(m_fillFeedback);
		}
	}

	@Override
	protected void eraseLayoutTargetFeedback(Request request) {
		if (m_fillFeedback != null) {
			removeFeedback(m_fillFeedback);
			m_fillFeedback = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		final MenuInfo menu = (MenuInfo) request.getNewObject();
		return new EditCommand(m_shell) {
			@Override
			protected void executeEdit() throws Exception {
				menu.command_CREATE(m_shell);
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
			// only one "bar"
			for (MenuInfo menuInfo : m_shell.getChildren(MenuInfo.class)) {
				if (menuInfo.isBar()) {
					return false;
				}
			}
			// check object
			Object newObject = request.getNewObject();
			if (newObject instanceof MenuInfo) {
				return ((MenuInfo) newObject).isBar();
			}
			// unknown object
			return false;
		}
	};
}
