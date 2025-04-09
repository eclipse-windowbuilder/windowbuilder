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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jface.preference.IPreferenceStore;

import java.util.Map;
import java.util.TreeMap;

/**
 * Support for managing name of {@link AbstractGridBagConstraintsInfo}, so that it corresponds to
 * the name of its parent {@link ComponentInfo}.
 *
 * @author sablin_aa
 * @coverage swing.model.layout
 */
public final class GridBagConstraintsNameSupport
extends
org.eclipse.wb.internal.core.model.layout.LayoutDataNameSupport<AbstractGridBagConstraintsInfo> {
	public final static String[] TEMPLATES = new String[]{
			"${constraintsAcronym}_${componentName}",
			"${constraintsAcronym}${componentName-cap}",
			"${componentName}${constraintsClassName}",
	"${defaultName}"};

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridBagConstraintsNameSupport(AbstractGridBagConstraintsInfo layoutData) {
		super(layoutData);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utilities
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getTemplate() {
		IPreferenceStore preferences = m_childInfo.getDescription().getToolkit().getPreferences();
		String template = preferences.getString(IPreferenceConstants.P_CONSTRAINTS_NAME_TEMPLATE);
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
			valueMap.put("constraintsAcronym", getAcronym());
			valueMap.put("constraintsClassName", getClassName());
			valueMap.put("componentName", getParentName());
			valueMap.put("componentName-cap", getParentNameCap());
		}
		return valueMap;
	}
}