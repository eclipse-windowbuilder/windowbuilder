/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.utils;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

import org.osgi.framework.Bundle;

import java.util.Map;

/**
 * Factory for creating {@link Object}'s that does not create new instance each time, but returns
 * always same instance, from <code>public static final INSTANCE</code> field.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public final class SingletonExtensionFactory
implements
IExecutableExtension,
IExecutableExtensionFactory {
	private Class<?> m_objectClass;

	////////////////////////////////////////////////////////////////////////////
	//
	// IExecutableExtension
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@SuppressWarnings("unchecked")
	public void setInitializationData(final IConfigurationElement config,
			final String propertyName,
			final Object data) throws CoreException {
		ExecutionUtils.runRethrow(() -> {
			Bundle extensionBundle = ExternalFactoriesHelper.getExtensionBundle(config);
			String objectClassName = ((Map<String, String>) data).get("class");
			m_objectClass = extensionBundle.loadClass(objectClassName);
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IExecutableExtensionFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object create() throws CoreException {
		return ExecutionUtils.runObject(() -> ReflectionUtils.getFieldObject(m_objectClass, "INSTANCE"));
	}
}
