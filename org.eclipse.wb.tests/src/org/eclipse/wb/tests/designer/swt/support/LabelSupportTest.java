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
package org.eclipse.wb.tests.designer.swt.support;

import org.eclipse.wb.internal.swt.support.LabelSupport;

/**
 * Tests for {@link LabelSupport}.
 * 
 * @author lobas_av
 */
public class LabelSupportTest extends AbstractSupportTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String[] getTestSource() {
    return new String[]{
        "public class Test extends Shell {",
        "  public Test() {",
        "    Label label = new Label(this, SWT.NONE);",
        "    label.setText(\"New Label\");",
        "  }",
        "}"};
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
  public void test_getLabelClass() throws Exception {
    assertSame(
        m_lastLoader.loadClass("org.eclipse.swt.widgets.Label"),
        LabelSupport.getLabelClass());
  }

  public void test_text() throws Exception {
    Object label = m_shell.getChildrenControls().get(0).getObject();
    assertEquals("New Label", LabelSupport.getText(label));
    LabelSupport.setText(label, "New My Label");
    assertEquals("New My Label", LabelSupport.getText(label));
  }

  /**
   * Test for {@link LabelSupport#newInstance(Object)}.
   */
  public void test_newInstance() throws Exception {
    Object shell = m_shell.getObject();
    Object label = LabelSupport.newInstance(shell);
    assertEquals("", LabelSupport.getText(label));
  }
}