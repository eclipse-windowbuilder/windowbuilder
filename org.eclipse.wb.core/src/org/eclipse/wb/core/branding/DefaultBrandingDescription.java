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

/**
 * The {@link IBrandingDescription} used if there are no branding extensions.
 *
 * @see BrandingUtils
 *
 * @author Jaime Wren
 * @coverage core.util
 */
final class DefaultBrandingDescription extends AbstractBrandingDescription {
	////////////////////////////////////////////////////////////////////////////
	//
	// Support info
	//
	////////////////////////////////////////////////////////////////////////////
	private static final IBrandingSupportInfo SUPPORT_INFO = new IBrandingSupportInfo() {
		@Override
		public String getBugtrackingUrl() {
			return "https://github.com/eclipse-windowbuilder/windowbuilder/issues";
		}

		@Override
		public String getForumUrl() {
			return "https://github.com/eclipse-windowbuilder/windowbuilder/discussions";
		}
	};
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static IBrandingDescription INSTANCE = new DefaultBrandingDescription();

	private DefaultBrandingDescription() {
		super("WindowBuilder", SUPPORT_INFO);
	}
}
