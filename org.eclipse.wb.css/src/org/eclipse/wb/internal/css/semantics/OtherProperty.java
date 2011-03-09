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
 * Composite property for other properties.
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public class OtherProperty extends AbstractSemanticsComposite {
  public final SimpleValue m_borderCollapse = mapSimpleProperty(this, "border-collapse");
  public final SimpleValue m_pageBreakBefore = mapSimpleProperty(this, "page-break-before");
  public final SimpleValue m_pageBreakAfter = mapSimpleProperty(this, "page-break-after");
  public final SimpleValue m_tableLayout = mapSimpleProperty(this, "table-layout");
  public final SimpleValue m_cursor = mapSimpleProperty(this, "cursor");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public OtherProperty(AbstractSemanticsComposite composite) {
    super(composite);
  }
}
