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
package org.eclipse.wb.internal.css;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.css.editors.TokenManager;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author scheglov_ke
 * @coverage CSS
 */
public class Activator extends AbstractUIPlugin {
  public static final String PLUGIN_ID = "org.eclipse.wb.css";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Bundle operations
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Activator m_plugin;

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
   * @return the bundle instance
   */
  public static Activator getDefault() {
    return m_plugin;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the preference store
   */
  public static IPreferenceStore getStore() {
    return m_plugin.getPreferenceStore();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Map<String, Image> m_nameToIconMap = Maps.newHashMap();

  /**
   * Open file from plugin directory.
   */
  public static InputStream getFile(String name) {
    try {
      return m_plugin.getBundle().getEntry(name).openStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get image from "icons" directory.
   */
  public static Image getImage(String name) {
    Image image = m_nameToIconMap.get(name);
    if (image == null) {
      if (m_plugin != null) {
        InputStream is = getFile("icons/" + name);
        try {
          image = new Image(Display.getCurrent(), is);
          m_nameToIconMap.put(name, image);
        } finally {
          IOUtils.closeQuietly(is);
        }
      } else {
        String devPath = "C:/eclipsePL/workspace/org.eclipse.wb.css/icons/";
        image = new Image(null, devPath + name);
      }
    }
    return image;
  }

  /**
   * @return the image descriptor for given path
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return imageDescriptorFromPlugin(PLUGIN_ID, path);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Token manager
  //
  ////////////////////////////////////////////////////////////////////////////
  private TokenManager m_tokenManager;

  /**
   * @return the {@link TokenManager} for CSS
   */
  public TokenManager getTokenManager() {
    if (m_tokenManager == null) {
      m_tokenManager = new TokenManager(getPreferenceStore());
    }
    return m_tokenManager;
  }
}
