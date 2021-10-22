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

import org.eclipse.wb.core.controls.palette.PaletteComposite;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.editor.structure.components.IComponentsTree;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;
import org.eclipse.wb.internal.core.xml.editor.SourcePage;
import org.eclipse.wb.internal.core.xml.editor.XmlDesignPage;
import org.eclipse.wb.internal.core.xml.editor.actions.DesignPageActions;
import org.eclipse.wb.internal.core.xml.editor.palette.DesignerPalette;
import org.eclipse.wb.internal.core.xml.editor.palette.PaletteManager;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.gef.graphical.GraphicalViewer;
import org.eclipse.wb.internal.gef.tree.TreeViewer;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.XML.AbstractXmlObjectTest;
import org.eclipse.wb.tests.gef.GraphicalRobot;
import org.eclipse.wb.tests.gef.TreeRobot;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDE;

/**
 * Test for {@link AbstractXmlEditor} and its parts.
 *
 * @author scheglov_ke
 */
public abstract class AbstractXmlGefTest extends AbstractXmlObjectTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    waitEventLoop(1);
    System.setProperty(DesignerPalette.FLAG_NO_PALETTE, "true");
    addExceptionsListener();
  }

  @Override
  protected void tearDown() throws Exception {
    System.clearProperty(DesignerPalette.FLAG_NO_PALETTE);
    waitEventLoop(0);
    TestUtils.closeAllEditors();
    waitEventLoop(0);
    // check for exceptions
    {
      removeExceptionsListener();
      assertNoLoggedExceptions();
    }
    // continue
    waitEventLoop(0);
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Open "Design" and fetch
  //
  ////////////////////////////////////////////////////////////////////////////
  protected AbstractXmlEditor m_designerEditor;
  protected SourcePage m_sourcePage;
  protected XmlDesignPage m_designPage;
  protected DesignPageActions m_designPageActions;
  protected DesignerPalette m_designerPalette;
  protected PaletteComposite m_paletteComposite;
  protected IComponentsTree m_componentsTree;
  protected PropertyTable m_propertyTable;
  protected PaletteManager m_paletteManager;
  // GEF
  protected XmlObjectInfo m_contentObject;
  protected GraphicalEditPart m_contentEditPart;
  // canvas event sender
  protected GraphicalViewer m_viewerCanvas;
  protected GraphicalRobot canvas;
  // tree event sender
  protected TreeViewer m_viewerTree;
  protected TreeRobot tree;

  /**
   * Opens given {@link IFile} in new editor and shows "Design" page.
   */
  protected final void openDesign(IFile file) throws Exception {
    openEditor(file);
    openDesignPage();
    fetchContentFields();
  }

  /**
   * Opens {@link AbstractXmlEditor} with given {@link IFile}.
   */
  protected final void openEditor(IFile file) throws Exception {
    // prepare MultiPageEditor
    IWorkbenchPage activePage = DesignerPlugin.getActiveWorkbenchWindow().getActivePage();
    m_designerEditor = (AbstractXmlEditor) IDE.openEditor(activePage, file, getEditorID());
    assertNotNull(m_designerEditor);
    // maximize editor
    activePage.toggleZoom(activePage.getActivePartReference());
    waitEventLoop(0);
    // fetch DesignPage parts
    fetchDesignViewers();
  }

  /**
   * @return the ID of {@link AbstractXmlEditor} to open.
   */
  protected abstract String getEditorID();

  /**
   * Opens "Source" page of current {@link AbstractXmlEditor}.
   */
  protected final void openSourcePage() throws Exception {
    m_designerEditor.showSource();
    waitEventLoop(0);
  }

  /**
   * Opens "Design" page of current {@link AbstractXmlEditor}.
   */
  protected final void openDesignPage() throws Exception {
    m_designerEditor.showDesign();
    waitEventLoop(0);
  }

  /**
   * Fills design field - edit part viewers, etc. Creating robots.<br>
   * We should do this after opening "Design" page.
   */
  private void fetchDesignViewers() {
    m_sourcePage = m_designerEditor.getSourcePage();
    // prepare DesignPage and DesignComposite
    m_designPage = m_designerEditor.getDesignPage();
    Object designComposite = ReflectionUtils.getFieldObject(m_designPage, "m_designComposite");
    // DesignComposite parts
    m_designPageActions =
        (DesignPageActions) ReflectionUtils.getFieldObject(designComposite, "m_pageActions");
    m_designerPalette =
        (DesignerPalette) ReflectionUtils.getFieldObject(designComposite, "m_designerPalette");
    m_paletteComposite = (PaletteComposite) m_designerPalette.getControl();
    // prepare GraphicalViewer
    {
      m_viewerCanvas =
          (GraphicalViewer) ReflectionUtils.getFieldObject(designComposite, "m_viewer");
      assertNotNull(m_viewerCanvas);
      // prepare robot
      canvas = new GraphicalRobot(m_viewerCanvas);
    }
    // prepare TreeViewer
    {
      Object componentsComposite =
          ReflectionUtils.getFieldObject(designComposite, "m_componentsComposite");
      Object treePage = ReflectionUtils.getFieldObject(componentsComposite, "m_treePage");
      m_viewerTree = (TreeViewer) ReflectionUtils.getFieldObject(treePage, "m_viewer");
      assertNotNull(m_viewerTree);
      // prepare robot
      tree = new TreeRobot(m_viewerTree);
    }
  }

  /**
   * Fills content field - {@link #m_contentEditPart}, etc.
   * <p>
   * We should do this after opening "Design" page and after undo/redo.
   */
  protected void fetchContentFields() {
    m_contentEditPart = (GraphicalEditPart) m_viewerCanvas.getRootContainer().getContent();
    if (m_contentEditPart == null) {
      return;
    }
    m_contentObject = (XmlObjectInfo) m_contentEditPart.getModel();
    m_lastObject = m_contentObject;
    m_lastContext = m_lastObject.getContext();
    m_lastLoader = m_lastContext.getClassLoader();
    {
      UiContext uiContext = new UiContext();
      uiContext.useShell(DesignerPlugin.getShell().getText());
      m_propertyTable = uiContext.findFirstWidget(PropertyTable.class);
    }
    // DesignPageSite
    {
      DesignPageSite designPageSite = DesignPageSite.Helper.getSite(m_contentObject);
      m_componentsTree = designPageSite.getComponentTree();
    }
    // PaletteManager
    m_paletteManager =
        (PaletteManager) ReflectionUtils.getFieldObject(m_designerPalette, "m_manager");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the "undo" action.
   */
  protected final IAction getUndoAction() {
    IActionBars actionBars = m_designerEditor.getEditorSite().getActionBars();
    return actionBars.getGlobalActionHandler(ActionFactory.UNDO.getId());
  }

  /**
   * @return the "redo" action.
   */
  protected final IAction getRedoAction() {
    IActionBars actionBars = m_designerEditor.getEditorSite().getActionBars();
    return actionBars.getGlobalActionHandler(ActionFactory.REDO.getId());
  }

  /**
   * @return the "delete" action.
   */
  protected final IAction getDeleteAction() {
    IActionBars actionBars = m_designerEditor.getEditorSite().getActionBars();
    return actionBars.getGlobalActionHandler(ActionFactory.DELETE.getId());
  }

  /**
   * @return the "cut" action.
   */
  protected final IAction getCutAction() {
    IActionBars actionBars = m_designerEditor.getEditorSite().getActionBars();
    return actionBars.getGlobalActionHandler(ActionFactory.CUT.getId());
  }

  /**
   * @return the "copy" action.
   */
  protected final IAction getCopyAction() {
    IActionBars actionBars = m_designerEditor.getEditorSite().getActionBars();
    return actionBars.getGlobalActionHandler(ActionFactory.COPY.getId());
  }

  /**
   * @return the "paste" action.
   */
  protected final IAction getPasteAction() {
    IActionBars actionBars = m_designerEditor.getEditorSite().getActionBars();
    return actionBars.getGlobalActionHandler(ActionFactory.PASTE.getId());
  }

  /**
   * Selects single object and then uses Copy/Paste actions.
   */
  protected final void doCopyPaste(Object object) {
    // copy
    {
      // select "javaInfo"
      canvas.select(object);
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
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts selection range in "XML" editor.
   */
  protected final void assertXMLSelection(int expectedOffset, int expectedLength) {
    ISelectionProvider selectionProvider = m_sourcePage.getXmlEditor().getSelectionProvider();
    ITextSelection selection = (ITextSelection) selectionProvider.getSelection();
    assertEquals(expectedOffset, selection.getOffset());
    assertEquals(expectedLength, selection.getLength());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GEF Tools
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Loads {@link CreationTool} for creating component with given class name.
   */
  protected final <T extends XmlObjectInfo> T loadCreationTool(String componentClassName)
      throws Exception {
    return loadCreationTool(componentClassName, null);
  }

  /**
   * Loads {@link CreationTool} for creating component with given class name.
   */
  @SuppressWarnings("unchecked")
  protected final <T extends XmlObjectInfo> T loadCreationTool(String componentClassName,
      String creationId) throws Exception {
    // prepare new component
    XmlObjectInfo newComponent;
    {
      newComponent =
          XmlObjectUtils.createObject(
              m_lastContext,
              componentClassName,
              new ElementCreationSupport(creationId));
      newComponent = XmlObjectUtils.getWrapped(newComponent);
      newComponent.putArbitraryValue(XmlObjectInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
    }
    // load CreationTool
    final XmlObjectInfo finalNewComponent = newComponent;
    ICreationFactory factory = new ICreationFactory() {
      public void activate() {
      }

      public Object getNewObject() {
        return finalNewComponent;
      }
    };
    CreationTool creationTool = new CreationTool(factory);
    m_viewerCanvas.getEditDomain().setActiveTool(creationTool);
    // return component that will be added
    return (T) newComponent;
  }
}
