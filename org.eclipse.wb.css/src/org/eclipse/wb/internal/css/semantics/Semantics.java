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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Semantics object for CSS rule.
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public final class Semantics extends AbstractSemanticsComposite {
  public final LengthSidedProperty m_margin = new LengthSidedProperty(this, "margin", null);
  public final LengthSidedProperty m_padding = new LengthSidedProperty(this, "padding", null);
  public final BorderProperty m_border = new BorderProperty(this);
  public final FontProperty m_font = new FontProperty(this);
  public final TextProperty m_text = new TextProperty(this);
  public final BackgroundProperty m_background = new BackgroundProperty(this);
  public final SimpleValue m_color = mapSimpleProperty(this, "color");
  public final OtherProperty m_other = new OtherProperty(this);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
  }
}
