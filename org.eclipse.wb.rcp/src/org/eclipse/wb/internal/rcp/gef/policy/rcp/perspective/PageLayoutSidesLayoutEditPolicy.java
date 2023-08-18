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
package org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.position.AbstractPositionLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.GefMessages;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.AbstractPartInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.FolderViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.IPageLayoutTopLevelInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutAddViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutCreateFolderInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.IPageLayout;

/**
 * Implementation of {@link LayoutEditPolicy} for adding new {@link PageLayoutAddViewInfo} relative
 * to some {@link AbstractPartInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class PageLayoutSidesLayoutEditPolicy extends AbstractPositionLayoutEditPolicy {
	private static final int FOLDER_RELATIONSHIP = -1;
	private static final ILayoutRequestValidator VALIDATOR = LayoutRequestValidators.or(
			LayoutRequestValidators.modelType(AbstractPartInfo.class),
			LayoutRequestValidators.modelType(FolderViewInfo.class));
	private final PageLayoutInfo m_page;
	private final IPageLayoutTopLevelInfo m_part;
	private final boolean m_withOnFolder;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PageLayoutSidesLayoutEditPolicy(PageLayoutInfo page,
			IPageLayoutTopLevelInfo part,
			boolean withOnFolder) {
		m_page = page;
		m_part = part;
		m_withOnFolder = withOnFolder;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requests
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isRequestCondition(Request request) {
		return super.isRequestCondition(request) || request instanceof ViewDropRequest;
	}

	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return VALIDATOR;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedbacks
	//
	////////////////////////////////////////////////////////////////////////////
	private static final int SIZE = 10;

	@Override
	protected void addFeedbacks() throws Exception {
		Rectangle clientArea = getHostFigure().getClientArea();
		// prepare constants
		int innerWidth = clientArea.width - 2 * (SIZE + 1);
		int innerHeight = clientArea.height - 2 * (SIZE + 1);
		int rightX = clientArea.right() - SIZE;
		int bottomY = clientArea.bottom() - SIZE;
		// add feedbacks
		addFeedback(
				new Rectangle(0, 0, clientArea.width, SIZE),
				GefMessages.PageLayoutSidesLayoutEditPolicy_sideTop,
				IPageLayout.TOP);
		addFeedback(
				new Rectangle(0, bottomY, clientArea.width, SIZE),
				GefMessages.PageLayoutSidesLayoutEditPolicy_sideBottom,
				IPageLayout.BOTTOM);
		addFeedback(
				new Rectangle(0, SIZE + 1, SIZE, innerHeight),
				GefMessages.PageLayoutSidesLayoutEditPolicy_sideLeft,
				IPageLayout.LEFT);
		addFeedback(
				new Rectangle(rightX, SIZE + 1, SIZE, innerHeight),
				GefMessages.PageLayoutSidesLayoutEditPolicy_sideRight,
				IPageLayout.RIGHT);
		if (m_withOnFolder) {
			addFeedback(
					new Rectangle(SIZE + 1, SIZE + 1, innerWidth, innerHeight),
					GefMessages.PageLayoutSidesLayoutEditPolicy_sameFolder,
					FOLDER_RELATIONSHIP);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getCommand(Request request, Object data) {
		if (request instanceof final ViewDropRequest viewDrop_Request) {
			final ViewInfo viewInfo = viewDrop_Request.getView();
			final int relationship = (Integer) data;
			return new EditCommand(m_page) {
				@Override
				protected void executeEdit() throws Exception {
					Object newView;
					if (relationship == FOLDER_RELATIONSHIP) {
						PageLayoutCreateFolderInfo folder = convertIntoFolder();
						newView = folder.command_CREATE(viewInfo.getId(), null);
					} else {
						newView = m_page.command_CREATE(viewInfo.getId(), relationship, 0.5f, m_part);
					}
					viewDrop_Request.setComponent(newView);
				}
			};
		}
		return super.getCommand(request, data);
	}

	@Override
	protected Command getCreateCommand(Object newObject, Object data) {
		return null;
	}

	@Override
	protected Command getPasteCommand(PasteRequest request, Object data) {
		return null;
	}

	@Override
	protected Command getMoveCommand(Object moveObject, Object data) {
		return null;
	}

	@Override
	protected Command getAddCommand(Object addObject, Object data) {
		final int relationship = (Integer) data;
		if (addObject instanceof final AbstractPartInfo item) {
			return new EditCommand(m_page) {
				@Override
				protected void executeEdit() throws Exception {
					if (relationship != FOLDER_RELATIONSHIP) {
						m_page.command_MOVE(item, relationship, 0.5f, m_part);
					} else if (item instanceof PageLayoutAddViewInfo view) {
						PageLayoutCreateFolderInfo folder = convertIntoFolder();
						// move "view" on new "folder" and select
						FolderViewInfo newView = folder.command_MOVE(view, null);
						PolicyUtils.scheduleSelection(getHost(), newView);
					}
				}
			};
		}
		if (addObject instanceof final FolderViewInfo item) {
			return new EditCommand(m_page) {
				@Override
				protected void executeEdit() throws Exception {
					if (relationship != FOLDER_RELATIONSHIP) {
						AbstractPartInfo newView = m_page.command_MOVE(item, relationship, 0.5f, m_part);
						PolicyUtils.scheduleSelection(getHost(), newView);
					} else {
						PageLayoutCreateFolderInfo folder = convertIntoFolder();
						FolderViewInfo newView = folder.command_MOVE(item, null);
						PolicyUtils.scheduleSelection(getHost(), newView);
					}
				}
			};
		}
		//
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Converts this {@link PageLayoutAddViewInfo} into {@link PageLayoutCreateFolderInfo}.
	 */
	private PageLayoutCreateFolderInfo convertIntoFolder() throws Exception {
		return m_page.convertIntoFolder((PageLayoutAddViewInfo) m_part);
	}
}
