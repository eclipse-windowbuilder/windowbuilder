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
package org.eclipse.wb.internal.xwt.model.widgets;

import org.eclipse.wb.internal.core.model.util.live.ILiveCacheEntry;
import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.clipboard.ComponentInfoMemento;
import org.eclipse.wb.internal.core.xml.model.creation.ILiveCreationSupport;
import org.eclipse.wb.internal.core.xml.model.utils.AbstractLiveManager;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.swt.model.widgets.live.SwtLiveCacheEntry;
import org.eclipse.wb.internal.xwt.model.layout.RowDataInfo;
import org.eclipse.wb.internal.xwt.model.layout.RowLayoutInfo;

import org.eclipse.swt.graphics.Image;

/**
 * Default live components manager implementation for SWT toolkit.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public class XwtLiveManager extends AbstractLiveManager {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XwtLiveManager(WidgetInfo widget) {
		super(widget);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Abstract_LiveManager
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractComponentInfo createLiveComponent() throws Exception {
		// prepare empty Shell
		CompositeInfo shell =
				(CompositeInfo) parse(new String[]{
						"<Shell x:Style='NONE'"
								+ " xmlns='http://www.eclipse.org/xwt/presentation'"
								+ " xmlns:x='http://www.eclipse.org/xwt'>",
								"  <Shell.layout>",
								"    <RowLayout/>",
								"  </Shell.layout>",
				"</Shell>"});
		// prepare widget
		WidgetInfo widget;
		{
			ILiveCreationSupport existing_creationSupport =
					(ILiveCreationSupport) m_component.getCreationSupport();
			widget =
					(WidgetInfo) XmlObjectUtils.createObject(
							shell.getContext(),
							m_component.getDescription().getComponentClass().getName(),
							existing_creationSupport.getLiveComponentCreation());
		}
		// add widget
		addWidget(shell, widget);
		// OK, process this widget
		return widget;
	}

	/**
	 * Adds widget onto parent.
	 */
	protected void addWidget(CompositeInfo shell, WidgetInfo widget) throws Exception {
		ControlInfo control = (ControlInfo) widget;
		RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
		rowLayout.command_CREATE(control, null);
		applyForcedSize(control);
	}

	private void applyForcedSize(ControlInfo control) throws Exception {
		String widthString = XmlObjectUtils.getParameter(m_component, "liveComponent.forcedSize.width");
		String heightString =
				XmlObjectUtils.getParameter(m_component, "liveComponent.forcedSize.height");
		if (widthString != null && heightString != null) {
			RowDataInfo rowData = RowLayoutInfo.getRowData(control);
			rowData.setWidth(Integer.parseInt(widthString));
			rowData.setHeight(Integer.parseInt(heightString));
		}
	}

	@Override
	protected ILiveCacheEntry createComponentCacheEntry(AbstractComponentInfo liveComponentInfo) {
		SwtLiveCacheEntry cacheEntry = new SwtLiveCacheEntry();
		// image
		cacheEntry.setImage(liveComponentInfo.getImage());
		liveComponentInfo.setImage(null);
		// style
		cacheEntry.setStyle(((WidgetInfo) liveComponentInfo).getStyle());
		// baseline
		cacheEntry.setBaseline(liveComponentInfo.getBaseline());
		// OK, we have filled cache entry
		return cacheEntry;
	}

	@Override
	protected ILiveCacheEntry createComponentCacheEntryEx(Throwable e) {
		SwtLiveCacheEntry cacheEntry = new SwtLiveCacheEntry();
		// set image
		{
			Image image = createImageForException(e);
			cacheEntry.setImage(image);
		}
		// done
		return cacheEntry;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Image} of this component.
	 */
	public Image getImage() {
		// get image from memento during paste
		{
			Image image = ComponentInfoMemento.getImage(m_component);
			if (image != null) {
				return image;
			}
		}
		// get from cache
		return ((SwtLiveCacheEntry) getCachedEntry()).getImage();
	}

	/**
	 * @return the style of this component.
	 */
	public int getStyle() {
		return ((SwtLiveCacheEntry) getCachedEntry()).getStyle();
	}

	/**
	 * @return the baseline of this component.
	 */
	public int getBaseline() {
		return ((SwtLiveCacheEntry) getCachedEntry()).getBaseline();
	}
}
