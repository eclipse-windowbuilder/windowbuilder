/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.databinding.rcp;

import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.model.parser.AbstractJavaInfoTest;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

/**
 * @author lobas_av
 *
 */
public class DatabindingTestUtils {
  /**
   * Configures given {@link TestProject} for using JFace DB.
   */
  public static void configure(TestProject testProject) throws Exception {
    BTestUtils.configure(testProject);
    testProject.addPlugin("com.ibm.icu");
    testProject.addPlugin("org.eclipse.core.databinding");
    testProject.addPlugin("org.eclipse.core.databinding.beans");
    testProject.addPlugin("org.eclipse.core.databinding.observable");
    testProject.addPlugin("org.eclipse.core.databinding.property");
    testProject.addPlugin("org.eclipse.jface.databinding");
  }

  /**
   * @return the source for RCP class in package "test".
   */
  public static String getTestSource(String... lines) {
    return DesignerTestCase.getSource(new String[][]{
        new String[]{
            "package test;",
            "import org.eclipse.swt.SWT;",
            "import org.eclipse.swt.events.*;",
            "import org.eclipse.swt.graphics.*;",
            "import org.eclipse.swt.widgets.*;",
            "import org.eclipse.swt.layout.*;",
            "import org.eclipse.swt.custom.*;",
            "import org.eclipse.jface.viewers.*;",
            "import org.eclipse.jface.preference.*;",
            "import org.eclipse.ui.forms.*;",
            "import org.eclipse.ui.forms.widgets.*;",
            "import org.eclipse.jface.resource.*;",
            "import org.eclipse.core.databinding.*;",
            "import org.eclipse.core.databinding.conversion.*;",
            "import org.eclipse.core.databinding.observable.*;",
            "import org.eclipse.core.databinding.observable.list.*;",
            "import org.eclipse.core.databinding.observable.map.*;",
            "import org.eclipse.core.databinding.observable.masterdetail.*;",
            "import org.eclipse.core.databinding.observable.set.*;",
            "import org.eclipse.core.databinding.observable.value.*;",
            "import org.eclipse.core.databinding.validation.*;",
            "import org.eclipse.core.databinding.beans.*;",
            "import org.eclipse.core.databinding.beans.typed.*;",
            "import org.eclipse.jface.databinding.swt.*;",
            "import org.eclipse.jface.databinding.swt.typed.*;",
            "import org.eclipse.jface.databinding.viewers.*;",
            "import org.eclipse.jface.databinding.viewers.typed.*;" },
        lines});
  }

  /**
   * @return the {@link CompositeInfo} for RCP source of class "Test" in package "test".
   */
  public static CompositeInfo parseTestSource(AbstractJavaInfoTest javaInfoTest, String[] lines)
      throws Exception {
    return (CompositeInfo) javaInfoTest.parseSource("test", "Test.java", getTestSource(lines));
  }
}