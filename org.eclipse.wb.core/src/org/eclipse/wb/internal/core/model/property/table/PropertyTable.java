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
package org.eclipse.wb.internal.core.model.property.table;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategoryProvider;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategoryProviders;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.editparts.PropertyEditPart;
import org.eclipse.wb.internal.core.model.property.table.editparts.PropertyEditPartFactory;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;

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
public class PropertyTable extends ScrollingGraphicalViewer {
	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	public static final String PROP_SPLITTER = "splitter";
	////////////////////////////////////////////////////////////////////////////
	//
	// Sizes
	//
	////////////////////////////////////////////////////////////////////////////
	private static final int MIN_COLUMN_WIDTH = 75;
	private static final int MARGIN_RIGHT = 1;
	private static final int MARGIN_BOTTOM = 1;
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
	private PropertyTableTooltipHelper m_toolTipHelper;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertyTable(Composite parent, int style) {
		createControl(parent);
		setEditPartFactory(new PropertyEditPartFactory());
		setEditDomain(new PropertyEditDomain());
		getControl().getViewport().setContentsTracksWidth(true);
		getControl().getViewport().setContentsTracksHeight(true);
		getControl().addListener(SWT.Resize, event -> handleResize());
		getControl().setScrollbarsMode(SWT.NONE);
		// calculate sizes
		m_rowHeight = 1 + FigureUtilities.getFontMetrics(getControl().getFont()).getHeight() + 1;
		// Initialize tool-tip helper
		m_toolTipHelper = new PropertyTableTooltipHelper(this);
		// Initialize with <No Properties>
		setInput(null);
		setProperty(PROP_SPLITTER, -1);
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
			int width = getSplitter();
			// set default value for splitter
			if (width <= MIN_COLUMN_WIDTH) {
				width = Math.max((int) (getControl().getClientArea().width * 0.4), MIN_COLUMN_WIDTH);
			}
			configureSplitter(width);
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
					PropertyEditPart editPart = getEditPartForModel(m_activePropertyInfo);
					Rectangle figureBounds = getAbsoluteBounds(editPart);
					int x = getSplitter() + 1;
					int y = figureBounds.top();
					int width = getControl().getClientArea().width - x - MARGIN_RIGHT;
					int height = figureBounds.height() - MARGIN_BOTTOM;
					bounds = new org.eclipse.swt.graphics.Rectangle(x, y, width, height);
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
	 * @return the bounds of the given edit part relative to the top right corner of
	 *         the viewport.
	 */
	private static Rectangle getAbsoluteBounds(GraphicalEditPart editPart) {
		IFigure figure = editPart.getFigure();
		Rectangle bounds = figure.getBounds().getCopy();
		figure.translateToAbsolute(bounds);
		return bounds;
	}

	/**
	 * Checks horizontal splitter value to boundary values.
	 */
	private void configureSplitter(int width) {
		int newWidth = width;
		org.eclipse.swt.graphics.Rectangle clientArea = getControl().getClientArea();
		// check title width
		if (newWidth < MIN_COLUMN_WIDTH) {
			newWidth = MIN_COLUMN_WIDTH;
		}
		// check value width
		if (clientArea.width - newWidth < MIN_COLUMN_WIDTH) {
			newWidth = clientArea.width - MIN_COLUMN_WIDTH;
		}
		setProperty(PROP_SPLITTER, newWidth);
	}

	/**
	 * Returns <code>true</code> if <code>x</code> coordinate is on splitter.
	 */
	private boolean isLocationSplitter(int x) {
		return Math.abs(getSplitter() - x) < 2;
	}

	/**
	 * @return <code>true</code> if given <code>x</code> is on value part of
	 *         property.
	 */
	private boolean isLocationValue(int x) {
		return x > getSplitter() + 2;
	}

	/**
	 * @param x the {@link PropertyTable} relative coordinate.
	 * @param y the {@link PropertyTable} relative coordinate.
	 *
	 * @return the location relative to the value part of property.
	 */
	private Point getValueRelativeLocation(int x, int y) {
		GraphicalEditPart editPart = (GraphicalEditPart) findObjectAt(new Point(x, y));
		return new Point(x - (getSplitter() + 2), getAbsoluteBounds(editPart).top());
	}

	/**
	 * The height for a row, based on the font height of the parent composite.
	 */
	public int getRowHeight() {
		return m_rowHeight;
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
		setContents(m_properties);
		getControl().getViewport().validate();
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
	public int getSplitter() {
		return (int) getProperty(PROP_SPLITTER);
	}

	/**
	 * @return the location of state image (plus/minus) for given {@link Property}.
	 */
	public org.eclipse.swt.graphics.Point forTests_getStateLocation(Property property) {
		PropertyInfo propertyInfo = getPropertyInfo(property);
		if (propertyInfo != null) {
			PropertyEditPart editPart = getEditPartForModel(propertyInfo);
			int x = editPart.getTitleX();
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
			PropertyEditPart editPart = getEditPartForModel(propertyInfo);
			int x = getSplitter() + 5;
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

	public final PropertyTableTooltipHelper getTooltipHelper() {
		return m_toolTipHelper;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ISelectionProvider
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public void setSelection(ISelection selection) {
		throw new NotImplementedException(PropertyTable.class.getName());
	}

	/**
	 * Sets the new active {@link PropertyInfo} and sends event to
	 * {@link ISelectionChangedListener} 's.
	 */
	private void setActivePropertyInfo(PropertyInfo activePropertyInfo) {
		if (activePropertyInfo == null) {
			m_activePropertyInfo = null;
			return;
		}
		PropertyEditPart editPart = getEditPartForModel(activePropertyInfo);
		if (editPart == null) {
			String msg = NLS.bind(ModelMessages.PropertyTable_unknownEditPart, activePropertyInfo);
			DesignerPlugin.log(Status.warning(msg));
			return;
		}
		select(editPart);
	}

	@Override
	public final void select(EditPart editPart) {
		m_activePropertyInfo = ((PropertyEditPart) editPart).getModel();
		// update m_activePropertyId only when really select property,
		// not just remove selection because there are no corresponding property for old
		// active
		// so, later for some other component, if we don't select other property, old
		// active will be selected
		if (m_activePropertyInfo != null) {
			m_activePropertyId = m_activePropertyInfo.m_id;
		}
		// make sure that active property is visible
		reveal(editPart);
		// send events
		super.select(editPart);
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
	public PropertyCategory getCategory(Property property) {
		return m_propertyCategoryProvider.getCategory(property);
	}

	/**
	 * Convenience method to look up an edit part for a given model element in the
	 * EditPart registry.
	 *
	 * See also {@link #getEditPartRegistry()} for details on the EditPart registry.
	 *
	 * @param model the model object for which an EditPart is looked up
	 * @return the edit part or null if for the given model no EditPart is
	 *         registered
	 */
	public PropertyEditPart getEditPartForModel(PropertyInfo propertyInfo) {
		if (propertyInfo == null) {
			return null;
		}
		return (PropertyEditPart) getEditPartForModel((Object) propertyInfo);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////

	public class PropertyEditDomain extends EditDomain {
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
				select(editPart);
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
					if (editPart.isLocationState(event.x)) {
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
							property.getEditor().doubleClick(property);
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
			}
		}

		@Override
		public void mouseDrag(MouseEvent event, EditPartViewer viewer) {
			// resize splitter
			if (m_splitterResizing) {
				configureSplitter(event.x);
				getControl().redraw();
			}
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
	// PropertyInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Class with information about single {@link Property}.
	 *
	 * @author scheglov_ke
	 */
	public final class PropertyInfo {
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