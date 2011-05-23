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

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.internal.core.editor.actions.SelectSupport;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;
import org.eclipse.wb.tests.gef.EventSender;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

/**
 * Test for {@link SelectSupport}.
 * 
 * @author scheglov_ke
 */
public class SelectSupportTest extends XwtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_all() throws Exception {
    XmlObjectInfo shell =
        openEditor(
            "<!-- filler filler filler filler filler -->",
            "<!-- filler filler filler filler filler -->",
            "<Shell text='Hello!'>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button_1' text='Button 1'/>",
            "  <Button wbp:name='button_2' text='Button 2'/>",
            "  <Text wbp:name='text_1' text='Text 1'/>",
            "  <Composite wbp:name='composite_1'>",
            "    <Button wbp:name='button_3' text='Button 3'/>",
            "  </Composite>",
            "</Shell>");
    XmlObjectInfo button_1 = getObjectByName("button_1");
    XmlObjectInfo button_2 = getObjectByName("button_2");
    XmlObjectInfo button_3 = getObjectByName("button_3");
    XmlObjectInfo text_1 = getObjectByName("text_1");
    XmlObjectInfo composite_1 = getObjectByName("composite_1");
    // use hot keys
    {
      // all
      canvas.deselectAll();
      {
        sendSelectKey(SWT.CTRL);
        canvas.assertSelection(shell, button_1, button_2, text_1, composite_1, button_3);
      }
      // same type 
      canvas.deselectAll();
      {
        canvas.select(button_1);
        sendSelectKey(SWT.CTRL | SWT.SHIFT);
        canvas.assertSelection(button_1, button_2, button_3);
      }
      // same parent 
      canvas.deselectAll();
      {
        canvas.select(text_1);
        sendSelectKey(SWT.CTRL | SWT.ALT);
        canvas.assertSelection(button_1, button_2, text_1, composite_1);
      }
    }
    // use context menu
    {
      IMenuManager contextMenu = getContextMenu(shell);
      IMenuManager selectMenu = findChildMenuManager(contextMenu, "Select");
      // all
      canvas.deselectAll();
      {
        findChildAction(selectMenu, "All").run();
        canvas.assertSelection(shell, button_1, button_2, text_1, composite_1, button_3);
      }
      // same type
      canvas.deselectAll();
      {
        canvas.select(button_1);
        findChildAction(selectMenu, "All of Same Type").run();
        canvas.assertSelection(button_1, button_2, button_3);
      }
      // same parent 
      canvas.deselectAll();
      {
        canvas.select(text_1);
        findChildAction(selectMenu, "All on Same Parent").run();
        canvas.assertSelection(button_1, button_2, text_1, composite_1);
      }
    }
  }

  public void test_disposeHierarchy() throws Exception {
    openEditor("<Shell/>");
    // reparse
    {
      IDesignPageSite.Helper.getSite(m_contentObject).reparse();
      fetchContentFields();
    }
    // selection hot keys still work
    canvas.deselectAll();
    {
      sendSelectKey(SWT.CTRL);
      canvas.assertSelection(m_contentObject);
    }
  }

  private void sendSelectKey(int stateMask) {
    Control control = m_viewerCanvas.getControl();
    EventSender eventSender = new EventSender(control);
    eventSender.setStateMask(stateMask);
    eventSender.keyDown('a');
  }
}
