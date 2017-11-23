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
package org.eclipse.wb.tests.designer.rcp.gef;

import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for changing layout and GEF.
 * 
 * @author scheglov_ke
 */
public class ChangeLayoutTest extends RcpGefTest {
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
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_removeSelectionPolicy_whenDropFillLayout() throws Exception {
    CompositeInfo composite =
        openComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setBounds(0, 0, 100, 20);",
            "    }",
            "  }",
            "}");
    ControlInfo button = composite.getChildrenControls().get(0);
    GraphicalEditPart buttonPart = canvas.getEditPart(button);
    // initially Button has "absolute" selection policy
    {
      EditPolicy policy = buttonPart.getEditPolicy(EditPolicy.SELECTION_ROLE);
      assertThat(policy).isNotNull();
      assertThat(policy.toString()).contains("AbsoluteLayoutSelectionEditPolicy");
    }
    // drop FillLayout
    loadCreationTool("org.eclipse.swt.layout.FillLayout");
    canvas.create();
    canvas.target(composite).in(10, 10).move();
    canvas.click();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new FillLayout(SWT.HORIZONTAL));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
    // FillLayout uses "non-resizable" selection policy
    {
      EditPolicy policy = buttonPart.getEditPolicy(EditPolicy.SELECTION_ROLE);
      assertThat(policy).isNotNull();
      assertThat(policy.toString()).contains("NonResizableSelectionEditPolicy");
    }
  }
}
