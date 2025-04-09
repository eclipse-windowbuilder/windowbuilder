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

import java.util.Collections;
import java.util.List;

/**
 * {@link IDescriptionVersionsProvider} that does not return any version. It can be used for example
 * when {@link IJavaProject} does not include supported toolkit/library.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class EmptyDescriptionVersionsProvider implements IDescriptionVersionsProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IDescriptionVersionsProvider INSTANCE =
			new EmptyDescriptionVersionsProvider();

	private EmptyDescriptionVersionsProvider() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDescriptionVersionsProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<String> getVersions(Class<?> componentClass) throws Exception {
		return Collections.emptyList();
	}
}