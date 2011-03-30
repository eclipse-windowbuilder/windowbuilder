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
package org.eclipse.wb.tests.designer.databinding.swing;

import org.eclipse.wb.internal.swing.databinding.DesignPageFactory;
import org.eclipse.wb.internal.swing.databinding.SwingDatabindingFactory;
import org.eclipse.wb.internal.swing.databinding.model.DataBindingsCodeUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 * @author sablin_aa
 */
public class SwingDatabindingsFactoryTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    do_projectDispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_noProvider() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JList list = new JList();",
            "    add(list);",
            "  }",
            "}");
    assertNotNull(panel);
    //
    if (DataBindingsCodeUtils.getExtrasBundle() == null) {
      assertFalse(DesignPageFactory.isSwingDB(panel.getEditor().getModelUnit()));
      SwingDatabindingFactory factory = new SwingDatabindingFactory();
      assertNotNull(factory.createProvider(panel.getRootJava()));
      assertNull(panel.getPropertyByTitle("bindings"));
    } else {
      assertTrue(DesignPageFactory.isSwingDB(panel.getEditor().getModelUnit()));
      SwingDatabindingFactory factory = new SwingDatabindingFactory();
      assertNotNull(factory.createProvider(panel.getRootJava()));
      assertNotNull(panel.getPropertyByTitle("bindings"));
    }
  }

  public void test_Provider() throws Exception {
    DatabindingTestUtils.configure(m_testProject);
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JList list = new JList();",
            "    add(list);",
            "  }",
            "}");
    assertNotNull(panel);
    //
    assertTrue(DesignPageFactory.isSwingDB(panel.getEditor().getModelUnit()));
    SwingDatabindingFactory factory = new SwingDatabindingFactory();
    assertNotNull(factory.createProvider(panel.getRootJava()));
    assertNotNull(panel.getPropertyByTitle("bindings"));
  }
}