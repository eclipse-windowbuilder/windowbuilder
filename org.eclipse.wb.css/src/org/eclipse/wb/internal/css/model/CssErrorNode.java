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
package org.eclipse.wb.internal.css.model;

/**
 * Node for any error parts of CSS document.
 * 
 * @author scheglov_ke
 * @coverage CSS.model
 */
public class CssErrorNode extends CssNode {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CssErrorNode(int offset, int end, String message) {
    setOffset(offset);
    setEnd(end);
    setMessage(message);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Message
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_message;

  public void setMessage(String message) {
    m_message = message;
  }

  public String getMessage() {
    return m_message;
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
