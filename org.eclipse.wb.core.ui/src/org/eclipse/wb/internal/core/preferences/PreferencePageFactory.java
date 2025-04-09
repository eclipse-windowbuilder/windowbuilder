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
package org.eclipse.wb.internal.core.preferences;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.utils.dialogfields.StatusUtils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.jface.preference.PreferencePage;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;

/**
 * Factory for creating {@link PreferencePage}'s that accept {@link ToolkitDescription} as single
 * parameter in constructor.
 *
 * @author scheglov_ke
 * @coverage core.preferences.ui
 */
public final class PreferencePageFactory
implements
IExecutableExtension,
IExecutableExtensionFactory {
	private String m_pageClassName;
	private String m_toolkitId;

	////////////////////////////////////////////////////////////////////////////
	//
	// IExecutableExtension
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		try {
			String[] parameters = StringUtils.split((String) data);
			m_pageClassName = parameters[0];
			m_toolkitId = parameters[1];
		} catch (Throwable e) {
			throw new CoreException(StatusUtils.createError(e.getMessage()));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IExecutableExtensionFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object create() throws CoreException {
		try {
			Class<?> pageClass = Class.forName(m_pageClassName);
			Constructor<?> constructor = pageClass.getConstructor(ToolkitDescription.class);
			//
			ToolkitDescription toolkit = DescriptionHelper.getToolkit(m_toolkitId);
			return constructor.newInstance(toolkit);
		} catch (Throwable e) {
			throw new CoreException(StatusUtils.createError(e.getMessage()));
		}
	}
}
