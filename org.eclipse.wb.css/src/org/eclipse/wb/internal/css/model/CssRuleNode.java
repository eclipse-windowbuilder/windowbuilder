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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.css.model.punctuation.CssCurlyBraceNode;
import org.eclipse.wb.internal.css.model.root.ModelChangedEvent;
import org.eclipse.wb.internal.css.model.string.CssSelectorNode;

import java.util.List;

/**
 * Node for single rule in CSS.
 * 
 * @author scheglov_ke
 * @coverage CSS.model
 */
public final class CssRuleNode extends CssNode {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Remove this {@link CssRuleNode} from parent {@link CssDocument}.
   */
  public void remove() {
    ((CssDocument) getParent()).removeRule(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selector
  //
  ////////////////////////////////////////////////////////////////////////////
  private CssSelectorNode m_selector;

  public void setSelector(CssSelectorNode selector) {
    checkNull(m_selector);
    m_selector = selector;
    adapt(selector);
  }

  public CssSelectorNode getSelector() {
    return m_selector;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Left curly brace
  //
  ////////////////////////////////////////////////////////////////////////////
  private CssCurlyBraceNode m_leftBrace;

  public void setLeftBrace(CssCurlyBraceNode leftBrace) {
    checkNull(m_leftBrace);
    m_leftBrace = leftBrace;
    adapt(leftBrace);
  }

  public CssCurlyBraceNode getLeftBrace() {
    return m_leftBrace;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Right curly brace
  //
  ////////////////////////////////////////////////////////////////////////////
  private CssCurlyBraceNode m_rightBrace;

  public void setRightBrace(CssCurlyBraceNode rightBrace) {
    checkNull(m_rightBrace);
    m_rightBrace = rightBrace;
    adapt(rightBrace);
  }

  public CssCurlyBraceNode getRightBrace() {
    return m_rightBrace;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Declarations
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<CssDeclarationNode> m_declarations = Lists.newArrayList();

  /**
   * Adds new {@link CssDeclarationNode} to the end.
   */
  public void addDeclaration(CssDeclarationNode declaration) {
    addDeclaration(m_declarations.size(), declaration);
  }

  /**
   * Adds new {@link CssDeclarationNode} at the specified position.
   */
  public void addDeclaration(int index, CssDeclarationNode declaration) {
    m_declarations.add(index, declaration);
    adapt(declaration);
    fireStructureChanged(declaration, ModelChangedEvent.INSERT);
  }

  /**
   * Removes given {@link CssDeclarationNode} from rule.
   */
  public void removeDeclaration(CssDeclarationNode declaration) {
    boolean removed = m_declarations.remove(declaration);
    if (removed) {
      fireStructureChanged(declaration, ModelChangedEvent.REMOVE);
    }
  }

  /**
   * @return all {@link CssDeclarationNode}s of this rule.
   */
  public List<CssDeclarationNode> getDeclarations() {
    return m_declarations;
  }

  /**
   * @return the {@link CssDeclarationNode} at given index.
   */
  public CssDeclarationNode getDeclaration(int index) {
    return m_declarations.get(index);
  }

  /**
   * @return the index of given {@link CssDeclarationNode}, may be <code>-1</code> if not found.
   */
  public int getIndex(CssDeclarationNode declaration) {
    return m_declarations.indexOf(declaration);
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
      m_selector.accept(visitor);
      m_leftBrace.accept(visitor);
      for (CssDeclarationNode declaration : m_declarations) {
        declaration.accept(visitor);
      }
      m_rightBrace.accept(visitor);
      visitor.endVisit(this);
    }
    visitor.postVisit(this);
  }
}
