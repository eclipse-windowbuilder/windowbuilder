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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.utils.AsyncMessagesSupport;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 * Test for {@link AsyncMessagesSupport}.
 * 
 * @author scheglov_ke
 */
public class AsyncMessagesSupportTest extends RcpModelTest {
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
   * If no <code>SWT.runAsyncMessages</code> flag, then we can not expect that async runnable will
   * be executed.
   */
  public void test_hasAsync_noMessagesRequest() throws Exception {
    prepareButtonWithAsync();
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setSize(450, 300);",
            "    MyButton button = new MyButton(this, SWT.NONE);",
            "    button.setValue(5);",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    assertEquals(0, ReflectionUtils.invokeMethod(button.getObject(), "getValue()"));
  }

  /**
   * We have <code>SWT.runAsyncMessages</code> flag, so expect that async runnable is executed.
   */
  public void test_hasAsync_hasMessagesRequest() throws Exception {
    prepareButtonWithAsync();
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='SWT.runAsyncMessages'>true</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setSize(450, 300);",
            "    MyButton button = new MyButton(this, SWT.NONE);",
            "    button.setValue(5);",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    assertEquals(5, ReflectionUtils.invokeMethod(button.getObject(), "getValue()"));
  }

  private void prepareButtonWithAsync() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  private int m_value;",
            "  public MyButton(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  public void setValue(final int value) {",
            "    getDisplay().asyncExec(new Runnable() {",
            "      public void run() {",
            "        m_value = value;",
            "      }",
            "    });",
            "  }",
            "  public int getValue() {",
            "    return m_value;",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
  }
}