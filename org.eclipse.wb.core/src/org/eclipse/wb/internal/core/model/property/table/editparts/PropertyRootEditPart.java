/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.property.table.editparts;

import org.eclipse.wb.internal.core.model.property.table.PropertyTable.PropertyInfo;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.Collections;
import java.util.List;

public final class PropertyRootEditPart extends AbstractPropertyEditPart {
	public PropertyRootEditPart(List<PropertyInfo> model) {
		setModel(model);
	}

	@Override
	protected IFigure createFigure() {
		FlowLayout flowLayout = new FlowLayout(true);
		flowLayout.setMajorSpacing(0);

		LineBorder border = new LineBorder(COLOR_LINE) {
			@Override
			public void paint(IFigure f, Graphics g, Insets i) {
				// draw rectangle around figure
				super.paint(f, g, i);
				// draw expand line
				tempRect = getPaintRectangle(f, i);
				drawExpandLines(g, tempRect);
				// draw splitter
				tempRect = getPaintRectangle(f, i);
				g.drawLine(getViewer().getSplitter(), 0, getViewer().getSplitter(), tempRect.height);
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Painting
			//
			////////////////////////////////////////////////////////////////////////////

			/**
			 * Draws lines from expanded complex property to its last sub-property.
			 */
			private void drawExpandLines(Graphics graphics, Rectangle clientArea) {
				int height = getViewer().getRowHeight() - MARGIN_BOTTOM;
				int xOffset = m_plusImage.getBounds().width / 2;
				int yOffset = (height - m_plusImage.getBounds().width) / 2;
				List<PropertyInfo> properties = getModelChildren();
				//
				graphics.setForegroundColor(COLOR_COMPLEX_LINE);
				for (int i = 0; i < properties.size(); i++) {
					PropertyInfo propertyInfo = properties.get(i);
					//
					if (propertyInfo.isExpanded()) {
						int index = properties.indexOf(propertyInfo);
						// prepare index of last sub-property
						int index2 = index;
						for (; index2 < properties.size(); index2++) {
							PropertyInfo nextPropertyInfo = properties.get(index2);
							if (nextPropertyInfo != propertyInfo
									&& nextPropertyInfo.getLevel() <= propertyInfo.getLevel()) {
								break;
							}
						}
						index2--;
						// draw line if there are children
						if (index2 > index) {
							PropertyInfo nextPropertyInfo = properties.get(index2);
							PropertyEditPart editPart = getViewer().getEditPartForModel(propertyInfo);
							PropertyEditPart nextEditPart = getViewer().getEditPartForModel(nextPropertyInfo);
							if (editPart != null && nextEditPart != null) {
								Rectangle bounds = editPart.getFigure().getBounds();
								Rectangle nextBounds = nextEditPart.getFigure().getBounds();
								int x = editPart.getTitleX() + xOffset;
								int y1 = bounds.top() + height - yOffset;
								int y2 = nextBounds.top() + getViewer().getRowHeight() / 2;
								graphics.drawLine(x, y1, x, y2);
								graphics.drawLine(x, y2, x + getViewer().getRowHeight() / 3, y2);
							}
						}
					}
					//
				}
			}
		};
		//
		figure = new Figure();
		figure.setBorder(border);
		figure.setBackgroundColor(COLOR_BACKGROUND);
		figure.setLayoutManager(flowLayout);
		figure.setOpaque(true);
		return figure;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PropertyInfo> getModel() {
		return (List<PropertyInfo>) super.getModel();
	}

	@Override
	protected List<PropertyInfo> getModelChildren() {
		List<PropertyInfo> model = getModel();
		if (model == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(model);
	}
}