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
package org.eclipse.wb.internal.css.editors.scanners;

import org.eclipse.wb.internal.css.editors.TokenManager;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Scanner for comment partition.
 * 
 * @author scheglov_ke
 * @coverage CSS.editor
 */
public class CommentScanner extends RuleBasedScanner {
  private static final Color COLOR = TokenManager.getColor(0x3F, 0x5F, 0xBF);
  private static final TextAttribute ATTRIBUTE = new TextAttribute(COLOR, null, SWT.NORMAL);
  private static final Token TOKEN = new Token(ATTRIBUTE);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CommentScanner(TokenManager tokenManager) {
    setDefaultReturnToken(TOKEN);
  }
}
