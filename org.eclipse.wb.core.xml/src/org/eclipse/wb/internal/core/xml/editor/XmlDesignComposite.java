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

import org.eclipse.wb.core.controls.flyout.FlyoutControlComposite;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.ICommandExceptionHandler;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.editor.DesignComposite;
import org.eclipse.wb.internal.core.editor.actions.SelectSupport;
import org.eclipse.wb.internal.core.utils.Debug;
import org.eclipse.wb.internal.core.xml.Messages;
import org.eclipse.wb.internal.core.xml.editor.actions.DesignPageActions;
import org.eclipse.wb.internal.core.xml.editor.palette.DesignerPalette;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;

/**
 * {@link DesignComposite} for XML.
 *
 * @author scheglov_ke
 * @coverage XML.editor
 */
public class XmlDesignComposite extends DesignComposite {
  private DesignPageActions m_pageActions;
  private XmlDesignToolbarHelper m_toolbarHelper;
  private DesignerPalette m_designerPalette;
  private XmlObjectInfo m_rootObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public XmlDesignComposite(Composite parent,
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
    m_toolbarHelper = new XmlDesignToolbarHelper(m_toolBar);
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
  // Design access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh(ObjectInfo rootObject, IProgressMonitor monitor) {
    m_rootObject = (XmlObjectInfo) rootObject;
    // refresh viewer's
    {
      monitor.subTask(Messages.XmlDesignComposite_statusGef);
      monitor.worked(1);
      m_viewer.setInput(m_rootObject);
      m_viewer.getControl().setDrawCached(false);
    }
    {
      monitor.subTask(Messages.XmlDesignComposite_statusProperties);
      monitor.worked(1);
      m_componentsComposite.setInput(m_viewer, m_rootObject);
    }
    {
      long start = System.currentTimeMillis();
      monitor.subTask(Messages.XmlDesignComposite_statucPalette);
      monitor.worked(1);
      {
        String toolkitId = m_rootObject.getDescription().getToolkit().getId();
        m_designerPalette.setInput(m_viewer, m_rootObject, toolkitId);
      }
      Debug.println("palette: " + (System.currentTimeMillis() - start));
    }
    /*{
    	monitor.subTask("Configuring errors action...");
    	monitor.worked(1);
    	m_pageActions.getErrorsAction().setRoot(m_rootObject);
    }*/
    // configure helpers
    m_pageActions.setRoot(m_rootObject);
    m_toolbarHelper.setRoot(m_rootObject);
    m_viewersComposite.setRoot(m_rootObject);
    new SelectSupport(rootObject, m_viewer, m_componentsComposite.getTreeViewer());
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
