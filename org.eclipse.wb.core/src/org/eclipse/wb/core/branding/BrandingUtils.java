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
package org.eclipse.wb.core.branding;

import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import java.util.List;

/**
 * Helper for {@link IBrandingDescription}s.
 *
 * @author Jaime Wren
 * @coverage core.util
 */
public final class BrandingUtils {
	/**
	 * @return the {@link IBrandingDescription} contributed by product provider, or default one for
	 *         WindowBuilder project.
	 */
	public static IBrandingDescription getBranding() {
		List<IBrandingDescription> instances =
				ExternalFactoriesHelper.getElementsInstances(
						IBrandingDescription.class,
						"org.eclipse.wb.core.branding",
						"branding");
		if (instances.size() != 0) {
			return instances.get(0);
		}
		return DefaultBrandingDescription.INSTANCE;
	}
}
