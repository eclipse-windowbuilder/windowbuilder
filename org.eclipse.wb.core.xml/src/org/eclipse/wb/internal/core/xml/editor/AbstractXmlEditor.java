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
package org.eclipse.wb.internal.core.xml.editor;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.editor.DesignComposite;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.ui.TabFolderDecorator;
import org.eclipse.wb.internal.core.views.IDesignCompositeProvider;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import java.util.List;

/**
 * Editor for any XML based UI.
 * 
 * @author scheglov_ke
 * @coverage XML.editor
 */
public abstract class AbstractXmlEditor extends MultiPageEditorPart
    implements
      IDesignCompositeProvider {
  private static final String CONTEXT_ID = "org.eclipse.wb.core.xml.editorScope";
  protected StructuredTextEditor m_xmlEditor;
  private final SourcePage m_sourcePage = new SourcePage();
  private XmlDesignPage m_designPage;
  private final List<IXmlEditorPage> m_additionalPages = Lists.newArrayList();
  private IXmlEditorPage m_activePage = m_sourcePage;
  private String m_cleanSource;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void dispose() {
    m_sourcePage.dispose();
    m_designPage.dispose();
    for (IXmlEditorPage page : m_additionalPages) {
      page.dispose();
    }
    super.dispose();
  }

  @Override
  protected void setInput(IEditorInput input) {
    super.setInput(input);
    initializeTitle(input);
  }

  private void initializeTitle(IEditorInput input) {
    if (input != null) {
      String title = input.getName();
      setPartName(title);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EditorPart
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
    if (!(editorInput instanceof IFileEditorInput)) {
      throw new PartInitException("Invalid Input: Must be IFileEditorInput");
    }
    super.init(site, editorInput);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IDocument} from XML editor.
   */
  public final IDocument getDocument() {
    return m_xmlEditor.getTextViewer().getDocument();
  }

  /**
   * @return the "XML" source editor.
   */
  public final StructuredTextEditor getXMLEditor() {
    return m_xmlEditor;
  }

  /**
   * @return the {@link XmlDesignPage} which is used as "Design" page.
   */
  public final XmlDesignPage getDesignPage() {
    return m_designPage;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  @SuppressWarnings("rawtypes")
  public Object getAdapter(Class adapter) {
    if (adapter == StructuredTextEditor.class) {
      return m_xmlEditor;
    }
    return super.getAdapter(adapter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDesignCompositeProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignComposite getDesignComposite() {
    return m_designPage.getDesignComposite();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Save
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void doSave(IProgressMonitor monitor) {
    rememberSourceContent();
    getEditor(0).doSave(monitor);
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  @Override
  public void doSaveAs() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createPages() {
    {
      CTabFolder tabFolder = (CTabFolder) getContainer();
      TabFolderDecorator.decorate(this, tabFolder);
    }
    createPageXML();
    createPageDesign();
    createAdditionalPages();
    activateEditorContext();
  }

  /**
   * Activates context of our XML editor.
   */
  private void activateEditorContext() {
    IContextService contextService = (IContextService) getSite().getService(IContextService.class);
    if (contextService != null) {
      contextService.activateContext(CONTEXT_ID);
    }
  }

  /**
   * Creates "XML Source" page of multi-page editor.
   */
  private void createPageXML() {
    try {
      m_sourcePage.initialize(this);
      //
      m_xmlEditor = createEditorXML();
      int index = addPage(m_xmlEditor, getEditorInput());
      m_sourcePage.setPageIndex(index);
      //
      setPageText(index, m_sourcePage.getName());
      setPageImage(index, m_sourcePage.getImage());
      trackDirty();
    } catch (PartInitException e) {
      ErrorDialog.openError(
          getSite().getShell(),
          "Error creating nested XML editor",
          null,
          e.getStatus());
    }
  }

  /**
   * @return the {@link StructuredTextEditor} to use as XML editor.
   */
  protected StructuredTextEditor createEditorXML() {
    return new StructuredTextEditor();
  }

  /**
   * Creates "Design" page of multi-page editor.
   */
  private void createPageDesign() {
    m_designPage = createDesignPage();
    addPage(m_designPage);
  }

  /**
   * Create additional pages.
   */
  private void createAdditionalPages() {
    List<IXmlEditorPageFactory> factories =
        ExternalFactoriesHelper.getElementsInstances(
            IXmlEditorPageFactory.class,
            "org.eclipse.wb.core.xml.XMLEditorPageFactories",
            "factory");
    for (IXmlEditorPageFactory factory : factories) {
      factory.createPages(this, m_additionalPages);
    }
    // initialize created pages
    for (IXmlEditorPage page : m_additionalPages) {
      page.initialize(this);
      addPage(page);
    }
  }

  /**
   * Add {@link IXmlEditorPage} page to this editor.
   */
  private void addPage(IXmlEditorPage page) {
    page.initialize(this);
    // create/add control
    Control control = page.createControl(getContainer());
    int pageIndex = addPage(control);
    page.setPageIndex(pageIndex);
    // presentation
    setPageText(pageIndex, page.getName());
    setPageImage(pageIndex, page.getImage());
  }

  /**
   * @return the {@link XmlDesignPage} to be used as "Design" page.
   */
  protected abstract XmlDesignPage createDesignPage();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dirty flag tracking
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * WST SSE has bug with undo/redo and "dirty" flag. So, we need to track "dirty" flag manually.
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=138100
   */
  private void trackDirty() {
    rememberSourceContent();
    getDocument().addDocumentListener(new IDocumentListener() {
      public void documentChanged(DocumentEvent event) {
        firePropertyChange(PROP_DIRTY);
      }

      public void documentAboutToBeChanged(DocumentEvent event) {
      }
    });
  }

  private void rememberSourceContent() {
    m_cleanSource = getDocument().get();
  }

  @Override
  public boolean isDirty() {
    return !getDocument().get().equals(m_cleanSource);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Page access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void pageChange(int pageIndex) {
    super.pageChange(pageIndex);
    // deactivate active page
    if (m_activePage != null) {
      m_activePage.setActive(false);
      m_activePage = null;
    }
    // prepare new active page
    if (pageIndex == m_sourcePage.getPageIndex()) {
      m_activePage = m_sourcePage;
    } else if (pageIndex == m_designPage.getPageIndex()) {
      m_activePage = m_designPage;
    } else {
      for (IXmlEditorPage page : m_additionalPages) {
        if (pageIndex == page.getPageIndex()) {
          m_activePage = page;
          break;
        }
      }
    }
    // activate new active page
    if (m_activePage != null) {
      m_activePage.setActive(true);
    }
  }

  /**
   * Switches between "Source" and "Design" pages.
   */
  public void switchSourceDesign() {
    if (m_activePage == m_sourcePage) {
      setActivePage(m_designPage);
    } else {
      setActivePage(m_sourcePage);
    }
  }

  /**
   * Shows "XML Source" page.
   */
  public void showSource() {
    if (m_activePage != m_sourcePage) {
      setActivePage(m_sourcePage);
    }
  }

  /**
   * Shows "Design" page.
   */
  public void showDesign() {
    if (m_activePage != m_designPage) {
      setActivePage(m_designPage);
    }
  }

  /**
   * Shows given {@link IXmlEditorPage}.
   */
  private void setActivePage(IXmlEditorPage page) {
    setActivePage(page.getPageIndex());
  }

  /**
   * Moves cursor to given position in "XML Source" editor.
   */
  public void showSourcePosition(final int position) {
    ExecutionUtils.runLogLater(new RunnableEx() {
      public void run() throws Exception {
        m_xmlEditor.selectAndReveal(position, 0);
      }
    });
  }
}
