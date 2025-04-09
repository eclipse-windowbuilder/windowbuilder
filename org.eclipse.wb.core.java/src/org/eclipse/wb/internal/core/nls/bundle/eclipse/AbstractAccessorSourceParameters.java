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
package org.eclipse.wb.internal.core.nls.bundle.eclipse;

import org.eclipse.wb.internal.core.nls.bundle.AbstractBundleSourceParameters;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * Value object for creating new strings source.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public class AbstractAccessorSourceParameters extends AbstractBundleSourceParameters {
	////////////////////////////////////////////////////////////////////////////
	//
	// Accessor parameters
	//
	////////////////////////////////////////////////////////////////////////////
	public IPackageFragmentRoot m_accessorSourceFolder;
	public IPackageFragment m_accessorPackage;
	public String m_accessorPackageName;
	public String m_accessorClassName;
	public String m_accessorFullClassName;
	public boolean m_accessorExists;
}
