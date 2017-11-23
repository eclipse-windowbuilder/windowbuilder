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
package org.eclipse.wb.tests.designer.XML.gef;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.xml.gef.policy.OpenListenerEditPolicy;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.ui.IEditorPart;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link OpenListenerEditPolicy}.
 * 
 * @author scheglov_ke
 */
public class OpenListenerEditPolicyTest extends XwtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_0() throws Exception {
    setFileContentSrc(
        "test/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "}"));
    waitForAutoBuild();
    // parse
    openEditor(
        "// filler filler filler filler filler",
        "<Shell x:Class='test.Test'>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    XmlObjectInfo button = getObjectByName("button");
    // double click on "button"
    canvas.doubleClick(button);
    // "Java" editor is opened
    {
      IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
      assertThat(activeEditor).isInstanceOf(CompilationUnitEditor.class);
    }
    // files updated
    assertEquals(
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "  public void onSelection(Event event) {",
            "  }",
            "}"),
        getFileContentSrc("test/Test.java"));
    assertXML(
        "// filler filler filler filler filler",
        "<Shell x:Class='test.Test'>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button' SelectionEvent='onSelection'/>",
        "</Shell>");
  }
}
