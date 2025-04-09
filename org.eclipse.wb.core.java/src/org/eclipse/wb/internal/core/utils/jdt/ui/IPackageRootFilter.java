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
package org.eclipse.wb.internal.core.utils.jdt.ui;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * Filter for {@link PackageRootSelectionDialogField}.
 *
 * @author scheglov_ke
 * @coverage core.util.jdt.ui
 */
public interface IPackageRootFilter {
	/**
	 * @return <code>true</code> if given {@link IJavaProject} can be selected.
	 */
	boolean select(IJavaProject javaProject);

	/**
	 * @return <code>true</code> if given {@link IPackageFragmentRoot} can be selected.
	 */
	boolean select(IPackageFragmentRoot packageFragmentRoot);
}
