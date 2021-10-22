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

import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.rcp.model.jface.FieldEditorInfo;
import org.eclipse.wb.internal.rcp.model.jface.FieldEditorPreferencePageInfo;
import org.eclipse.wb.internal.rcp.palette.DoubleFieldEditorEntryInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 * Test for {@link DoubleFieldEditorEntryInfo}.
 *
 * @author scheglov_ke
 */
public class DoubleFieldEditorEntryInfoTest extends RcpModelTest {
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
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(FLAT);",
            "  }",
            "  protected void createFieldEditors() {",
            "  }",
            "}");
    page.refresh();
    // prepare palette entry
    ToolEntryInfo entry = new DoubleFieldEditorEntryInfo();
    assertNotNull(entry.getIcon());
    assertNotNull(entry.getName());
    assertNotNull(entry.getDescription());
    assertTrue(entry.initialize(null, page));
    // use Tool to create DoubleFieldEditor
    FieldEditorInfo newField;
    {
      CreationTool creationTool = (CreationTool) entry.createTool();
      ICreationFactory creationFactory = creationTool.getFactory();
      creationFactory.activate();
      newField = (FieldEditorInfo) creationFactory.getNewObject();
    }
    // add it
    page.command_CREATE(newField, null);
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "import org.eclipse.wb.swt.DoubleFieldEditor;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "    super(FLAT);",
        "  }",
        "  protected void createFieldEditors() {",
        "    addField(new DoubleFieldEditor('id', 'New DoubleFieldEditor', getFieldEditorParent()));",
        "  }",
        "}");
  }
}