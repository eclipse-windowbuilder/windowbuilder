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
package org.eclipse.wb.tests.designer.core.model.description;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 * Tests for {@link ComponentDescriptionHelper}, {@link ComponentDescription}, etc.
 *
 * @author scheglov_ke
 */
public class ComponentDescriptionIbmTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // enable IBM emulation
    EnvironmentUtils.setForcedIBM(true);
    // enable development time
    EnvironmentUtils.setTestingTime(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Method 'setTestProperty(int)' not visible for IBM JVM, and will be skipped in user mode.
   */
  public void test_description_forIBM() throws Exception {
    // disable development time
    EnvironmentUtils.setTestingTime(false);
    // parse source
    ContainerInfo panel = createDefaultProjectContents();
    // method 'setTestProperty(int)' not exists (skipped)
    assertNull(panel.getPropertyByTitle("testProperty"));
  }

  /**
   * Method 'setTestProperty(int)' not visible for IBM JVM, and parsing must be fail in development
   * mode.
   */
  public void test_descriptin_fail() throws Exception {
    try {
      createDefaultProjectContents();
      fail();
    } catch (DesignerException e) {
      if (e.getCode() == ICoreExceptionConstants.DESCRIPTION_LOAD_ERROR) {
        // OK
      } else {
        throw e;
      }
    }
  }

  /**
   * Normal test, method 'setTestProperty(int)' must exists.
   */
  public void test_descriptin_nonIBM() throws Exception {
    // disable IBM emulation
    EnvironmentUtils.setForcedIBM(false);
    // parse source
    ContainerInfo panel = createDefaultProjectContents();
    assertNotNull(panel.getPropertyByTitle("testProperty"));
  }

  @Override
  public void test_tearDown() throws Exception {
    super.test_tearDown();
    // reset Environment
    EnvironmentUtils.setForcedIBM(false);
    EnvironmentUtils.setTestingTime(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private ContainerInfo createDefaultProjectContents() throws Exception {
    setJavaContentSrc("test", "TestPanel", new String[]{
        "public class TestPanel extends JPanel {",
        "  public TestPanel(){",
        "  }",
        "  protected void setTestProperty(int value){",
        "  }",
        "}"}, new String[]{
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
        "  <property id='setTestProperty(int)'>",
        "    <defaultValue value='7'/>",
        "  </property>",
        "</component>"});
    return parseContainer(
        "// filler filler filler",
        "public class Test extends TestPanel {",
        "  public Test() {",
        "  }",
        "}");
  }
}
