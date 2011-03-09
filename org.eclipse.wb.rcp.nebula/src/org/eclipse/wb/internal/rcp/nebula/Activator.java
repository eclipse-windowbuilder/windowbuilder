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
package org.eclipse.wb.internal.rcp.nebula;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;

import java.io.InputStream;
import java.util.Map;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.wb.rcp.nebula";
  // The shared instance
  private static Activator plugin;

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Icons
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Map<String, Image> m_nameToIconMap = Maps.newHashMap();

  /**
   * @return the {@link InputStream} for file from plugin directory.
   */
  public static InputStream getFile(final String path) {
    return ExecutionUtils.runObject(new RunnableObjectEx<InputStream>() {
      public InputStream runObject() throws Exception {
        return plugin.getBundle().getEntry(path).openStream();
      }
    }, "Unable to open plugin file %s", path);
  }

  /**
   * @return the {@link Image} from "icons" directory.
   */
  public static Image getImage(String path) {
    Image image = m_nameToIconMap.get(path);
    if (image == null) {
      InputStream is = getFile(path);
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
