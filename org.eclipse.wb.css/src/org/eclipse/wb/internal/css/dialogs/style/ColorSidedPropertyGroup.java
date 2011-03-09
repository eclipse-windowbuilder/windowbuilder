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
package org.eclipse.wb.internal.css.dialogs.style;

import org.eclipse.wb.internal.css.semantics.AbstractValue;
import org.eclipse.wb.internal.css.semantics.SimpleSidedProperty;
import org.eclipse.wb.internal.css.semantics.SimpleValue;

import org.eclipse.swt.widgets.Composite;

/**
 * Group for editing {@link SimpleSidedProperty}.
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public final class ColorSidedPropertyGroup extends AbstractSidedPropertyGroup {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColorSidedPropertyGroup(Composite parent,
      int style,
      StyleEditOptions options,
      String title,
      SimpleSidedProperty property) {
    super(parent, style, options, title, 4, property);
    createParts();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parts
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractValueEditor createPart(AbstractValue value, String title) {
    return new ColorValueEditor(m_options, title, (SimpleValue) value);
  }
}
