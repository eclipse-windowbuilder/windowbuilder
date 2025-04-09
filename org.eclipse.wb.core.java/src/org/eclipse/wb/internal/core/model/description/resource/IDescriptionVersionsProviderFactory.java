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

import org.eclipse.jdt.core.IJavaProject;

import java.util.Map;

/**
 * Factory for creating {@link IDescriptionVersionsProvider} depending on version of toolkit or
 * library used in {@link IJavaProject}. It should be implemented for each toolkit or library for
 * which we support multiple versions.
 *
 * @see IDescriptionVersionsProvider
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public interface IDescriptionVersionsProviderFactory {
	/**
	 * @return the {@link Map} with versions, for example <code>rcp.version -> 3.2</code> or
	 *         <code>gwt_version -> 1.5</code>.
	 */
	Map<String, Object> getVersions(IJavaProject javaProject, ClassLoader classLoader)
			throws Exception;

	/**
	 * @param javaProject
	 *          the {@link IJavaProject} that can be used to detect version of toolkit/library.
	 * @param classLoader
	 *          the editor {@link ClassLoader} that can be used to detect version of toolkit/library.
	 *
	 * @return the {@link IDescriptionVersionsProvider} for toolkit or library that is supported by
	 *         this {@link IDescriptionVersionsProviderFactory}, may be <code>null</code>.
	 */
	IDescriptionVersionsProvider getProvider(IJavaProject javaProject, ClassLoader classLoader)
			throws Exception;
}