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
package org.eclipse.wb.tests.designer.rcp;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.model.parser.AbstractJavaInfoRelatedTest;

import org.eclipse.swt.widgets.Button;

/**
 * Utilities for RCP testing. "B" in name means "big". :-)
 *
 * @author scheglov_ke
 */
public final class BTestUtils {
  /**
   * Configures given {@link TestProject} for using RCP.
   */
  public static void configureSWT(TestProject testProject) throws Exception {
    if (EnvironmentUtils.IS_WINDOWS) {
      testProject.addPlugin("org.eclipse.swt.win32.win32.x86_64");
    } else if (EnvironmentUtils.IS_LINUX) {
        testProject.addPlugin("org.eclipse.swt.gtk.linux.x86_64");
    } else if (EnvironmentUtils.IS_MAC) {
      testProject.addPlugin("org.eclipse.swt.cocoa.macosx.x86_64");
    } else {
      throw new UnsupportedOperationException("Unsupported platform.");
    }
  }

  public static void configure(TestProject testProject) throws Exception {
    configureSWT(testProject);
    testProject.addPlugin("org.eclipse.jface");
    testProject.addPlugin("org.eclipse.osgi");
    testProject.addPlugin("org.eclipse.equinox.common");
    testProject.addPlugin("org.eclipse.equinox.registry");
    testProject.addPlugin("org.eclipse.core.runtime");
    testProject.addPlugin("org.eclipse.core.jobs");
    testProject.addPlugin("org.eclipse.core.commands");
    testProject.addPlugin("org.eclipse.ui.forms");
    testProject.addPlugin("org.eclipse.ui.workbench");
    testProject.addPlugin("org.eclipse.ui.views");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the new instance of {@link ControlInfo} for given fully qualified class name.
   */
  public static ControlInfo createControl(String componentClassName) throws Exception {
    return AbstractJavaInfoRelatedTest.createJavaInfo(componentClassName);
  }

  /**
   * @return the new instance of {@link LayoutInfo} for given fully qualified class name.
   */
  public static LayoutInfo createLayout(String layoutClassName) throws Exception {
    return AbstractJavaInfoRelatedTest.createJavaInfo(layoutClassName);
  }

  /**
   * @return the new instance of {@link ControlInfo} for SWT {@link Button}.
   */
  public static ControlInfo createButton() throws Exception {
    return AbstractJavaInfoRelatedTest.createJavaInfo("org.eclipse.swt.widgets.Button", "empty");
  }
}
