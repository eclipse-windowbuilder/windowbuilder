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
package org.eclipse.wb.internal.core.views;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.editor.DesignComposite;
import org.eclipse.wb.internal.core.editor.DesignComposite.IExtractableControl;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.ViewPart;

import java.util.Map;

/**
 * Abstract {@link ViewPart} for displaying in it some {@link IExtractableControl} from
 * {@link DesignComposite}.
 *
 * @author scheglov_ke
 * @coverage core.views
 */
public abstract class AbstractExtractableDesignView extends PageBookView {
  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void init(IViewSite site) throws PartInitException {
    super.init(site);
  }

  @Override
  public void createPartControl(Composite parent) {
    super.createPartControl(parent);
    hookIntoWorkbench();
    // simulate activation for all opened editors,
    // because it is possible that more than one of them are visible
    parent.getDisplay().asyncExec(new Runnable() {
      public void run() {
        IWorkbenchPage activePage = getSite().getWorkbenchWindow().getActivePage();
        IWorkbenchPart activePart = activePage.getActivePart();
        // show pages for all editors
        IEditorReference[] editorReferences = activePage.getEditorReferences();
        for (IEditorReference editorReference : editorReferences) {
          IEditorPart editor = editorReference.getEditor(false);
          if (isImportant(editor)) {
            partActivated(editor);
          }
        }
        // show page for original active part
        partActivated(activePart);
      }
    });
  }

  /**
   * Installs {@link IPerspectiveListener2} for intercepting closing this {@link ViewPart} before
   * {@link #dispose()}. Problem with {@link #dispose()} is that at this time all {@link Control}'s
   * are already disposed, including {@link IExtractableControl}'s, that we "borrowed" from
   * {@link DesignComposite}. So, we need some early notification to return
   * {@link IExtractableControl} back.
   */
  private void hookIntoWorkbench() {
    final IWorkbenchPage page = getSite().getPage();
    // track this view visible/hide events
    final IPartListener2 partListener = new IPartListener2() {
      public void partVisible(IWorkbenchPartReference partRef) {
        // some "part" become visible, if this means that "editor" restored, do extract
        if (!isEditorMaximized()) {
          doExtract();
        }
      }

      public void partHidden(IWorkbenchPartReference partRef) {
        // some "part" become hidden, if this means that "editor" maximized, do restore;
        // do in async, because editor state updated after "partHidden" event
        Display.getCurrent().asyncExec(new Runnable() {
          public void run() {
            if (isEditorMaximized()) {
              doRestore();
            }
          }
        });
      }

      public void partClosed(IWorkbenchPartReference partRef) {
        // if this "part" closed, do restore
        if (partRef.getPart(false) == AbstractExtractableDesignView.this) {
          doRestore();
        }
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Utils
      //
      ////////////////////////////////////////////////////////////////////////////
      private boolean isEditorMaximized() {
        IEditorReference[] editorReferences = page.getEditorReferences();
        for (IEditorReference editorReference : editorReferences) {
          if (page.getPartState(editorReference) == IWorkbenchPage.STATE_MAXIMIZED) {
            return true;
          }
        }
        return false;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Unused
      //
      ////////////////////////////////////////////////////////////////////////////
      public void partOpened(IWorkbenchPartReference partRef) {
      }

      public void partActivated(IWorkbenchPartReference partRef) {
      }

      public void partDeactivated(IWorkbenchPartReference partRef) {
      }

      public void partBroughtToTop(IWorkbenchPartReference partRef) {
      }

      public void partInputChanged(IWorkbenchPartReference partRef) {
      }
    };
    page.addPartListener(partListener);
    // remove perspective listener on dispose
    getPageBook().addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        page.removePartListener(partListener);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Extract/restore
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Extract all registered {@link IExtractableControl}'s.
   */
  private void doExtract() {
    for (IExtractableControl extractableControl : m_extractableControls.values()) {
      extractableControl.extract(getPageBook());
    }
  }

  /**
   * Restores all registered {@link IExtractableControl}'s.
   */
  private void doRestore() {
    for (IExtractableControl extractableControl : m_extractableControls.values()) {
      extractableControl.restore();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PageBookView
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<IWorkbenchPart, IExtractableControl> m_extractableControls = Maps.newHashMap();

  @Override
  protected IPage createDefaultPage(PageBook book) {
    MessagePage page = new MessagePage();
    page.createControl(getPageBook());
    initPage(page);
    return page;
  }

  @Override
  protected PageRec doCreatePage(final IWorkbenchPart part) {
    // prepare extractable
    final IExtractableControl extractableControl;
    {
      IDesignCompositeProvider provider = (IDesignCompositeProvider) part;
      DesignComposite designComposite = provider.getDesignComposite();
      extractableControl = getExtractableControl(designComposite);
      m_extractableControls.put(part, extractableControl);
    }
    // create page
    final Control extractedControl = extractableControl.getControl();
    IPageBookViewPage page = new Page() {
      @Override
      public void createControl(Composite parent) {
        extractableControl.extract(parent);
      }

      @Override
      public Control getControl() {
        return extractedControl.getParent() == getPageBook() ? extractedControl : null;
      }

      @Override
      public void setFocus() {
        extractedControl.setFocus();
      }
    };
    initPage(page);
    page.createControl(getPageBook());
    return new PageRec(part, page);
  }

  @Override
  protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
    m_extractableControls.remove(part);
  }

  @Override
  protected IWorkbenchPart getBootstrapPart() {
    return getSite().getPage().getActiveEditor();
  }

  @Override
  protected boolean isImportant(IWorkbenchPart part) {
    return part instanceof IDesignCompositeProvider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IExtractableControl} to extract from {@link DesignComposite}.
   */
  protected abstract IExtractableControl getExtractableControl(DesignComposite designComposite);
}
