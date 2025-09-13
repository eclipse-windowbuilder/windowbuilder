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
package org.eclipse.wb.internal.swing.laf.model;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.laf.external.ILookAndFeelInitializer;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;

import org.osgi.framework.Bundle;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

/**
 * Class representing look-n-feel loaded from using Eclipse plugin API.
 *
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public class PluginLafInfo extends AbstractCustomLafInfo {
	private ILookAndFeelInitializer m_initializer = ILookAndFeelInitializer.DEFAULT;
	private final Bundle m_extensionBundle;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PluginLafInfo(final IConfigurationElement element) {
		super(ExternalFactoriesHelper.getRequiredAttribute(element, "id"),
				ExternalFactoriesHelper.getRequiredAttribute(element, "name"),
				ExternalFactoriesHelper.getRequiredAttribute(element, "class"),
				resolveJarFile(
						ExternalFactoriesHelper.getRequiredAttribute(element, "jarFile"),
						ExternalFactoriesHelper.getExtensionBundle(element)));
		m_extensionBundle = ExternalFactoriesHelper.getExtensionBundle(element);
		String initializerValue = element.getAttribute("initializer");
		if (initializerValue != null) {
			ExecutionUtils.runLog(new RunnableEx() {
				@Override
				public void run() throws Exception {
					m_initializer =
							(ILookAndFeelInitializer) element.createExecutableExtension("initializer");
				}
			});
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	private Reference<javax.swing.LookAndFeel> m_instanceReference;

	@Override
	public LafValue getLookAndFeelInstance() throws Exception {
		javax.swing.LookAndFeel laf = null;
		if (m_instanceReference != null) {
			laf = m_instanceReference.get();
		}
		if (laf == null) {
			m_initializer.initialize();
			Class<?> lafClass = m_extensionBundle.loadClass(getClassName());
			laf = (javax.swing.LookAndFeel) lafClass.getDeclaredConstructor().newInstance();
			m_instanceReference = new SoftReference<>(laf);
		}
		return new LafValue(laf);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the full path to JAR file.
	 */
	private static String resolveJarFile(String jarFile, Bundle extensionBundle) {
		try {
			return FileLocator.toFileURL(extensionBundle.getEntry(jarFile)).getPath();
		} catch (Throwable e) {
			throw ReflectionUtils.propagate(e);
		}
	}
}
