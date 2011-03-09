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
package org.eclipse.wb.internal.css.model.at;

import org.eclipse.wb.internal.css.model.CssNode;
import org.eclipse.wb.internal.css.model.CssVisitor;
import org.eclipse.wb.internal.css.model.punctuation.CssSemiColonNode;
import org.eclipse.wb.internal.css.model.string.CssStringNode;

/**
 * Node for @charset rule.
 * 
 * @author scheglov_ke
 * @coverage CSS.model
 */
public final class CssCharsetNode extends CssNode {
  ////////////////////////////////////////////////////////////////////////////
  //
  // String
  //
  ////////////////////////////////////////////////////////////////////////////
  private CssStringNode m_string;

  public void setString(CssStringNode string) {
    checkNull(m_string);
    m_string = string;
  }

  public CssStringNode getString() {
    return m_string;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Semicolon
  //
  ////////////////////////////////////////////////////////////////////////////
  private CssSemiColonNode m_semiColon;

  public void setSemiColon(CssSemiColonNode semiColon) {
    checkNull(m_semiColon);
    m_semiColon = semiColon;
    adapt(semiColon);
  }

  public CssSemiColonNode getSemiColon() {
    return m_semiColon;
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
      m_string.accept(visitor);
      m_semiColon.accept(visitor);
      visitor.endVisit(this);
    }
    visitor.postVisit(this);
  }
}
