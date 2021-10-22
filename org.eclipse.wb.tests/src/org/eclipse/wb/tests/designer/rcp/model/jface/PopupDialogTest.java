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

import org.eclipse.wb.internal.rcp.model.jface.PopupDialogInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.dialogs.PopupDialog;

/**
 * Test for {@link PopupDialogInfo}.
 *
 * @author scheglov_ke
 */
public class PopupDialogTest extends RcpModelTest {
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
    PopupDialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.PopupDialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell, SWT.DIALOG_TRIM, true, true, true, true, 'Title text', 'Info text');",
            "  }",
            "  protected Control createDialogArea(Composite parent) {",
            "    Composite container = (Composite) super.createDialogArea(parent);",
            "    Button button = new Button(container, SWT.NONE);",
            "    return container;",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.jface.dialogs.PopupDialog} {this} {}",
        "  {parameter} {parent} {/super.createDialogArea(parent)/}",
        "    {casted-superInvocation: (Composite)super.createDialogArea(parent)} {local-unique: container} {/(Composite) super.createDialogArea(parent)/ /new Button(container, SWT.NONE)/ /container/}",
        "      {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
        "      {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(container, SWT.NONE)/}",
        "        {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}");
    // TopBoundsSupport does not show preview
    assertFalse(dialog.getTopBoundsSupport().show());
  }

  /**
   * {@link PopupDialog} has complex constructor with many bound properties.
   */
  public void test_boundProperties() throws Exception {
    PopupDialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.PopupDialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell, SWT.DIALOG_TRIM, true, true, true, true, 'Title text', 'Info text');",
            "  }",
            "}");
    // set properties
    dialog.getPropertyByTitle("shellStyle").setValue(0);
    dialog.getPropertyByTitle("titleText").setValue("New title text");
    dialog.getPropertyByTitle("infoText").setValue("New info text");
    assertEditor(
        "import org.eclipse.jface.dialogs.*;",
        "public class Test extends org.eclipse.jface.dialogs.PopupDialog {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell, 0, true, true, true, true, 'New title text', 'New info text');",
        "  }",
        "}");
  }
}