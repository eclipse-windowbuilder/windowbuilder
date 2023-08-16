/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.xwt.model.forms.layout.table;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.editor.DesignContextMenuProvider;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.ITableWrapDataInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutImages;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutSupport;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.actions.SetAlignmentAction;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.actions.SetGrabAction;
import org.eclipse.wb.internal.xwt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.forms.widgets.TableWrapData;

import java.util.List;

/**
 * Model for {@link TableWrapData}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.forms
 */
public final class TableWrapDataInfo extends LayoutDataInfo implements ITableWrapDataInfo {
	private boolean m_initialized;
	int x = -1;
	int y = -1;
	int width = 1;
	int height = 1;
	boolean horizontalGrab;
	boolean verticalGrab;
	int horizontalAlignment;
	int verticalAlignment;
	int heightHint;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TableWrapDataInfo(EditorContext context,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(context, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initialize() throws Exception {
		super.initialize();
		// events
		addBroadcastListener(new ObjectEventListener() {
			@Override
			public void addContextMenu(List<? extends ObjectInfo> objects,
					ObjectInfo object,
					IMenuManager manager) throws Exception {
				if (getParent() == object) {
					TableWrapDataInfo.this.addContextMenu(manager);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		// prepare x/y
		getCurrentObjectCell(getLayout(), getControl());
		// prepare values from TableWrapData
		getCurrentObjectFields();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Initializes fields of this {@link TableWrapDataInfo} using object of given
	 * {@link TableWrapLayoutInfo} and {@link ControlInfo}.
	 */
	void initialize(TableWrapLayoutInfo layout, ControlInfo controlInfo) throws Exception {
		if (!m_initialized) {
			m_initialized = true;
			// prepare x/y
			getCurrentObjectCell(layout, controlInfo);
			// prepare values from TableWrapData
			getCurrentObjectFields();
		}
	}

	/**
	 * Gets cell using objects of given {@link TableWrapLayoutInfo} and {@link ControlInfo}.
	 */
	private void getCurrentObjectCell(TableWrapLayoutInfo layout, ControlInfo controlInfo)
			throws Exception {
		if (layout.getObject() != null && controlInfo.getObject() != null) {
			Point xyPoint = TableWrapLayoutSupport.getXY(layout.getObject(), controlInfo.getObject());
			if (xyPoint != null) {
				x = xyPoint.x;
				y = xyPoint.y;
			}
		}
	}

	/**
	 * Gets values from {@link TableWrapData} object to this {@link TableWrapDataInfo} fields.
	 */
	private void getCurrentObjectFields() throws Exception {
		Object object = getObject();
		width = ReflectionUtils.getFieldInt(object, "colspan");
		height = ReflectionUtils.getFieldInt(object, "rowspan");
		horizontalGrab = ReflectionUtils.getFieldBoolean(object, "grabHorizontal");
		verticalGrab = ReflectionUtils.getFieldBoolean(object, "grabVertical");
		horizontalAlignment = ReflectionUtils.getFieldInt(object, "align");
		verticalAlignment = ReflectionUtils.getFieldInt(object, "valign");
		heightHint = ReflectionUtils.getFieldInt(object, "heightHint");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal access
	//
	////////////////////////////////////////////////////////////////////////////
	private ControlInfo getControl() {
		return (ControlInfo) getParent();
	}

	private TableWrapLayoutInfo getLayout() {
		CompositeInfo composite = (CompositeInfo) getControl().getParent();
		return (TableWrapLayoutInfo) composite.getLayout();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Location
	//
	////////////////////////////////////////////////////////////////////////////
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Span
	//
	////////////////////////////////////////////////////////////////////////////
	public int getHorizontalSpan() {
		return width;
	}

	public void setHorizontalSpan(int width) throws Exception {
		if (this.width != width) {
			this.width = width;
			getPropertyByTitle("colspan").setValue(width);
		}
	}

	public int getVerticalSpan() {
		return height;
	}

	public void setVerticalSpan(int height) throws Exception {
		if (this.height != height) {
			this.height = height;
			getPropertyByTitle("rowspan").setValue(height);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Grab
	//
	////////////////////////////////////////////////////////////////////////////
	public boolean getHorizontalGrab() {
		return horizontalGrab;
	}

	public void setHorizontalGrab(boolean grab) throws Exception {
		if (horizontalGrab != grab) {
			horizontalGrab = grab;
			getPropertyByTitle("grabHorizontal").setValue(grab);
		}
	}

	public boolean getVerticalGrab() {
		return verticalGrab;
	}

	public void setVerticalGrab(boolean grab) throws Exception {
		if (verticalGrab != grab) {
			verticalGrab = grab;
			getPropertyByTitle("grabVertical").setValue(grab);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment
	//
	////////////////////////////////////////////////////////////////////////////
	public int getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public void setHorizontalAlignment(int alignment) throws Exception {
		if (horizontalAlignment != alignment) {
			horizontalAlignment = alignment;
			setEnumProperty("align", alignment);
		}
	}

	public int getVerticalAlignment() {
		return verticalAlignment;
	}

	public void setVerticalAlignment(int alignment) throws Exception {
		if (verticalAlignment != alignment) {
			verticalAlignment = alignment;
			setEnumProperty("valign", alignment);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Hint
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return value of <code>heightHint</code> property.
	 */
	public int getHeightHint() {
		return heightHint;
	}

	/**
	 * Sets value of <code>heightHint</code> property.
	 */
	public void setHeightHint(int heightHint) throws Exception {
		if (this.heightHint != heightHint) {
			this.heightHint = heightHint;
			getPropertyByTitle("heightHint").setValue(heightHint);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets value for {@link Property} with {@link StaticFieldPropertyEditor}.
	 */
	private void setEnumProperty(String propertyTitle, int value) throws Exception {
		Property property = getPropertyByTitle(propertyTitle);
		property.setValue(value);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Images
	//
	////////////////////////////////////////////////////////////////////////////
	public ImageDescriptor getSmallAlignmentImage(boolean horizontal) {
		if (horizontal) {
			switch (horizontalAlignment) {
			case TableWrapData.LEFT :
				return TableWrapLayoutImages.getImageDescriptor("h/left.gif");
			case TableWrapData.CENTER :
				return TableWrapLayoutImages.getImageDescriptor("h/center.gif");
			case TableWrapData.RIGHT :
				return TableWrapLayoutImages.getImageDescriptor("h/right.gif");
			default :
				Assert.isTrue(horizontalAlignment == TableWrapData.FILL);
				return TableWrapLayoutImages.getImageDescriptor("h/fill.gif");
			}
		} else {
			switch (verticalAlignment) {
			case TableWrapData.TOP :
				return TableWrapLayoutImages.getImageDescriptor("v/top.gif");
			case TableWrapData.MIDDLE :
				return TableWrapLayoutImages.getImageDescriptor("v/middle.gif");
			case TableWrapData.BOTTOM :
				return TableWrapLayoutImages.getImageDescriptor("v/bottom.gif");
			default :
				Assert.isTrue(verticalAlignment == TableWrapData.FILL);
				return TableWrapLayoutImages.getImageDescriptor("v/fill.gif");
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds items to the context {@link IMenuManager}.
	 */
	public void addContextMenu(IMenuManager manager) {
		// horizontal
		{
			IMenuManager manager2 = new MenuManager("Horizontal alignment");
			manager.appendToGroup(DesignContextMenuProvider.GROUP_TOP, manager2);
			//
			manager2.add(new SetGrabAction(this, "&Grab excess space", "grow.gif", true));
			manager2.add(new Separator());
			//
			fillHorizontalAlignmentMenu(manager2);
		}
		// vertical
		{
			IMenuManager manager2 = new MenuManager("Vertical alignment");
			manager.appendToGroup(DesignContextMenuProvider.GROUP_TOP, manager2);
			//
			manager2.add(new SetGrabAction(this, "&Grab excess space", "grow.gif", false));
			manager2.add(new Separator());
			//
			fillVerticalAlignmentMenu(manager2);
		}
	}

	public void fillHorizontalAlignmentMenu(IMenuManager manager) {
		manager.add(new SetAlignmentAction(this, "&Left", "left.gif", true, TableWrapData.LEFT));
		manager.add(new SetAlignmentAction(this, "&Center", "center.gif", true, TableWrapData.CENTER));
		manager.add(new SetAlignmentAction(this, "&Right", "right.gif", true, TableWrapData.RIGHT));
		manager.add(new SetAlignmentAction(this, "&Fill", "fill.gif", true, TableWrapData.FILL));
	}

	public void fillVerticalAlignmentMenu(IMenuManager manager) {
		manager.add(new SetAlignmentAction(this, "&Top", "top.gif", false, TableWrapData.TOP));
		manager.add(new SetAlignmentAction(this, "&Middle", "middle.gif", false, TableWrapData.MIDDLE));
		manager.add(new SetAlignmentAction(this, "&Bottom", "bottom.gif", false, TableWrapData.BOTTOM));
		manager.add(new SetAlignmentAction(this, "&Fill", "fill.gif", false, TableWrapData.FILL));
	}
}