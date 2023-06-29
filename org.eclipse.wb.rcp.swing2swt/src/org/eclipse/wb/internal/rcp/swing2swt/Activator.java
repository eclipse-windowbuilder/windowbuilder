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
package org.eclipse.wb.internal.rcp.swing2swt;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author scheglov_ke
 * @coverage rcp.swing2swt
 */
public class Activator extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.wb.rcp.swing2swt";
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
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return m_plugin;
	}

	/**
	 * @return this {@link Bundle}, can be used even without starting this plugin.
	 */
	public static Bundle getBundleStatic() {
		return Platform.getBundle(PLUGIN_ID);
	}
}
