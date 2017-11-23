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
package org.eclipse.wb.tests.designer.rcp.swing2swt;

import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.swing2swt.layout.BoxLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test {@link BoxLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class BoxLayoutTest extends AbstractSwing2SwtTest {
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
  public void test_parse() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import swing2swt.layout.BoxLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new BoxLayout(BoxLayout.X_AXIS));",
            "  }",
            "}");
    shell.refresh();
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new BoxLayout(BoxLayout.X_AXIS))/}",
        "  {new: swing2swt.layout.BoxLayout} {empty} {/setLayout(new BoxLayout(BoxLayout.X_AXIS))/}");
    BoxLayoutInfo layout = (BoxLayoutInfo) shell.getLayout();
    // BoxLayout is "flow container"
    assertThat(new FlowContainerFactory(layout, true).get()).isNotEmpty();
    assertThat(new FlowContainerFactory(layout, false).get()).isNotEmpty();
  }

  /**
   * Test for "axis" property and {@link BoxLayoutInfo#isHorizontal()}.
   */
  public void test_axis() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import swing2swt.layout.BoxLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new BoxLayout(BoxLayout.X_AXIS));",
            "  }",
            "}");
    shell.refresh();
    BoxLayoutInfo layout = (BoxLayoutInfo) shell.getLayout();
    // X_AXIS, so horizontal
    assertTrue(layout.isHorizontal());
    // set Y_AXIS
    layout.getPropertyByTitle("axis").setValue(
        ReflectionUtils.getFieldObject(layout.getObject(), "Y_AXIS"));
    assertEditor(
        "import swing2swt.layout.BoxLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new BoxLayout(BoxLayout.Y_AXIS));",
        "  }",
        "}");
    // Y_AXIS, so vertical
    assertFalse(layout.isHorizontal());
  }
}