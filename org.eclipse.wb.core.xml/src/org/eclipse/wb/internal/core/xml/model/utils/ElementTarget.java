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
package org.eclipse.wb.internal.core.xml.model.utils;

import org.eclipse.wb.internal.core.utils.xml.DocumentElement;

/**
 * Target in {@link DocumentElement}.
 * 
 * @author scheglov_ke
 * @coverage XML.model.utils
 */
public final class ElementTarget {
  private final DocumentElement m_element;
  private final int m_index;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ElementTarget(DocumentElement element, int index) {
    m_element = element;
    m_index = index;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the target (i.e. parent) {@link DocumentElement}.
   */
  public DocumentElement getElement() {
    return m_element;
  }

  /**
   * @return the index to add new {@link DocumentElement} at.
   */
  public int getIndex() {
    return m_index;
  }
}
