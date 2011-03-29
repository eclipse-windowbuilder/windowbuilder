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

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author mitin_aa
 * @coverage os.linux
 */
public class Activator extends AbstractUIPlugin implements IStartup {
  private static boolean gconfAvailable = false;
  static {
    try {
      System.loadLibrary("wbp-compiz");
      // try to invoke
      _isCompizSet();
      gconfAvailable = true;
    } catch (Throwable e) {
      // can't load gconf-related lib, skipping all compiz checks.
    }
  }
  private static final String PK_ASK_FOR_WORKAROUND =
      "org.eclipse.wb.os.linux.compizDontAskForWorkaround";
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Start-up
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean isRunningCompiz() {
    try {
      File procs = new File("/proc");
      File[] procFiles = procs.listFiles(new FileFilter() {
        public boolean accept(File pathname) {
          return pathname.isDirectory();
        }
      });
      for (File procDir : procFiles) {
        File cmdLineFile = new File(procDir, "cmdline");
        if (cmdLineFile.exists()) {
          String cmdLine = IOUtils2.readString(cmdLineFile);
          if (cmdLine.indexOf("compiz") != -1) {
            return true;
          }
        }
      }
    } catch (Throwable e) {
      // ignore silently
    }
    return false;
  }

  private static Display getStandardDisplay() {
    Display display = Display.getCurrent();
    if (display == null) {
      display = Display.getDefault();
    }
    return display;
  }

  private boolean askAgain() {
    String value = getPreferenceStore().getString(PK_ASK_FOR_WORKAROUND);
    return StringUtils.isEmpty(value);
  }

  private boolean isCompizSet() {
    return _isCompizSet();
  }

  private void setupCompiz() {
    _setupCompiz();
  }

  public void earlyStartup() {
    if (!gconfAvailable) {
      // no necessary gconf libs installed, skip checks.
      return;
    }
    getStandardDisplay().syncExec(new Runnable() {
      public void run() {
        if (isRunningCompiz() && !isCompizSet() && askAgain()) {
          MessageDialogWithToggle dialog =
              MessageDialogWithToggle.openYesNoQuestion(
                  null,
                  "WindowBuilder",
                  "It seems that you're running the Compiz Window Manager. "
                      + "It allows the special windows to appear offscreen and this will allow "
                      + "the product to prevent preview window flickering.\n\n"
                      + "Do you want to setup the Compiz WM to move the preview windows into the offscreen area?",
                  "Do not ask again",
                  false,
                  getPreferenceStore(),
                  PK_ASK_FOR_WORKAROUND);
          int returnCode = dialog.getReturnCode();
          if (returnCode == IDialogConstants.YES_ID) {
            setupCompiz();
          }
        }
      }
    });
  }

  private static native boolean _setupCompiz();

  private static native boolean _isCompizSet();
}