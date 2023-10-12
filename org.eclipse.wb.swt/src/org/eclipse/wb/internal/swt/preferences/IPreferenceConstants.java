/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.preferences;

import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;

import org.eclipse.jface.resource.LocalResourceManager;

/**
 * Contains various preference constants for SWT.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage swt.preferences
 */
public interface IPreferenceConstants {
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
	 * The template for {@link LayoutInfo} name.
	 */
	String P_LAYOUT_NAME_TEMPLATE = "templateLayoutName";
	/**
	 * The template for {@link LayoutDataInfo} name.
	 */
	String P_LAYOUT_DATA_NAME_TEMPLATE = "templateLayoutDataName";
}