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
package org.eclipse.wb.internal.css.test;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.css.model.CssDocument;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.parser.CssEditContext;
import org.eclipse.wb.internal.css.semantics.Semantics;

import org.eclipse.jface.text.Document;

import java.io.File;

/**
 * @author scheglov_ke
 */
public class SacTest {
  public static void main(String[] args) throws Exception {
    long start = System.currentTimeMillis();
    for (int i = 0; i < 1; i++) {
      String path =
          "C:/eclipsePL/workspace/org.eclipse.wb.css/src/org/eclipse/wb/css/test/test.css";
      String content = IOUtils2.readString(new File(path));
      CssEditContext context = new CssEditContext(new Document(content));
      CssDocument document = context.getCssDocument();
      //
      CssRuleNode rule = document.getRule(0);
      Semantics semantics = new Semantics();
      semantics.parse(rule);
      System.out.println(semantics);
    }
    System.out.println("time: " + (System.currentTimeMillis() - start));
  }
}
