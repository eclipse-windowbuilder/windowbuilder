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
package org.eclipse.wb.core.controls;

import org.eclipse.wb.draw2d.IColorConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Composite containing {@link Browser} widget or placeholder with {@link Label} if the
 * {@link Browser} can't be created.
 *
 * @author mitin_aa
 * @coverage core.controls
 */
public class BrowserComposite extends Composite {
  private Browser m_browser;
  private Text m_text;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public BrowserComposite(Composite parent, int style) {
    super(parent, style);
    setLayout(new FillLayout());
    if (browserAvailable(this)) {
      m_browser = new Browser(this, SWT.NONE);
    } else {
      m_text = new Text(this, SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
      m_text.setBackground(IColorConstants.button);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setText(String text) {
    if (m_browser != null) {
      m_browser.setText(text);
    } else {
      m_text.setText(text);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Check availability
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean m_initDone;
  private static boolean m_available;

  /**
   * @return <code>true</code> if the Browser widget is available to display html-based text.
   */
  public static boolean browserAvailable(Composite parent) {
    if (!m_initDone) {
      Browser browser = null;
      try {
        browser = new Browser(parent, SWT.NONE);
        m_available = true;
        browser.dispose();
      } catch (Throwable e) {
        // don't care
      }
      // don't try again
      m_initDone = true;
    }
    return m_available;
  }
}
