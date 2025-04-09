/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.rcp.databinding.emf;

import org.eclipse.wb.internal.core.BundleResourceProvider;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.BundleContext;

import java.io.InputStream;

/**
 * The activator and utility class controls the EMF Data Bindings plug-in life cycle.
 *
 * @author lobas_av
 */
public class Activator extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.wb.rcp.databinding.emf";
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
		// TODO Auto-generated method stub
		super.start(context);
		m_plugin = this;
	}

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
	// Preferences
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns preference store for DB plugin.
	 */
	public static IPreferenceStore getStore() {
		return m_plugin.getPreferenceStore();
	}
}