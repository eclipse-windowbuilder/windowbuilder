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

import org.eclipse.wb.internal.css.model.punctuation.CssColonNode;
import org.eclipse.wb.internal.css.model.punctuation.CssSemiColonNode;
import org.eclipse.wb.internal.css.model.string.CssPropertyNode;
import org.eclipse.wb.internal.css.model.string.CssValueNode;

/**
 * Node for single declaration in {@link CssRuleNode}.
 * 
 * @author scheglov_ke
 * @coverage CSS.model
 */
public final class CssDeclarationNode extends CssNode {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removes this {@link CssDeclarationNode} from parent {@link CssRuleNode}.
   */
  public void remove() {
    ((CssRuleNode) getParent()).removeDeclaration(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  private CssPropertyNode m_property;

  public void setProperty(CssPropertyNode property) {
    checkNull(m_property);
    m_property = property;
    adapt(property);
  }

  public CssPropertyNode getProperty() {
    return m_property;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ':'
  //
  ////////////////////////////////////////////////////////////////////////////
  private CssColonNode m_colon;

  public void setColon(CssColonNode colon) {
    checkNull(m_colon);
    m_colon = colon;
    adapt(colon);
  }

  public CssColonNode getColon() {
    return m_colon;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  private CssValueNode m_value;

  public void setValue(CssValueNode value) {
    checkNull(m_value);
    m_value = value;
    adapt(value);
  }

  public CssValueNode getValue() {
    return m_value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Optional ';'
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
      m_property.accept(visitor);
      m_colon.accept(visitor);
      m_value.accept(visitor);
      if (m_semiColon != null) {
        m_semiColon.accept(visitor);
      }
      visitor.endVisit(this);
    }
    visitor.postVisit(this);
  }
}
