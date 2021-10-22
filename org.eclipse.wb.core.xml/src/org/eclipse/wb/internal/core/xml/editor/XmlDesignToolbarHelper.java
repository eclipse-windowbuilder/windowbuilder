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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.editor.DesignToolbarHelper;
import org.eclipse.wb.internal.core.xml.editor.actions.DesignPageActions;

import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.ToolBar;

/**
 * Helper for managing actions on internal {@link ToolBarManager} of {@link XmlEditorPage}.
 *
 * @author scheglov_ke
 * @coverage XML.editor
 */
public final class XmlDesignToolbarHelper extends DesignToolbarHelper {
  private DesignPageActions m_pageActions;

  //private ExternalizeStringsContributionItem m_externalizeItem;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public XmlDesignToolbarHelper(ToolBar toolBar) {
    super(toolBar);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Initializes with {@link DesignPageActions} and {@link IEditPartViewer}.
   */
  public void initialize(DesignPageActions pageActions, IEditPartViewer viewer) {
    super.initialize(viewer);
    m_pageActions = pageActions;
  }

  /**
   * Fills {@link ToolBar} with actions.
   */
  @Override
  public void fill() {
    /*{
    	m_toolBarManager.add(m_pageActions.getErrorsAction());
    	m_toolBarManager.add(new Separator());
    }*/
    {
      m_toolBarManager.add(m_pageActions.getTestAction());
      m_toolBarManager.add(m_pageActions.getRefreshAction());
      m_toolBarManager.add(new Separator());
    }
    super.fill();
    {
      m_toolBarManager.add(m_pageActions.getAssistantAction());
      m_toolBarManager.add(new Separator());
    }
    /*{
    	m_externalizeItem = new ExternalizeStringsContributionItem();
    	m_toolBarManager.add(m_externalizeItem);
    }*/
    super.fill2();
  }

  @Override
  public void setRoot(ObjectInfo rootObject) {
    super.setRoot(rootObject);
    //m_externalizeItem.setRoot((JavaInfo) rootObject);
    m_toolBarManager.getControl().getParent().layout();
  }
}