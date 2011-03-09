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
package org.eclipse.wb.internal.css.model.string;

import org.eclipse.wb.internal.css.model.CssNode;

/**
 * Node for string.
 * 
 * @author scheglov_ke
 * @coverage CSS.model
 */
public abstract class AbstractCssStringNode extends CssNode {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractCssStringNode() {
  }

  public AbstractCssStringNode(int offset, String value) {
    setOffset(offset);
    setLength(value.length());
    m_value = value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_value;

  public void setValue(String value) {
    String oldValue = m_value;
    m_value = value;
    firePropertyChanged(this, "value", oldValue, value);
  }

  public String getValue() {
    return m_value;
  }
}
