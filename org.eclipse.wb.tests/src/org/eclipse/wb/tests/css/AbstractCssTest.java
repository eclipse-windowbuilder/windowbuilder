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
package org.eclipse.wb.tests.css;

import org.eclipse.wb.internal.css.model.CssDocument;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.parser.CssEditContext;
import org.eclipse.wb.internal.css.semantics.Semantics;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.jface.text.Document;

/**
 * Abstract test for CSS.
 * 
 * @author scheglov_ke
 */
public class AbstractCssTest extends DesignerTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static Semantics parseFirstRuleSemantics(String... lines) throws Exception {
    CssRuleNode rule = parseFirstRule(lines);
    Semantics semantics = new Semantics();
    semantics.parse(rule);
    return semantics;
  }

  protected static CssRuleNode parseFirstRule(String... lines) throws Exception {
    CssDocument document = parseDocument(lines);
    return document.getRule(0);
  }

  private static CssDocument parseDocument(String... lines) throws Exception {
    String content = getSource(lines);
    Document document = new Document(content);
    CssEditContext context = new CssEditContext(document);
    return context.getCssDocument();
  }
}
