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
package org.eclipse.wb.internal.rcp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.internal.core.model.description.resource.FromListDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProviderFactory;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.SWT;

import java.util.List;
import java.util.Map;

/**
 * {@link IDescriptionVersionsProviderFactory} for RCP.
 * 
 * @author scheglov_ke
 * @coverage rcp
 */
public final class RcpDescriptionVersionsProviderFactory
    implements
      IDescriptionVersionsProviderFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IDescriptionVersionsProviderFactory INSTANCE =
      new RcpDescriptionVersionsProviderFactory();

  private RcpDescriptionVersionsProviderFactory() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDescriptionVersionsProviderFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public Map<String, Object> getVersions(IJavaProject javaProject, ClassLoader classLoader)
      throws Exception {
    if (!isRCP(javaProject)) {
      return ImmutableMap.of();
    }
    // OK, RCP project
    String version = getSWTVersion();
    return ImmutableMap.<String, Object>of("rcp_version", version);
  }

  public IDescriptionVersionsProvider getProvider(IJavaProject javaProject, ClassLoader classLoader)
      throws Exception {
    if (!isRCP(javaProject)) {
      return null;
    }
    // OK, RCP project
    String version = getSWTVersion();
    List<String> allVersions =
        ImmutableList.of("3.2", "3.3", "3.4", "3.5", "3.6", "3.7", "3.8", "4.2");
    return new FromListDescriptionVersionsProvider(allVersions, version) {
      @Override
      protected boolean validate(Class<?> componentClass) throws Exception {
        String className = componentClass.getName();
        return className.startsWith("org.eclipse.swt.")
            || className.startsWith("org.eclipse.jface.")
            || className.startsWith("org.eclipse.ui.");
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean isRCP(IJavaProject javaProject) throws JavaModelException {
    return javaProject.findType("org.eclipse.swt.custom.CTabFolder") != null;
  }

  private static String getSWTVersion() {
    int version = SWT.getVersion();
    int major = version / 1000;
    int minor = (version - major * 1000) / 100;
    return major + "." + minor;
  }
}
