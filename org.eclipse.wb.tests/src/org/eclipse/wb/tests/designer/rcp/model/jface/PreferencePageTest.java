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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.internal.rcp.model.jface.PreferencePageInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link PreferencePageInfo}.
 * 
 * @author scheglov_ke
 */
public class PreferencePageTest extends RcpModelTest {
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
  public void test_0() throws Exception {
    PreferencePageInfo preferencePage =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends PreferencePage {",
            "  public Test() {",
            "  }",
            "  public Control createContents(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "    return container;",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.preference.PreferencePage} {this} {}",
        "  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
        "    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NULL)/ /container/}",
        "      {implicit-layout: absolute} {implicit-layout} {}");
    CompositeInfo parentComposite = preferencePage.getChildren(CompositeInfo.class).get(0);
    CompositeInfo container = (CompositeInfo) parentComposite.getChildrenControls().get(0);
    // refresh()
    preferencePage.refresh();
    // check bounds
    assertThat(preferencePage.getBounds().width).isEqualTo(600);
    assertThat(preferencePage.getBounds().height).isEqualTo(500);
    assertThat(parentComposite.getBounds().width).isGreaterThan(300);
    assertThat(parentComposite.getBounds().height).isGreaterThan(30);
    assertThat(container.getBounds().width).isGreaterThan(300);
    assertThat(container.getBounds().height).isGreaterThan(300);
    // set new bounds
    preferencePage.getTopBoundsSupport().setSize(500, 400);
    preferencePage.refresh();
    assertThat(preferencePage.getBounds().width).isEqualTo(500);
    assertThat(preferencePage.getBounds().height).isEqualTo(400);
  }
}