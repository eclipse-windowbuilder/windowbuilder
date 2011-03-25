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
package org.eclipse.wb.internal.swing.laf.model;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.LookAndFeel;

/**
 * Class representing user-defined Look-n-Feel.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public class UserDefinedLafInfo extends AbstractCustomLafInfo {
  private Class<?> m_lafClass;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public UserDefinedLafInfo(String name, String className, String jarFile) {
    this("laf_" + System.currentTimeMillis(), name, className, jarFile);
  }

  public UserDefinedLafInfo(String id, String name, String className, String jarFile) {
    super(id, name, className, jarFile);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public LookAndFeel getLookAndFeelInstance() throws Exception {
    if (m_lafClass == null) {
      ClassLoader classLoader = getClassLoader();
      m_lafClass = classLoader.loadClass(getClassName());
    }
    return (LookAndFeel) m_lafClass.newInstance();
  }

  private ClassLoader getClassLoader() throws Exception {
    File jarFile = new File(getJarFile());
    URL jarURL = jarFile.toURI().toURL();
    // special hack for Substance
    if (jarFile.getName().equals("substance.jar")) {
      URL secondaryJarURL = new File(jarFile.getParentFile(), "trident.jar").toURI().toURL();
      return new URLClassLoader(new URL[]{jarURL, secondaryJarURL});
    }
    // single jar
    return new URLClassLoader(new URL[]{jarURL});
  }
}
