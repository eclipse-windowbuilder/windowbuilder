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

import org.eclipse.wb.internal.core.utils.check.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link IDescriptionVersionsProvider} that returns versions from {@link List}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public abstract class FromListDescriptionVersionsProvider implements IDescriptionVersionsProvider {
	private final List<String> m_versions = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FromListDescriptionVersionsProvider(List<String> allVersions, String currentVersion) {
		int currentVersionIndex = allVersions.indexOf(currentVersion);
		Assert.isTrue(
				currentVersionIndex != -1,
				"Version %s is not present in %s",
				currentVersion,
				allVersions);
		// add versions from current to earlier versions
		for (String version : allVersions) {
			m_versions.add(0, version);
			if (version.equals(currentVersion)) {
				break;
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDescriptionVersionsProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<String> getVersions(Class<?> componentClass) throws Exception {
		if (validate(componentClass)) {
			return m_versions;
		}
		return Collections.emptyList();
	}

	protected abstract boolean validate(Class<?> componentClass) throws Exception;
}