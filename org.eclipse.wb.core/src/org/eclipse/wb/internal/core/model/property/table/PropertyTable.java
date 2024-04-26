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
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
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
public class PropertyTable extends GraphicalViewerImpl {
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
	private Image m_bufferedImage;
	private int m_rowHeight;
	private int m_selection;
	private int m_page;
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
		setControl(new Canvas(parent, style | SWT.V_SCROLL | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE));
		setEditDomain(new PropertyEditDomain());
		hookControlEvents();
		// calculate sizes
		m_rowHeight = 1 + FigureUtilities.getFontMetrics(getControl().getFont()).getHeight() + 1;
		m_baseFont = parent.getFont();
		m_boldFont = DrawUtils.getBoldFont(m_baseFont);
		m_italicFont = DrawUtils.getItalicFont(m_baseFont);
	}

	@Override
	protected void handleDispose(DisposeEvent e) {
		m_boldFont.dispose();
		m_italicFont.dispose();
	}

	@Override
	public Canvas getControl() {
		return (Canvas) super.getControl();
	}

	@Override
	public PropertyEditDomain getEditDomain() {
		return (PropertyEditDomain) super.getEditDomain();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds listeners for events.
	 */
	private void hookControlEvents() {
		getControl().addListener(SWT.Dispose, event -> disposeBufferedImage());
		getControl().addListener(SWT.Resize, event -> handleResize());
		getControl().addListener(SWT.Paint, event -> handlePaint(event.gc, event.x, event.y, event.width, event.height));
		getControl().getVerticalBar().addListener(SWT.Selection, event -> handleVerticalScrolling());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events: dispose, resize, scroll
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Disposes image used for double buffered painting.
	 */
	private void disposeBufferedImage() {
		if (m_bufferedImage != null) {
			m_bufferedImage.dispose();
			m_bufferedImage = null;
		}
	}

	/**
	 * Handles {@link SWT#Resize} event.
	 */
	private void handleResize() {
		disposeBufferedImage();
		configureScrolling();
		// splitter
		{
			// set default value for splitter
			if (m_splitter <= MIN_COLUMN_WIDTH) {
				m_splitter = Math.max((int) (getControl().getClientArea().width * 0.4), MIN_COLUMN_WIDTH);
			}
			configureSplitter();
		}
	}

	/**
	 * Handles {@link SWT#Selection} event for vertical {@link ScrollBar}.
	 */
	private void handleVerticalScrolling() {
		ScrollBar verticalBar = getControl().getVerticalBar();
		if (verticalBar.getEnabled()) {
			// update selection
			m_selection = verticalBar.getSelection();
			// redraw (but not include vertical bar to avoid flashing)
			{
				org.eclipse.swt.graphics.Rectangle clientArea = getControl().getClientArea();
				getControl().redraw(clientArea.x, clientArea.y, clientArea.width, clientArea.height, false);
			}
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
					org.eclipse.swt.graphics.Rectangle clientArea = getControl().getClientArea();
					int x = m_splitter + 1;
					int width = clientArea.width - x - MARGIN_RIGHT;
					int y = m_rowHeight * (index - m_selection) + 1;
					int height = m_rowHeight - 1;
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
	// Scrolling
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Configures vertical {@link ScrollBar}.
	 */
	private void configureScrolling() {
		ScrollBar verticalBar = getControl().getVerticalBar();
		if (m_properties == null) {
			verticalBar.setEnabled(false);
		} else {
			m_page = getControl().getClientArea().height / m_rowHeight;
			m_selection = Math.max(0, Math.min(m_properties.size() - m_page, m_selection));
			verticalBar.setValues(m_selection, 0, m_properties.size(), m_page, 1, m_page);
			// enable/disable scrolling
			if (m_properties.size() <= m_page) {
				verticalBar.setEnabled(false);
			} else {
				verticalBar.setEnabled(true);
			}
		}
		// redraw, we reconfigure scrolling only if list of properties was changed, so
		// we should redraw
		getControl().redraw();
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
	 * @return the index in {@link #m_properties} corresponding given <code>y</code>
	 *         location.
	 */
	private int getPropertyIndex(int y) {
		return m_selection + y / m_rowHeight;
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
		return new Point(x - (m_splitter + 2), y - m_rowHeight * getPropertyIndex(y));
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
		// update scroll bar
		configureScrolling();
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
			int index = m_properties.indexOf(propertyInfo);
			int x = getTitleX(propertyInfo);
			int y = m_rowHeight * (index - m_selection) + 1;
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
			int index = m_properties.indexOf(propertyInfo);
			int x = m_splitter + 5;
			int y = m_rowHeight * (index - m_selection) + 1;
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
		if (m_activePropertyInfo != null) {
			int row = m_properties.indexOf(m_activePropertyInfo);
			if (m_selection <= row && row < m_selection + m_page) {
			} else {
				m_selection = row;
				configureScrolling();
			}
		}
		// send events
		fireSelectionChanged();
		// re-draw
		getControl().redraw();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Painting
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean m_painting;

	/**
	 * Handles {@link SWT#Paint} event.
	 */
	private void handlePaint(GC gc, int x, int y, int width, int height) {
		// sometimes we disable Eclipse Shell to prevent user actions, but we do this
		// for short time
		if (!getControl().isEnabled()) {
			return;
		}
		// prevent recursion
		if (m_painting) {
			return;
		}
		m_painting = true;
		//
		try {
			setActiveEditorBounds();
			// prepare buffered image
			if (m_bufferedImage == null || m_bufferedImage.isDisposed()) {
				org.eclipse.swt.graphics.Point size = getControl().getSize();
				m_bufferedImage = new Image(DesignerPlugin.getStandardDisplay(), size.x, size.y);
			}
			// prepare buffered GC
			GC bufferedGC = null;
			try {
				// perform some drawing
				{
					bufferedGC = new GC(m_bufferedImage);
					bufferedGC.setClipping(x, y, width, height);
					bufferedGC.setBackground(gc.getBackground());
					bufferedGC.setForeground(gc.getForeground());
					bufferedGC.setFont(gc.getFont());
					bufferedGC.setLineStyle(gc.getLineStyle());
					bufferedGC.setLineWidth(gc.getLineWidth());
				}
				// fill client area
				{
					org.eclipse.swt.graphics.Rectangle clientArea = getControl().getClientArea();
					bufferedGC.setBackground(COLOR_BACKGROUND);
					bufferedGC.fillRectangle(clientArea);
				}
				// draw content
				if (m_properties == null) {
					drawEmptyContent(bufferedGC);
				} else {
					Graphics graphics = new SWTGraphics(bufferedGC);
					try {
						drawContent(graphics);
					} catch (Exception e) {
						DesignerPlugin.log(e);
					} finally {
						graphics.dispose();
					}
				}
			} finally {
				// flush image
				if (bufferedGC != null) {
					bufferedGC.dispose();
				}
			}
			gc.drawImage(m_bufferedImage, 0, 0);
		} finally {
			m_painting = false;
		}
	}

	/**
	 * Draws content when there are no properties.
	 */
	private void drawEmptyContent(GC gc) {
		org.eclipse.swt.graphics.Rectangle area = getControl().getClientArea();
		// draw message
		gc.setForeground(COLOR_NO_PROPERTIES);
		DrawUtils.drawStringCHCV(gc, ModelMessages.PropertyTable_noProperties, 0, 0, area.width, area.height);
	}

	/**
	 * Draws all {@link PropertyInfo}'s, separators, etc.
	 */
	private void drawContent(Graphics graphics) {
		org.eclipse.swt.graphics.Rectangle clientArea = getControl().getClientArea();
		// show presentations
		int[] presentationsWidth = showPresentations(clientArea);
		// draw properties
		{
			int y = clientArea.y - m_rowHeight * m_selection;
			for (int i = 0; i < m_properties.size(); i++) {
				// skip, if not visible yet
				if (y + m_rowHeight < 0) {
					y += m_rowHeight;
					continue;
				}
				// stop, if already invisible
				if (y > clientArea.height) {
					break;
				}
				// draw single property
				{
					PropertyInfo propertyInfo = m_properties.get(i);
					PropertyFigure propertyFigure = new PropertyFigure(propertyInfo);
					propertyFigure.setBounds(new Rectangle(0, y + 1, clientArea.width - presentationsWidth[i], m_rowHeight - 1));
					propertyFigure.paint(graphics);
					y += m_rowHeight;
				}
				// draw row separator
				graphics.setForegroundColor(COLOR_LINE);
				graphics.drawLine(0, y, clientArea.width, y);
			}
		}
		// draw expand line
		drawExpandLines(graphics, clientArea);
		// draw rectangle around table
		graphics.setForegroundColor(COLOR_LINE);
		graphics.drawRectangle(0, 0, clientArea.width - 1, clientArea.height - 1);
		// draw splitter
		graphics.setForegroundColor(COLOR_LINE);
		graphics.drawLine(m_splitter, 0, m_splitter, clientArea.height);
	}

	/**
	 * Shows {@link PropertyEditorPresentation}'s for all {@link Property}'s, i.e.
	 * updates also their bounds. So, some {@link PropertyEditorPresentation}'s
	 * become invisible because they are moved above or below visible client area.
	 *
	 * @return the array of width for each {@link PropertyEditorPresentation}'s,
	 *         consumed on the right.
	 */
	private int[] showPresentations(org.eclipse.swt.graphics.Rectangle clientArea) {
		int[] presentationsWidth = new int[m_properties.size()];
		// prepare value rectangle
		int x = m_splitter + 4;
		int w = clientArea.width - x - MARGIN_RIGHT;
		// show presentation's for all properties
		int y = clientArea.y - m_rowHeight * m_selection;
		for (int i = 0; i < m_properties.size(); i++) {
			PropertyInfo propertyInfo = m_properties.get(i);
			Property property = propertyInfo.getProperty();
			PropertyEditorPresentation presentation = property.getEditor().getPresentation();
			if (presentation != null) {
				presentationsWidth[i] = presentation.show(this, property, x, y + 1, w, m_rowHeight - 1);
			}
			y += m_rowHeight;
		}
		return presentationsWidth;
	}

	/**
	 * Draws lines from expanded complex property to its last sub-property.
	 */
	private void drawExpandLines(Graphics graphics, org.eclipse.swt.graphics.Rectangle clientArea) {
		int height = m_rowHeight - 1;
		int xOffset = m_plusImage.getBounds().width / 2;
		int yOffset = (height - m_plusImage.getBounds().width) / 2;
		//
		int y = clientArea.y - m_selection * m_rowHeight;
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
					if (nextPropertyInfo != propertyInfo && nextPropertyInfo.getLevel() <= propertyInfo.getLevel()) {
						break;
					}
				}
				index2--;
				// draw line if there are children
				if (index2 > index) {
					int x = getTitleX(propertyInfo) + xOffset;
					int y1 = y + height - yOffset;
					int y2 = y + m_rowHeight * (index2 - index) + m_rowHeight / 2;
					graphics.drawLine(x, y1, x, y2);
					graphics.drawLine(x, y2, x + m_rowHeight / 3, y2);
				}
			}
			//
			y += m_rowHeight;
		}
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
			if (!m_splitterResizing && m_properties != null) {
				int propertyIndex = getPropertyIndex(event.y);
				if (propertyIndex >= m_properties.size()) {
					return;
				}
				// prepare property
				setActivePropertyInfo(m_properties.get(propertyIndex));
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
				if (m_properties != null) {
					int index = getPropertyIndex(event.y);
					if (index < m_properties.size()) {
						PropertyInfo propertyInfo = m_properties.get(index);
						// check for expand/collapse
						if (isLocationState(propertyInfo, event.x)) {
							try {
								m_lastExpandCollapseTime = System.currentTimeMillis();
								propertyInfo.flip();
								configureScrolling();
							} catch (Throwable e) {
								DesignerPlugin.log(e);
							}
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
							configureScrolling();
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
			if (m_properties != null) {
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
			int propertyIndex = getPropertyIndex(event.y);
			//
			if (propertyIndex < m_properties.size()) {
				PropertyInfo propertyInfo = m_properties.get(propertyIndex);
				Property property = propertyInfo.getProperty();
				int y = (propertyIndex - m_selection) * m_rowHeight;
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
							configureScrolling();
							return;
						}
						if (m_activePropertyInfo.isExpanded() && (e.character == '-' || e.keyCode == SWT.ARROW_LEFT)) {
							m_activePropertyInfo.collapse();
							configureScrolling();
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
			org.eclipse.swt.graphics.Rectangle clientArea = getControl().getClientArea();
			//
			int newIndex = index;
			if (e.keyCode == SWT.HOME) {
				newIndex = 0;
			} else if (e.keyCode == SWT.END) {
				newIndex = m_properties.size() - 1;
			} else if (e.keyCode == SWT.PAGE_UP) {
				newIndex = Math.max(index - m_page + 1, 0);
			} else if (e.keyCode == SWT.PAGE_DOWN) {
				newIndex = Math.min(index + m_page - 1, m_properties.size() - 1);
			} else if (e.keyCode == SWT.ARROW_UP) {
				newIndex = Math.max(index - 1, 0);
			} else if (e.keyCode == SWT.ARROW_DOWN) {
				newIndex = Math.min(index + 1, m_properties.size() - 1);
			}
			// activate new property
			if (newIndex != index && newIndex < m_properties.size()) {
				setActivePropertyInfo(m_properties.get(newIndex));
				// check for scrolling
				int y = m_rowHeight * (newIndex - m_selection);
				if (y < 0) {
					m_selection = newIndex;
					configureScrolling();
				} else if (y + m_rowHeight > clientArea.height) {
					m_selection = newIndex - m_page + 1;
					configureScrolling();
				}
				// repaint
				getControl().redraw();
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

	private final class PropertyFigure extends Figure {
		private final PropertyInfo m_propertyInfo;

		public PropertyFigure(PropertyInfo propertyInfo) {
			m_propertyInfo = propertyInfo;
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
				// set background
				{
					if (isActiveProperty) {
						graphics.setBackgroundColor(COLOR_PROPERTY_BG_SELECTED);
					} else {
						if (property.isModified()) {
							graphics.setBackgroundColor(COLOR_PROPERTY_BG_MODIFIED);
						} else {
							graphics.setBackgroundColor(COLOR_PROPERTY_BG);
						}
					}
					graphics.fillRectangle(0, y, width, height);
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
					int w = width - x - MARGIN_RIGHT;
					// paint value
					property.getEditor().paint(property, graphics, x, y, w, height);
				}
			} catch (Throwable e) {
				DesignerPlugin.log(e);
			}
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