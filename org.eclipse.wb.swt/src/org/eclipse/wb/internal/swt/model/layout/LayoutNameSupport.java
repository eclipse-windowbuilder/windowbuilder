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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import java.util.Map;

/**
 * Support for managing name of {@link LayoutInfo}, so that it corresponds to the name of its parent
 * {@link CompositeInfo}.
 *
 * @author sablin_aa
 * @coverage swt.model.layout
 */
public final class LayoutNameSupport extends org.eclipse.wb.internal.core.model.layout.LayoutNameSupport<LayoutInfo> {
	public final static String[] TEMPLATES = new String[] { //
			"${layoutAcronym}_${compositeName}", //
			"${layoutAcronym}${compositeName-cap}", //
			"${compositeName}${layoutClassName}", //
			"${defaultName}" };

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutNameSupport(LayoutInfo layout) {
		super(layout);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utilities
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String[] getTemplates() {
		return TEMPLATES;
	}

	@Override
	protected Map<String, String> getValueMap() {
		// prepare variables
		Map<String, String> valueMap = super.getValueMap();
		{
			valueMap.put("compositeName", getParentName());
			valueMap.put("compositeName-cap", getParentNameCap());
		}
		return valueMap;
	}
}