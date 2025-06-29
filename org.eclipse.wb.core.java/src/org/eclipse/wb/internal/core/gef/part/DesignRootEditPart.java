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
package org.eclipse.wb.internal.core.gef.part;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.border.Border;
import org.eclipse.wb.draw2d.border.MarginBorder;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.gef.part.nonvisual.NonVisualBeanEditPart;
import org.eclipse.wb.internal.core.gef.policy.nonvisual.NonVisualLayoutEditPolicy;
import org.eclipse.wb.internal.core.model.DesignRootObject;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanInfo;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.draw2d.IPreferredSizeProvider;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;

import java.util.List;

/**
 * {@link EditPart} for Designer root object.
 *
 * @author lobas_av
 * @coverage core.gef
 */
public final class DesignRootEditPart extends GraphicalEditPart {
	/**
	 * Counterpart to {@link AbstractComponentEditPart#TOP_LOCATION} which describes
	 * the margin at the bottom right of the design viewer. This is necessary if the
	 * widget is larger than the viewer, because then the edges of the figure touch
	 * the edges of the root figure, thus making it harder to e.g. select the resize
	 * tool.
	 */
	private static final Point BOTTOM_MARGIN = new Point(8, 8);
	private final DesignRootObject m_designRootObject;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DesignRootEditPart(DesignRootObject designRootObject) {
		m_designRootObject = designRootObject;
		setModel(m_designRootObject);
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
		m_designRootObject.getRootObject().addBroadcastListener(new ObjectEventListener() {
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
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		installEditPolicy(
				EditPolicy.LAYOUT_ROLE,
				new NonVisualLayoutEditPolicy(m_designRootObject.getRootObject()));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<?> getModelChildren() {
		return m_designRootObject.getChildren();
	}

	@Override
	protected EditPart createChild(Object model) {
		if (m_designRootObject.getRootObject() != model) {
			// direct create non visual bean part
			JavaInfo javaInfo = (JavaInfo) model;
			NonVisualBeanInfo nonVisualInfo = NonVisualBeanInfo.getNonVisualInfo(javaInfo);
			// create EditPart only if location specified
			if (nonVisualInfo != null && nonVisualInfo.getLocation() != null) {
				return new NonVisualBeanEditPart(javaInfo);
			} else {
				return null;
			}
		}
		// use factory
		return super.createChild(model);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return java root {@link EditPart}.
	 */
	public EditPart getJavaRootEditPart() {
		return getChildren().get(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Figure createFigure() {
		Figure figure = new TopFigure();
		Border border = new MarginBorder(new Insets(0, 0, BOTTOM_MARGIN.x, BOTTOM_MARGIN.y));
		figure.setBorder(border);
		return figure;
	}

	/**
	 * Special {@link Figure} that cover full area of parent.
	 */
	private static final class TopFigure extends Figure implements IPreferredSizeProvider {
		////////////////////////////////////////////////////////////////////////////
		//
		// Figure
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Rectangle getBounds() {
			IFigure parentFigure = getParent();
			if (parentFigure != null) {
				return new Rectangle(new Point(), parentFigure.getSize());
			}
			return super.getBounds();
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// IPreferredSizeProvider
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Dimension getPreferredSize(Dimension originalPreferredSize) {
			Rectangle preferred = new Rectangle();
			for (IFigure figure : getChildren()) {
				if (figure.isVisible()) {
					preferred.union(figure.getBounds());
				}
			}
			preferred.expand(getInsets());
			return preferred.getSize();
		}
	}
}