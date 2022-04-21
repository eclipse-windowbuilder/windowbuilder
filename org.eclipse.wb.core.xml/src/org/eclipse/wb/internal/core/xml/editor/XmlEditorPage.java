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

/**
 * Abstract {@link IXmlEditorPage}.
 *
 * @author scheglov_ke
 * @coverage XML.editor
 */
public abstract class XmlEditorPage implements IXmlEditorPage {
  protected AbstractXmlEditor m_editor;
  protected boolean m_active;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void initialize(AbstractXmlEditor editor) {
    m_editor = editor;
  }

  @Override
  public void dispose() {
  }

  @Override
  public void setActive(boolean active) {
    m_active = active;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Page
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_pageIndex;

  @Override
  public int getPageIndex() {
    return m_pageIndex;
  }

  @Override
  public void setPageIndex(int index) {
    m_pageIndex = index;
  }
}