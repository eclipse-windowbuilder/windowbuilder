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
package org.eclipse.wb.internal.css.parser;

import org.eclipse.wb.internal.css.model.CssDeclarationNode;
import org.eclipse.wb.internal.css.model.CssDocument;
import org.eclipse.wb.internal.css.model.CssErrorNode;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.model.at.CssCharsetNode;
import org.eclipse.wb.internal.css.model.punctuation.CssColonNode;
import org.eclipse.wb.internal.css.model.punctuation.CssCurlyBraceNode;
import org.eclipse.wb.internal.css.model.punctuation.CssSemiColonNode;
import org.eclipse.wb.internal.css.model.string.CssPropertyNode;
import org.eclipse.wb.internal.css.model.string.CssSelectorNode;
import org.eclipse.wb.internal.css.model.string.CssStringNode;
import org.eclipse.wb.internal.css.model.string.CssValueNode;
import org.eclipse.wb.internal.css.parser.scanner.LexicalUnits;
import org.eclipse.wb.internal.css.parser.scanner.Scanner;
import org.eclipse.wb.internal.css.parser.scanner.Token;

import java.io.Reader;

/**
 * Parser of {@link Reader} with CSS text into {@link CssDocument}.
 * 
 * @author scheglov_ke
 * @coverage CSS.parser
 */
public class CssParser {
  private final Scanner m_scanner;
  private final CssDocument m_document;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CssParser(Reader reader) {
    m_scanner = new Scanner(reader);
    m_document = new CssDocument();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Performs parsing.
   * 
   * @return the parsed {@link CssDocument}.
   */
  public CssDocument parse() {
    readFirstToken();
    // parse statements
    while (token.getType() != LexicalUnits.EOF) {
      parseDocumentStatement();
    }
    // done
    return m_document;
  }

  /**
   * Reads token, skips any bad characters.
   * <p>
   * TODO may be remove
   */
  private void readFirstToken() {
    Throwable error = null;
    do {
      try {
        next();
        break;
      } catch (Throwable e) {
        error = e;
        m_scanner.skipToNextLine();
      }
    } while (token == null || token.getType() != LexicalUnits.EOF);
    // log error
    if (error != null) {
      m_document.addError(new CssErrorNode(0, token.getOffset(), error.getMessage()));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  private Token token;

  /**
   * Parses top level CSS statement, such as {@link CssCharsetNode} or {@link CssRuleNode}.
   */
  private void parseDocumentStatement() {
    int offset = m_scanner.getTokenOffset();
    // read @charset
    if (token.getType() == LexicalUnits.CHARSET_SYMBOL) {
      CssCharsetNode charset = new CssCharsetNode();
      charset.setOffset(offset);
      // string
      {
        next(LexicalUnits.STRING, "charset.string");
        charset.setString(new CssStringNode(token.getOffset(), token.getValue()));
      }
      // ';'
      {
        next(LexicalUnits.SEMI_COLON, "semicolon");
        charset.setSemiColon(new CssSemiColonNode(token.getOffset()));
      }
      // remember
      charset.setEnd(charset.getSemiColon().getEnd());
      m_document.setCharset(charset);
      // select next token
      next();
    }
    // should be rule
    parseRule();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule set
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parses tokens into {@link CssRuleNode}.
   */
  private void parseRule() {
    CssRuleNode rule = new CssRuleNode();
    rule.setOffset(token.getOffset());
    try {
      // parse selector
      rule.setSelector(parseRuleSelector());
      // parse block
      {
        // add '{'
        rule.setLeftBrace(new CssCurlyBraceNode(token.getOffset(), true));
        next();
        // parse declarations
        while (token.getType() != LexicalUnits.RIGHT_CURLY_BRACE) {
          if (token.getType() == LexicalUnits.EOF) {
            reportError("eof");
          }
          parseDeclaration(rule);
        }
        // add '}'
        rule.setRightBrace(new CssCurlyBraceNode(token.getOffset(), false));
        next();
      }
      // add rule
      rule.setEnd(rule.getRightBrace().getEnd());
      m_document.addRule(rule);
    } catch (Throwable e) {
      // skip tokens to the '}' - end of current rule
      while (true) {
        if (token.getType() == LexicalUnits.EOF) {
          break;
        }
        if (token.getType() == LexicalUnits.RIGHT_CURLY_BRACE) {
          next();
          break;
        }
        next();
      }
      // log error
      m_document.addError(new CssErrorNode(rule.getOffset(), token.getOffset(), e.getMessage()));
    }
  }

  /**
   * Reads selector as string until '{'.
   */
  private CssSelectorNode parseRuleSelector() {
    CssSelectorNode selector = new CssSelectorNode();
    selector.setOffset(token.getOffset());
    // read until '{'
    Token lastSelectorToken = token;
    while (token.getType() != LexicalUnits.LEFT_CURLY_BRACE) {
      if (token.getType() == LexicalUnits.EOF) {
        reportError("eof");
      }
      lastSelectorToken = token;
      next();
    }
    // finish selection
    selector.setEnd(lastSelectorToken.getEnd());
    selector.setValue(m_scanner.getStringValue(selector.getOffset(), selector.getLength()));
    return selector;
  }

  /**
   * Parses single declaration in CSS rule.
   */
  private void parseDeclaration(CssRuleNode rule) {
    CssDeclarationNode declaration = new CssDeclarationNode();
    declaration.setOffset(token.getOffset());
    try {
      // parse property
      {
        if (token.getType() != LexicalUnits.IDENTIFIER) {
          reportError("identifier");
        }
        declaration.setProperty(new CssPropertyNode(token.getOffset(), token.getValue()));
      }
      // ':'
      {
        next(LexicalUnits.COLON, "colon");
        declaration.setColon(new CssColonNode(token.getOffset()));
      }
      next();
      // parse value
      {
        CssValueNode value = new CssValueNode();
        value.setOffset(token.getOffset());
        // read until ';' or '}'
        Token lastValueToken = token;
        while (true) {
          next0();
          if (token.getType() == LexicalUnits.EOF) {
            m_document.addError(new CssErrorNode(declaration.getOffset(), token.getOffset(), "eof"));
            return;
          }
          if (token.getType() == LexicalUnits.SEMI_COLON
              || token.getType() == LexicalUnits.RIGHT_CURLY_BRACE) {
            break;
          }
          if (!isWhitespace(token.getType())) {
            lastValueToken = token;
          }
        }
        // finish value
        value.setEnd(lastValueToken.getEnd());
        value.setValue(m_scanner.getStringValue(value.getOffset(), value.getLength()));
        declaration.setValue(value);
      }
      // read optional ';'
      declaration.setEnd(declaration.getValue().getEnd());
      if (token.getType() == LexicalUnits.SEMI_COLON) {
        declaration.setSemiColon(new CssSemiColonNode(token.getOffset()));
        declaration.setEnd(declaration.getSemiColon().getEnd());
        next();
      }
      // add prepared declaration
      rule.addDeclaration(declaration);
    } catch (Throwable e) {
      // skip tokens to: ';' (end of declaration) or '}' (end of rule)
      while (true) {
        if (token.getType() == LexicalUnits.EOF
            || token.getType() == LexicalUnits.RIGHT_CURLY_BRACE) {
          break;
        }
        if (token.getType() == LexicalUnits.SEMI_COLON) {
          next();
          break;
        }
        next();
      }
      // log error
      m_document.addError(new CssErrorNode(declaration.getOffset(),
          token.getOffset(),
          e.getMessage()));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  private void reportError(String string) {
    // TODO
    throw new Error(string);
  }

  /**
   * Get any next token (including comments).
   */
  private void next0() {
    token = m_scanner.next();
  }

  /**
   * Get next non-whitespace and non-comment token.
   */
  private void next() {
    while (true) {
      next0();
      if (!isWhitespace(token.getType())) {
        break;
      }
    }
  }

  private static boolean isWhitespace(int type) {
    return type == LexicalUnits.COMMENT
        || type == LexicalUnits.SPACE
        || type == LexicalUnits.CDO
        || type == LexicalUnits.CDC;
  }

  /**
   * Get token as {@link #next()} but also checks that it has expected type or reports error with
   * given key
   */
  private void next(int expectedType, String errorKey) {
    next();
    if (token.getType() != expectedType) {
      reportError(errorKey);
    }
  }
}
