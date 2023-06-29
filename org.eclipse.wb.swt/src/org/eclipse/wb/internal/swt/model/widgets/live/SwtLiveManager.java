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
package org.eclipse.wb.internal.swt.model.widgets.live;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ComponentInfoMemento;
import org.eclipse.wb.internal.core.model.util.live.AbstractLiveManager;
import org.eclipse.wb.internal.core.model.util.live.ILiveCacheEntry;
import org.eclipse.wb.internal.core.model.variable.EmptyPureVariableSupport;
import org.eclipse.wb.internal.swt.model.layout.RowDataInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.WidgetInfo;

import org.eclipse.swt.graphics.Image;

/**
 * Default live components manager implementation for SWT toolkit.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage swt.model.widgets.live
 */
public class SwtLiveManager extends AbstractLiveManager {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SwtLiveManager(AbstractComponentInfo component) {
		super(component);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractLiveComponentsManager
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractComponentInfo createLiveComponent() throws Exception {
		// prepare empty Shell
		CompositeInfo shell;
		{
			String[] sourceLines =
					new String[]{
							"  org.eclipse.swt.widgets.Shell __wbp_shell = "
									+ "new org.eclipse.swt.widgets.Shell(org.eclipse.swt.SWT.NONE);",
			"  __wbp_shell.setLayout(new org.eclipse.swt.layout.RowLayout());"};
			shell = (CompositeInfo) parse(sourceLines);
		}
		// prepare component
		WidgetInfo widget = createClone();
		widget.setVariableSupport(new EmptyPureVariableSupport(m_component));
		// add component
		addWidget(shell, widget);
		// apply forced size
		{
			String forcedWidthString =
					JavaInfoUtils.getParameter(m_component, "liveComponent.forcedSize.width");
			String forcedHeightString =
					JavaInfoUtils.getParameter(m_component, "liveComponent.forcedSize.height");
			if (forcedWidthString != null && forcedHeightString != null) {
				RowDataInfo rowData = RowLayoutInfo.getRowData((ControlInfo) widget);
				rowData.setWidth(Integer.parseInt(forcedWidthString));
				rowData.setHeight(Integer.parseInt(forcedHeightString));
			}
		}
		// OK, process this widget
		return widget;
	}

	/**
	 * Adds widget onto parent.
	 */
	protected void addWidget(CompositeInfo shell, WidgetInfo widget) throws Exception {
		RowLayoutInfo rowLayoutInfo = (RowLayoutInfo) shell.getLayout();
		rowLayoutInfo.command_CREATE((ControlInfo) widget, null);
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
