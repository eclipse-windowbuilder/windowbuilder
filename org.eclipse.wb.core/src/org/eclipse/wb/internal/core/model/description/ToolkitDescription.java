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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.jface.preference.IPreferenceStore;

import org.osgi.framework.Bundle;

/**
 * Description of GUI toolkit.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public abstract class ToolkitDescription {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the ID of toolkit.
	 */
	public abstract String getId();
	/**
	 * @return the name of toolkit.
	 */
	public abstract String getName();
	/**
	 * @return the name of toolkit.
	 */
	public abstract String getProductName();
	/**
	 * @return the {@link Bundle} that provides this toolkit.
	 */
	public abstract Bundle getBundle();
	/**
	 * @return the {@link IPreferenceStore} for this toolkit.
	 */
	public abstract IPreferenceStore getPreferences();
}
