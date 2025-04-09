/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.swing.FormLayout.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.BundleLibraryInfo;

import org.eclipse.jdt.core.IJavaProject;

import com.jgoodies.forms.factories.DefaultComponentFactory;

/**
 * {@link EntryInfo} for factory methods from {@link DefaultComponentFactory}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public abstract class DefaultComponentFactoryEntryInfo extends ToolEntryInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Ensures that jar/source with {@link DefaultComponentFactory} is added to {@link IJavaProject}.
	 */
	protected final void ensureLibrary() throws Exception {
			new BundleLibraryInfo("com.jgoodies.common", "com.jgoodies.common.base.Objects") //
					.ensure(m_javaProject);
			new BundleLibraryInfo("com.jgoodies.forms", "com.jgoodies.forms.layout.FormLayout") //
					.ensure(m_javaProject);
	}
}
