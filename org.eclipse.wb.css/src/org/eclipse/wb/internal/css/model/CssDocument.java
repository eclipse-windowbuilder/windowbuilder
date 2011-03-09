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

import org.eclipse.wb.internal.css.model.at.CssCharsetNode;
import org.eclipse.wb.internal.css.model.root.Model;
import org.eclipse.wb.internal.css.model.root.ModelChangedEvent;

import java.util.List;

/**
 * Document for CSS.
 * 
 * @author scheglov_ke
 * @coverage CSS.model
 */
public final class CssDocument extends CssNode {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CssDocument() {
    setModel(new Model());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // @charset
  //
  ////////////////////////////////////////////////////////////////////////////
  private CssCharsetNode m_charset;

  public void setCharset(CssCharsetNode charset) {
    if (m_charset != null) {
      fireStructureChanged(m_charset, ModelChangedEvent.REMOVE);
    }
    // set new
    m_charset = charset;
    // adapt new if exists
    if (m_charset != null) {
      adapt(m_charset);
      if (m_charset != null) {
        fireStructureChanged(charset, ModelChangedEvent.INSERT);
      }
    }
  }

  public CssCharsetNode getCharset() {
    return m_charset;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rules
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<CssRuleNode> m_rules = Lists.newArrayList();

  /**
   * Adds {@link CssRuleNode} to the end.
   */
  public void addRule(CssRuleNode rule) {
    addRule(m_rules.size(), rule);
  }

  /**
   * Adds {@link CssRuleNode} at the specified position.
   */
  public void addRule(int index, CssRuleNode rule) {
    m_rules.add(index, rule);
    adapt(rule);
    fireStructureChanged(rule, ModelChangedEvent.INSERT);
  }

  /**
   * Removes given {@link CssRuleNode}.
   */
  public void removeRule(CssRuleNode rule) {
    boolean removed = m_rules.remove(rule);
    if (removed) {
      fireStructureChanged(rule, ModelChangedEvent.REMOVE);
    }
  }

  /**
   * @return all {@link CssRuleNode}s.
   */
  public List<CssRuleNode> getRules() {
    return m_rules;
  }

  /**
   * For internal use only. Replaces list of rules.
   */
  public void setRules(List<CssRuleNode> rules) {
    m_rules.clear();
    m_rules.addAll(rules);
  }

  /**
   * @return the index of this {@link CssRuleNode}.
   */
  public int getIndex(CssRuleNode rule) {
    return m_rules.indexOf(rule);
  }

  /**
   * @return the {@link CssRuleNode} at given position.
   */
  public CssRuleNode getRule(int index) {
    return m_rules.get(index);
  }

  /**
   * Replaces given rule with new one. This method is used during rules replacement with new source.
   * It does not send any event.
   */
  public void replaceRule(CssRuleNode oldRule, CssRuleNode newRule) {
    int index = getIndex(oldRule);
    m_rules.set(index, newRule);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Errors
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<CssErrorNode> m_errors = Lists.newArrayList();

  public void addError(CssErrorNode error) {
    m_errors.add(error);
    adapt(error);
    fireStructureChanged(error, ModelChangedEvent.INSERT);
  }

  public List<CssErrorNode> getErrors() {
    return m_errors;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void accept(CssVisitor visitor) {
    visitor.preVisit(this);
    if (m_charset != null) {
      m_charset.accept(visitor);
    }
    for (CssRuleNode rule : m_rules) {
      rule.accept(visitor);
    }
    for (CssErrorNode error : m_errors) {
      error.accept(visitor);
    }
    visitor.postVisit(this);
  }
}
