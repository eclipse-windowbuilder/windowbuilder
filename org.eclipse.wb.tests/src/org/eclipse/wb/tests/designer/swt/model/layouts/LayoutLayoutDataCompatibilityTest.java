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
package org.eclipse.wb.tests.designer.swt.model.layouts;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.swt.IExceptionConstants;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Layout;

/**
 * Test that <code>LayoutData</code> corresponds to {@link Layout}.
 *
 * @author scheglov_ke
 */
public class LayoutLayoutDataCompatibilityTest extends RcpModelTest {
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
  /**
   * OK, {@link GridLayout} requires {@link GridData} and it is used.
   */
  public void test_compatible() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new GridLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new GridData());",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new GridLayout())/ /new Button(this, SWT.NONE)/}",
        "  {new: org.eclipse.swt.layout.GridLayout} {empty} {/setLayout(new GridLayout())/}",
        "  {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(this, SWT.NONE)/ /button.setLayoutData(new GridData())/}",
        "    {new: org.eclipse.swt.layout.GridData} {empty} {/button.setLayoutData(new GridData())/}");
    // refresh()
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * OK, for {@link FillLayout} we can/should not set any <code>LayoutData</code>.
   */
  public void test_noData() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new Button(this, SWT.NONE)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(this, SWT.NONE)/}");
    // refresh()
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * {@link GridLayout} requires {@link GridData}, but {@link RowData} used.
   */
  public void test_notCompatible() throws Exception {
    try {
      parseComposite(
          "public class Test extends Shell {",
          "  public Test() {",
          "    setLayout(new GridLayout());",
          "    {",
          "      Button button = new Button(this, SWT.NONE);",
          "      button.setLayoutData(new RowData());",
          "    }",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(IExceptionConstants.INCOMPATIBLE_LAYOUT_DATA, de.getCode());
    }
  }

  /**
   * {@link FillLayout} has no <code>LayoutData</code> (for user), but still assigns
   * <code>FillData</code>.
   */
  public void test_FillLayout_generatedFillData() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource(
            "public class MyShell extends Shell {",
            "  public MyShell() {",
            "    setLayout(new FillLayout());",
            "  }",
            "  protected void setDoComputeSize(boolean b) {",
            "    computeSize(-1, -1); // force FillData assignment",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "public class Test extends MyShell {",
            "  public Test() {",
            "    Button button = new Button(this, SWT.NONE);",
            "    setDoComputeSize(true);",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyShell} {this} {/new Button(this, SWT.NONE)/ /setDoComputeSize(true)/}",
        "  {implicit-layout: org.eclipse.swt.layout.FillLayout} {implicit-layout} {}",
        "  {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(this, SWT.NONE)/}");
    // refresh()
    shell.refresh();
    assertNoErrors(shell);
  }
}