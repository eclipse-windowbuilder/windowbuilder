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
package org.eclipse.wb.internal.swt.utils;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.IClassLoaderInitializer;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * Implementation of {@link IClassLoaderInitializer} for initializing RCP
 * <code>ResourceManager</code>.
 *
 * @author scheglov_ke
 * @coverage swt.utils
 */
public final class ResourceManagerClassLoaderInitializer implements IClassLoaderInitializer {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IClassLoaderInitializer INSTANCE =
      new ResourceManagerClassLoaderInitializer();

  private ResourceManagerClassLoaderInitializer() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClassLoaderInitializer
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void initialize(final ClassLoader classLoader) {
    ExecutionUtils.runIgnore(new RunnableEx() {
      @Override
      public void run() throws Exception {
        Class<?> managerClass = classLoader.loadClass("org.eclipse.wb.swt.ResourceManager");
        Class<?> providerClass =
            classLoader.loadClass("org.eclipse.wb.swt.ResourceManager$PluginResourceProvider");
        initialize_ResourceManager(classLoader, managerClass, providerClass);
      }
    });
  }

  @Override
  public void deinitialize(ClassLoader classLoader) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Initializes already loaded <code>ResourceManager</code> class.
   */
  private void initialize_ResourceManager(ClassLoader classLoader,
      Class<?> managerClass,
      Class<?> providerClass) throws Exception {
    Object provider = createProvider(classLoader, providerClass);
    ReflectionUtils.setField(managerClass, "m_designTimePluginResourceProvider", provider);
  }

  /**
   * @return the implementation of <code>PluginResourceProvider</code>.
   */
  private Object createProvider(ClassLoader classLoader, Class<?> providerClass) {
    try {
      return new ByteBuddy() //
          .subclass(providerClass) //
          .method(ElementMatchers.named("getEntry")) //
          .intercept(InvocationHandlerAdapter.of((Object proxy, Method method, Object[] args) -> {
            String symbolicName = (String) args[0];
            String fullPath = (String) args[1];
            return getEntry(symbolicName, fullPath);
          })) //
          .make() //
          .load(classLoader) //
          .getLoaded() //
          .getConstructor() //
          .newInstance();
    } catch (ReflectiveOperationException e) {
      throw new DesignerException(ICoreExceptionConstants.EVAL_BYTEBUDDY, e);
    }
  }

  /**
   * @return the {@link URL} for resource in plugin.
   */
  private URL getEntry(String symbolicName, String fullPath) throws Exception {
    // try target platform
    {
      IPluginModelBase modelBase = PluginRegistry.findModel(symbolicName);
      String installLocation = modelBase.getInstallLocation();
      if (!StringUtils.isEmpty(installLocation) && installLocation.toLowerCase().endsWith(".jar")) {
        String urlPath = "jar:file:/" + installLocation + "!/" + fullPath;
        urlPath = FilenameUtils.normalize(urlPath, true);
        return new URL(urlPath);
      }
    }
    // try workspace plugin
    {
      IPluginModelBase pluginModel = PluginRegistry.findModel(symbolicName);
      if (pluginModel != null) {
        IResource underlyingResource = pluginModel.getUnderlyingResource();
        if (underlyingResource != null) {
          IProject project = underlyingResource.getProject();
          return project.getFile(new Path(fullPath)).getLocationURI().toURL();
        }
      }
    }
    // try runtime plugin
    {
      Bundle bundle = Platform.getBundle(symbolicName);
      if (bundle != null) {
        return bundle.getEntry(fullPath);
      }
    }
    // not found
    return null;
  }
}
