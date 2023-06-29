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
package org.eclipse.wb.internal.core.xml;

import org.eclipse.wb.internal.core.BundleResourceProvider;
import org.eclipse.wb.internal.core.editor.palette.DesignerPalette;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.xml.model.description.ComponentPresentationHelper;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.BundleContext;

import java.io.InputStream;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author scheglov_ke
 * @coverage XML
 */
public class Activator extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.wb.core.xml";
	private static Activator m_plugin;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void stop(BundleContext context) throws Exception {
		m_plugin = null;
		super.stop(context);
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		m_plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static Activator getDefault() {
		return m_plugin;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resources
	//
	////////////////////////////////////////////////////////////////////////////
	private static final BundleResourceProvider m_resourceProvider =
			BundleResourceProvider.get(PLUGIN_ID);

	/**
	 * @return the {@link InputStream} for file from plugin directory.
	 */
	public static InputStream getFile(String path) {
		return m_resourceProvider.getFile(path);
	}

	/**
	 * @return the {@link Image} from "icons" directory, with caching.
	 */
	public static Image getImage(String path) {
		return m_resourceProvider.getImage("icons/" + path);
	}

	/**
	 * @return the {@link ImageDescriptor} from "icons" directory.
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return m_resourceProvider.getImageDescriptor("icons/" + path);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Early start up
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean m_cachesLoaded = false;

	/**
	 * This is fast method which scheduled jobs for pre-loading caches. If jobs where already
	 * scheduled in this Eclipse session, second and following invocations are ignored.
	 */
	public static synchronized void scheduleCachesLoading() {
		if (m_cachesLoaded) {
			return;
		}
		m_cachesLoaded = true;
		// pre-load palette
		if (System.getProperty(DesignerPalette.FLAG_NO_PALETTE) == null) {
			ExecutionUtils.runLog(new RunnableEx() {
				@Override
				public void run() throws Exception {
					ToolkitDescription[] toolkits = DescriptionHelper.getToolkits();
					for (ToolkitDescription toolkitDescription : toolkits) {
						String toolkitId = toolkitDescription.getId();
						ComponentPresentationHelper.scheduleFillingPresentationCache(toolkitId);
					}
				}
			});
		}
	}
}
