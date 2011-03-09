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
 * The factory for simplifying CSS nodes creation.
 * 
 * @author scheglov_ke
 * @coverage CSS.model
 */
public final class CssFactory {
  /**
   * @return new {@link CssCharsetNode} with given value.
   */
  public static CssCharsetNode newCharset(String value) {
    CssCharsetNode charset = new CssCharsetNode();
    {
      CssStringNode string = new CssStringNode();
      string.setValue("\"" + value + "\"");
      charset.setString(string);
    }
    charset.setSemiColon(new CssSemiColonNode(0));
    return charset;
  }

  /**
   * @return new {@link CssRuleNode} with selector and left/right braces but without declarations.
   */
  public static CssRuleNode newRule(String selectorValue) {
    CssRuleNode rule = new CssRuleNode();
    {
      CssSelectorNode selector = new CssSelectorNode();
      selector.setValue(selectorValue);
      rule.setSelector(selector);
    }
    rule.setLeftBrace(new CssCurlyBraceNode(0, true));
    rule.setRightBrace(new CssCurlyBraceNode(0, false));
    return rule;
  }

  /**
   * @return new {@link CssDeclarationNode} with given property name and value.
   */
  public static CssDeclarationNode newDeclaration(String name, String value) {
    CssDeclarationNode declaration = new CssDeclarationNode();
    declaration.setProperty(new CssPropertyNode(0, name));
    declaration.setColon(new CssColonNode(0));
    declaration.setValue(new CssValueNode(0, value));
    return declaration;
  }
}
