/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.property.table;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategoryProvider;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategoryProviders;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.SeparatorBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Control that can display {@link Property}'s and edit them using
 * {@link PropertyEditor}'s.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage core.model.property.table
 */
public class PropertyTable extends ScrollingGraphicalViewer {
	////////////////////////////////////////////////////////////////////////////
	//
	// Colors
	//
	////////////////////////////////////////////////////////////////////////////
	private static final Color COLOR_BACKGROUND = ColorConstants.listBackground;
	private static final Color COLOR_NO_PROPERTIES = ColorConstants.gray;
	private static final Color COLOR_LINE = ColorConstants.lightGray;
	private static final Color COLOR_COMPLEX_LINE = DrawUtils.getShiftedColor(ColorConstants.lightGray, -32);
	private static final Color COLOR_PROPERTY_BG = DrawUtils.getShiftedColor(COLOR_BACKGROUND, -12);
	private static final Color COLOR_PROPERTY_BG_MODIFIED = COLOR_BACKGROUND;
	private static final Color COLOR_PROPERTY_FG_TITLE = ColorConstants.listForeground;
	private static final Color COLOR_PROPERTY_FG_VALUE = DrawUtils.isDarkColor(ColorConstants.listBackground)
			? ColorConstants.lightBlue
					: ColorConstants.darkBlue;
	private static final Color COLOR_PROPERTY_BG_SELECTED = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION);
	private static final Color COLOR_PROPERTY_FG_SELECTED = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
	private static final Color COLOR_PROPERTY_FG_ADVANCED = ColorConstants.gray;
	////////////////////////////////////////////////////////////////////////////
	//
	// Sizes
	//
	////////////////////////////////////////////////////////////////////////////
	private static final int MIN_COLUMN_WIDTH = 75;
	private static final int MARGIN_LEFT = 2;
	private static final int MARGIN_RIGHT = 1;
	private static final int MARGIN_BOTTOM = 1;
	private static final int STATE_IMAGE_MARGIN_RIGHT = 4;
	////////////////////////////////////////////////////////////////////////////
	//
	// Images
	//
	////////////////////////////////////////////////////////////////////////////
	private static final Image m_plusImage = DesignerPlugin.getImage("properties/plus.gif");
	private static final Image m_minusImage = DesignerPlugin.getImage("properties/minus.gif");
	private static int m_stateWidth = 9;
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean m_showAdvancedProperties;
	private Property[] m_rawProperties;
	private List<PropertyInfo> m_properties;
	private final Set<String> m_expandedIds = new TreeSet<>();
	private int m_rowHeight;
	private int m_splitter = -1;
	private Font m_baseFont;
	private Font m_boldFont;
	private Font m_italicFont;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertyTable(Composite parent, int style) {
		createControl(parent);
		setEditPartFactory(new PropertyEditPartFactory());
		setEditDomain(new PropertyEditDomain());
		getControl().addListener(SWT.Resize, event -> handleResize());
		// calculate sizes
		m_rowHeight = 1 + FigureUtilities.getFontMetrics(getControl().getFont()).getHeight() + 1;
		m_baseFont = parent.getFont();
		m_boldFont = DrawUtils.getBoldFont(m_baseFont);
		m_italicFont = DrawUtils.getItalicFont(m_baseFont);
		// Initialize with <No Properties>
		setInput(null);
	}

	@Override
	protected void handleDispose(DisposeEvent e) {
		m_boldFont.dispose();
		m_italicFont.dispose();
	}

	@Override
	public FigureCanvas getControl() {
		return (FigureCanvas) super.getControl();
	}

	@Override
	public PropertyEditDomain getEditDomain() {
		return (PropertyEditDomain) super.getEditDomain();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events: dispose, resize, scroll
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Handles {@link SWT#Resize} event.
	 */
	private void handleResize() {
		// splitter
		{
			// set default value for splitter
			if (m_splitter <= MIN_COLUMN_WIDTH) {
				m_splitter = Math.max((int) (getControl().getClientArea().width * 0.4), MIN_COLUMN_WIDTH);
			}
			configureSplitter();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editor
	//
	////////////////////////////////////////////////////////////////////////////
	private PropertyInfo m_activePropertyInfo;
	private String m_activePropertyId;
	private PropertyEditor m_activeEditor;

	/**
	 * Tries to activate editor for {@link PropertyInfo} under cursor.
	 *
	 * @param location the mouse location, if editor is activated using mouse click,
	 *                 or <code>null</code> if it is activated using keyboard.
	 */
	public void activateEditor(Property property, Point location) {
		try {
			// de-activate old editor
			deactivateEditor(true);
			// activate editor
			PropertyEditor editor = property.getEditor();
			try {
				if (editor.activate(this, property, location)) {
					m_activeEditor = editor;
				}
			} catch (Throwable e) {
				handleException(e);
			}
			// set bounds
			setActiveEditorBounds();
		} catch (Throwable e) {
			DesignerPlugin.log(e);
		}
	}

	/**
	 * Deactivates current {@link PropertyEditor}.
	 */
	public void deactivateEditor(boolean save) {
		if (m_activeEditor != null) {
			PropertyEditor activeEditor = m_activeEditor;
			m_activeEditor = null;
			if (m_activePropertyInfo != null && m_activePropertyInfo.m_property != null) {
				activeEditor.deactivate(this, m_activePropertyInfo.m_property, save);
			}
		}
	}

	/**
	 * Sets correct bounds for active editor, for example we need update bounds
	 * after scrolling.
	 */
	private void setActiveEditorBounds() {
		if (m_activeEditor != null) {
			int index = m_properties.indexOf(m_activePropertyInfo);
			if (index == -1) {
				// it is possible that active property was hidden because its parent was
				// collapsed
				deactivateEditor(true);
			} else {
				// prepare bounds for editor
				org.eclipse.swt.graphics.Rectangle bounds;
				{
					PropertyEditPart editPart = (PropertyEditPart) getEditPartRegistry().get(m_activePropertyInfo);
					Rectangle figureBounds = getAbsoluteBounds(editPart);
					int x = m_splitter + 1;
					int y = figureBounds.top();
					int width = getControl().getClientArea().width - x - MARGIN_RIGHT;
					int height = figureBounds.height() - MARGIN_BOTTOM;
					bounds = new org.eclipse.swt.graphics.Rectangle(x, y, width, height);
				}
				// update bounds using presentation
				{
					PropertyEditorPresentation presentation = m_activeEditor.getPresentation();
					if (presentation != null) {
						int presentationWidth = presentation.show(this, m_activePropertyInfo.m_property, bounds.x,
								bounds.y, bounds.width, bounds.height);
						bounds.width -= presentationWidth;
					}
				}
				// set editor bounds
				m_activeEditor.setBounds(bounds);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Exceptions
	//
	////////////////////////////////////////////////////////////////////////////
	private IPropertyExceptionHandler m_exceptionHandler;

	/**
	 * Sets {@link IPropertyExceptionHandler} for handling all exceptions.
	 */
	public void setExceptionHandler(IPropertyExceptionHandler exceptionHandler) {
		m_exceptionHandler = exceptionHandler;
	}

	/**
	 * Handles given {@link Throwable}.<br>
	 * Right now it just logs it, but in future we can open some dialog here.
	 */
	public void handleException(Throwable e) {
		m_exceptionHandler.handle(e);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Location/size utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the <code>X</code> position for first pixel of {@link PropertyInfo}
	 *         title (location of state image).
	 */
	private int getTitleX(PropertyInfo propertyInfo) {
		return MARGIN_LEFT + getLevelIndent() * propertyInfo.getLevel();
	}

	/**
	 * @return the <code>X</code> position for first pixel of {@link PropertyInfo}
	 *         title text.
	 */
	private int getTitleTextX(PropertyInfo propertyInfo) {
		return getTitleX(propertyInfo) + getLevelIndent();
	}

	/**
	 * @return the bounds of the given edit part relative to the top right corner of
	 *         the viewport.
	 */
	private static Rectangle getAbsoluteBounds(PropertyEditPart editPart) {
		IFigure figure = editPart.getFigure();
		Rectangle bounds = figure.getBounds().getCopy();
		figure.translateToAbsolute(bounds);
		return bounds;
	}

	/**
	 * @return the indentation for single level.
	 */
	private int getLevelIndent() {
		return m_stateWidth + STATE_IMAGE_MARGIN_RIGHT;
	}

	/**
	 * Checks horizontal splitter value to boundary values.
	 */
	private void configureSplitter() {
		org.eclipse.swt.graphics.Rectangle clientArea = getControl().getClientArea();
		// check title width
		if (m_splitter < MIN_COLUMN_WIDTH) {
			m_splitter = MIN_COLUMN_WIDTH;
		}
		// check value width
		if (clientArea.width - m_splitter < MIN_COLUMN_WIDTH) {
			m_splitter = clientArea.width - MIN_COLUMN_WIDTH;
		}
	}

	/**
	 * @return <code>true</code> if given <code>x</code> coordinate is on state
	 *         (plus/minus) image.
	 */
	private boolean isLocationState(PropertyInfo propertyInfo, int x) {
		int levelX = getTitleX(propertyInfo);
		return propertyInfo.isComplex() && levelX <= x && x <= levelX + m_stateWidth;
	}

	/**
	 * Returns <code>true</code> if <code>x</code> coordinate is on splitter.
	 */
	private boolean isLocationSplitter(int x) {
		return Math.abs(m_splitter - x) < 2;
	}

	/**
	 * @return <code>true</code> if given <code>x</code> is on value part of
	 *         property.
	 */
	private boolean isLocationValue(int x) {
		return x > m_splitter + 2;
	}

	/**
	 * @param x the {@link PropertyTable} relative coordinate.
	 * @param y the {@link PropertyTable} relative coordinate.
	 *
	 * @return the location relative to the value part of property.
	 */
	private Point getValueRelativeLocation(int x, int y) {
		PropertyEditPart editPart = (PropertyEditPart) findObjectAt(new Point(x, y));
		return new Point(x - (m_splitter + 2), getAbsoluteBounds(editPart).top());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Shows or hides {@link Property}-s with {@link PropertyCategory#ADVANCED}.
	 */
	public void setShowAdvancedProperties(boolean showAdvancedProperties) {
		m_showAdvancedProperties = showAdvancedProperties;
		setInput0();
	}

	/**
	 * Sets the array of {@link Property}'s to display/edit.
	 */
	public void setInput(Property[] properties) {
		m_rawProperties = properties;
		setInput0();
	}

	private void setInput0() {
		// send "hide" to all PropertyEditorPresentation's
		if (m_properties != null) {
			for (PropertyInfo propertyInfo : m_properties) {
				Property property = propertyInfo.getProperty();
				// hide presentation
				{
					PropertyEditorPresentation presentation = property.getEditor().getPresentation();
					if (presentation != null) {
						presentation.hide(this, property);
					}
				}
			}
		}
		// set new properties
		if (m_rawProperties == null || m_rawProperties.length == 0) {
			deactivateEditor(false);
			m_properties = new ArrayList<>();
		} else {
			try {
				// add PropertyInfo for each Property
				m_properties = new ArrayList<>();
				for (Property property : m_rawProperties) {
					if (rawProperties_shouldShow(property)) {
						PropertyInfo propertyInfo = new PropertyInfo(property);
						m_properties.add(propertyInfo);
					}
				}
				// expand properties using history
				while (true) {
					boolean expanded = false;
					List<PropertyInfo> currentProperties = new ArrayList<>(m_properties);
					for (PropertyInfo propertyInfo : currentProperties) {
						expanded |= propertyInfo.expandFromHistory();
					}
					// stop
					if (!expanded) {
						break;
					}
				}
			} catch (Throwable e) {
				DesignerPlugin.log(e);
			}
		}
		// update active property
		if (m_activePropertyId != null) {
			PropertyInfo newActivePropertyInfo = null;
			// try to find corresponding PropertyInfo
			if (m_properties != null) {
				for (PropertyInfo propertyInfo : m_properties) {
					if (propertyInfo.m_id.equals(m_activePropertyId)) {
						newActivePropertyInfo = propertyInfo;
						break;
					}
				}
			}
			// set new PropertyInfo
			setActivePropertyInfo(newActivePropertyInfo);
		}
		setContents(m_properties);
	}

	/**
	 * @return <code>true</code> if given {@link Property} should be displayed.
	 */
	private boolean rawProperties_shouldShow(Property property) throws Exception {
		PropertyCategory category = getCategory(property);
		// filter out hidden properties
		if (category.isHidden()) {
			return false;
		}
		// filter out advanced properties
		if (category.isAdvanced()) {
			if (!m_showAdvancedProperties && !property.isModified()) {
				return false;
			}
		}
		if (category.isAdvancedReally()) {
			return m_showAdvancedProperties;
		}
		// OK
		return true;
	}

	/**
	 * Activates given {@link Property}.
	 */
	public void setActiveProperty(Property property) {
		for (PropertyInfo propertyInfo : m_properties) {
			if (propertyInfo.m_property == property) {
				setActivePropertyInfo(propertyInfo);
				break;
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access: only for testing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the count of properties in "expanded" list.
	 */
	public int forTests_getPropertiesCount() {
		return m_properties.size();
	}

	/**
	 * @return the {@link Property} from "expanded" list.
	 */
	public Property forTests_getProperty(int index) {
		return m_properties.get(index).getProperty();
	}

	/**
	 * Expands the {@link PropertyInfo} with given index.
	 */
	public void forTests_expand(int index) throws Exception {
		m_properties.get(index).expand();
	}

	/**
	 * @return the position of splitter.
	 */
	public int forTests_getSplitter() {
		return m_splitter;
	}

	/**
	 * @return the location of state image (plus/minus) for given {@link Property}.
	 */
	public org.eclipse.swt.graphics.Point forTests_getStateLocation(Property property) {
		PropertyInfo propertyInfo = getPropertyInfo(property);
		if (propertyInfo != null) {
			PropertyEditPart editPart = (PropertyEditPart) getEditPartRegistry().get(propertyInfo);
			int x = getTitleX(propertyInfo);
			int y = getAbsoluteBounds(editPart).y();
			return new org.eclipse.swt.graphics.Point(x, y);
		}
		return null;
	}

	/**
	 * @return the location of state image (plus/minus) for given {@link Property}.
	 */
	public org.eclipse.swt.graphics.Point forTests_getValueLocation(Property property) {
		PropertyInfo propertyInfo = getPropertyInfo(property);
		if (propertyInfo != null) {
			PropertyEditPart editPart = (PropertyEditPart) getEditPartRegistry().get(propertyInfo);
			int x = m_splitter + 5;
			int y = getAbsoluteBounds(editPart).y();
			return new org.eclipse.swt.graphics.Point(x, y);
		}
		return null;
	}

	/**
	 * @return the active {@link PropertyEditor}.
	 */
	public PropertyEditor forTests_getActiveEditor() {
		return m_activeEditor;
	}

	/**
	 * @return the {@link PropertyCategory} that is used by this
	 *         {@link PropertyTable} to display.
	 */
	public PropertyCategory forTests_getCategory(Property property) {
		return getCategory(property);
	}

	/**
	 * @return the {@link PropertyInfo}for given {@link Property}.
	 */
	private PropertyInfo getPropertyInfo(Property property) {
		for (PropertyInfo propertyInfo : m_properties) {
			if (propertyInfo.getProperty() == property) {
				return propertyInfo;
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ISelectionProvider
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public ISelection getSelection() {
		if (m_activePropertyInfo != null) {
			return new StructuredSelection(m_activePropertyInfo.getProperty());
		} else {
			return StructuredSelection.EMPTY;
		}
	}

	@Override
	public void setSelection(ISelection selection) {
		throw new NotImplementedException(PropertyTable.class.getName());
	}

	/**
	 * Sets the new active {@link PropertyInfo} and sends event to
	 * {@link ISelectionChangedListener} 's.
	 */
	private void setActivePropertyInfo(PropertyInfo activePropertyInfo) {
		m_activePropertyInfo = activePropertyInfo;
		// update m_activePropertyId only when really select property,
		// not just remove selection because there are no corresponding property for old
		// active
		// so, later for some other component, if we don't select other property, old
		// active will be selected
		if (m_activePropertyInfo != null) {
			m_activePropertyId = m_activePropertyInfo.m_id;
		}
		// make sure that active property is visible
		if (getEditPartRegistry().get(m_activePropertyInfo) instanceof PropertyEditPart editPart) {
			reveal(editPart);
		}
		// send events
		fireSelectionChanged();
		// re-draw
		getControl().redraw();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PropertyCategory
	//
	////////////////////////////////////////////////////////////////////////////
	private PropertyCategoryProvider m_propertyCategoryProvider = PropertyCategoryProviders.fromProperty();

	/**
	 * Sets the {@link PropertyCategoryProvider} that can be used to tweak usual
	 * {@link PropertyCategory}.
	 */
	public void setPropertyCategoryProvider(PropertyCategoryProvider propertyCategoryProvider) {
		m_propertyCategoryProvider = propertyCategoryProvider;
	}

	/**
	 * @return the {@link PropertyCategory} that is used by this
	 *         {@link PropertyTable} to display.
	 */
	private PropertyCategory getCategory(Property property) {
		return m_propertyCategoryProvider.getCategory(property);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////

	public class PropertyEditDomain extends EditDomain {
		private final PropertyTableTooltipHelper m_tooltipHelper = new PropertyTableTooltipHelper(PropertyTable.this);
		private boolean m_splitterResizing;
		/**
		 * We do expand/collapse on to events: click on state sign and on double click.
		 * But when we double click on state sign, we will have <em>two</em> events, so
		 * we should ignore double click.
		 */
		private long m_lastExpandCollapseTime;

		////////////////////////////////////////////////////////////////////////////
		//
		// Events: mouse
		//
		////////////////////////////////////////////////////////////////////////////

		@Override
		public void mouseDown(MouseEvent event, EditPartViewer viewer) {
			m_splitterResizing = event.button == 1 && m_properties != null && isLocationSplitter(event.x);
			// click in property
			if (!m_splitterResizing && findObjectAt(new Point(event.x, event.y)) instanceof PropertyEditPart editPart) {
				// prepare property
				setActivePropertyInfo(editPart.getModel());
				Property property = m_activePropertyInfo.getProperty();
				// de-activate current editor
				deactivateEditor(true);
				getControl().redraw();
				// activate editor
				if (isLocationValue(event.x)) {
					activateEditor(property, getValueRelativeLocation(event.x, event.y));
				}
			}
		}

		@Override
		public void mouseUp(MouseEvent event, EditPartViewer viewer) {
			if (event.button == 1) {
				// resize splitter
				if (m_splitterResizing) {
					m_splitterResizing = false;
					return;
				}
				// if out of bounds, then ignore
				if (!getControl().getClientArea().contains(event.x, event.y)) {
					return;
				}
				// update
				if (findObjectAt(new Point(event.x, event.y)) instanceof PropertyEditPart editPart) {
					PropertyInfo propertyInfo = editPart.getModel();
					// check for expand/collapse
					if (isLocationState(propertyInfo, event.x)) {
						try {
							m_lastExpandCollapseTime = System.currentTimeMillis();
							propertyInfo.flip();
						} catch (Throwable e) {
							DesignerPlugin.log(e);
						}
					}
				}
			}
		}

		@Override
		public void mouseDoubleClick(MouseEvent event, EditPartViewer viewer) {
			if (System.currentTimeMillis() - m_lastExpandCollapseTime > getControl().getDisplay()
					.getDoubleClickTime()) {
				try {
					if (m_activePropertyInfo != null) {
						if (m_activePropertyInfo.isComplex()) {
							m_activePropertyInfo.flip();
						} else {
							Property property = m_activePropertyInfo.getProperty();
							property.getEditor().doubleClick(property, getValueRelativeLocation(event.x, event.y));
						}
					}
				} catch (Throwable e) {
					handleException(e);
				}
			}
		}

		@Override
		public void mouseMove(MouseEvent event, EditPartViewer viewer) {
			// if out of bounds, then ignore
			if (!getControl().getClientArea().contains(event.x, event.y)) {
				return;
			}
			// update
			if (findObjectAt(new Point(event.x, event.y)) instanceof PropertyEditPart) {
				// update cursor
				if (isLocationSplitter(event.x)) {
					getControl().setCursor(Cursors.SIZEWE);
				} else {
					getControl().setCursor(null);
				}
				// update tooltip helper
				updateTooltip(event);
			} else {
				updateTooltipNoProperty();
			}
		}

		@Override
		public void mouseDrag(MouseEvent event, EditPartViewer viewer) {
			// resize splitter
			if (m_splitterResizing) {
				m_splitter = event.x;
				configureSplitter();
				getControl().redraw();
			}
		}

		/**
		 * Updates {@link PropertyTableTooltipHelper}.
		 */
		private void updateTooltip(MouseEvent event) {
			int x = event.x;
			//
			if (findObjectAt(new Point(x, event.y)) instanceof PropertyEditPart editPart) {
				PropertyInfo propertyInfo = editPart.getModel();
				Property property = propertyInfo.getProperty();
				int y = getAbsoluteBounds(editPart).bottom();
				// check for title
				{
					int titleX = getTitleTextX(propertyInfo);
					int titleRight = m_splitter - 2;
					if (titleX <= x && x < titleRight) {
						m_tooltipHelper.update(property, true, false, titleX, titleRight, y, m_rowHeight);
						return;
					}
				}
				// check for value
				{
					int valueX = m_splitter + 3;
					if (x > valueX) {
						m_tooltipHelper.update(property, false, true, valueX, getControl().getClientArea().width, y,
								m_rowHeight);
					}
				}
			} else {
				updateTooltipNoProperty();
			}
		}

		private void updateTooltipNoProperty() {
			m_tooltipHelper.update(null, false, false, 0, 0, 0, 0);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Keyboard
		//
		////////////////////////////////////////////////////////////////////////////

		@Override
		public void keyDown(KeyEvent e, EditPartViewer viewer) {
			if (m_activePropertyInfo != null) {
				try {
					Property property = m_activePropertyInfo.getProperty();
					// expand/collapse
					if (m_activePropertyInfo.isComplex()) {
						if (!m_activePropertyInfo.isExpanded()
								&& (e.character == '+' || e.keyCode == SWT.ARROW_RIGHT)) {
							m_activePropertyInfo.expand();
							return;
						}
						if (m_activePropertyInfo.isExpanded() && (e.character == '-' || e.keyCode == SWT.ARROW_LEFT)) {
							m_activePropertyInfo.collapse();
							return;
						}
					}
					// navigation
					if (navigate(e)) {
						return;
					}
					// editor activation
					if (e.character == ' ' || e.character == SWT.CR) {
						activateEditor(property, null);
						return;
					}
					// DEL
					if (e.keyCode == SWT.DEL) {
						e.doit = false;
						property.setValue(Property.UNKNOWN_VALUE);
						return;
					}
					// send to editor
					property.getEditor().keyDown(PropertyTable.this, property, e);
				} catch (Throwable ex) {
					DesignerPlugin.log(ex);
				}
			}
		}

		/**
		 * @return <code>true</code> if given {@link KeyEvent} was navigation event, so
		 *         new {@link PropertyInfo} was selected.
		 */
		public boolean navigate(KeyEvent e) {
			int index = m_properties.indexOf(m_activePropertyInfo);
			int page = getControl().getClientArea().height / m_rowHeight;
			//
			int newIndex = index;
			if (e.keyCode == SWT.HOME) {
				newIndex = 0;
			} else if (e.keyCode == SWT.END) {
				newIndex = m_properties.size() - 1;
			} else if (e.keyCode == SWT.PAGE_UP) {
				newIndex = Math.max(index - page + 1, 0);
			} else if (e.keyCode == SWT.PAGE_DOWN) {
				newIndex = Math.min(index + page - 1, m_properties.size() - 1);
			} else if (e.keyCode == SWT.ARROW_UP) {
				newIndex = Math.max(index - 1, 0);
			} else if (e.keyCode == SWT.ARROW_DOWN) {
				newIndex = Math.min(index + 1, m_properties.size() - 1);
			}
			// activate new property
			if (newIndex != index && newIndex < m_properties.size()) {
				setActivePropertyInfo(m_properties.get(newIndex));
				return true;
			}
			// no navigation change
			return false;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PropertyFigure
	//
	////////////////////////////////////////////////////////////////////////////

	public class PropertyEditPartFactory implements EditPartFactory {
		@Override
		@SuppressWarnings("unchecked")
		public EditPart createEditPart(EditPart context, Object model) {
			if (model instanceof List properties) {
				if (properties.isEmpty()) {
					return new NoPropertyEditPart();
				}
				return new PropertyRootEditPart((List<PropertyInfo>) model);
			}
			return new PropertyEditPart((PropertyInfo) model);
		}
	}

	private static class NoPropertyEditPart extends AbstractGraphicalEditPart {

		@Override
		protected IFigure createFigure() {
			Label label = new Label();
			label.setBackgroundColor(COLOR_BACKGROUND);
			label.setForegroundColor(COLOR_NO_PROPERTIES);
			label.setText(ModelMessages.PropertyTable_noProperties);
			label.setOpaque(true);
			return label;
		}

		@Override
		protected void createEditPolicies() {
			// Nothing to do
		}

	}

	private final class PropertyRootEditPart extends AbstractGraphicalEditPart {
		public PropertyRootEditPart(List<PropertyInfo> model) {
			setModel(model);
		}

		@Override
		protected IFigure createFigure() {
			GridLayout gridLayout = new GridLayout();
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;
			gridLayout.horizontalSpacing = 0;
			gridLayout.verticalSpacing = 0;

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
					g.drawLine(m_splitter, 0, m_splitter, tempRect.height);
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
					int height = m_rowHeight - MARGIN_BOTTOM;
					int xOffset = m_plusImage.getBounds().width / 2;
					int yOffset = (height - m_plusImage.getBounds().width) / 2;
					//
					graphics.setForegroundColor(COLOR_COMPLEX_LINE);
					for (int i = 0; i < m_properties.size(); i++) {
						PropertyInfo propertyInfo = m_properties.get(i);
						//
						if (propertyInfo.isExpanded()) {
							int index = m_properties.indexOf(propertyInfo);
							// prepare index of last sub-property
							int index2 = index;
							for (; index2 < m_properties.size(); index2++) {
								PropertyInfo nextPropertyInfo = m_properties.get(index2);
								if (nextPropertyInfo != propertyInfo
										&& nextPropertyInfo.getLevel() <= propertyInfo.getLevel()) {
									break;
								}
							}
							index2--;
							// draw line if there are children
							if (index2 > index
									&& getEditPartRegistry().get(propertyInfo) instanceof PropertyEditPart editPart) {
								int y = editPart.getFigure().getBounds().top();
								int x = getTitleX(propertyInfo) + xOffset;
								int y1 = y + height - yOffset;
								int y2 = y + m_rowHeight * (index2 - index) + m_rowHeight / 2;
								graphics.drawLine(x, y1, x, y2);
								graphics.drawLine(x, y2, x + m_rowHeight / 3, y2);
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
			figure.setLayoutManager(gridLayout);
			figure.setOpaque(true);
			return figure;
		}

		@Override
		protected void createEditPolicies() {
			// Nothing to do
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

	private final class PropertyEditPart extends AbstractGraphicalEditPart {
		public PropertyEditPart(PropertyInfo propertyInfo) {
			setModel(propertyInfo);
		}

		@Override
		public PropertyInfo getModel() {
			return (PropertyInfo) super.getModel();
		}

		@Override
		protected IFigure createFigure() {
			SeparatorBorder border = new SeparatorBorder(new Insets(0, 0, MARGIN_BOTTOM, 1), PositionConstants.BOTTOM);
			border.setColor(COLOR_LINE);
			figure = new PropertyFigure(getModel());
			figure.setBorder(border);
			return figure;
		}

		@Override
		protected void createEditPolicies() {
			// Nothing to do
		}
	}

	private final class PropertyFigure extends Figure {
		private final PropertyInfo m_propertyInfo;

		public PropertyFigure(PropertyInfo propertyInfo) {
			m_propertyInfo = propertyInfo;
		}

		@Override
		public void setParent(IFigure parent) {
			super.setParent(parent);
			if (parent != null) {
				parent.setConstraint(this, new GridData(SWT.FILL, SWT.FILL, true, false));
			}
		}

		@Override
		public Dimension getPreferredSize(int wHint, int hHint) {
			return new Dimension(wHint, m_rowHeight);
		}

		@Override
		protected void paintFigure(Graphics graphics) {
			int width = bounds.width();
			int height = bounds.height();
			int y = bounds.y();
			// draw property
			try {
				Property property = m_propertyInfo.getProperty();
				boolean isActiveProperty = m_activePropertyInfo != null && m_activePropertyInfo.getProperty() == property;
				int presentationWidth = 0;
				PropertyEditorPresentation presentation = property.getEditor().getPresentation();
				if (presentation != null) {
					Point p = new Point(m_splitter + 4, y);
					translateToAbsolute(p);
					//
					int w = width - p.x() - MARGIN_RIGHT;
					int h = height - MARGIN_BOTTOM;
					//
					presentationWidth = presentation.show(PropertyTable.this, property, p.x(), p.y(), w, h);
				}
				// set background
				{
					if (isActiveProperty) {
						graphics.setBackgroundColor(COLOR_PROPERTY_BG_SELECTED);
						// Might've been moved due to resizing the table
						setActiveEditorBounds();
					} else {
						if (property.isModified()) {
							graphics.setBackgroundColor(COLOR_PROPERTY_BG_MODIFIED);
						} else {
							graphics.setBackgroundColor(COLOR_PROPERTY_BG);
						}
					}
					graphics.fillRectangle(0, y, width - presentationWidth, height);
				}
				// draw state image
				if (m_propertyInfo.isShowComplex()) {
					Image stateImage = m_propertyInfo.isExpanded() ? m_minusImage : m_plusImage;
					DrawUtils.drawImageCV(graphics, stateImage, getTitleX(m_propertyInfo), y, height);
				}
				// draw title
				{
					// configure GC
					{
						graphics.setForegroundColor(COLOR_PROPERTY_FG_TITLE);
						// check category
						if (getCategory(property).isAdvanced()) {
							graphics.setForegroundColor(COLOR_PROPERTY_FG_ADVANCED);
							graphics.setFont(m_italicFont);
						} else if (getCategory(property).isPreferred() || getCategory(property).isSystem()) {
							graphics.setFont(m_boldFont);
						}
						// check for active
						if (isActiveProperty) {
							graphics.setForegroundColor(COLOR_PROPERTY_FG_SELECTED);
						}
					}
					// paint title
					int x = getTitleTextX(m_propertyInfo);
					DrawUtils.drawStringCV(graphics, property.getTitle(), x, y, m_splitter - x, height);
				}
				// draw value
				{
					// configure GC
					graphics.setFont(m_baseFont);
					if (!isActiveProperty) {
						graphics.setForegroundColor(COLOR_PROPERTY_FG_VALUE);
					}
					// prepare value rectangle
					int x = m_splitter + 4;
					int w = getControl().getClientArea().width - x - MARGIN_RIGHT;
					// paint value
					property.getEditor().paint(property, graphics, x, y, w - presentationWidth, height);
				}
			} catch (Throwable e) {
				DesignerPlugin.log(e);
			}
		}

		@Override
		public void erase() {
			Property property = m_propertyInfo.getProperty();
			PropertyEditorPresentation presentation = property.getEditor().getPresentation();
			if (presentation != null) {
				presentation.hide(PropertyTable.this, property);
			}
			super.erase();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PropertyInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Class with information about single {@link Property}.
	 *
	 * @author scheglov_ke
	 */
	private final class PropertyInfo {
		private final String m_id;
		private final int m_level;
		private final Property m_property;
		private final boolean m_stateComplex;
		private boolean m_stateExpanded;
		private List<PropertyInfo> m_children;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public PropertyInfo(Property property) {
			this(property, "", 0);
		}

		private PropertyInfo(Property property, String idPrefix, int level) {
			m_id = idPrefix + "|" + property.getTitle();
			m_level = level;
			m_property = property;
			m_stateComplex = property.getEditor() instanceof IComplexPropertyEditor;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// State
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * @return <code>true</code> if this property is complex.
		 */
		public boolean isComplex() {
			return m_stateComplex;
		}

		public boolean isShowComplex() throws Exception {
			if (m_stateComplex) {
				prepareChildren();
				return !CollectionUtils.isEmpty(m_children);
			}
			return false;
		}

		/**
		 * @return <code>true</code> if this complex property is expanded.
		 */
		public boolean isExpanded() {
			return m_stateExpanded;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * @return the level of this property, i.e. on which level of complex property
		 *         it is located.
		 */
		public int getLevel() {
			return m_level;
		}

		/**
		 * @return the {@link Property}.
		 */
		public Property getProperty() {
			return m_property;
		}

		/**
		 * Flips collapsed/expanded state and adds/removes sub-properties.
		 */
		public void flip() throws Exception {
			Assert.isTrue(m_stateComplex);
			if (m_stateExpanded) {
				collapse();
			} else {
				expand();
			}
		}

		/**
		 * Expands this property.
		 */
		public void expand() throws Exception {
			Assert.isTrue(m_stateComplex);
			Assert.isTrue(!m_stateExpanded);
			//
			m_stateExpanded = true;
			m_expandedIds.add(m_id);
			prepareChildren();
			//
			int index = m_properties.indexOf(this);
			addChildren(index + 1);
			setContents(m_properties);
		}

		/**
		 * Collapses this property.
		 */
		public void collapse() throws Exception {
			Assert.isTrue(m_stateComplex);
			Assert.isTrue(m_stateExpanded);
			//
			m_stateExpanded = false;
			m_expandedIds.remove(m_id);
			prepareChildren();
			//
			int index = m_properties.indexOf(this);
			removeChildren(index + 1);
			setContents(m_properties);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Internal
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * Adds children properties.
		 *
		 * @return the index for new properties to add.
		 */
		private int addChildren(int index) throws Exception {
			prepareChildren();
			for (PropertyInfo child : m_children) {
				// skip if should not display raw Property
				if (!rawProperties_shouldShow(child.m_property)) {
					continue;
				}
				// add child
				m_properties.add(index++, child);
				// add children of current child
				if (child.isExpanded()) {
					index = child.addChildren(index);
				}
			}
			return index;
		}

		/**
		 * Removes children properties.
		 */
		private void removeChildren(int index) throws Exception {
			prepareChildren();
			for (PropertyInfo child : m_children) {
				// skip if should not display raw Property
				if (!rawProperties_shouldShow(child.m_property)) {
					continue;
				}
				// hide presentation
				{
					PropertyEditorPresentation presentation = child.getProperty().getEditor().getPresentation();
					if (presentation != null) {
						presentation.hide(PropertyTable.this, child.getProperty());
					}
				}
				// remove child
				m_properties.remove(index);
				// remove children of current child
				if (child.isExpanded()) {
					child.removeChildren(index);
				}
			}
		}

		/**
		 * Prepares children {@link PropertyInfo}'s, for sub-properties.
		 */
		private void prepareChildren() throws Exception {
			if (m_children == null) {
				m_children = new ArrayList<>();
				for (Property subProperty : getSubProperties()) {
					PropertyInfo subPropertyInfo = createSubPropertyInfo(subProperty);
					m_children.add(subPropertyInfo);
				}
			}
		}

		private PropertyInfo createSubPropertyInfo(Property subProperty) {
			return new PropertyInfo(subProperty, m_id, m_level + 1);
		}

		private Property[] getSubProperties() throws Exception {
			IComplexPropertyEditor complexEditor = (IComplexPropertyEditor) m_property.getEditor();
			List<Property> subProperties = new ArrayList<>();
			for (Property subProperty : complexEditor.getProperties(m_property)) {
				if (getCategory(subProperty).isHidden() && !subProperty.isModified()) {
					// skip hidden properties
					continue;
				}
				subProperties.add(subProperty);
			}
			return subProperties.toArray(new Property[subProperties.size()]);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Persistent expanding support
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * @return <code>true</code> if this {@link PropertyInfo} was expanded from
		 *         history.
		 */
		public boolean expandFromHistory() throws Exception {
			if (isComplex() && !isExpanded() && m_expandedIds.contains(m_id)) {
				expand();
				return true;
			}
			return false;
		}
	}
}