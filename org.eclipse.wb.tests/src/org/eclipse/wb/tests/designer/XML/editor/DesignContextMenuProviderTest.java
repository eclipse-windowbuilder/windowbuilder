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

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.editor.DesignContextMenuProvider;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.gef.core.AbstractEditPartViewer;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Menu;

/**
 * Test for {@link DesignContextMenuProvider}.
 * 
 * @author scheglov_ke
 */
public class DesignContextMenuProviderTest extends XwtGefTest {
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
   * Test that context menu exists for "tree" and "canvas".
   */
  public void test_hasMenu() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell/>");
    assertHasContextMenu(m_viewerTree);
    assertHasContextMenu(m_viewerCanvas);
  }

  public void test_noSelection() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell/>");
    // no selection, so not actions
    IAction action = getContextMenuAction("Refresh");
    assertNull(action);
  }

  public void test_refreshAction() throws Exception {
    XmlObjectInfo shell =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell/>");
    canvas.select(shell);
    // run "Refresh" action
    IAction action = getContextMenuAction("Refresh");
    assertNotNull(action);
    action.run();
    // assert that refresh was performed
    fetchContentFields();
    assertNotSame(shell, m_lastObject);
  }

  public void test_testAction() throws Exception {
    XmlObjectInfo shell =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell/>");
    canvas.select(shell);
    // run "Test" action
    IAction action = getContextMenuAction("Test/Preview...");
    assertNotNull(action);
  }

  public void test_deleteAction() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    // select "button"
    XmlObjectInfo button = getObjectByName("button");
    canvas.select(button);
    // run "Delete" action
    IAction action = getContextMenuAction("Delete");
    assertNotNull(action);
    action.run();
    // assert that "button" was deleted
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_contentMenuInTree() throws Exception {
    XmlObjectInfo shell =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell/>");
    canvas.select(shell);
    // run "Refresh" action
    IAction action = getContextMenuAction(m_viewerTree, "Refresh");
    assertNotNull(action);
    action.run();
    // assert that refresh was performed
    fetchContentFields();
    assertNotSame(shell, m_lastObject);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void assertHasContextMenu(AbstractEditPartViewer viewer) {
    Menu menu = viewer.getControl().getMenu();
    assertNotNull(menu);
    assertFalse(menu.isDisposed());
  }

  /**
   * @return the {@link IAction} from context menu of canvas.
   */
  private IAction getContextMenuAction(String text) throws Exception {
    return getContextMenuAction(m_viewerCanvas, text);
  }

  /**
   * @return the {@link IAction} from context menu of {@link IEditPartViewer}.
   */
  private IAction getContextMenuAction(IEditPartViewer viewer, String text) throws Exception {
    MenuManager contextMenu = (MenuManager) ReflectionUtils.getFieldObject(viewer, "m_contextMenu");
    ReflectionUtils.invokeMethod(
        contextMenu,
        "fireAboutToShow(org.eclipse.jface.action.IMenuManager)",
        contextMenu);
    return findChildAction(contextMenu, text);
  }
}
