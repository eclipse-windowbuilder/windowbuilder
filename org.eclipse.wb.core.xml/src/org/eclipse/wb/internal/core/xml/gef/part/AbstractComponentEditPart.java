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
package org.eclipse.wb.internal.core.xml.gef.part;

import org.eclipse.wb.core.gef.command.CompoundEditCommand;
import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.xml.gef.policy.OpenListenerEditPolicy;
import org.eclipse.wb.internal.core.xml.gef.policy.TopSelectionEditPolicy;
import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.draw2d.FigureCanvas;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import java.util.Collections;
import java.util.List;

/**
 * {@link GraphicalEditPart} for {@link AbstractComponentInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.gef
 */
public class AbstractComponentEditPart extends GraphicalEditPart {
	public static final Point TOP_LOCATION = EnvironmentUtils.IS_MAC
			? new Point(20, 28)
					: new Point(20, 20);
	private final AbstractComponentInfo m_component;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractComponentEditPart(AbstractComponentInfo component) {
		m_component = component;
		setModel(m_component);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link AbstractComponentInfo} for this {@link AbstractComponentEditPart}.
	 */
	public final AbstractComponentInfo getComponent() {
		return m_component;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void activate() {
		refreshVisualsOnModelRefresh();
		super.activate();
	}

	private void refreshVisualsOnModelRefresh() {
		if (!m_component.isRoot()) {
			return;
		}
		m_component.addBroadcastListener(new ObjectEventListener() {
			@Override
			public void refreshDispose() throws Exception {
				if (isActive()) {
					getFigureCanvas().setDrawCached(true);
				}
			}

			@Override
			public void refreshed() throws Exception {
				getFigureCanvas().setDrawCached(false);
				getFigureCanvas().redraw();
				refresh();
			}

			private FigureCanvas getFigureCanvas() {
				return (FigureCanvas) getViewer().getControl();
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Figure createFigure() {
		return new Figure() {
			@Override
			protected void paintClientArea(Graphics graphics) {
				if (m_component.isRoot()) {
					Image image = m_component.getImage();
					graphics.drawImage(image, 0, 0);
				}
				drawCustomBorder(this, graphics);
			}
		};
	}

	/**
	 * Draw custom "control specific" graphics objects for given {@link Figure}.
	 */
	protected void drawCustomBorder(Figure figure, Graphics graphics) {
		if (shouldDrawDotsBorder()) {
			graphics.setForegroundColor(IColorConstants.gray);
			graphics.setLineStyle(SWT.LINE_DOT);
			Rectangle area = figure.getClientArea();
			graphics.drawRectangle(0, 0, area.width - 1, area.height - 1);
		}
	}

	private boolean shouldDrawDotsBorder() {
		return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
			@Override
			public Boolean runObject() throws Exception {
				return m_component.shouldDrawDotsBorder();
			}
		}, false);
	}

	@Override
	protected void refreshVisuals() {
		Rectangle bounds = m_component.getBounds();
		// prevent NPE if bounds for some component were not fetched
		if (bounds == null) {
			bounds = new Rectangle();
		}
		// root
		if (m_component.isRoot()) {
			Point rootLocation = getRootLocation();
			bounds = bounds.getCopy().setLocation(rootLocation);
		}
		// apply bounds
		getFigure().setBounds(bounds);
	}

	/**
	 * @return the location to use, if this {@link AbstractComponentInfo} is root.
	 */
	protected Point getRootLocation() {
		return TOP_LOCATION;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		if (m_component.isRoot()) {
			installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new TopSelectionEditPolicy(m_component));
		} else {
			installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new NonResizableSelectionEditPolicy());
		}
		//
		installEditPolicy(new OpenListenerEditPolicy(m_component));
		/*OpenErrorLog_EditPolicy.install(this);
    refreshEditPolicies();*/
	}

	/**
	 * Installs {@link EditPolicy}'s after model refresh. For example we should install new
	 * {@link LayoutEditPolicy} if component has now new layout.
	 */
	protected void refreshEditPolicies() {
		//OpenErrorLog_EditPolicy.refresh(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requests/Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public CompoundCommand createCompoundCommand() {
		return new CompoundEditCommand(m_component);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void refresh() {
		refreshEditPolicies();
		super.refresh();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<?> getModelChildren() {
		return ExecutionUtils.runObjectLog(new RunnableObjectEx<List<?>>() {
			@Override
			public List<?> runObject() throws Exception {
				return m_component.getPresentation().getChildrenGraphical();
			}
		}, Collections.emptyList());
	}
}