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
 * Composite property for font.
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public class FontProperty extends AbstractSemanticsComposite {
  private final SimpleValue m_family = mapSimpleProperty(this, "font-family");
  private final SimpleValue m_style = mapSimpleProperty(this, "font-style");
  private final SimpleValue m_variant = mapSimpleProperty(this, "font-variant");
  private final SimpleValue m_weight = mapSimpleProperty(this, "font-weight");
  private final SimpleValue m_stretch = mapSimpleProperty(this, "font-stretch");
  private final LengthValue m_size = mapLengthProperty(this, "font-size");
  private final SimpleValue m_sizeAdjust = mapSimpleProperty(this, "font-size-adjust");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FontProperty(AbstractSemanticsComposite composite) {
    super(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleValue getFamily() {
    return m_family;
  }

  public SimpleValue getStyle() {
    return m_style;
  }

  public SimpleValue getVariant() {
    return m_variant;
  }

  public SimpleValue getWeight() {
    return m_weight;
  }

  public SimpleValue getStretch() {
    return m_stretch;
  }

  public LengthValue getSize() {
    return m_size;
  }

  public SimpleValue getSizeAdjust() {
    return m_sizeAdjust;
  }
}
