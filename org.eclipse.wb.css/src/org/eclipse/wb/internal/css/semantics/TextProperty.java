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
 * Composite property for text.
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public class TextProperty extends AbstractSemanticsComposite {
  public final LengthValue m_indent = mapLengthProperty(this, "text-indent");
  public final SimpleValue m_align = mapSimpleProperty(this, "text-align");
  public final SimpleValue m_decoration = mapSimpleProperty(this, "text-decoration");
  // TODO text-shadow, later, in any case it is not supported in browsers :-(
  public final LengthValue m_letterSpacing = mapLengthProperty(this, "letter-spacing");
  public final LengthValue m_wordSpacing = mapLengthProperty(this, "word-spacing");
  public final SimpleValue m_transform = mapSimpleProperty(this, "text-transform");
  public final SimpleValue m_whiteSpace = mapSimpleProperty(this, "white-space");
  public final LengthValue m_verticalAlign = mapLengthProperty(this, "vertical-align");
  public final LengthValue m_lineHeight = mapLengthProperty(this, "line-height");
  public final SimpleValue m_direction = mapSimpleProperty(this, "direction");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TextProperty(AbstractSemanticsComposite composite) {
    super(composite);
  }
}
