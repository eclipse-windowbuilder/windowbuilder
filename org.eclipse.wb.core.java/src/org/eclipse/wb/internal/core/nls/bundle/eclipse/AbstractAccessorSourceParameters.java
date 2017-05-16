/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
