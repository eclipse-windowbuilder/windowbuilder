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
package org.eclipse.wb.internal.core.utils.reflect;

import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.core.runtime.Platform;

import org.osgi.framework.Bundle;

/**
 * Implementation of {@link ClassLoader} for loading classes from OSGi {@link Bundle}.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public final class BundleClassLoader extends ClassLoader {
  private final Bundle m_bundle;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BundleClassLoader(Bundle bundle) {
    Assert.isNotNull(bundle);
    m_bundle = bundle;
  }

  public BundleClassLoader(String bundleId) {
    Bundle bundle = Platform.getBundle(bundleId);
    Assert.isNotNull(bundle, "Unable to find Bundle %s", bundleId);
    m_bundle = bundle;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    return m_bundle.loadClass(name);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Factory
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ClassLoader} for {@link Bundle}.
   */
  public static ClassLoader create(final Bundle bundle) {
    return new BundleClassLoader(bundle);
  }
}
