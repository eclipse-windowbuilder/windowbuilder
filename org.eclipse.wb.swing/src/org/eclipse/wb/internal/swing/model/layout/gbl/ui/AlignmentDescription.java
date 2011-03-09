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
package org.eclipse.wb.internal.swing.model.layout.gbl.ui;

import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;

/**
 * Description for {@link DimensionInfo} alignment.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout.ui
 */
public final class AlignmentDescription<A extends Enum<?>> {
  private final A m_alignment;
  private final String m_title;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AlignmentDescription(A alignment, String title) {
    m_alignment = alignment;
    m_title = title;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the alignment value.
   */
  public A getAlignment() {
    return m_alignment;
  }

  /**
   * @return the title for alignment.
   */
  public String getTitle() {
    return m_title;
  }
}
