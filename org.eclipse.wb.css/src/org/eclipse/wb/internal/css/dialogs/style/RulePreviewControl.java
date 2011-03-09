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
package org.eclipse.wb.internal.css.dialogs.style;

import org.eclipse.wb.internal.css.model.CssDeclarationNode;
import org.eclipse.wb.internal.css.model.CssRuleNode;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Control for previewing {@link CssRuleNode}'s.
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public class RulePreviewControl extends Composite {
  private static final String SAMPLE_TEXT =
      "Nolite mittere margaeritas ante porcas! <i>Latin proverb</i>";
  private final Browser m_browser;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RulePreviewControl(Composite parent, int style) {
    super(parent, style);
    setLayout(new FillLayout());
    m_browser = new Browser(this, SWT.BORDER);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void showRule(CssRuleNode rule) {
    // prepare style
    String style = "";
    for (CssDeclarationNode declaration : rule.getDeclarations()) {
      style +=
          declaration.getProperty().getValue() + ": " + declaration.getValue().getValue() + "; ";
    }
    // set HTML
    String html = "<html><body><p style=\"" + style + "\">" + SAMPLE_TEXT + "</p></body>";
    m_browser.setText(html);
  }

  public void clear() {
    m_browser.setText("");
  }
}
