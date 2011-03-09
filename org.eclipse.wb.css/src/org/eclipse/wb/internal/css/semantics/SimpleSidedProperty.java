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
package org.eclipse.wb.internal.css.semantics;

/**
 * Property for four {@link SimpleValue}'s.
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public final class SimpleSidedProperty extends AbstractSidedProperty {
  private final SimpleValue m_top = new SimpleValue(this);
  private final SimpleValue m_right = new SimpleValue(this);
  private final SimpleValue m_bottom = new SimpleValue(this);
  private final SimpleValue m_left = new SimpleValue(this);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleSidedProperty(AbstractSemanticsComposite composite, String prefix, String suffix) {
    super(composite, prefix, suffix);
    setValue(SIDE_TOP, m_top);
    setValue(SIDE_RIGHT, m_right);
    setValue(SIDE_BOTTOM, m_bottom);
    setValue(SIDE_LEFT, m_left);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleValue getTop() {
    return m_top;
  }

  public SimpleValue getRight() {
    return m_right;
  }

  public SimpleValue getBottom() {
    return m_bottom;
  }

  public SimpleValue getLeft() {
    return m_left;
  }
}
