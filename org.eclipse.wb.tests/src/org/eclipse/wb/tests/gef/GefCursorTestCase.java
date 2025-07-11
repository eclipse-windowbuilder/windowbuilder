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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.graphical.GraphicalViewer;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Shell;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lobas_av
 *
 */
public abstract class GefCursorTestCase extends GefTestCase {
	protected Shell m_shell;
	protected GraphicalViewer m_viewer;
	protected EditDomain m_domain;
	protected CursorLogger m_actualLogger;
	protected EventSender m_sender;
	protected IDefaultToolProvider m_defaultToolProvider;

	////////////////////////////////////////////////////////////////////////////
	//
	// SetUp
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		// create shell
		m_shell = new Shell();
		// create actual logger and viewer
		m_actualLogger = new CursorLogger();
		m_viewer = new TestGraphicalViewer(m_shell, m_actualLogger);
		m_viewer.getControl().setSize(500, 400);
		// set edit domain
		m_domain = new EditDomain() {
			@Override
			public Tool getDefaultTool() {
				if (GefCursorTestCase.this == null || m_defaultToolProvider == null) {
					return null;
				}
				return m_defaultToolProvider.getDefaultTool();
			}
		};
		m_viewer.setEditDomain(m_domain);
		// create sender
		m_sender = new EventSender(m_viewer.getControl());
	}

	@AfterEach
	public void tearDown() throws Exception {
		m_shell.dispose();
		m_shell = null;
		m_viewer = null;
		m_actualLogger = null;
		m_sender = null;
		m_defaultToolProvider = null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EditDomain support
	//
	////////////////////////////////////////////////////////////////////////////
	protected static interface IDefaultToolProvider {
		Tool getDefaultTool();
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Test GraphicalViewer implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class TestGraphicalViewer extends GraphicalViewer {
		protected TestGraphicalViewer(Shell parent, final CursorLogger actualLogger) {
			super(new FigureCanvas(parent, SWT.H_SCROLL | SWT.V_SCROLL) {
				@Override
				protected void setDefaultEventManager() {
				}

				@Override
				public void setCursor(Cursor cursor) {
					actualLogger.setCursor(cursor);
					super.setCursor(cursor);
				}
			});
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	protected static final GraphicalEditPart createEditPart(EditPart parentEditPart,
			int x,
			int y,
			int width,
			int height,
			final Command acceptCreateCommand,
			final Command acceptResizeCommand) throws Exception {
		GraphicalEditPart editPart = new GraphicalEditPart() {
			@Override
			protected void createEditPolicies() {
				installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutEditPolicy() {
					@Override
					protected Command getCreateCommand(CreateRequest request) {
						return acceptCreateCommand;
					}
				});
				installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new SelectionEditPolicy() {
					@Override
					protected List<Handle> createSelectionHandles() {
						List<Handle> handles = new ArrayList<>();
						handles.add(new MoveHandle(getHost()));
						handles.add(createResizeHandle(PositionConstants.EAST));
						handles.add(createResizeHandle(PositionConstants.SOUTH_EAST));
						handles.add(createResizeHandle(PositionConstants.SOUTH));
						handles.add(createResizeHandle(PositionConstants.SOUTH_WEST));
						handles.add(createResizeHandle(PositionConstants.WEST));
						handles.add(createResizeHandle(PositionConstants.NORTH_WEST));
						handles.add(createResizeHandle(PositionConstants.NORTH));
						handles.add(createResizeHandle(PositionConstants.NORTH_EAST));
						return handles;
					}

					private Handle createResizeHandle(int direction) {
						ResizeHandle handle = new ResizeHandle(getHost(), direction);
						handle.setDragTracker(new ResizeTracker(direction, "REQ_RESIZE"));
						return handle;
					}

					@Override
					public boolean understandsRequest(Request request) {
						return super.understandsRequest(request) || "REQ_RESIZE".equals(request.getType());
					}

					@Override
					public org.eclipse.wb.gef.core.EditPart getTargetEditPart(org.eclipse.gef.Request request) {
						if (understandsRequest(request)) {
							return getHost();
						}
						return super.getTargetEditPart(request);
					}

					@Override
					public Command getCommand(Request request) {
						if (understandsRequest(request)) {
							return acceptResizeCommand;
						}
						return null;
					}
				});
			}

			@Override
			protected Figure createFigure() {
				return new Figure();
			}
		};
		editPart.getFigure().setBounds(new Rectangle(x, y, width, height));
		addChildEditPart(parentEditPart, editPart);
		return editPart;
	}
}