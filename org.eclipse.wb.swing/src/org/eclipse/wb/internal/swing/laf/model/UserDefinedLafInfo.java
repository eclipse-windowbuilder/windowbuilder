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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Class representing user-defined Look-n-Feel.
 *
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public class UserDefinedLafInfo extends AbstractCustomLafInfo {
	private Class<?> m_lafClass;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public UserDefinedLafInfo(String name, String className, String jarFile) {
		this("laf_" + System.currentTimeMillis(), name, className, jarFile);
	}

	public UserDefinedLafInfo(String id, String name, String className, String jarFile) {
		super(id, name, className, jarFile);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public LafValue getLookAndFeelInstance() throws Exception {
		if (m_lafClass == null) {
			ClassLoader classLoader = getClassLoader();
			m_lafClass = classLoader.loadClass(getClassName());
		}
		return new LafValue((javax.swing.LookAndFeel) m_lafClass.getDeclaredConstructor().newInstance());
	}

	private ClassLoader getClassLoader() throws Exception {
		File jarFile = new File(getJarFile());
		URL jarURL = jarFile.toURI().toURL();
		// special hack for Substance
		if (jarFile.getName().equals("substance.jar")) {
			URL secondaryJarURL = new File(jarFile.getParentFile(), "trident.jar").toURI().toURL();
			return new URLClassLoader(new URL[]{jarURL, secondaryJarURL});
		}
		// single jar
		return new URLClassLoader(new URL[]{jarURL});
	}
}
