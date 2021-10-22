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

import org.eclipse.wb.internal.core.xml.editor.actions.CutAction;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.eclipse.jface.action.IAction;

/**
 * Test for {@link CutAction}.
 *
 * @author scheglov_ke
 */
public class CutActionTest extends XwtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * "Cut" action is disabled if no selection.
   */
  public void test_noSelection() throws Exception {
    openEditor("<Shell/>");
    // prepare "Cut" action
    IAction cutAction = getCutAction();
    // no selection - disabled action
    canvas.select();
    assertFalse(cutAction.isEnabled());
  }

  /**
   * "This" component can not be copied or deleted.
   */
  public void test_thisSelection() throws Exception {
    XmlObjectInfo shell = openEditor("<Shell/>");
    // prepare "Cut" action
    IAction cutAction = getCutAction();
    // "this" selected - disabled action
    canvas.select(shell);
    assertFalse(cutAction.isEnabled());
  }

  /**
   * Test for cut/paste single component.
   */
  public void test_cutSingle() throws Exception {
    XmlObjectInfo shell =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button' text='Button'/>",
            "</Shell>");
    XmlObjectInfo button = getObjectByName("button");
    // select "button"
    canvas.select(button);
    // cut
    {
      IAction cutAction = getCutAction();
      assertTrue(cutAction.isEnabled());
      cutAction.run();
      assertXML(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "</Shell>");
    }
    // paste
    {
      IAction pasteAction = getPasteAction();
      assertTrue(pasteAction.isEnabled());
      pasteAction.run();
      // do paste
      canvas.moveTo(shell, 10, 10);
      canvas.click();
      assertXML(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <Button text='Button'/>",
          "</Shell>");
    }
  }
}
