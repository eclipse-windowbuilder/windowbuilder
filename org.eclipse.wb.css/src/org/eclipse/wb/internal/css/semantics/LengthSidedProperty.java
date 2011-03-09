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
 * Property for four {@link LengthValue}'s.
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public final class LengthSidedProperty extends AbstractSidedProperty {
  private final LengthValue m_top = new LengthValue(this);
  private final LengthValue m_right = new LengthValue(this);
  private final LengthValue m_bottom = new LengthValue(this);
  private final LengthValue m_left = new LengthValue(this);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LengthSidedProperty(AbstractSemanticsComposite composite, String prefix, String suffix) {
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
  public LengthValue getTop() {
    return m_top;
  }

  public LengthValue getRight() {
    return m_right;
  }

  public LengthValue getBottom() {
    return m_bottom;
  }

  public LengthValue getLeft() {
    return m_left;
  }
}
