/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.preferences;

import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;

import org.eclipse.jface.resource.LocalResourceManager;

/**
 * Contains various preference constants for SWT.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage swt.preferences
 */
public interface IPreferenceConstants extends org.eclipse.wb.internal.core.preferences.IPreferenceConstants {
	////////////////////////////////////////////////////////////////////////////
	//
	// Preferences
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * When <code>true</code>, we automatically add {@link LocalResourceManager} and
	 * use it for color/font/image access. This allows use resources sharing.
	 */
	String P_USE_RESOURCE_MANAGER = "useResourceManager";
	/**
	 * The template for {@link LayoutDataInfo} name.
	 */
	String P_LAYOUT_DATA_NAME_TEMPLATE = "templateLayoutDataName";
}