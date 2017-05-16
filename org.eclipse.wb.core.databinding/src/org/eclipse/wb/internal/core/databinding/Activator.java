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
package org.eclipse.wb.internal.core.databinding;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * The activator and utility class controls the Bindings plug-in life cycle.
 *
 * @author lobas_av
 */
public final class Activator extends AbstractUIPlugin {
  public static final String PLUGIN_ID = "org.eclipse.wb.core.databinding";
  private static Activator m_plugin;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Activator() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void stop(BundleContext context) throws Exception {
    m_plugin = null;
    super.stop(context);
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    m_plugin = this;
  }

  public static Activator getDefault() {
    return m_plugin;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link InputStream} for file from plugin directory.
   */
  public static InputStream getFile(final String path) {
    return ExecutionUtils.runObject(new RunnableObjectEx<InputStream>() {
      public InputStream runObject() throws Exception {
        return m_plugin.getBundle().getEntry(path).openStream();
      }
    }, "Unable to open plugin file %s", path);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Caches
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Map<String, Image> m_nameToIconMap = Maps.newHashMap();

  /**
   * Get image from "icons" directory.
   */
  public static Image getImage(String name) {
    Image image = m_nameToIconMap.get(name);
    if (image == null) {
      // prepare path
      String path;
      if (name.startsWith("/")) {
        path = name;
      } else {
        path = "icons/" + name;
      }
      //
      InputStream is = getFile(path);
      try {
        image = new Image(Display.getCurrent(), is);
        m_nameToIconMap.put(name, image);
      } finally {
        IOUtils.closeQuietly(is);
      }
    }
    return image;
  }

  /**
   * Get image descriptor from "icons" directory.
   */
  public static ImageDescriptor getImageDescriptor(String name) {
    URL url = m_plugin.getBundle().getEntry("icons/" + name);
    return ImageDescriptor.createFromURL(url);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns preference store for DB plugin.
   */
  public static IPreferenceStore getStore() {
    return m_plugin.getPreferenceStore();
  }
}