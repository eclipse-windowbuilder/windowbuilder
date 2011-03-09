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
 * Node for CSS selector(s).
 * 
 * Currently we don't parse selectors and present them as single node. Later, if we will need this,
 * we can replace this node with list of concrete selectors.
 * 
 * @author scheglov_ke
 * @coverage CSS.model
 */
public class CssSelectorNode extends AbstractCssStringNode {
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
