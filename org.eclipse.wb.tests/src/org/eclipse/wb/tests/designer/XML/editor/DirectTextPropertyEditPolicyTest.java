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
package org.eclipse.wb.tests.designer.XML.editor;

import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.xml.gef.policy.DirectTextPropertyEditPolicy;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Test for {@link DirectTextPropertyEditPolicy}.
 * 
 * @author scheglov_ke
 */
public class DirectTextPropertyEditPolicyTest extends XwtGefTest {
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
  public void test_activateManually() throws Exception {
    CompositeInfo shell = openEditor("<Shell/>");
    // do edit
    canvas.performDirectEdit(shell, "New text");
    assertXML("<Shell text='New text'/>");
  }

  public void test_activateForNew() throws Exception {
    CompositeInfo shell =
        openEditor(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "</Shell>");
    //
    IPreferenceStore preferences = shell.getDescription().getToolkit().getPreferences();
    preferences.setValue(IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD, true);
    try {
      // drop new Button
      loadCreationTool("org.eclipse.swt.widgets.Button");
      canvas.moveTo(shell, 100, 100).click();
      // set new text
      canvas.animateDirectEdit("New text");
      assertXML(
          "// filler filler filler filler filler",
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <Button text='New text'/>",
          "</Shell>");
    } finally {
      preferences.setValue(IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD, false);
    }
  }

  public void test_usePropertyPath() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "public void setTitle(String title) {",
        "}"}, new String[]{
        "<parameters>",
        "  <parameter name='directEdit.x-property'>title</parameter>",
        "</parameters>"});
    //
    openEditor(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:MyComponent wbp:name='control'/>",
        "</Shell>");
    ControlInfo control = getObjectByName("control");
    // do edit
    canvas.performDirectEdit(control, "New title");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:MyComponent wbp:name='control' title='New title'/>",
        "</Shell>");
  }
}
