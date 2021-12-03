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
package org.eclipse.wb.internal.core.editor;

import org.eclipse.wb.core.controls.flyout.FlyoutControlComposite;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.ICommandExceptionHandler;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.editor.actions.DesignPageActions;
import org.eclipse.wb.internal.core.editor.actions.SelectSupport;
import org.eclipse.wb.internal.core.editor.palette.DesignerPalette;
import org.eclipse.wb.internal.core.model.DesignRootObject;
import org.eclipse.wb.internal.core.utils.Debug;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;

/**
 * {@link Composite} with GUI for visual design, i.e. properties table, palette, GEF.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public final class JavaDesignComposite extends DesignComposite {
  private DesignPageActions m_pageActions;
  private JavaDesignToolbarHelper m_toolbarHelper;
  private DesignerPalette m_designerPalette;
  private JavaInfo m_rootObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaDesignComposite(Composite parent,
      int style,
      IEditorPart editorPart,
      ICommandExceptionHandler exceptionHandler) {
    super(parent, style, editorPart, exceptionHandler);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation of UI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createDesignActions() {
    IEditPartViewer treeViewer = m_componentsComposite.getTreeViewer();
    m_pageActions = new DesignPageActions(m_editorPart, treeViewer);
    m_viewer.setContextMenu(new DesignContextMenuProvider(m_viewer, m_pageActions));
    // install dispose listener
    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        m_pageActions.dispose();
      }
    });
  }

  @Override
  protected void createDesignToolbarHelper() {
    m_toolbarHelper = new JavaDesignToolbarHelper(m_toolBar);
    m_toolbarHelper.initialize(m_pageActions, m_viewer);
    m_toolbarHelper.fill();
  }

  @Override
  protected void createPalette(FlyoutControlComposite gefComposite) {
    m_designerPalette = new DesignerPalette(gefComposite.getFlyoutParent(), SWT.NONE, true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notifies that "Design" page was activated.
   */
  public void onActivate() {
    m_pageActions.installActions();
  }

  /**
   * Notifies that "Design" page was deactivated.
   */
  public void onDeActivate() {
    m_pageActions.uninstallActions();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IAction} if it is implement by "Design" page, or <code>null</code>.
   */
  public IAction getAction(String actionID) {
    return m_pageActions.getAction(actionID);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Design access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * New model was parsed. We should display it.
   */
  @Override
  public void refresh(ObjectInfo rootObject, IProgressMonitor monitor) {
    m_rootObject = (JavaInfo) rootObject;
    // refresh viewer's
    {
      monitor.subTask("Updating GEF viewer...");
      monitor.worked(1);
      m_viewer.setInput(new DesignRootObject(m_rootObject));
      m_viewer.getControl().setDrawCached(false);
    }
    {
      monitor.subTask("Updating Property Composite...");
      monitor.worked(1);
      m_componentsComposite.setInput(m_viewer, m_rootObject);
    }
    {
      long start = System.currentTimeMillis();
      monitor.subTask("Loading palette...");
      monitor.worked(1);
      {
        String toolkitId = m_rootObject.getDescription().getToolkit().getId();
        m_designerPalette.setInput(m_viewer, m_rootObject, toolkitId);
      }
      Debug.println("palette: " + (System.currentTimeMillis() - start));
    }
    {
      monitor.subTask("Configuring errors action...");
      monitor.worked(1);
      m_pageActions.getErrorsAction().setRoot(m_rootObject);
    }
    // configure helpers
    m_pageActions.setRoot(m_rootObject);
    m_toolbarHelper.setRoot(m_rootObject);
    m_viewersComposite.setRoot(m_rootObject);
    new SelectSupport(rootObject, m_viewer, m_componentsComposite.getTreeViewer());
  }

  @Override
  public void disposeDesign() {
    super.disposeDesign();
    // clear palette
    if (!m_designerPalette.getControl().isDisposed()) {
      m_designerPalette.setInput(m_viewer, null, null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Structure/Palette reparenting access
  //
  ////////////////////////////////////////////////////////////////////////////
  private IExtractableControl m_extractablePalette;

  @Override
  public IExtractableControl getExtractablePalette() {
    if (m_extractablePalette == null) {
      m_extractablePalette = new ExtractableControl(m_designerPalette.getControl(), this);
    }
    return m_extractablePalette;
  }
}
