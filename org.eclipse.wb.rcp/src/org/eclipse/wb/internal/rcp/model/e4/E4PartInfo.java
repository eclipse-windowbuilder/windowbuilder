/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.e4;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

/**
 * Model for any E4-like GUI component.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.e4
 */
public class E4PartInfo extends CompositeInfo {
  private Shell m_shell;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public E4PartInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    //JavaInfoUtils.scheduleSpecialRendering(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractComponentInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /*@Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new E4PartTopBoundsSupport(this);
  }*/
  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getComponentObject() {
    return m_shell;
  }

  /**
   * @return the top level {@link Shell}.
   */
  public Shell getShell() {
    return m_shell;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_create() throws Exception {
    m_shell = new Shell(SWT.NONE);
    m_shell.setLayout(new FillLayout());
    setObject(m_shell);
    super.refresh_create();
  }

  @Override
  public void refresh_dispose() throws Exception {
    if (m_shell != null) {
      m_shell.dispose();
      m_shell = null;
    }
    super.refresh_dispose();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    // TODO Auto-generated method stub
    super.refresh_fetch();
  }
}
