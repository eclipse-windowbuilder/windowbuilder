/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.property.table.editparts;

import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.IPropertyTooltipSite;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable.PropertyInfo;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.MouseWheelListener;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.SeparatorBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import java.beans.PropertyChangeListener;

public final class PropertyEditPart extends AbstractPropertyEditPart {
	private final ISelectionChangedListener listener;
	private final PropertyChangeListener propListener;
	private TitleFigure titleFigure;
	private ValueFigure valueFigure;

	public PropertyEditPart(PropertyInfo propertyInfo) {
		setModel(propertyInfo);
		listener = event -> refreshVisuals();
		propListener = event -> {
			if (PropertyTable.PROP_SPLITTER.equals(event.getPropertyName())) {
				int width = (int) event.getNewValue();
				int height = getViewer().getRowHeight();
				titleFigure.setPreferredSize(width, height);
			}
		};
	}

	@Override
	public void addNotify() {
		super.addNotify();
		getViewer().addSelectionChangedListener(listener);
		getViewer().addPropertyChangeListener(propListener);
	}

	@Override
	public void removeNotify() {
		getViewer().removePropertyChangeListener(propListener);
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

	private MouseMotionListener getMouseMotionListener(PropertyFigure figureUnderCursor) {
		return new MouseMotionListener.Stub() {
			@Override
			public void mouseHover(MouseEvent me) {
				Point location = Point.SINGLETON;
				location.setX(me.x);
				location.setY(me.y);

				figureUnderCursor.translateToAbsolute(location);
				getViewer().getTooltipHelper().displayToolTipNear(figureUnderCursor, location.x, location.y);
			}
		};
	}

	private MouseWheelListener getMouseWheelListener() {
		return event -> getViewer().getTooltipHelper().hideTooltip();
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
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		figure = new Figure();
		figure.setLayoutManager(gridLayout);
		figure.setBorder(border);
		//
		titleFigure = new TitleFigure();
		titleFigure.setPreferredSize(new Dimension(getViewer().getSplitter(), getViewer().getRowHeight()));
		titleFigure.setOpaque(true);
		titleFigure.addMouseMotionListener(getMouseMotionListener(titleFigure));
		titleFigure.addMouseWheelListener(getMouseWheelListener());
		figure.add(titleFigure, new GridData(SWT.FILL, SWT.FILL, false, false));

		valueFigure = new ValueFigure();
		valueFigure.setPreferredSize(new Dimension(SWT.DEFAULT, getViewer().getRowHeight()));
		valueFigure.setOpaque(true);
		valueFigure.addMouseMotionListener(getMouseMotionListener(valueFigure));
		valueFigure.addMouseWheelListener(getMouseWheelListener());
		figure.add(valueFigure, new GridData(SWT.FILL, SWT.FILL, true, false));
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
		if (isActiveProperty()) {
			titleFigure.setBackgroundColor(COLOR_PROPERTY_BG_SELECTED);
			valueFigure.setBackgroundColor(COLOR_PROPERTY_BG_SELECTED);
		} else {
			Property property = getModel().getProperty();
			try {
				if (property.isModified()) {
					titleFigure.setBackgroundColor(COLOR_PROPERTY_BG_MODIFIED);
					valueFigure.setBackgroundColor(COLOR_PROPERTY_BG_MODIFIED);
				} else {
					titleFigure.setBackgroundColor(COLOR_PROPERTY_BG);
					valueFigure.setBackgroundColor(COLOR_PROPERTY_BG);
				}
			} catch (Exception e) {
				DesignerPlugin.log(e);
			}
		}
	}

	private boolean isActiveProperty() {
		return getSelected() == EditPart.SELECTED_PRIMARY;
	}

	public abstract sealed class PropertyFigure extends Figure permits TitleFigure, ValueFigure {
		public Property getProperty() {
			return PropertyEditPart.this.getProperty();
		}

		public PropertyTooltipProvider getPropertyTooltipProvider() {
			IAdaptable adaptable = getPropertyToolTipAdaptable();
			if (adaptable == null) {
				return null;
			}
			PropertyTooltipProvider provider = adaptable.getAdapter(PropertyTooltipProvider.class);
			if (provider != null) {
				return provider;
			}
			return getBasicTooltipProvider();
		}

		protected abstract IAdaptable getPropertyToolTipAdaptable();

		/**
		 * Returns a human-readable representation of this property. May be {@code
		 * null}.
		 */
		protected abstract String getText();

		@Override
		public String toString() {
			return "[%s] %s".formatted(getClass().getSimpleName(), getProperty().getTitle());
		}

		/**
		 * Returns the tool-tip provider for the given property figure if no other
		 * provider is specific. This method return a provider if and only if the figure
		 * has a string representation and if this representation doesn't fit into the
		 * column.<br>
		 * The tool-tip is closed when clicked on.
		 */
		private PropertyTooltipProvider getBasicTooltipProvider() {
			String text = getText();
			if (text == null) {
				return null;
			}
			Dimension size = FigureUtils.calculateTextSize(text, getFont());
			if (getSize().width >= size.width) {
				return null;
			}
			return new PropertyTooltipProvider() {
				@Override
				public Control createTooltipControl(Property property, Composite parent, IPropertyTooltipSite site) {
					Label label = new Label(parent, SWT.NONE);
					label.setText(text);
					label.addListener(SWT.MouseDown, new HideListener(site));
					return label;
				}
			};
		}
	}

	public final class TitleFigure extends PropertyFigure {
		@Override
		protected void paintClientArea(Graphics graphics) {
			int height = bounds.height();
			int y = bounds.y();
			// draw property
			try {
				Property property = getProperty();
				// draw state image
				if (getModel().isShowComplex()) {
					Image stateImage = getModel().isExpanded() ? m_minusImage : m_plusImage;
					DrawUtils.drawImageCV(graphics, stateImage, getTitleX(), y, height);
				}
				// draw title
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
				// paint title
				int x = getTitleTextX();
				int width = getViewer().getSplitter() - x;
				DrawUtils.drawStringCV(graphics, property.getTitle(), x, y, width, height);
			} catch (Throwable e) {
				DesignerPlugin.log(e);
			}
		}

		@Override
		protected IAdaptable getPropertyToolTipAdaptable() {
			return getProperty();
		}

		@Override
		protected String getText() {
			return getProperty().getTitle();
		}
	}

	public final class ValueFigure extends PropertyFigure {
		@Override
		protected void paintClientArea(Graphics graphics) {
			int height = bounds.height();
			int y = bounds.y();
			// draw property
			try {
				Property property = getProperty();
				// configure GC
				graphics.setFont(getFont());
				if (!isActiveProperty()) {
					graphics.setForegroundColor(COLOR_PROPERTY_FG_VALUE);
				}
				// prepare value rectangle
				int x = bounds.x() + 4;
				int width = bounds.width() - MARGIN_RIGHT;
				// paint value
				property.getEditor().paint(property, graphics, x, y, width, height);
			} catch (Throwable e) {
				DesignerPlugin.log(e);
			}
		}

		@Override
		protected IAdaptable getPropertyToolTipAdaptable() {
			return getProperty().getEditor();
		}

		@Override
		protected String getText() {
			Object value = ExecutionUtils.runObjectIgnore(() -> getProperty().getValue(), null);
			if (value instanceof String text) {
				return text;
			}
			return null;
		}
	}
}