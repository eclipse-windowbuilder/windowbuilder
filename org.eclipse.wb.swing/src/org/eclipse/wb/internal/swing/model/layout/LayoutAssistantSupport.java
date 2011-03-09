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
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.core.model.ObjectInfo;

/**
 * Swing provider for layout assistant pages.
 * 
 * @author lobas_av
 * @coverage swing.assistant
 */
public class LayoutAssistantSupport
    extends
      org.eclipse.wb.core.editor.actions.assistant.LayoutAssistantSupport {
  protected final LayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutAssistantSupport(LayoutInfo layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // 
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final String getConstraintsPageTitle() {
    return "Constraints";
  }

  @Override
  protected final ObjectInfo getContainer() {
    return m_layout.getContainer();
  }
}