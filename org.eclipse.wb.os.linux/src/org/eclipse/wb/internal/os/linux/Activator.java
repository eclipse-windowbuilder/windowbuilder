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
package org.eclipse.wb.internal.os.linux;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author mitin_aa
 * @coverage os.linux
 */
public class Activator extends AbstractUIPlugin {
  public static final String PLUGIN_ID = "org.eclipse.wb.os.linux";
  //
  private static Activator m_plugin;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    m_plugin = this;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    m_plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance.
   */
  public static Activator getDefault() {
    return m_plugin;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Files
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link InputStream} for file from plugin directory.
   */
  public static InputStream getFile(String path) {
    try {
      URL url = new URL(getInstallURL(), path);
      return url.openStream();
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  /**
   * @return the install {@link URL} for this {@link Plugin}.
   */
  public static URL getInstallURL() {
    return getInstallUrl(getDefault());
  }

  /**
   * @return the install {@link URL} for given {@link Plugin}.
   */
  private static URL getInstallUrl(Plugin plugin) {
    return plugin.getBundle().getEntry("/");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Map<String, Image> m_nameToIconMap = Maps.newHashMap();

  /**
   * @return the {@link Image} from "icons" directory.
   */
  public static Image getImage(String path) {
    Image image = m_nameToIconMap.get(path);
    if (image == null) {
      InputStream is = getFile("icons/" + path);
      try {
        image = new Image(Display.getCurrent(), is);
        m_nameToIconMap.put(path, image);
      } finally {
        IOUtils.closeQuietly(is);
      }
    }
    return image;
  }
}