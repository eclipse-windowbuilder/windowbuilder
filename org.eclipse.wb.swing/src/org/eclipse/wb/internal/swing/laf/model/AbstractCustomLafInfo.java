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
package org.eclipse.wb.internal.swing.laf.model;

/**
 * Represents custom (external from plugin or user-defined) look-n-feel.
 *
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public abstract class AbstractCustomLafInfo extends LafInfo {
	private String m_jarFile;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractCustomLafInfo(String id, String name, String className, String jarFile) {
		super(id, name, className);
		m_jarFile = jarFile;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the full path to jar-file containing this LookAndFeel.
	 */
	public final String getJarFile() {
		return m_jarFile;
	}

	/**
	 * Sets the full path to jar-file containing this LookAndFeel.
	 */
	public void setJarFile(String jarFile) {
		m_jarFile = jarFile;
	}
}
