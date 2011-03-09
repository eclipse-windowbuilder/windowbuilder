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
package org.eclipse.wb.tests.designer.ercp.model.widgets;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.support.ControlSupport;
import org.eclipse.wb.internal.swt.support.SwtSupport;
import org.eclipse.wb.tests.designer.ercp.ErcpModelTest;

/**
 * @author lobas_av
 * @author mitin_aa
 * @author scheglov_ke
 */
public class ControlTest extends ErcpModelTest {
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
  public void test_classLoader_1() throws Exception {
    test_classLoader(new String[]{
        "class Test {",
        "  public static void main(String[] args) {",
        "    Shell shell = new Shell();",
        "  }",
        "}"});
  }

  public void test_classLoader_2() throws Exception {
    test_classLoader(new String[]{
        "class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "  }",
        "}"});
  }

  private void test_classLoader(String[] lines) throws Exception {
    CompositeInfo compositeInfo = parseComposite(lines);
    //
    Class<?> swtClass = m_lastLoader.loadClass("org.eclipse.swt.SWT");
    assertNotNull(swtClass);
    //
    assertEquals(swtClass.getField("BORDER").get(null), SwtSupport.getFlag("BORDER"));
    assertEquals(swtClass.getField("BORDER").getInt(null), SwtSupport.getIntFlag("BORDER"));
    assertEquals(swtClass.getField("BORDER").getInt(null), SwtSupport.BORDER);
    //
    Class<?> shellClass = m_lastLoader.loadClass("org.eclipse.swt.widgets.Shell");
    assertNotNull(shellClass);
    //
    Class<?> mobileShellClass = m_lastLoader.loadClass("org.eclipse.ercp.swt.mobile.MobileShell");
    assertNotNull(mobileShellClass);
    //
    compositeInfo.refresh();
    Object control = compositeInfo.getObject();
    //
    assertNotNull(ControlSupport.getBounds(control));
    assertNotNull(ControlSupport.toDisplay(control, 0, 0));
    assertNotNull(ControlSupport.getStyle(control));
  }

  /**
   * Test that descriptions for eRCP are shared, when needed.<br>
   * Problem is that we use new {@link ClassLoader} each time, so we can cache
   * {@link ComponentDescription} 's just by {@link Class}, we should use something more clever.<br>
   * Note that JAR with JFace classes is loaded always in separate {@link ClassLoader}, so these
   * descriptions are not cached.
   */
  public void test_sharingDescriptions() throws Exception {
    CompositeInfo shell_1 =
        (CompositeInfo) parseSource(
            "test",
            "Test_1.java",
            getSourceDQ(
                "package test;",
                "public class Test_1 extends org.eclipse.swt.widgets.Shell {",
                "  public Test_1() {",
                "    new org.eclipse.jface.viewers.TableViewer(this, 0);",
                "  }",
                "}"));
    ViewerInfo viewer_1 =
        (ViewerInfo) shell_1.getChildrenControls().get(0).getChildrenJava().get(0);
    CompositeInfo shell_2 =
        (CompositeInfo) parseSource(
            "test",
            "Test_2.java",
            getSourceDQ(
                "package test;",
                "public class Test_2 extends org.eclipse.swt.widgets.Shell {",
                "  public Test_2() {",
                "    new org.eclipse.jface.viewers.TableViewer(this, 0);",
                "  }",
                "}"));
    ViewerInfo viewer_2 =
        (ViewerInfo) shell_2.getChildrenControls().get(0).getChildrenJava().get(0);
    // do checks
    assertSame(shell_1.getDescription(), shell_2.getDescription());
    assertNotSame(viewer_1.getDescription(), viewer_2.getDescription());
  }
}