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
package org.eclipse.wb.internal.core.utils.platform;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import java.net.URL;

/**
 * The class <code>PluginUtilities</code> defines utility methods for working with plug-ins.
 *
 * @author Brian Wilkerson
 * @version $Revision$
 */
public class PluginUtilities {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Prevent the creation of instances of this class.
	 */
	private PluginUtilities() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// General Accessing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Return the unique identifier of the given plug-in.
	 *
	 * @return the unique identifier of the given plug-in
	 */
	public static String getId(Plugin plugin) {
		return plugin.getBundle().getSymbolicName();
	}

	/**
	 * Return the name of the given plug-in. If the plug-in does not have a name, return the unique
	 * identifier for the plug-in instead.
	 *
	 * @return the name of the given plug-in
	 */
	public static String getName(Plugin plugin) {
		String label;
		Object bundleName;
		label = null;
		bundleName = plugin.getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_NAME);
		if (bundleName instanceof String) {
			label = (String) bundleName;
		}
		if (label == null || label.trim().length() == 0) {
			return getId(plugin);
		}
		return label;
	}

	/**
	 * Return the version identifier associated with the plug-in with the given identifier, or
	 * <code>null</code> if there is no such plug-in.
	 *
	 * @param pluginId
	 *          the identifier of the plug-in
	 *
	 * @return the version identifier for the specified plug-in
	 */
	public static Version getVersion(String pluginId) {
		Bundle bundle;
		String version;
		bundle = Platform.getBundle(pluginId);
		if (bundle == null) {
			return null;
		}
		version = bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
		return new Version(version);
	}

	/**
	 * Return the version identifier associated with the plug-in with the given identifier, or
	 * <code>null</code> if there is no such plug-in.
	 *
	 * @param pluginId
	 *          the identifier of the plug-in
	 *
	 * @return the version identifier for the specified plug-in
	 */
	public static Version getVersion(Plugin plugin) {
		String version;
		if (plugin == null) {
			return null;
		}
		version = plugin.getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
		return new Version(version);
	}

	/**
	 * Return the version identifier associated with the plug-in with the given identifier, or
	 * <code>null</code> if there is no such plug-in.
	 *
	 * @param pluginId
	 *          the identifier of the plug-in
	 *
	 * @return the version identifier for the specified plug-in
	 */
	public static String getVersionString(String pluginId) {
		Bundle bundle;
		bundle = Platform.getBundle(pluginId);
		if (bundle == null) {
			return null;
		}
		return bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
	}

	/**
	 * Return the version identifier associated with the plug-in with the given identifier, or
	 * <code>null</code> if there is no such plug-in.
	 *
	 * @param pluginId
	 *          the identifier of the plug-in
	 *
	 * @return the version identifier for the specified plug-in
	 */
	public static String getVersionString(Plugin plugin) {
		if (plugin == null) {
			return null;
		}
		return plugin.getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// File Accessing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Return an URL representing the installation directory of the plug-in with the given identifier,
	 * or <code>null</code> if there is no plug-in with the given identifier.
	 *
	 * @param pluginId
	 *          the identifier of the plug-in
	 *
	 * @return the specified plug-in's installation directory
	 */
	public static URL getInstallUrl(String pluginId) {
		Bundle bundle;
		bundle = Platform.getBundle(pluginId);
		if (bundle == null) {
			return null;
		}
		return bundle.getEntry("/");
	}

	/**
	 * Return an URL representing the given plug-in's installation directory.
	 *
	 * @param plugin
	 *          the plug-in
	 *
	 * @return the given plug-in's installation directory
	 */
	public static URL getInstallUrl(Plugin plugin) {
		if (plugin == null) {
			return null;
		}
		return plugin.getBundle().getEntry("/");
	}

	/**
	 * Return an URL for the file located within the installation directory of the plug-in that has
	 * the given identifier that has the given relative path.
	 *
	 * @param pluginId
	 *          the identifier for the plug-in
	 * @param relativePath
	 *          the relative path of the file within the installation directory
	 *
	 * @return the URL for the specified file
	 */
	public static URL getUrl(String pluginId, String relativePath) {
		Bundle bundle;
		if (pluginId == null || relativePath == null) {
			return null;
		}
		bundle = Platform.getBundle(pluginId);
		if (bundle != null) {
			return bundle.getEntry(relativePath);
		}
		return null;
	}

	/**
	 * Return an URL for the file located within the installation directory of the given plug-in that
	 * has the given relative path.
	 *
	 * @param pluginId
	 *          the identifier for the plug-in
	 * @param relativePath
	 *          the relative path of the file within the installation directory
	 *
	 * @return the URL for the specified file
	 */
	public static URL getUrl(Plugin plugin, String relativePath) {
		if (plugin == null || relativePath == null) {
			return null;
		}
		return plugin.getBundle().getEntry(relativePath);
	}
}