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
package org.eclipse.wb.tests.designer.ercp;

import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.model.variable.NamesManager.ComponentNameDescription;
import org.eclipse.wb.internal.ercp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.model.parser.AbstractJavaInfoRelatedTest;

import org.eclipse.swt.widgets.Button;

import java.util.Collections;

/**
 * Utilities for eSWT testing.
 * 
 * @author scheglov_ke
 */
public class ETestUtils {
  /**
   * Configures given {@link TestProject} for using eSWT.
   */
  public static void configure(TestProject testProject) throws Exception {
    {
      String ePath = "/resources/eRCP/";
      String swtPath = ePath + "org.eclipse.ercp.swt.win32_1.3.0/ws/win32/";
      testProject.addBundleJars("org.eclipse.wb.tests.support", ePath);
      testProject.addBundleJars("org.eclipse.wb.tests.support", swtPath);
    }
    // disable forced Field instead of Local
    NamesManager.setNameDescriptions(
        ToolkitProvider.DESCRIPTION,
        Collections.<ComponentNameDescription>emptyList());
    // disable SWTResourceManager
    ToolkitProvider.DESCRIPTION.getPreferences().setValue(
        IPreferenceConstants.P_USE_RESOURCE_MANAGER,
        false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the new instance of {@link ControlInfo} for given fully qualified class name.
   */
  public static ControlInfo createControl(String qualifiedClassName) throws Exception {
    return createControl(qualifiedClassName, null);
  }

  /**
   * @return the new instance of {@link ControlInfo} for given fully qualified class name.
   */
  public static ControlInfo createControl(String qualifiedClassName, String creationId)
      throws Exception {
    return AbstractJavaInfoRelatedTest.createJavaInfo(qualifiedClassName, creationId);
  }

  /**
   * @return the new instance of {@link ControlInfo} for SWT {@link Button}, without text.
   */
  public static ControlInfo createButton() throws Exception {
    return createControl("org.eclipse.swt.widgets.Button", "empty");
  }

  /**
   * @return the new instance of {@link LayoutInfo} for given fully qualified class name.
   */
  public static LayoutInfo createLayout(String qualifiedClassName) throws Exception {
    return AbstractJavaInfoRelatedTest.createJavaInfo(qualifiedClassName);
  }
}
