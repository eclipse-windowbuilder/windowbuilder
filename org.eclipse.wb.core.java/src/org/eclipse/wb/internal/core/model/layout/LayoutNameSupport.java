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
package org.eclipse.wb.internal.core.model.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;

import java.util.Map;
import java.util.TreeMap;

/**
 * Support for managing name of <code>Layout</code>, so that it corresponds to the name of its
 * <code>Container</code>.
 *
 * @author sablin_aa
 * @coverage core.model.layout
 */
public abstract class LayoutNameSupport<T extends JavaInfo> extends SyncParentChildVariableNameSupport<T> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutNameSupport(T layout) {
		super(layout);
	}

	/**
	 * @return All supported layout patterns.
	 */
	protected abstract String[] getTemplates();

	@Override
	protected Map<String, String> getValueMap() {
		// prepare variables
		Map<String, String> valueMap = new TreeMap<>();
		{
			valueMap.put("layoutAcronym", getAcronym());
			valueMap.put("layoutClassName", getClassName());
		}
		return valueMap;
	}

	@Override
	protected String getTemplate() {
		IPreferenceStore preferences = m_childInfo.getDescription().getToolkit().getPreferences();
		String template = preferences.getString(IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE);
		if (!isValidTemplate(getTemplates(), template)) {
			template = getTemplateForDefault();
		}
		return template;
	}
}