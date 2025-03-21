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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable.PropertyInfo;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.SeparatorBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public final class PropertyEditPart extends AbstractPropertyEditPart {
	private final ISelectionChangedListener listener;
	private PropertyFigure propertyFigure;

	public PropertyEditPart(PropertyInfo propertyInfo) {
		setModel(propertyInfo);
		listener = event -> refreshVisuals();
	}

	@Override
	public void addNotify() {
		super.addNotify();
		getViewer().addSelectionChangedListener(listener);
	}

	@Override
	public void removeNotify() {
		getViewer().removeSelectionChangedListener(listener);
		super.removeNotify();
	}

	/**
	 * @return the <code>X</code> position for first pixel of {@link PropertyInfo}
	 *         title (location of state image).
	 */
	public int getTitleX() {
		return MARGIN_LEFT + getLevelIndent() * getModel().getLevel();
	}

	/**
	 * @return the <code>X</code> position for first pixel of {@link PropertyInfo}
	 *         title text.
	 */
	public int getTitleTextX() {
		return getTitleX() + getLevelIndent();
	}

	/**
	 * @return the indentation for single level.
	 */
	private int getLevelIndent() {
		return m_stateWidth + STATE_IMAGE_MARGIN_RIGHT;
	}

	/**
	 * @return <code>true</code> if given <code>x</code> coordinate is on state
	 *         (plus/minus) image.
	 */
	public boolean isLocationState(int x) {
		int levelX = getTitleX();
		return getModel().isComplex() && levelX <= x && x <= levelX + m_stateWidth;
	}

	private Font getBoldFont(Font font) {
		return getViewer().getResourceManager().create(FontDescriptor.createFrom(font).withStyle(SWT.BOLD));
	}

	private Font getItalicFont(Font font) {
		return getViewer().getResourceManager().create(FontDescriptor.createFrom(font).withStyle(SWT.ITALIC));
	}

	/**
	 * Convenience method to avoid accessing the internal {@link PropertyInfo}
	 * class.
	 *
	 * @return The {@link Property} of this edit part's model.
	 */
	public Property getProperty() {
		return getModel().getProperty();
	}

	@Override
	public PropertyInfo getModel() {
		return (PropertyInfo) super.getModel();
	}

	@Override
	protected IFigure createFigure() {
		SeparatorBorder border = new SeparatorBorder(new Insets(0, 0, MARGIN_BOTTOM, 1), PositionConstants.BOTTOM);
		border.setColor(COLOR_LINE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		figure = new Figure();
		figure.setLayoutManager(gridLayout);
		figure.setBorder(border);
		//
		propertyFigure = new PropertyFigure();
		propertyFigure.setPreferredSize(new Dimension(SWT.DEFAULT, getViewer().getRowHeight()));
		propertyFigure.setOpaque(true);
		figure.add(propertyFigure, new GridData(SWT.FILL, SWT.FILL, true, false));
		//
		PropertyEditorPresentation presentation = getModel().getProperty().getEditor().getPresentation();
		if (presentation != null) {
			gridLayout.numColumns++;
			figure.add(presentation.getFigure(getViewer(), getModel().getProperty()));
		}
		return figure;
	}

	@Override
	protected void refreshVisuals() {
		if (propertyFigure.isActiveProperty()) {
			propertyFigure.setBackgroundColor(COLOR_PROPERTY_BG_SELECTED);
		} else {
			Property property = getModel().getProperty();
			try {
				if (property.isModified()) {
					propertyFigure.setBackgroundColor(COLOR_PROPERTY_BG_MODIFIED);
				} else {
					propertyFigure.setBackgroundColor(COLOR_PROPERTY_BG);
				}
			} catch (Exception e) {
				DesignerPlugin.log(e);
			}
		}
	}

	private final class PropertyFigure extends Figure {

		@Override
		protected void paintClientArea(Graphics graphics) {
			int height = bounds.height();
			int y = bounds.y();
			// draw property
			try {
				Property property = getModel().getProperty();
				// draw state image
				if (getModel().isShowComplex()) {
					Image stateImage = getModel().isExpanded() ? m_minusImage : m_plusImage;
					DrawUtils.drawImageCV(graphics, stateImage, getTitleX(), y, height);
				}
				// draw title
				{
					// configure GC
					{
						graphics.setForegroundColor(COLOR_PROPERTY_FG_TITLE);
						// check category
						if (getViewer().getCategory(property).isAdvanced()) {
							graphics.setForegroundColor(COLOR_PROPERTY_FG_ADVANCED);
							graphics.setFont(getItalicFont(getFont()));
						} else if (getViewer().getCategory(property).isPreferred() || getViewer().getCategory(property).isSystem()) {
							graphics.setFont(getBoldFont(getFont()));
						}
						// check for active
						if (isActiveProperty()) {
							graphics.setForegroundColor(COLOR_PROPERTY_FG_SELECTED);
						}
					}
					// paint title
					int x = getTitleTextX();
					DrawUtils.drawStringCV(graphics, property.getTitle(), x, y, getViewer().getSplitter() - x, height);
				}
				// draw value
				{
					// configure GC
					graphics.setFont(getFont());
					if (!isActiveProperty()) {
						graphics.setForegroundColor(COLOR_PROPERTY_FG_VALUE);
					}
					// prepare value rectangle
					int x = getViewer().getSplitter() + 4;
					int w = getViewer().getControl().getClientArea().width - x - MARGIN_RIGHT;
					// paint value
					property.getEditor().paint(property, graphics, x, y, w, height);
				}
			} catch (Throwable e) {
				DesignerPlugin.log(e);
			}
		}

		public boolean isActiveProperty() {
			return getSelected() == EditPart.SELECTED_PRIMARY;
		}
	}
}