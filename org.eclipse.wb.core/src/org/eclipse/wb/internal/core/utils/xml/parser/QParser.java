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
package org.eclipse.wb.internal.core.utils.xml.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 * Quick and Dirty XML parser.
 * <p>
 * This parser is, like the SAX parser, an event based parser, but with much less functionality.
 * <p>
 * However it provides offset/length information for elements and attributes, which is very
 * important for parsing XML into AST-like model and keeping them later synchronized.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public final class QParser {
  ////////////////////////////////////////////////////////////////////////////
  //
  // States
  //
  ////////////////////////////////////////////////////////////////////////////
  private final static int TEXT = 1;
  private final static int ENTITY = 2;
  private final static int OPEN_TAG = 3;
  private final static int CLOSE_TAG = 4;
  private final static int START_TAG = 5;
  private final static int ATTRIBUTE_LVALUE = 6;
  private final static int ATTRIBUTE_EQUAL = 9;
  private final static int ATTRIBUTE_RVALUE = 10;
  private final static int QUOTE = 7;
  private final static int IN_TAG = 8;
  private final static int SINGLE_TAG = 12;
  private final static int COMMENT = 13;
  private final static int DONE = 11;
  private final static int DOCTYPE = 14;
  private final static int PRE = 15;
  private final static int CDATA = 16;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void parse(Reader reader, QHandler handler) throws Exception {
    Stack<Integer> st = new Stack<Integer>();
    int depth = 0;
    int mode = PRE;
    int quotec = '"';
    depth = 0;
    StringBuffer sb = new StringBuffer();
    StringBuffer etag = new StringBuffer();
    String tagName = null;
    String lvalue = null;
    String rvalue = null;
    QAttribute attribute = null;
    Map<String, String> attrs = null;
    List<QAttribute> attrList = null;
    handler.startDocument();
    int line = 1, col = 0;
    boolean eol = false;
    //
    int offset = 0;
    int startTagOffset = 0;
    while (true) {
      int c = reader.read();
      offset++;
      if (c == -1) {
        break;
      }
      // use "\r", "\n" or "\r\n" as to move to next line
      if (c == '\n' && eol) {
        eol = false;
      } else if (eol) {
        eol = false;
      } else if (c == '\n') {
        line++;
        col = 0;
      } else if (c == '\r') {
        eol = true;
        line++;
        col = 0;
      } else {
        col++;
      }
      //
      if (mode == DONE) {
        handler.endDocument();
        return;
        // We are between tags collecting text.
      } else if (mode == TEXT) {
        if (c == '<') {
          st.push(mode);
          mode = START_TAG;
          startTagOffset = offset - 1;
          if (sb.length() > 0) {
            handler.text(sb.toString(), false);
            sb.setLength(0);
          }
        } else if (c == '&') {
          st.push(mode);
          mode = ENTITY;
          etag.setLength(0);
        } else {
          sb.append((char) c);
        }
        // we are processing a closing tag: e.g. </foo>
      } else if (mode == CLOSE_TAG) {
        if (c == '>') {
          mode = popMode(st);
          tagName = sb.toString();
          sb.setLength(0);
          depth--;
          if (depth == 0) {
            mode = DONE;
          }
          handler.endElement(startTagOffset, offset, tagName);
        } else {
          sb.append((char) c);
        }
        // we are processing CDATA
      } else if (mode == CDATA) {
        if (c == '>' && sb.toString().endsWith("]]")) {
          sb.setLength(sb.length() - 2);
          handler.text(sb.toString(), true);
          sb.setLength(0);
          mode = popMode(st);
        } else {
          sb.append((char) c);
        }
        // we are processing a comment.  We are inside
        // the <!-- .... --> looking for the -->.
      } else if (mode == COMMENT) {
        if (c == '>' && sb.toString().endsWith("--")) {
          sb.setLength(0);
          mode = popMode(st);
        } else {
          sb.append((char) c);
        }
        // We are outside the root tag element
      } else if (mode == PRE) {
        if (c == '<') {
          mode = TEXT;
          st.push(mode);
          mode = START_TAG;
          startTagOffset = offset - 1;
        }
        // We are inside one of these <? ... ?>
        // or one of these <!DOCTYPE ... >
      } else if (mode == DOCTYPE) {
        if (c == '>') {
          mode = popMode(st);
          if (mode == TEXT) {
            mode = PRE;
          }
        }
        // we have just seen a < and
        // are wondering what we are looking at
        // <foo>, </foo>, <!-- ... --->, etc.
      } else if (mode == START_TAG) {
        mode = popMode(st);
        if (c == '/') {
          st.push(mode);
          mode = CLOSE_TAG;
        } else if (c == '?') {
          mode = DOCTYPE;
        } else {
          st.push(mode);
          mode = OPEN_TAG;
          tagName = null;
          attrs = new TreeMap<String, String>();
          attrList = new ArrayList<QAttribute>();
          sb.append((char) c);
        }
        // we are processing an entity, e.g. &lt;, &#187;, etc.
      } else if (mode == ENTITY) {
        if (c == ';') {
          mode = popMode(st);
          String cent = etag.toString();
          etag.setLength(0);
          if (cent.equals("lt")) {
            sb.append('<');
          } else if (cent.equals("gt")) {
            sb.append('>');
          } else if (cent.equals("amp")) {
            sb.append('&');
          } else if (cent.equals("quot")) {
            sb.append('"');
          } else if (cent.equals("apos")) {
            sb.append('\'');
          } else if (cent.startsWith("#")) {
            cent = cent.substring(1);
            if (cent.startsWith("x")) {
              cent = cent.substring(1);
              sb.append((char) Integer.parseInt(cent, 16));
            } else {
              sb.append((char) Integer.parseInt(cent));
            }
          } else {
            // ignore, right now we don't need entities
            sb.append("&" + cent + ";");
          }
        } else {
          etag.append((char) c);
        }
        // we have just seen something like this:
        // <foo a="b"/
        // and are looking for the final >.
      } else if (mode == SINGLE_TAG) {
        if (tagName == null) {
          tagName = sb.toString();
        }
        if (c != '>') {
          throwException("Expected > for tag: <" + tagName + "/>", line, col);
        }
        handler.startElement(startTagOffset, -1, tagName, attrs, attrList, true);
        handler.endElement(-1, offset, tagName);
        if (depth == 0) {
          handler.endDocument();
          return;
        }
        sb.setLength(0);
        attrs = new TreeMap<String, String>();
        attrList = new ArrayList<QAttribute>();
        tagName = null;
        mode = popMode(st);
        // we are processing something
        // like this <foo ... >.  It could
        // still be a <!-- ... --> or something.
      } else if (mode == OPEN_TAG) {
        if (c == '>') {
          if (tagName == null) {
            tagName = sb.toString();
          }
          sb.setLength(0);
          depth++;
          handler.startElement(
              startTagOffset,
              offset - startTagOffset,
              tagName,
              attrs,
              attrList,
              false);
          tagName = null;
          attrs = new TreeMap<String, String>();
          attrList = new ArrayList<QAttribute>();
          mode = popMode(st);
        } else if (c == '/') {
          mode = SINGLE_TAG;
        } else if (c == '-' && sb.toString().equals("!-")) {
          mode = COMMENT;
        } else if (c == '[' && sb.toString().equals("![CDATA")) {
          mode = CDATA;
          sb.setLength(0);
        } else if (c == 'E' && sb.toString().equals("!DOCTYP")) {
          sb.setLength(0);
          mode = DOCTYPE;
        } else if (Character.isWhitespace((char) c)) {
          tagName = sb.toString();
          sb.setLength(0);
          mode = IN_TAG;
        } else {
          sb.append((char) c);
        }
        // We are processing the quoted right-hand side
        // of an element's attribute.
      } else if (mode == QUOTE) {
        if (c == quotec) {
          rvalue = sb.toString();
          sb.setLength(0);
          attribute.setValue(rvalue);
          attribute.setValueEndOffset(offset - 1);
          attrList.add(attribute);
          attribute = null;
          attrs.put(lvalue, rvalue);
          mode = IN_TAG;
          // See section the XML spec, section 3.3.3
          // on normalization processing.
        } else if (" \r\n\u0009".indexOf(c) >= 0) {
          sb.append(' ');
        } else if (c == '&') {
          st.push(mode);
          mode = ENTITY;
          etag.setLength(0);
        } else {
          sb.append((char) c);
        }
      } else if (mode == ATTRIBUTE_RVALUE) {
        if (c == '"' || c == '\'') {
          quotec = c;
          attribute.setValueOffset(offset);
          mode = QUOTE;
        } else if (Character.isWhitespace((char) c)) {
        } else {
          throwException("Error in attribute processing", line, col);
        }
      } else if (mode == ATTRIBUTE_LVALUE) {
        if (Character.isWhitespace((char) c)) {
          lvalue = sb.toString();
          sb.setLength(0);
          attribute.setName(lvalue);
          attribute.setNameEndOffset(offset - 1);
          mode = ATTRIBUTE_EQUAL;
        } else if (c == '=') {
          lvalue = sb.toString();
          sb.setLength(0);
          attribute.setName(lvalue);
          attribute.setNameEndOffset(offset - 1);
          mode = ATTRIBUTE_RVALUE;
        } else {
          sb.append((char) c);
        }
      } else if (mode == ATTRIBUTE_EQUAL) {
        if (c == '=') {
          mode = ATTRIBUTE_RVALUE;
        } else if (Character.isWhitespace((char) c)) {
        } else {
          throwException("Error in attribute processing", line, col);
        }
      } else if (mode == IN_TAG) {
        if (c == '>') {
          mode = popMode(st);
          handler.startElement(
              startTagOffset,
              offset - startTagOffset,
              tagName,
              attrs,
              attrList,
              false);
          depth++;
          tagName = null;
          attrs = new TreeMap<String, String>();
          attrList = new ArrayList<QAttribute>();
        } else if (c == '/') {
          mode = SINGLE_TAG;
        } else if (Character.isWhitespace((char) c)) {
        } else {
          mode = ATTRIBUTE_LVALUE;
          attribute = new QAttribute();
          attribute.setNameOffset(offset - 1);
          sb.append((char) c);
        }
      }
    }
    if (mode == DONE) {
      handler.endDocument();
    } else {
      throwException("missing end tag", line, col);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the state from given {@link Stack}.
   */
  private static int popMode(Stack<Integer> stack) {
    if (stack.empty()) {
      return PRE;
    }
    return stack.pop();
  }

  /**
   * Throws {@link Exception} with given message and location.
   */
  private static void throwException(String message, int line, int col) throws Exception {
    throw new QException(message + " near line " + line + ", column " + col);
  }
}