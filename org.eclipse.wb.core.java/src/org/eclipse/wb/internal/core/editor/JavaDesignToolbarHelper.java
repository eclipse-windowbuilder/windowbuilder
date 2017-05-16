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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.editor.actions.DesignPageActions;
import org.eclipse.wb.internal.core.nls.ExternalizeStringsContributionItem;

import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.ToolBar;

/**
 * Helper for managing actions on internal {@link ToolBarManager} of {@link DesignPage}.
 *
 * @author lobas_av
 * @author scheglov_ke
 * @coverage core.editor
 */
public final class JavaDesignToolbarHelper extends DesignToolbarHelper {
  private DesignPageActions m_pageActions;
  private ExternalizeStringsContributionItem m_externalizeItem;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaDesignToolbarHelper(ToolBar toolBar) {
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
    {
      m_toolBarManager.add(m_pageActions.getErrorsAction());
      m_toolBarManager.add(new Separator());
    }
    {
      m_toolBarManager.add(m_pageActions.getTestAction());
      m_toolBarManager.add(m_pageActions.getRefreshAction());
      m_toolBarManager.add(new Separator());
    }
    super.fill();
    /* $if oem.name != "RIM" $ */
    {
      m_toolBarManager.add(m_pageActions.getAssistantAction());
      m_toolBarManager.add(new Separator());
    }
    {
      m_externalizeItem = new ExternalizeStringsContributionItem();
      m_toolBarManager.add(m_externalizeItem);
    }
    /* $endif$ */
    super.fill2();
  }

  /**
   * Sets the root {@link JavaInfo} on {@link DesignPage}.
   */
  @Override
  public void setRoot(ObjectInfo rootObject) {
    super.setRoot(rootObject);
    /* $if oem.name != "RIM" $ */
    m_externalizeItem.setRoot((JavaInfo) rootObject);
    /* $endif$ */
    m_toolBarManager.getControl().getParent().layout();
  }
}