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
package org.eclipse.wb.internal.core.utils.pde;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.osgi.service.resolver.BundleDescription;

import org.osgi.framework.Bundle;

import java.util.List;

/**
 * Access for PDE functions using reflection.
 *
 * @author scheglov_ke
 * @coverage core.util.pde
 */
public class ReflectivePDE {
  /**
   * Adds plugin libraries into the list.
   */
  public static void addPluginLibraries(String pluginId, List<IClasspathEntry> entries)
      throws Exception {
    Object model = findModel(pluginId);
    if (model != null) {
      invokeStatic(
          "org.eclipse.pde.internal.core.ClasspathUtilCore",
          "addLibraries(org.eclipse.pde.core.plugin.IPluginModelBase,java.util.ArrayList)",
          model,
          entries);
    }
  }

  /**
   * Calls {@link org.eclipse.pde.core.plugin.PluginRegistry#findModel(String)}.
   */
  public static Object findModel(String pluginId) throws Exception {
    try {
      return invokeStatic(
          "org.eclipse.pde.core.plugin.PluginRegistry",
          "findModel(java.lang.String)",
          pluginId);
    } catch (Throwable e) {
      return null;
    }
  }

  /**
   * Calls {@link org.eclipse.pde.core.plugin.PluginRegistry#findModel(IProject)}.
   */
  public static Object findModel(IProject project) {
    try {
      return invokeStatic(
          "org.eclipse.pde.core.plugin.PluginRegistry",
          "findModel(org.eclipse.core.resources.IProject)",
          project);
    } catch (Throwable e) {
      return null;
    }
  }

  /**
   * Calls {@link org.eclipse.pde.core.plugin.IPluginModelBase#getBundleDescription()}.
   */
  public static BundleDescription getPluginModelBundleDescription(Object pluginModel)
      throws Exception {
    return (BundleDescription) ReflectionUtils.invokeMethod(pluginModel, "getBundleDescription()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Object invokeStatic(String className, String signature, Object... args)
      throws Exception {
    Class<?> clazz = loadClass(className);
    return ReflectionUtils.invokeMethod(clazz, signature, args);
  }

  private static Class<?> loadClass(String name) throws Exception {
    Bundle bundle = Platform.getBundle("org.eclipse.pde.core");
    return bundle.loadClass(name);
  }
}