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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el;

import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;

/**
 * {@link IRule} for detect {@code EL} keywords.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public class KeywordsRule implements IRule {
  private static final int[] AND = {'n', 'd', ' '};
  private static final int[] DIV = {'i', 'v', ' '};
  private static final int[] EMPTY = {'p', 't', 'y', ' '};
  private static final int[] FALSE = {'a', 'l', 's', 'e', ' '};
  private static final int[] INSTANCE_OF = {'n', 's', 't', 'a', 'n', 'c', 'e', 'o', 'f', ' '};
  private static final int[] NOT = {'t', ' '};
  private static final int[] NULL = {'l', 'l', ' '};
  private static final int[] MOD = {'o', 'd', ' '};
  private static final int[] OR = {'r', ' '};
  private static final int[] TRUE = {'r', 'u', 'e', ' '};
  private final IToken m_token;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public KeywordsRule(ISourceViewer sourceViewer, ElPropertyUiConfiguration configuration) {
    m_token =
        new Token(new TextAttribute(configuration.getKeywordsColor(),
            null,
            SWT.NORMAL,
            SwtResourceManager.getBoldFont(sourceViewer.getTextWidget().getFont())));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRule
  //
  ////////////////////////////////////////////////////////////////////////////
  public IToken evaluate(ICharacterScanner scanner) {
    CharacterScannerWrapper wrapper = new CharacterScannerWrapper(scanner);
    if (wrapper.test(' ')) {
      switch (wrapper.read()) {
        case 'a' :
          if (wrapper.test(AND)) {
            return m_token;
          }
          break;
        case 'd' :
          if (wrapper.test(DIV)) {
            return m_token;
          }
          break;
        case 'e' :
          switch (wrapper.read()) {
            case 'm' :
              if (wrapper.test(EMPTY)) {
                return m_token;
              }
              break;
            case 'q' :
              if (wrapper.test(' ')) {
                return m_token;
              }
              break;
          }
          break;
        case 'f' :
          if (wrapper.test(FALSE)) {
            return m_token;
          }
          break;
        case 'g' :
        case 'l' :
          switch (wrapper.read()) {
            case 'e' :
            case 't' :
              if (wrapper.test(' ')) {
                return m_token;
              }
              break;
          }
          break;
        case 'i' :
          if (wrapper.test(INSTANCE_OF)) {
            return m_token;
          }
          break;
        case 'n' :
          switch (wrapper.read()) {
            case 'e' :
              if (wrapper.test(' ')) {
                return m_token;
              }
              break;
            case 'o' :
              if (wrapper.test(NOT)) {
                return m_token;
              }
              break;
            case 'u' :
              if (wrapper.test(NULL)) {
                return m_token;
              }
              break;
          }
          break;
        case 'm' :
          if (wrapper.test(MOD)) {
            return m_token;
          }
          break;
        case 'o' :
          if (wrapper.test(OR)) {
            return m_token;
          }
          break;
        case 't' :
          if (wrapper.test(TRUE)) {
            return m_token;
          }
          break;
      }
    }
    wrapper.unread();
    return Token.UNDEFINED;
  }
}