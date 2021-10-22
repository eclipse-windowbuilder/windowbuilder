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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.editor.actions.CopyAction;
import org.eclipse.wb.internal.core.xml.editor.actions.PasteAction;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;
import org.eclipse.wb.tests.designer.core.model.TestObjectInfo;

import org.eclipse.jface.action.IAction;

/**
 * Test for {@link CopyAction} and {@link PasteAction}.
 *
 * @author scheglov_ke
 */
public class CopyActionTest extends XwtGefTest {
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
  /**
   * "Copy" action is disabled if no selection.
   */
  public void test_noSelection() throws Exception {
    openEditor("<Shell/>");
    // prepare "Copy" action
    IAction copyAction = getCopyAction();
    // no selection - disabled action
    canvas.select();
    assertFalse(copyAction.isEnabled());
  }

  /**
   * "Copy" action is disabled if no selection.
   */
  public void test_notXMLObject_EditPart() throws Exception {
    XmlObjectInfo shell = openEditor("<Shell/>");
    // add artificial EditPart
    EditPart myEditPart;
    {
      ObjectInfo myObject = new TestObjectInfo();
      shell.addChild(myObject);
      myEditPart = new TreeEditPart() {
      };
      myEditPart.setModel(myObject);
      ReflectionUtils.invokeMethod(
          tree.getEditPart(shell),
          "addChild(org.eclipse.wb.gef.core.EditPart,int)",
          myEditPart,
          0);
    }
    // prepare "Copy" action
    IAction copyAction = getCopyAction();
    // no selection - disabled action
    tree.select(myEditPart);
    assertFalse(copyAction.isEnabled());
  }

  /**
   * "This" component can not be copied.
   */
  public void test_thisSelection() throws Exception {
    XmlObjectInfo shell = openEditor("<Shell/>");
    // prepare "Copy" action
    IAction copyAction = getCopyAction();
    // "this" selected - disabled action
    canvas.select(shell);
    assertFalse(copyAction.isEnabled());
  }

  /**
   * Test for copy/paste single component.
   */
  public void test_copySingle() throws Exception {
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
    // copy "button"
    {
      IAction copyAction = getCopyAction();
      assertTrue(copyAction.isEnabled());
      copyAction.run();
    }
    // paste
    {
      IAction pasteAction = getPasteAction();
      assertTrue(pasteAction.isEnabled());
      pasteAction.run();
      // do paste
      {
        canvas.target(button).outX(10).inY(10).move();
        canvas.click();
      }
      assertXML(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <Button wbp:name='button' text='Button'/>",
          "  <Button text='Button'/>",
          "</Shell>");
    }
  }

  /**
   * If container and its child are selected, then only container should be copied, it will copy
   * child automatically.
   */
  public void test_copyParentAndItsChild() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='composite'>",
        "    <Composite.layout>",
        "      <FillLayout/>",
        "    </Composite.layout>",
        "    <Button wbp:name='button'/>",
        "  </Composite>",
        "</Shell>");
    XmlObjectInfo composite = getObjectByName("composite");
    XmlObjectInfo button = getObjectByName("button");
    // copy "composite" and "button"
    {
      canvas.select(composite, button);
      // do copy
      IAction copyAction = getCopyAction();
      assertTrue(copyAction.isEnabled());
      copyAction.run();
    }
    // paste
    {
      IAction pasteAction = getPasteAction();
      assertTrue(pasteAction.isEnabled());
      pasteAction.run();
      // do paste
      {
        canvas.target(composite).outX(10).inY(10).move();
        canvas.click();
      }
      assertXML(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <Composite wbp:name='composite'>",
          "    <Composite.layout>",
          "      <FillLayout/>",
          "    </Composite.layout>",
          "    <Button wbp:name='button'/>",
          "  </Composite>",
          "  <Composite>",
          "    <Composite.layout>",
          "      <FillLayout/>",
          "    </Composite.layout>",
          "    <Button/>",
          "  </Composite>",
          "</Shell>");
    }
  }
}
