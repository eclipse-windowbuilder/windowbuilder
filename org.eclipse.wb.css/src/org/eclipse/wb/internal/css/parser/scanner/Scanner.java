/**
   Copyright 2000-2004  The Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.eclipse.wb.internal.css.parser.scanner;

import java.io.IOException;
import java.io.Reader;

/**
 * This class represents a CSS scanner - an object which decodes CSS lexical units.
 * 
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class Scanner {
  /**
   * The reader.
   */
  private final CountingReader m_reader;
  /**
   * The current char.
   */
  private int m_current;
  /**
   * The recording buffer.
   */
  private char[] m_buffer = new char[128];
  /**
   * The current position in the buffer.
   */
  private int m_position;
  /**
   * The type of the current lexical unit.
   */
  private int m_type;
  /**
   * The start offset of the last lexical unit.
   */
  private int m_start;
  /**
   * The end offset of the last lexical unit.
   */
  private int m_end;
  /**
   * The characters to skip to create the string which represents the current token.
   */
  private int m_blankCharacters;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Scanner(Reader reader) throws ParseException {
    try {
      m_reader = new CountingReader(reader);
      m_current = nextChar();
    } catch (IOException e) {
      throw new ParseException(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Positions
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_tokenOffset;
  private int m_tokenLength;

  /**
   * @return offset of just read token
   */
  public int getTokenOffset() {
    return m_tokenOffset;
  }

  /**
   * @return length of just read token
   */
  public int getTokenLength() {
    return m_tokenLength;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buffer
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the buffer used to store the chars.
   */
  public char[] getBuffer() {
    return m_buffer;
  }

  /**
   * Returns the start offset of the last lexical unit.
   */
  public int getStart() {
    return m_start;
  }

  /**
   * Returns the end offset of the last lexical unit.
   */
  public int getEnd() {
    return m_end;
  }

  /**
   * Clears the buffer.
   */
  public void clearBuffer() {
    if (m_position <= 0) {
      m_position = 0;
    } else {
      m_buffer[0] = m_buffer[m_position - 1];
      m_position = 1;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The current lexical unit type like defined in LexicalUnits.
   */
  public int getType() {
    return m_type;
  }

  /**
   * Returns the string representation of the current lexical unit.
   */
  public String getStringValue(int offset, int length) {
    return new String(m_buffer, offset, length);
  }

  /**
   * Scans a @rule value. This method assumes that the current lexical unit is a at keyword.
   */
  public void scanAtRule() throws ParseException {
    try {
      // waiting for EOF, ';' or '{'
      loop : for (;;) {
        switch (m_current) {
          case '{' :
            int brackets = 1;
            for (;;) {
              nextChar();
              switch (m_current) {
                case '}' :
                  if (--brackets > 0) {
                    break;
                  }
                case -1 :
                  break loop;
                case '{' :
                  brackets++;
              }
            }
          case -1 :
          case ';' :
            break loop;
        }
        nextChar();
      }
      m_end = m_position;
    } catch (IOException e) {
      throw new ParseException(e);
    }
  }

  /**
   * Returns the next token.
   */
  public Token next() throws ParseException {
    m_blankCharacters = 0;
    m_start = m_position - 1;
    m_tokenOffset = m_reader.getOffset() - 1;
    //
    nextToken();
    //
    m_end = m_position - endGap();
    m_tokenLength = Math.max(m_reader.getOffset() - 1 - m_tokenOffset, 1);
    //
    String stringValue;
    if (m_type != LexicalUnits.EOF) {
      stringValue = getStringValue(m_tokenOffset, m_tokenLength);
    } else {
      stringValue = null;
    }
    //
    return new Token(m_type, stringValue, m_tokenOffset, m_tokenLength);
  }

  /**
   * Skips characters to the end of current line.
   */
  public void skipToNextLine() throws ParseException {
    try {
      while (m_current != '\r' && m_current != '\n') {
        nextChar();
      }
    } catch (IOException e) {
      throw new ParseException(e);
    }
  }

  /**
   * Returns the end gap of the current lexical unit.
   */
  protected int endGap() {
    int result = m_current == -1 ? 0 : 1;
    switch (m_type) {
      case LexicalUnits.FUNCTION :
      case LexicalUnits.STRING :
      case LexicalUnits.S :
      case LexicalUnits.PERCENTAGE :
        result += 1;
        break;
      case LexicalUnits.COMMENT :
      case LexicalUnits.HZ :
      case LexicalUnits.EM :
      case LexicalUnits.EX :
      case LexicalUnits.PC :
      case LexicalUnits.PT :
      case LexicalUnits.PX :
      case LexicalUnits.CM :
      case LexicalUnits.MM :
      case LexicalUnits.IN :
      case LexicalUnits.MS :
        result += 2;
        break;
      case LexicalUnits.KHZ :
      case LexicalUnits.DEG :
      case LexicalUnits.RAD :
        result += 3;
        break;
      case LexicalUnits.GRAD :
        result += 4;
    }
    return result + m_blankCharacters;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Scanning
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the next token.
   */
  protected void nextToken() throws ParseException {
    try {
      switch (m_current) {
        case -1 :
          m_type = LexicalUnits.EOF;
          return;
        case '{' :
          nextChar();
          m_type = LexicalUnits.LEFT_CURLY_BRACE;
          return;
        case '}' :
          nextChar();
          m_type = LexicalUnits.RIGHT_CURLY_BRACE;
          return;
        case '=' :
          nextChar();
          m_type = LexicalUnits.EQUAL;
          return;
        case '+' :
          nextChar();
          m_type = LexicalUnits.PLUS;
          return;
        case ',' :
          nextChar();
          m_type = LexicalUnits.COMMA;
          return;
        case ';' :
          nextChar();
          m_type = LexicalUnits.SEMI_COLON;
          return;
        case '>' :
          nextChar();
          m_type = LexicalUnits.PRECEDE;
          return;
        case '[' :
          nextChar();
          m_type = LexicalUnits.LEFT_BRACKET;
          return;
        case ']' :
          nextChar();
          m_type = LexicalUnits.RIGHT_BRACKET;
          return;
        case '*' :
          nextChar();
          m_type = LexicalUnits.ANY;
          return;
        case '(' :
          nextChar();
          m_type = LexicalUnits.LEFT_BRACE;
          return;
        case ')' :
          nextChar();
          m_type = LexicalUnits.RIGHT_BRACE;
          return;
        case ':' :
          nextChar();
          m_type = LexicalUnits.COLON;
          return;
        case ' ' :
        case '\t' :
        case '\r' :
        case '\n' :
        case '\f' :
          do {
            nextChar();
          } while (ScannerUtilities.isCssSpace((char) m_current));
          m_type = LexicalUnits.SPACE;
          return;
        case '/' :
          nextChar();
          if (m_current != '*') {
            m_type = LexicalUnits.DIVIDE;
            return;
          }
          // Comment
          nextChar();
          m_start = m_position - 1;
          do {
            while (m_current != -1 && m_current != '*') {
              nextChar();
            }
            do {
              nextChar();
            } while (m_current != -1 && m_current == '*');
          } while (m_current != -1 && m_current != '/');
          if (m_current == -1) {
            throw new ParseException("eof", m_reader.getOffset());
          }
          nextChar();
          m_type = LexicalUnits.COMMENT;
          return;
        case '\'' : // String1
          m_type = string1();
          return;
        case '"' : // String2
          m_type = string2();
          return;
        case '<' :
          nextChar();
          if (m_current != '!') {
            throw new ParseException("character", m_reader.getOffset());
          }
          nextChar();
          if (m_current == '-') {
            nextChar();
            if (m_current == '-') {
              nextChar();
              m_type = LexicalUnits.CDO;
              return;
            }
          }
          throw new ParseException("character", m_reader.getOffset());
        case '-' :
          nextChar();
          if (m_current != '-') {
            m_type = LexicalUnits.MINUS;
            return;
          }
          nextChar();
          if (m_current == '>') {
            nextChar();
            m_type = LexicalUnits.CDC;
            return;
          }
          throw new ParseException("character", m_reader.getOffset());
        case '|' :
          nextChar();
          if (m_current == '=') {
            nextChar();
            m_type = LexicalUnits.DASHMATCH;
            return;
          }
          throw new ParseException("character", m_reader.getOffset());
        case '~' :
          nextChar();
          if (m_current == '=') {
            nextChar();
            m_type = LexicalUnits.INCLUDES;
            return;
          }
          throw new ParseException("character", m_reader.getOffset());
        case '#' :
          nextChar();
          if (ScannerUtilities.isCssNameCharacter((char) m_current)) {
            m_start = m_position - 1;
            do {
              nextChar();
              while (m_current == '\\') {
                nextChar();
                escape();
              }
            } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
            m_type = LexicalUnits.HASH;
            return;
          }
          throw new ParseException("character", m_reader.getOffset());
        case '@' :
          nextChar();
          switch (m_current) {
            case 'c' :
            case 'C' :
              m_start = m_position - 1;
              if (isEqualIgnoreCase(nextChar(), 'h')
                  && isEqualIgnoreCase(nextChar(), 'a')
                  && isEqualIgnoreCase(nextChar(), 'r')
                  && isEqualIgnoreCase(nextChar(), 's')
                  && isEqualIgnoreCase(nextChar(), 'e')
                  && isEqualIgnoreCase(nextChar(), 't')) {
                nextChar();
                m_type = LexicalUnits.CHARSET_SYMBOL;
                return;
              }
              break;
            case 'f' :
            case 'F' :
              m_start = m_position - 1;
              if (isEqualIgnoreCase(nextChar(), 'o')
                  && isEqualIgnoreCase(nextChar(), 'n')
                  && isEqualIgnoreCase(nextChar(), 't')
                  && isEqualIgnoreCase(nextChar(), '-')
                  && isEqualIgnoreCase(nextChar(), 'f')
                  && isEqualIgnoreCase(nextChar(), 'a')
                  && isEqualIgnoreCase(nextChar(), 'c')
                  && isEqualIgnoreCase(nextChar(), 'e')) {
                nextChar();
                m_type = LexicalUnits.FONT_FACE_SYMBOL;
                return;
              }
              break;
            case 'i' :
            case 'I' :
              m_start = m_position - 1;
              if (isEqualIgnoreCase(nextChar(), 'm')
                  && isEqualIgnoreCase(nextChar(), 'p')
                  && isEqualIgnoreCase(nextChar(), 'o')
                  && isEqualIgnoreCase(nextChar(), 'r')
                  && isEqualIgnoreCase(nextChar(), 't')) {
                nextChar();
                m_type = LexicalUnits.IMPORT_SYMBOL;
                return;
              }
              break;
            case 'm' :
            case 'M' :
              m_start = m_position - 1;
              if (isEqualIgnoreCase(nextChar(), 'e')
                  && isEqualIgnoreCase(nextChar(), 'd')
                  && isEqualIgnoreCase(nextChar(), 'i')
                  && isEqualIgnoreCase(nextChar(), 'a')) {
                nextChar();
                m_type = LexicalUnits.MEDIA_SYMBOL;
                return;
              }
              break;
            case 'p' :
            case 'P' :
              m_start = m_position - 1;
              if (isEqualIgnoreCase(nextChar(), 'a')
                  && isEqualIgnoreCase(nextChar(), 'g')
                  && isEqualIgnoreCase(nextChar(), 'e')) {
                nextChar();
                m_type = LexicalUnits.PAGE_SYMBOL;
                return;
              }
              break;
            default :
              if (!ScannerUtilities.isCssIdentifierStartCharacter((char) m_current)) {
                throw new ParseException("identifier.character", m_reader.getOffset());
              }
              m_start = m_position - 1;
          }
          do {
            nextChar();
            while (m_current == '\\') {
              nextChar();
              escape();
            }
          } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
          m_type = LexicalUnits.AT_KEYWORD;
          return;
        case '!' :
          do {
            nextChar();
          } while (m_current != -1 && ScannerUtilities.isCssSpace((char) m_current));
          if (isEqualIgnoreCase(m_current, 'i')
              && isEqualIgnoreCase(nextChar(), 'm')
              && isEqualIgnoreCase(nextChar(), 'p')
              && isEqualIgnoreCase(nextChar(), 'o')
              && isEqualIgnoreCase(nextChar(), 'r')
              && isEqualIgnoreCase(nextChar(), 't')
              && isEqualIgnoreCase(nextChar(), 'a')
              && isEqualIgnoreCase(nextChar(), 'n')
              && isEqualIgnoreCase(nextChar(), 't')) {
            nextChar();
            m_type = LexicalUnits.IMPORTANT_SYMBOL;
            return;
          }
          if (m_current == -1) {
            throw new ParseException("eof", m_reader.getOffset());
          } else {
            throw new ParseException("character", m_reader.getOffset());
          }
        case '0' :
        case '1' :
        case '2' :
        case '3' :
        case '4' :
        case '5' :
        case '6' :
        case '7' :
        case '8' :
        case '9' :
          m_type = number();
          return;
        case '.' :
          switch (nextChar()) {
            case '0' :
            case '1' :
            case '2' :
            case '3' :
            case '4' :
            case '5' :
            case '6' :
            case '7' :
            case '8' :
            case '9' :
              m_type = dotNumber();
              return;
            default :
              m_type = LexicalUnits.DOT;
              return;
          }
        case 'u' :
        case 'U' :
          nextChar();
          switch (m_current) {
            case '+' :
              boolean range = false;
              for (int i = 0; i < 6; i++) {
                nextChar();
                switch (m_current) {
                  case '?' :
                    range = true;
                    break;
                  default :
                    if (range && !ScannerUtilities.isCssHexadecimalCharacter((char) m_current)) {
                      throw new ParseException("character", m_reader.getOffset());
                    }
                }
              }
              nextChar();
              if (range) {
                m_type = LexicalUnits.UNICODE_RANGE;
                return;
              }
              if (m_current == '-') {
                nextChar();
                if (!ScannerUtilities.isCssHexadecimalCharacter((char) m_current)) {
                  throw new ParseException("character", m_reader.getOffset());
                }
                nextChar();
                if (!ScannerUtilities.isCssHexadecimalCharacter((char) m_current)) {
                  m_type = LexicalUnits.UNICODE_RANGE;
                  return;
                }
                nextChar();
                if (!ScannerUtilities.isCssHexadecimalCharacter((char) m_current)) {
                  m_type = LexicalUnits.UNICODE_RANGE;
                  return;
                }
                nextChar();
                if (!ScannerUtilities.isCssHexadecimalCharacter((char) m_current)) {
                  m_type = LexicalUnits.UNICODE_RANGE;
                  return;
                }
                nextChar();
                if (!ScannerUtilities.isCssHexadecimalCharacter((char) m_current)) {
                  m_type = LexicalUnits.UNICODE_RANGE;
                  return;
                }
                nextChar();
                if (!ScannerUtilities.isCssHexadecimalCharacter((char) m_current)) {
                  m_type = LexicalUnits.UNICODE_RANGE;
                  return;
                }
                nextChar();
                m_type = LexicalUnits.UNICODE_RANGE;
                return;
              }
            case 'r' :
            case 'R' :
              nextChar();
              switch (m_current) {
                case 'l' :
                case 'L' :
                  nextChar();
                  switch (m_current) {
                    case '(' :
                      do {
                        nextChar();
                      } while (m_current != -1 && ScannerUtilities.isCssSpace((char) m_current));
                      switch (m_current) {
                        case '\'' :
                          string1();
                          m_blankCharacters += 2;
                          while (m_current != -1 && ScannerUtilities.isCssSpace((char) m_current)) {
                            m_blankCharacters++;
                            nextChar();
                          }
                          if (m_current == -1) {
                            throw new ParseException("eof", m_reader.getOffset());
                          }
                          if (m_current != ')') {
                            throw new ParseException("character", m_reader.getOffset());
                          }
                          nextChar();
                          m_type = LexicalUnits.URI;
                          return;
                        case '"' :
                          string2();
                          m_blankCharacters += 2;
                          while (m_current != -1 && ScannerUtilities.isCssSpace((char) m_current)) {
                            m_blankCharacters++;
                            nextChar();
                          }
                          if (m_current == -1) {
                            throw new ParseException("eof", m_reader.getOffset());
                          }
                          if (m_current != ')') {
                            throw new ParseException("character", m_reader.getOffset());
                          }
                          nextChar();
                          m_type = LexicalUnits.URI;
                          return;
                        case ')' :
                          throw new ParseException("character", m_reader.getOffset());
                        default :
                          if (!ScannerUtilities.isCssURICharacter((char) m_current)) {
                            throw new ParseException("character", m_reader.getOffset());
                          }
                          m_start = m_position - 1;
                          do {
                            nextChar();
                          } while (m_current != -1
                              && ScannerUtilities.isCssURICharacter((char) m_current));
                          m_blankCharacters++;
                          while (m_current != -1 && ScannerUtilities.isCssSpace((char) m_current)) {
                            m_blankCharacters++;
                            nextChar();
                          }
                          if (m_current == -1) {
                            throw new ParseException("eof", m_reader.getOffset());
                          }
                          if (m_current != ')') {
                            throw new ParseException("character", m_reader.getOffset());
                          }
                          nextChar();
                          m_type = LexicalUnits.URI;
                          return;
                      }
                  }
              }
          }
          while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
            nextChar();
          }
          if (m_current == '(') {
            nextChar();
            m_type = LexicalUnits.FUNCTION;
            return;
          }
          m_type = LexicalUnits.IDENTIFIER;
          return;
        default :
          if (m_current == '\\') {
            do {
              nextChar();
              escape();
            } while (m_current == '\\');
          } else if (!ScannerUtilities.isCssIdentifierStartCharacter((char) m_current)) {
            nextChar();
            throw new ParseException("identifier.character", m_reader.getOffset());
          }
          // Identifier
          while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
            nextChar();
            while (m_current == '\\') {
              nextChar();
              escape();
            }
          }
          if (m_current == '(') {
            nextChar();
            m_type = LexicalUnits.FUNCTION;
            return;
          }
          m_type = LexicalUnits.IDENTIFIER;
          return;
      }
    } catch (IOException e) {
      throw new ParseException(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Strings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Scans a single quoted string.
   */
  protected int string1() throws IOException {
    m_start = m_position; // fix bug #29416
    loop : for (;;) {
      switch (nextChar()) {
        case -1 :
          throw new ParseException("eof", m_reader.getOffset());
        case '\'' :
          break loop;
        case '"' :
          break;
        case '\\' :
          switch (nextChar()) {
            case '\n' :
            case '\f' :
              break;
            default :
              escape();
          }
          break;
        default :
          if (!ScannerUtilities.isCssStringCharacter((char) m_current)) {
            throw new ParseException("character", m_reader.getOffset());
          }
      }
    }
    nextChar();
    return LexicalUnits.STRING;
  }

  /**
   * Scans a double quoted string.
   */
  protected int string2() throws IOException {
    m_start = m_position; // fix bug #29416
    loop : for (;;) {
      switch (nextChar()) {
        case -1 :
          throw new ParseException("eof", m_reader.getOffset());
        case '\'' :
          break;
        case '"' :
          break loop;
        case '\\' :
          switch (nextChar()) {
            case '\n' :
            case '\f' :
              break;
            default :
              escape();
          }
          break;
        default :
          if (!ScannerUtilities.isCssStringCharacter((char) m_current)) {
            throw new ParseException("character", m_reader.getOffset());
          }
      }
    }
    nextChar();
    return LexicalUnits.STRING;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Numbers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Scans a number.
   */
  protected int number() throws IOException {
    loop : for (;;) {
      switch (nextChar()) {
        case '.' :
          switch (nextChar()) {
            case '0' :
            case '1' :
            case '2' :
            case '3' :
            case '4' :
            case '5' :
            case '6' :
            case '7' :
            case '8' :
            case '9' :
              return dotNumber();
          }
          throw new ParseException("character", m_reader.getOffset());
        default :
          break loop;
        case '0' :
        case '1' :
        case '2' :
        case '3' :
        case '4' :
        case '5' :
        case '6' :
        case '7' :
        case '8' :
        case '9' :
      }
    }
    return numberUnit(true);
  }

  /**
   * Scans the decimal part of a number.
   */
  protected int dotNumber() throws IOException {
    loop : for (;;) {
      switch (nextChar()) {
        default :
          break loop;
        case '0' :
        case '1' :
        case '2' :
        case '3' :
        case '4' :
        case '5' :
        case '6' :
        case '7' :
        case '8' :
        case '9' :
      }
    }
    return numberUnit(false);
  }

  /**
   * Scans the unit of a number.
   */
  protected int numberUnit(boolean integer) throws IOException {
    switch (m_current) {
      case '%' :
        nextChar();
        return LexicalUnits.PERCENTAGE;
      case 'c' :
      case 'C' :
        switch (nextChar()) {
          case 'm' :
          case 'M' :
            nextChar();
            if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              do {
                nextChar();
              } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
              return LexicalUnits.DIMENSION;
            }
            return LexicalUnits.CM;
          default :
            while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              nextChar();
            }
            return LexicalUnits.DIMENSION;
        }
      case 'd' :
      case 'D' :
        switch (nextChar()) {
          case 'e' :
          case 'E' :
            switch (nextChar()) {
              case 'g' :
              case 'G' :
                nextChar();
                if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
                  do {
                    nextChar();
                  } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
                  return LexicalUnits.DIMENSION;
                }
                return LexicalUnits.DEG;
            }
          default :
            while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              nextChar();
            }
            return LexicalUnits.DIMENSION;
        }
      case 'e' :
      case 'E' :
        switch (nextChar()) {
          case 'm' :
          case 'M' :
            nextChar();
            if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              do {
                nextChar();
              } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
              return LexicalUnits.DIMENSION;
            }
            return LexicalUnits.EM;
          case 'x' :
          case 'X' :
            nextChar();
            if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              do {
                nextChar();
              } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
              return LexicalUnits.DIMENSION;
            }
            return LexicalUnits.EX;
          default :
            while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              nextChar();
            }
            return LexicalUnits.DIMENSION;
        }
      case 'g' :
      case 'G' :
        switch (nextChar()) {
          case 'r' :
          case 'R' :
            switch (nextChar()) {
              case 'a' :
              case 'A' :
                switch (nextChar()) {
                  case 'd' :
                  case 'D' :
                    nextChar();
                    if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
                      do {
                        nextChar();
                      } while (m_current != -1
                          && ScannerUtilities.isCssNameCharacter((char) m_current));
                      return LexicalUnits.DIMENSION;
                    }
                    return LexicalUnits.GRAD;
                }
            }
          default :
            while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              nextChar();
            }
            return LexicalUnits.DIMENSION;
        }
      case 'h' :
      case 'H' :
        nextChar();
        switch (m_current) {
          case 'z' :
          case 'Z' :
            nextChar();
            if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              do {
                nextChar();
              } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
              return LexicalUnits.DIMENSION;
            }
            return LexicalUnits.HZ;
          default :
            while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              nextChar();
            }
            return LexicalUnits.DIMENSION;
        }
      case 'i' :
      case 'I' :
        switch (nextChar()) {
          case 'n' :
          case 'N' :
            nextChar();
            if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              do {
                nextChar();
              } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
              return LexicalUnits.DIMENSION;
            }
            return LexicalUnits.IN;
          default :
            while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              nextChar();
            }
            return LexicalUnits.DIMENSION;
        }
      case 'k' :
      case 'K' :
        switch (nextChar()) {
          case 'h' :
          case 'H' :
            switch (nextChar()) {
              case 'z' :
              case 'Z' :
                nextChar();
                if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
                  do {
                    nextChar();
                  } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
                  return LexicalUnits.DIMENSION;
                }
                return LexicalUnits.KHZ;
            }
          default :
            while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              nextChar();
            }
            return LexicalUnits.DIMENSION;
        }
      case 'm' :
      case 'M' :
        switch (nextChar()) {
          case 'm' :
          case 'M' :
            nextChar();
            if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              do {
                nextChar();
              } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
              return LexicalUnits.DIMENSION;
            }
            return LexicalUnits.MM;
          case 's' :
          case 'S' :
            nextChar();
            if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              do {
                nextChar();
              } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
              return LexicalUnits.DIMENSION;
            }
            return LexicalUnits.MS;
          default :
            while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              nextChar();
            }
            return LexicalUnits.DIMENSION;
        }
      case 'p' :
      case 'P' :
        switch (nextChar()) {
          case 'c' :
          case 'C' :
            nextChar();
            if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              do {
                nextChar();
              } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
              return LexicalUnits.DIMENSION;
            }
            return LexicalUnits.PC;
          case 't' :
          case 'T' :
            nextChar();
            if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              do {
                nextChar();
              } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
              return LexicalUnits.DIMENSION;
            }
            return LexicalUnits.PT;
          case 'x' :
          case 'X' :
            nextChar();
            if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              do {
                nextChar();
              } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
              return LexicalUnits.DIMENSION;
            }
            return LexicalUnits.PX;
          default :
            while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              nextChar();
            }
            return LexicalUnits.DIMENSION;
        }
      case 'r' :
      case 'R' :
        switch (nextChar()) {
          case 'a' :
          case 'A' :
            switch (nextChar()) {
              case 'd' :
              case 'D' :
                nextChar();
                if (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
                  do {
                    nextChar();
                  } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
                  return LexicalUnits.DIMENSION;
                }
                return LexicalUnits.RAD;
            }
          default :
            while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current)) {
              nextChar();
            }
            return LexicalUnits.DIMENSION;
        }
      case 's' :
      case 'S' :
        nextChar();
        return LexicalUnits.S;
      default :
        if (m_current != -1 && ScannerUtilities.isCssIdentifierStartCharacter((char) m_current)) {
          do {
            nextChar();
          } while (m_current != -1 && ScannerUtilities.isCssNameCharacter((char) m_current));
          return LexicalUnits.DIMENSION;
        }
        return integer ? LexicalUnits.INTEGER : LexicalUnits.REAL;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Escape sequence
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Scans an escape sequence, if one.
   */
  protected void escape() throws IOException {
    if (ScannerUtilities.isCssHexadecimalCharacter((char) m_current)) {
      nextChar();
      if (!ScannerUtilities.isCssHexadecimalCharacter((char) m_current)) {
        if (ScannerUtilities.isCssSpace((char) m_current)) {
          nextChar();
        }
        return;
      }
      nextChar();
      if (!ScannerUtilities.isCssHexadecimalCharacter((char) m_current)) {
        if (ScannerUtilities.isCssSpace((char) m_current)) {
          nextChar();
        }
        return;
      }
      nextChar();
      if (!ScannerUtilities.isCssHexadecimalCharacter((char) m_current)) {
        if (ScannerUtilities.isCssSpace((char) m_current)) {
          nextChar();
        }
        return;
      }
      nextChar();
      if (!ScannerUtilities.isCssHexadecimalCharacter((char) m_current)) {
        if (ScannerUtilities.isCssSpace((char) m_current)) {
          nextChar();
        }
        return;
      }
      nextChar();
      if (!ScannerUtilities.isCssHexadecimalCharacter((char) m_current)) {
        if (ScannerUtilities.isCssSpace((char) m_current)) {
          nextChar();
        }
        return;
      }
    }
    if (m_current >= ' ' && m_current <= '~' || m_current >= 128) {
      nextChar();
      return;
    }
    throw new ParseException("character", m_reader.getOffset());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Characters
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Compares the given int with the given character, ignoring case.
   */
  protected static boolean isEqualIgnoreCase(int i, char c) {
    return i == -1 ? false : Character.toLowerCase((char) i) == c;
  }

  /**
   * Sets the value of the current char to the next character or -1 if the end of stream has been
   * reached.
   */
  protected int nextChar() throws IOException {
    m_current = m_reader.read();
    if (m_current == -1) {
      return m_current;
    }
    if (m_position == m_buffer.length) {
      char[] t = new char[m_position * 3 / 2];
      for (int i = 0; i < m_position; i++) {
        t[i] = m_buffer[i];
      }
      m_buffer = t;
    }
    return m_buffer[m_position++] = (char) m_current;
  }
}
