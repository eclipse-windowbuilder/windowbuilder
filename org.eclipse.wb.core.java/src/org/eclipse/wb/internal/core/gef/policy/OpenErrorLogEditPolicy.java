/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.core.gef.policy;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.actions.errors.ErrorsAction;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

/**
 * This {@link EditPolicy} check that if creation of {@link JavaInfo} object was failed and it was
 * replaced with placeholder, then we should display special {@link Figure} to open error log.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class OpenErrorLogEditPolicy extends EditPolicy {
	////////////////////////////////////////////////////////////////////////////
	//
	// Install and access
	//
	////////////////////////////////////////////////////////////////////////////
	private static final Object KEY = OpenErrorLogEditPolicy.class;

	/**
	 * Installs {@link OpenErrorLogEditPolicy} for given {@link AbstractComponentEditPart}.
	 */
	public static void install(AbstractComponentEditPart editPart) {
		OpenErrorLogEditPolicy policy = new OpenErrorLogEditPolicy(editPart);
		editPart.installEditPolicy(KEY, policy);
	}

	/**
	 * Notifies that {@link AbstractComponentEditPart} was refresh, so {@link OpenErrorLogEditPolicy}
	 * should be refreshed too.
	 */
	public static void refresh(AbstractComponentEditPart editPart) {
		OpenErrorLogEditPolicy policy = (OpenErrorLogEditPolicy) editPart.getEditPolicy(KEY);
		if (policy != null) {
			policy.refresh();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private final AbstractComponentEditPart m_editPart;
	private final JavaInfo m_javaInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public OpenErrorLogEditPolicy(AbstractComponentEditPart editPart) {
		m_editPart = editPart;
		m_javaInfo = m_editPart.getComponent();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	private Figure m_figure;

	/**
	 * If placeholder used - show error log figure. If not - hide it.
	 */
	private void refresh() {
		if (m_javaInfo.isPlaceholder()) {
			if (m_figure == null) {
				createFigure();
			}
			if (m_figure.getParent() == null) {
				m_editPart.getFigure().add(m_figure);
			}
		} else {
			FigureUtils.removeFigure(m_figure);
		}
	}

	private void createFigure() {
		Locator locator = new Locator() {
			@Override
			public void relocate(IFigure target) {
				Figure componentFigure = m_editPart.getFigure();
				Rectangle componentArea = componentFigure.getClientArea();
				target.setBounds(new Rectangle(5, componentArea.bottom() - 5 - 16, 16, 16));
			}
		};
		m_figure = new Handle(m_editPart, locator) {
			@Override
			protected void paintClientArea(Graphics graphics) {
				Image icon = DesignerPlugin.getImage("actions/errors/errors.gif");
				graphics.drawImage(icon, 0, 0);
			}
		};
		// open "log" on click
		m_figure.addMouseListener(new MouseListener.Stub() {
			@Override
			public void mousePressed(MouseEvent event) {
				scheduleOpenErrorLog();
			}
		});
	}

	/**
	 * Schedules showing error log later. We do this to allow normal "click" processing first.
	 */
	private void scheduleOpenErrorLog() {
		ExecutionUtils.runAsync(new RunnableEx() {
			@Override
			public void run() throws Exception {
				openErrorLog();
			}
		});
	}

	/**
	 * Shows errors log.
	 */
	private void openErrorLog() {
		ErrorsAction errorsAction = new ErrorsAction();
		errorsAction.setRoot(m_javaInfo.getRoot());
		errorsAction.run();
	}
}
