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
package org.eclipse.wb.internal.rcp.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ICheckboxViewerWrapper;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Content provider for choose properties from Tree (properties + sub properties) without order
 * choosen properties.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public abstract class ChooseClassAndTreePropertiesUiContentProvider2
    extends
      ChooseClassAndTreePropertiesUiContentProvider {
  protected ICheckboxViewerWrapper m_propertiesViewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChooseClassAndTreePropertiesUiContentProvider2(ChooseClassAndPropertiesConfiguration configuration) {
    super(configuration);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContent(Composite parent, int columns) {
    super.createContent(parent, columns);
    GridDataFactory.modify(m_propertiesViewer.getViewer().getControl()).grab(true, false).minV(0).hintVC(
        10);
  }

  @Override
  protected Control createViewers(Composite parent) {
    m_propertiesViewer = createPropertiesViewer(parent);
    setPropertiesViewer(m_propertiesViewer);
    return m_propertiesViewer.getViewer().getControl();
  }
}