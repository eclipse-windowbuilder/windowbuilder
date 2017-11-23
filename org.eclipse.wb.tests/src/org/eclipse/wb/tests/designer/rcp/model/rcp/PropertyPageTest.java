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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.internal.rcp.model.rcp.PropertyPageInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link PropertyPageInfo}.
 * 
 * @author scheglov_ke
 */
public class PropertyPageTest extends RcpModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_0() throws Exception {
    PropertyPageInfo page =
        parseJavaInfo(
            "import org.eclipse.ui.dialogs.*;",
            "public class Test extends PropertyPage {",
            "  public Test() {",
            "  }",
            "  public Control createContents(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "    return container;",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.ui.dialogs.PropertyPage} {this} {}",
        "  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
        "    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NULL)/ /container/}",
        "      {implicit-layout: absolute} {implicit-layout} {}");
    CompositeInfo parentComposite = page.getChildren(CompositeInfo.class).get(0);
    CompositeInfo container = (CompositeInfo) parentComposite.getChildrenControls().get(0);
    // refresh()
    page.refresh();
    assertNoErrors(page);
    // check bounds
    assertThat(page.getBounds().width).isEqualTo(600);
    assertThat(page.getBounds().height).isEqualTo(500);
    assertThat(parentComposite.getBounds().width).isGreaterThan(300);
    assertThat(parentComposite.getBounds().height).isGreaterThan(30);
    assertThat(container.getBounds().width).isGreaterThan(300);
    assertThat(container.getBounds().height).isGreaterThan(300);
  }
}