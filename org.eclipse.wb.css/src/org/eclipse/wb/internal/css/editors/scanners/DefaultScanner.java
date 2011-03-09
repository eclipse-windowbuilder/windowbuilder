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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.internal.css.editors.TokenManager;
import org.eclipse.wb.internal.css.model.CssDeclarationNode;
import org.eclipse.wb.internal.css.model.CssDocument;
import org.eclipse.wb.internal.css.model.CssErrorNode;
import org.eclipse.wb.internal.css.model.CssNode;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.model.CssVisitor;
import org.eclipse.wb.internal.css.model.string.CssPropertyNode;
import org.eclipse.wb.internal.css.model.string.CssSelectorNode;
import org.eclipse.wb.internal.css.model.string.CssValueNode;
import org.eclipse.wb.internal.css.parser.CssParser;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Scanner for default partition.
 * 
 * @author scheglov_ke
 * @coverage CSS.editor
 */
public class DefaultScanner implements ITokenScanner {
  static final Color SELECTOR_COLOR = TokenManager.getColor(63, 127, 127);
  static final Color PROPERTY_COLOR = TokenManager.getColor(127, 0, 127);
  static final Color VALUE_COLOR = TokenManager.getColor(42, 0, 225);
  static final Color ERROR_COLOR = TokenManager.getColor(255, 0, 0);
  static final Color ERROR_COLOR_BG = TokenManager.getColor(255, 255, 225);
  static final Color OTHER_COLOR = TokenManager.getColor(0, 0, 0);
  static final TextAttribute SELECTOR_ATTRIBUTE = new TextAttribute(SELECTOR_COLOR, null, SWT.BOLD);
  static final TextAttribute PROPERTY_ATTRIBUTE = new TextAttribute(PROPERTY_COLOR,
      null,
      SWT.NORMAL);
  static final TextAttribute VALUE_ATTRIBUTE = new TextAttribute(VALUE_COLOR, null, SWT.NORMAL);
  static final TextAttribute ERROR_ATTRIBUTE = new TextAttribute(ERROR_COLOR,
      ERROR_COLOR_BG,
      SWT.NORMAL);
  static final TextAttribute OTHER_ATTRIBUTE = new TextAttribute(OTHER_COLOR, null, SWT.NORMAL);
  private static final Token SELECTOR_TOKEN = new Token(SELECTOR_ATTRIBUTE);
  private static final Token PROPERTY_TOKEN = new Token(PROPERTY_ATTRIBUTE);
  private static final Token VALUE_TOKEN = new Token(VALUE_ATTRIBUTE);
  private static final Token ERROR_TOKEN = new Token(ERROR_ATTRIBUTE);
  private static final Token OTHER_TOKEN = new Token(OTHER_ATTRIBUTE);
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<VisualToken> m_visualTokens = Lists.newArrayList();
  private Iterator<VisualToken> m_tokensIterator;
  private VisualToken m_token;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DefaultScanner(TokenManager tokenManager) {
    // TODO use token manager
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ITokenScanner
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setRange(IDocument document, int offset, int length) {
    List<CssNode> nodes = parseDocument(document, offset, length);
    // prepare visual tokens
    m_visualTokens.clear();
    if (!nodes.isEmpty()) {
      // add special tokens for known nodes
      for (CssNode node : nodes) {
        Token token;
        if (node instanceof CssSelectorNode) {
          token = SELECTOR_TOKEN;
        } else if (node instanceof CssPropertyNode) {
          token = PROPERTY_TOKEN;
        } else if (node instanceof CssDeclarationNode) {
          //token = PROPERTY_TOKEN;
          continue;
        } else if (node instanceof CssValueNode) {
          token = VALUE_TOKEN;
        } else if (node instanceof CssErrorNode) {
          token = ERROR_TOKEN;
        } else {
          token = OTHER_TOKEN;
        }
        // add visual token
        VisualToken visualToken = new VisualToken(token, node.getOffset(), node.getLength());
        m_visualTokens.add(visualToken);
      }
      // ensure that regions without special token have at least "other" token
      {
        ListIterator<VisualToken> iterator = m_visualTokens.listIterator();
        int lastEnd = nodes.get(0).getOffset();
        while (iterator.hasNext()) {
          VisualToken token = iterator.next();
          int begin = token.getOffset();
          if (begin > lastEnd) {
            iterator.previous();
            iterator.add(new VisualToken(OTHER_TOKEN, lastEnd, begin - lastEnd));
            iterator.next();
          }
          lastEnd = token.getEnd();
        }
      }
    }
    // initialize iterator
    m_tokensIterator = m_visualTokens.listIterator();
  }

  /**
   * @return the {@link CssNode}-s, may be empty {@link List} if parse error.
   */
  private List<CssNode> parseDocument(IDocument document, final int offset, final int length) {
    CssDocument parsedDocument;
    try {
      Reader reader = new StringReader(document.get());
      CssParser parser = new CssParser(reader);
      parsedDocument = parser.parse();
    } catch (Throwable e) {
      return ImmutableList.of();
    }
    // prepare list of nodes in given document range
    final List<CssNode> nodes = Lists.newArrayList();
    parsedDocument.accept(new CssVisitor() {
      @Override
      public void preVisit(CssNode node) {
        if (node instanceof CssRuleNode) {
          return;
        }
        if (isInRange(node.getOffset()) || isInRange(node.getEnd())) {
          nodes.add(node);
        }
      }

      private boolean isInRange(int location) {
        return offset <= location && location <= offset + length;
      }
    });
    Collections.sort(nodes, new Comparator<CssNode>() {
      public int compare(CssNode o1, CssNode o2) {
        return o1.getOffset() - o2.getOffset();
      }
    });
    return nodes;
  }

  public IToken nextToken() {
    if (!m_tokensIterator.hasNext()) {
      return Token.EOF;
    }
    //
    m_token = m_tokensIterator.next();
    return m_token.getToken();
  }

  public int getTokenOffset() {
    return m_token.getOffset();
  }

  public int getTokenLength() {
    return m_token.getLength();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // VisualToken
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class VisualToken {
    private final Token m_token;
    private final int m_offset;
    private final int m_length;

    public VisualToken(Token token, int offset, int length) {
      m_token = token;
      m_offset = offset;
      m_length = length;
    }

    public Token getToken() {
      return m_token;
    }

    public int getOffset() {
      return m_offset;
    }

    public int getLength() {
      return m_length;
    }

    public int getEnd() {
      return m_offset + m_length;
    }

    @Override
    public String toString() {
      return "(" + getOffset() + "," + getEnd() + ")";
    }
  }
}
