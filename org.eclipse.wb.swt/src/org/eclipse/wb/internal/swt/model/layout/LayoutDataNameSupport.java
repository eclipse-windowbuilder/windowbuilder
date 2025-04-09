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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;

import java.util.Map;
import java.util.TreeMap;

/**
 * Support for managing name of {@link LayoutDataInfo}, so that it corresponds to the name of its
 * parent {@link ControlInfo}.
 *
 * @author sablin_aa
 * @coverage swt.model.layout
 */
public final class LayoutDataNameSupport
extends
org.eclipse.wb.internal.core.model.layout.LayoutDataNameSupport<LayoutDataInfo> {
	public final static String[] TEMPLATES = new String[]{
			"${dataAcronym}_${controlName}",
			"${dataAcronym}${controlName-cap}",
			"${controlName}${dataClassName}",
	"${defaultName}"};

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutDataNameSupport(LayoutDataInfo layoutData) {
		super(layoutData);
	}

	@Override
	protected String getTemplate() {
		IPreferenceStore preferences = m_childInfo.getDescription().getToolkit().getPreferences();
		String template = preferences.getString(IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE);
		if (!isValidTemplate(TEMPLATES, template)) {
			template = getTemplateForDefault();
		}
		return template;
	}

	@Override
	protected Map<String, String> getValueMap() {
		// prepare variables
		Map<String, String> valueMap = new TreeMap<>();
		{
			valueMap.put("dataAcronym", getAcronym());
			valueMap.put("dataClassName", getClassName());
			valueMap.put("controlName", getParentName());
			valueMap.put("controlName-cap", getParentNameCap());
		}
		return valueMap;
	}
}