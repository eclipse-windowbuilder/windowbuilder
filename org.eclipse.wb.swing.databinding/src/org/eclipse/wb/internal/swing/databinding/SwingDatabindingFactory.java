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
package org.eclipse.wb.internal.swing.databinding;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingFactory;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.parser.DatabindingParser;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * {@link IDatabindingFactory} factory for support Swing beans bindings API.
 *
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public final class SwingDatabindingFactory implements IDatabindingFactory {
	////////////////////////////////////////////////////////////////////////////
	//
	// IDatabindingFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IDatabindingsProvider createProvider(JavaInfo javaInfoRoot) throws Exception {
		if (isSwingObject(javaInfoRoot)) {
			DatabindingsProvider provider = new DatabindingsProvider(javaInfoRoot);
			DatabindingParser.parse(provider);
			provider.hookJavaInfoEvents();
			return provider;
		}
		return null;
	}

	@Override
	public AbstractUIPlugin getPlugin() {
		return Activator.getDefault();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if given {@link JavaInfo} is Swing/AWT object.
	 */
	private static boolean isSwingObject(JavaInfo javaInfoRoot) {
		return javaInfoRoot.getDescription().getToolkit().getId() == org.eclipse.wb.internal.swing.preferences.IPreferenceConstants.TOOLKIT_ID;
	}
}