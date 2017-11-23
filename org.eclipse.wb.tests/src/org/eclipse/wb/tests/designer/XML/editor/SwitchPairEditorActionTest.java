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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.xml.editor.actions.SwitchPairEditorAction;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.ui.IEditorPart;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link SwitchPairEditorAction}.
 * 
 * @author scheglov_ke
 */
public class SwitchPairEditorActionTest extends XwtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for switching to "Source" and back to "Design".
   */
  public void test_run() throws Exception {
    setFileContentSrc(
        "test/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "}"));
    openEditor("<Shell x:Class='test.Test'/>");
    // prepare action
    SwitchPairEditorAction switchAction;
    {
      switchAction = new SwitchPairEditorAction();
      switchAction.setActiveEditor(null, m_designerEditor);
    }
    // initially XML editor is active
    assertSame(m_designerEditor, DesignerPlugin.getActiveEditor());
    // switch to Java using action
    switchAction.run(null);
    waitEventLoop(0);
    {
      IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
      assertThat(activeEditor).isInstanceOf(CompilationUnitEditor.class);
      switchAction.setActiveEditor(null, activeEditor);
    }
    // switch to XML using action
    switchAction.run(null);
    waitEventLoop(0);
    assertSame(m_designerEditor, DesignerPlugin.getActiveEditor());
  }
}
