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
package org.eclipse.wb.internal.core.nls.bundle;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * Value object for creating new source for bundle-based sources.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public abstract class AbstractBundleSourceParameters {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Bundle parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  public IPackageFragmentRoot m_propertySourceFolder;
  public IPackageFragment m_propertyPackage;
  public String m_propertyFileName;
  public String m_propertyBundleName;
  public boolean m_propertyFileExists;
}
