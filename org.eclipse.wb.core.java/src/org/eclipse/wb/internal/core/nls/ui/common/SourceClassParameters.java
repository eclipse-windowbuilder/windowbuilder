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
package org.eclipse.wb.internal.core.nls.ui.common;

import org.eclipse.wb.internal.core.nls.ui.common.AbstractFieldsSourceNewComposite.ClassSelectionGroup;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * Value object for class parameters selected using {@link ClassSelectionGroup}.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public class SourceClassParameters {
	public IPackageFragmentRoot m_sourceFolder;
	public IPackageFragment m_package;
	public IFolder m_packageFolder;
	public String m_packageName;
	public String m_className;
	public String m_fullClassName;
	public boolean m_exists;
}
