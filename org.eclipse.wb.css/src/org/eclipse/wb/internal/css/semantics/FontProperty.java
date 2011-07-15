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
  private final SimpleValue family = mapSimpleProperty(this, "font-family");
  private final SimpleValue style = mapSimpleProperty(this, "font-style");
  private final SimpleValue variant = mapSimpleProperty(this, "font-variant");
  private final SimpleValue weight = mapSimpleProperty(this, "font-weight");
  private final SimpleValue stretch = mapSimpleProperty(this, "font-stretch");
  private final LengthValue size = mapLengthProperty(this, "font-size");
  private final SimpleValue sizeAdjust = mapSimpleProperty(this, "font-size-adjust");

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
    return family;
  }

  public SimpleValue getStyle() {
    return style;
  }

  public SimpleValue getVariant() {
    return variant;
  }

  public SimpleValue getWeight() {
    return weight;
  }

  public SimpleValue getStretch() {
    return stretch;
  }

  public LengthValue getSize() {
    return size;
  }

  public SimpleValue getSizeAdjust() {
    return sizeAdjust;
  }
}
