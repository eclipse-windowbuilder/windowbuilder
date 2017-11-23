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
import org.eclipse.wb.internal.rcp.swing2swt.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test {@link FlowLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class FlowLayoutTest extends AbstractSwing2SwtTest {
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
            "import swing2swt.layout.FlowLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));",
            "  }",
            "}");
    shell.refresh();
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5))/}",
        "  {new: swing2swt.layout.FlowLayout} {empty} {/setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5))/}");
    FlowLayoutInfo layout = (FlowLayoutInfo) shell.getLayout();
    // FlowLayout is "flow container"
    assertThat(new FlowContainerFactory(layout, true).get()).isNotEmpty();
    assertThat(new FlowContainerFactory(layout, false).get()).isNotEmpty();
  }
}