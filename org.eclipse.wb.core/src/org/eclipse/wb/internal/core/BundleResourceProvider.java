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
package org.eclipse.wb.internal.core;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import java.io.InputStream;
import java.util.Map;

/**
 * Provider for resources of some {@link Bundle}.
 *
 * @author scheglov_ke
 * @coverage core
 */
public final class BundleResourceProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Map<String, BundleResourceProvider> m_providers = Maps.newHashMap();

  /**
   * @return the {@link BundleResourceProvider} for {@link Bundle} with given id.
   */
  public static BundleResourceProvider get(String id) {
    synchronized (m_providers) {
      BundleResourceProvider provider = m_providers.get(id);
      if (provider == null) {
        provider = new BundleResourceProvider(id);
        m_providers.put(id, provider);
      }
      return provider;
    }
  }

  /**
   * @return the {@link BundleResourceProvider} for {@link Bundle}.
   */
  public static BundleResourceProvider get(Bundle bundle) {
    String id = bundle.getSymbolicName();
    return get(id);
  }

  /**
   * Configures automatic resources clean up on {@link Bundle} uninstalling.
   */
  public static void configureCleanUp(BundleContext context) {
    context.addBundleListener(new BundleListener() {
      public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.UNINSTALLED) {
          // prepare provider
          final BundleResourceProvider provider;
          synchronized (m_providers) {
            String id = event.getBundle().getSymbolicName();
            provider = m_providers.remove(id);
          }
          // clean up
          if (provider != null) {
            Display.getDefault().asyncExec(new Runnable() {
              public void run() {
                provider.disposeImages();
              }
            });
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_id;
  private final Bundle m_bundle;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private BundleResourceProvider(String id) {
    m_id = id;
    m_bundle = Platform.getBundle(id);
    Assert.isNotNull(m_bundle, "Unable for find bundle %s", id);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Files
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link String} content of file from bundle directory.
   */
  public String getFileString(final String path) {
    return ExecutionUtils.runObject(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        InputStream inputStream = getFile(path);
        return IOUtils2.readString(inputStream);
      }
    });
  }

  /**
   * @return the {@link InputStream} for file from bundle directory.
   */
  public InputStream getFile(String path) {
    path = normalizePath(path);
    return getFile0(path);
  }

  private InputStream getFile0(final String path) {
    return ExecutionUtils.runObject(new RunnableObjectEx<InputStream>() {
      public InputStream runObject() throws Exception {
        return m_bundle.getEntry(path).openStream();
      }
    }, "Unable to open file %s from %s", path, m_id);
  }

  private static String normalizePath(String path) {
    // remove extra "/"
    while (path.indexOf("//") != -1) {
      path = StringUtils.replace(path, "//", "/");
    }
    // ensure leading "/", required for Eclipse 3.2
    if (path.length() != 0 && path.charAt(0) != '/') {
      path = "/" + path;
    }
    return path;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, Image> m_pathToImage = Maps.newTreeMap();
  private final Map<String, ImageDescriptor> m_pathToImageDescriptor = Maps.newTreeMap();

  /**
   * @return the {@link Image}, with caching.
   */
  public Image getImage(String path) {
    path = normalizePath(path);
    Image image = m_pathToImage.get(path);
    if (image == null) {
      InputStream is = getFile(path);
      try {
        image = new Image(Display.getCurrent(), is);
        m_pathToImage.put(path, image);
      } finally {
        IOUtils.closeQuietly(is);
      }
    }
    return image;
  }

  /**
   * @return the {@link ImageDescriptor}, with caching.
   */
  public ImageDescriptor getImageDescriptor(String path) {
    path = normalizePath(path);
    ImageDescriptor descriptor = m_pathToImageDescriptor.get(path);
    if (descriptor == null) {
      Image image = getImage(path);
      descriptor = new ImageImageDescriptor(image);
      m_pathToImageDescriptor.put(path, descriptor);
    }
    return descriptor;
  }

  /**
   * Disposed loaded {@link Image}s.
   */
  private void disposeImages() {
    for (Image image : m_pathToImage.values()) {
      if (!image.isDisposed()) {
        image.dispose();
      }
    }
  }
}
