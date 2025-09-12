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
package org.eclipse.wb.internal.swing;

import org.eclipse.wb.internal.core.BundleResourceProvider;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.BundleContext;

import java.io.InputStream;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author scheglov_ke
 * @coverage swing
 */
public final class Activator extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.wb.swing";
	private static final Class<?> APPLET_CLASS = ExecutionUtils
			.runObjectIgnore(() -> Activator.class.getClassLoader().loadClass("java.applet.Applet"), null);
	private static final Class<?> JAPPLET_CLASS = ExecutionUtils
			.runObjectIgnore(() -> Activator.class.getClassLoader().loadClass("javax.swing.JApplet"), null);
	private static Activator m_plugin;

	////////////////////////////////////////////////////////////////////////////
	//
	// Bundle operations
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		m_plugin = this;
		System.setProperty("javax.swing.adjustPopupLocationToFit", "false");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		m_plugin = null;
		super.stop(context);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the instance of {@link Activator}.
	 */
	public static Activator getDefault() {
		return m_plugin;
	}

	/**
	 * @return {@code true}, if {@code clazz} extends {@link java.applet.Applet
	 *         Applet}.
	 */
	public static boolean isAssignableFromApplet(Class<?> clazz) {
		if (APPLET_CLASS == null) {
			return false;
		}
		return APPLET_CLASS.isAssignableFrom(clazz);
	}

	/**
	 * @return {@code true}, if {@code clazz} extends {@link javax.swing.JApplet
	 *         JApplet}.
	 */
	public static boolean isAssignableFromJApplet(Class<?> clazz) {
		if (JAPPLET_CLASS == null) {
			return false;
		}
		return JAPPLET_CLASS.isAssignableFrom(clazz);
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
}
