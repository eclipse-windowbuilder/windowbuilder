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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.internal.core.model.creation.IThisMethodParameterEvaluator;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * @author lobas_av
 */
public class ThisCompositeTest extends RcpModelTest {
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
   * Test for parsing "this" Composite. This tests that implementation of
   * {@link IThisMethodParameterEvaluator} for SWT is correct.
   */
  public void test_create() throws Exception {
    CompositeInfo compositeInfo =
        parseComposite(
            "class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new FillLayout());",
            "    Button button = new Button(this, SWT.NONE);",
            "    button.setText('push');",
            "  }",
            "}");
    //
    List<ControlInfo> children = compositeInfo.getChildrenControls();
    assertEquals(1, children.size());
    //
    ControlInfo buttonInfo = children.get(0);
    Property buttonProperty = buttonInfo.getPropertyByTitle("text");
    assertNotNull(buttonProperty);
    assertEquals("push", buttonProperty.getValue());
    assertInstanceOf(StringPropertyEditor.class, buttonProperty.getEditor());
    //
    compositeInfo.refresh();
    //
    assertNotNull(compositeInfo.getImage());
    assertEquals(
        new org.eclipse.swt.graphics.Rectangle(0, 0, 450, 300),
        compositeInfo.getImage().getBounds());
    assertNotNull(compositeInfo.getClientAreaInsets());
    assertEquals(new Dimension(450, 300), compositeInfo.getBounds().getSize());
    //
    assertNotNull(buttonInfo.getImage());
    assertNotNull(buttonInfo.getClientAreaInsets());
  }

  /**
   * Test for using {@link Shell#Shell(org.eclipse.swt.widgets.Display, int)} constructor.
   */
  public void test_Shell_Display() throws Exception {
    parseComposite(
        "class Test extends Shell {",
        "  public Test(Display display, int style) {",
        "    super(display, style);",
        "  }",
        "}");
  }
}