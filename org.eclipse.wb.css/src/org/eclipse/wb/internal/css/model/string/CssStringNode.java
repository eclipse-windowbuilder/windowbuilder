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

import org.eclipse.wb.internal.css.model.CssVisitor;

/**
 * Node for string.
 * 
 * @author scheglov_ke
 * @coverage CSS.model
 */
public final class CssStringNode extends AbstractCssStringNode {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CssStringNode() {
  }

  public CssStringNode(int offset, String value) {
    super(offset, value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void accept(CssVisitor visitor) {
    visitor.preVisit(this);
    if (visitor.visit(this)) {
      visitor.endVisit(this);
    }
    visitor.postVisit(this);
  }
}
