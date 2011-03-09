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

import org.eclipse.wb.internal.css.editors.scanners.DefaultScanner;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * Test for {@link DefaultScanner}.
 * 
 * @author scheglov_ke
 */
public class DefaultScannerTest extends DesignerTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Test
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_0() throws Exception {
    IDocument document = new Document("     ");
    ITokenScanner scanner = new DefaultScanner(null);
    scanner.setRange(document, 2, 1);
    // no tokens
    IToken token = scanner.nextToken();
    assertSame(Token.EOF, token);
  }
}
