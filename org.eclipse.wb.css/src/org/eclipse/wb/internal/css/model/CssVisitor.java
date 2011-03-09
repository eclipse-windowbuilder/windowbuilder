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

import org.eclipse.wb.internal.css.model.at.CssCharsetNode;
import org.eclipse.wb.internal.css.model.punctuation.CssColonNode;
import org.eclipse.wb.internal.css.model.punctuation.CssCurlyBraceNode;
import org.eclipse.wb.internal.css.model.punctuation.CssSemiColonNode;
import org.eclipse.wb.internal.css.model.string.CssPropertyNode;
import org.eclipse.wb.internal.css.model.string.CssSelectorNode;
import org.eclipse.wb.internal.css.model.string.CssStringNode;
import org.eclipse.wb.internal.css.model.string.CssValueNode;

/**
 * The visitor for CSS model.
 * 
 * @author scheglov_ke
 * @coverage CSS.model
 */
public class CssVisitor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Generic
  //
  ////////////////////////////////////////////////////////////////////////////
  public void preVisit(CssNode node) {
  }

  public void postVisit(CssNode node) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CssCharsetNode
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean visit(CssCharsetNode node) {
    return true;
  }

  public void endVisit(CssCharsetNode node) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CssStringNode
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean visit(CssStringNode node) {
    return true;
  }

  public void endVisit(CssStringNode node) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CssCurlyBraceNode
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean visit(CssCurlyBraceNode node) {
    return true;
  }

  public void endVisit(CssCurlyBraceNode node) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CssColonNode
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean visit(CssColonNode node) {
    return true;
  }

  public void endVisit(CssColonNode node) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CssSemiColonNode
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean visit(CssSemiColonNode node) {
    return true;
  }

  public void endVisit(CssSemiColonNode node) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CssRuleNode
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean visit(CssRuleNode node) {
    return true;
  }

  public void endVisit(CssRuleNode node) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CssSelector
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean visit(CssSelectorNode node) {
    return true;
  }

  public void endVisit(CssSelectorNode node) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CssDeclarationNode
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean visit(CssDeclarationNode node) {
    return true;
  }

  public void endVisit(CssDeclarationNode node) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CssPropertyNode
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean visit(CssPropertyNode node) {
    return true;
  }

  public void endVisit(CssPropertyNode node) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CssValueNode
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean visit(CssValueNode node) {
    return true;
  }

  public void endVisit(CssValueNode node) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CssErrorNode
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean visit(CssErrorNode node) {
    return true;
  }

  public void endVisit(CssErrorNode node) {
  }
}
