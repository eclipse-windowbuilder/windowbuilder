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
package org.eclipse.wb.internal.core.model.description.resource;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;

import org.osgi.framework.Bundle;

import java.net.URL;

/**
 * Information about single resource file.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ResourceInfo {
	private final Bundle m_bundle;
	private final ToolkitDescription m_toolkit;
	private final URL m_url;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ResourceInfo(Bundle bundle, ToolkitDescription toolkit, URL url) {
		m_bundle = bundle;
		m_toolkit = toolkit;
		m_url = url;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Bundle} that contains this {@link ResourceInfo}.
	 */
	public Bundle getBundle() {
		return m_bundle;
	}

	/**
	 * @return the {@link ToolkitDescription} of {@link Bundle}.
	 */
	public ToolkitDescription getToolkit() {
		return m_toolkit;
	}

	/**
	 * @return the {@link URL} with reference on this {@link ResourceInfo}.
	 */
	public URL getURL() {
		return m_url;
	}
}