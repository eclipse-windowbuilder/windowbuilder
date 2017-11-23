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
package org.eclipse.wb.tests.designer.core.util.xml;

import org.eclipse.wb.internal.core.utils.xml.parser.QAttribute;
import org.eclipse.wb.internal.core.utils.xml.parser.QException;
import org.eclipse.wb.internal.core.utils.xml.parser.QHandlerAdapter;
import org.eclipse.wb.internal.core.utils.xml.parser.QParser;
import org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Test for {@link QParser}.
 * 
 * @author scheglov_ke
 */
public class QParserTest extends AbstractJavaProjectTest {
  private QHandlerAdapter handler;
  private final StringBuilder events = new StringBuilder();

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
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (m_testProject == null) {
      do_projectCreate();
    }
  }

  @Override
  protected void tearDown() throws Exception {
    handler = null;
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parses given lines using {@link #handler}.
   */
  private void parse(String... lines) throws Exception {
    String source = getSourceDQ(lines);
    parseSource(source);
  }

  private void parseSource(String source) throws Exception {
    if (handler == null) {
      rememberEventsDuringParsing();
    }
    StringReader reader = new StringReader(source);
    QParser.parse(reader, handler);
  }

  private void rememberEventsDuringParsing() {
    events.append("----------------------- filler -----------------------\n");
    handler = new QHandlerAdapter() {
      ////////////////////////////////////////////////////////////////////////////
      //
      // Document
      //
      ////////////////////////////////////////////////////////////////////////////
      @Override
      public void startDocument() throws Exception {
        addLine("startDocument");
        m_level++;
      }

      @Override
      public void endDocument() throws Exception {
        m_level--;
        addLine("endDocument");
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Element
      //
      ////////////////////////////////////////////////////////////////////////////
      @Override
      public void startElement(int offset,
          int length,
          String tag,
          Map<String, String> attributes,
          List<QAttribute> attrList,
          boolean closed) throws Exception {
        addLine("<" + tag + " offset:" + offset + " length:" + length + (closed ? " closed" : ""));
        m_level++;
        for (QAttribute attribute : attrList) {
          addLine("A "
              + attribute.getName()
              + " |"
              + attribute.getValue()
              + "|"
              + " nameOffset:"
              + attribute.getNameOffset()
              + " nameLength:"
              + attribute.getNameLength()
              + " valueOffset:"
              + attribute.getValueOffset()
              + " valueLength:"
              + attribute.getValueLength());
        }
      }

      @Override
      public void endElement(int offset, int endOffset, String tag) throws Exception {
        m_level--;
        addLine(MessageFormat.format(">{0} offset:{1} endOffset:{2}", tag, offset, endOffset));
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Text
      //
      ////////////////////////////////////////////////////////////////////////////
      @Override
      public void text(String text, boolean isCDATA) throws Exception {
        addLine("T " + isCDATA + " " + text);
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Utils
      //
      ////////////////////////////////////////////////////////////////////////////
      private int m_level;

      private void addLine(String line) {
        events.append(StringUtils.repeat("\t", m_level));
        events.append(line);
        events.append("\n");
      }
    };
  }

  private void assertParseEvents(String... lines) {
    assertEquals(getSource(lines), events.toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bad
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_badEmpty() throws Exception {
    try {
      parseSource("");
    } catch (QException e) {
      assertThat(e.getMessage()).contains("missing end tag");
    }
  }

  public void test_badEmpty_line1() throws Exception {
    try {
      parseSource("");
    } catch (QException e) {
      assertThat(e.getMessage()).contains("line 1").contains("column 0");
    }
  }

  public void test_badEmpty_line2_slashN() throws Exception {
    try {
      parseSource("\n");
    } catch (QException e) {
      assertThat(e.getMessage()).contains("line 2").contains("column 0");
    }
  }

  public void test_badEmpty_line2_slashR() throws Exception {
    try {
      parseSource("\r");
    } catch (QException e) {
      assertThat(e.getMessage()).contains("line 2").contains("column 0");
    }
  }

  public void test_badEmpty_line2_slashR_slashN() throws Exception {
    try {
      parseSource("\r\n");
    } catch (QException e) {
      assertThat(e.getMessage()).contains("line 2").contains("column 0");
    }
  }

  public void test_badEmpty_line3_slashR_space_slashN() throws Exception {
    try {
      parseSource("\r \n");
    } catch (QException e) {
      assertThat(e.getMessage()).contains("line 3").contains("column 0");
    }
  }

  public void test_badEmpty_line1_column3() throws Exception {
    try {
      parseSource("123");
    } catch (QException e) {
      assertThat(e.getMessage()).contains("line 1").contains("column 3");
    }
  }

  public void test_bad_noCloseAngleBrace_afterSlash() throws Exception {
    try {
      parseSource("<root/foo");
    } catch (QException e) {
      assertThat(e.getMessage()).isEqualTo("Expected > for tag: <root/> near line 1, column 7");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Prefixes
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_withComment() throws Exception {
    parseSource("<root><!-- abc --></root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "  >root offset:18 endOffset:25",
        "endDocument");
  }

  public void test_withXMLVersion() throws Exception {
    String prefix = "<?xml version='1.0' encoding='UTF-8'?>";
    int prefixLength = prefix.length() + "\n".length();
    parseSource(getSourceDQ(prefix, "<root/>"));
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:" + (0 + prefixLength) + " length:-1 closed",
        "  >root offset:-1 endOffset:" + (7 + prefixLength),
        "endDocument");
  }

  public void test_withDocType() throws Exception {
    String prefix = "<!DOCTYPE strict>";
    int prefixLength = prefix.length() + "\n".length();
    parse(prefix, "<root/>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:" + (0 + prefixLength) + " length:-1 closed",
        "  >root offset:-1 endOffset:" + (7 + prefixLength),
        "endDocument");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tags
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_rootElement_closed() throws Exception {
    parseSource("<root/>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:-1 closed",
        "  >root offset:-1 endOffset:7",
        "endDocument");
  }

  public void test_rootElement_ignoreAllAfterCloseRoot() throws Exception {
    parseSource("<root></root>ignored");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "  >root offset:6 endOffset:13",
        "endDocument");
  }

  public void test_rootElement_open() throws Exception {
    parseSource("<root></root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "  >root offset:6 endOffset:13",
        "endDocument");
  }

  public void test_withSubTag() throws Exception {
    parseSource("<root><inner/></root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "    <inner offset:6 length:-1 closed",
        "    >inner offset:-1 endOffset:14",
        "  >root offset:14 endOffset:21",
        "endDocument");
  }

  public void test_useAdapter() throws Exception {
    handler = new QHandlerAdapter() {
    };
    parseSource("<root>txt</root>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Attributes
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_attribute() throws Exception {
    parseSource("<root foo='baar'/>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:-1 closed",
        "    A foo |baar| nameOffset:6 nameLength:3 valueOffset:11 valueLength:4",
        "  >root offset:-1 endOffset:18",
        "endDocument");
  }

  public void test_attribute_inOpen() throws Exception {
    parseSource("<root foo='baar'></root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:17",
        "    A foo |baar| nameOffset:6 nameLength:3 valueOffset:11 valueLength:4",
        "  >root offset:17 endOffset:24",
        "endDocument");
  }

  public void test_attribute_whitespaceBeforeEquals() throws Exception {
    parseSource("<root foo ='baar'/>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:-1 closed",
        "    A foo |baar| nameOffset:6 nameLength:3 valueOffset:12 valueLength:4",
        "  >root offset:-1 endOffset:19",
        "endDocument");
  }

  public void test_attribute_whitespaceBeforeEquals2() throws Exception {
    parseSource("<root foo  ='baar'/>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:-1 closed",
        "    A foo |baar| nameOffset:6 nameLength:3 valueOffset:13 valueLength:4",
        "  >root offset:-1 endOffset:20",
        "endDocument");
  }

  public void test_attribute_whitespaceAfterEquals() throws Exception {
    parseSource("<root foo= 'baar'/>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:-1 closed",
        "    A foo |baar| nameOffset:6 nameLength:3 valueOffset:12 valueLength:4",
        "  >root offset:-1 endOffset:19",
        "endDocument");
  }

  public void test_attribute_invalidBeforeEquals() throws Exception {
    try {
      parseSource("<root foo invalid=/>");
    } catch (QException e) {
      assertThat(e.getMessage()).isEqualTo("Error in attribute processing near line 1, column 11");
    }
  }

  public void test_attribute_invalidAfterEquals() throws Exception {
    try {
      parseSource("<root foo=invalid/>");
    } catch (QException e) {
      assertThat(e.getMessage()).isEqualTo("Error in attribute processing near line 1, column 11");
    }
  }

  public void test_attribute_normalizeWhitespaces() throws Exception {
    parseSource("<root foo='a\n\r\t b'/>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:-1 closed",
        "    A foo |a    b| nameOffset:6 nameLength:3 valueOffset:11 valueLength:6",
        "  >root offset:-1 endOffset:20",
        "endDocument");
  }

  public void test_attribute_entity() throws Exception {
    parseSource("<root foo='&#65;&#66;'/>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:-1 closed",
        "    A foo |AB| nameOffset:6 nameLength:3 valueOffset:11 valueLength:10",
        "  >root offset:-1 endOffset:24",
        "endDocument");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Text
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_text_simple() throws Exception {
    parseSource("<root>abc</root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "    T false abc",
        "  >root offset:9 endOffset:16",
        "endDocument");
  }

  public void test_text_CDATA() throws Exception {
    parseSource("<root><![CDATA[abc]]></root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "    T true abc",
        "  >root offset:21 endOffset:28",
        "endDocument");
  }

  public void test_text_slashR_slashN() throws Exception {
    parseSource("<root>\r\n123\r\n</root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "    T false \r\n123\r\n",
        "  >root offset:13 endOffset:20",
        "endDocument");
  }

  public void test_textSpecial_lt() throws Exception {
    parseSource("<root>&lt;</root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "    T false <",
        "  >root offset:10 endOffset:17",
        "endDocument");
  }

  public void test_textSpecial_gt() throws Exception {
    parseSource("<root>&gt;</root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "    T false >",
        "  >root offset:10 endOffset:17",
        "endDocument");
  }

  public void test_textSpecial_amp() throws Exception {
    parseSource("<root>&amp;</root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "    T false &",
        "  >root offset:11 endOffset:18",
        "endDocument");
  }

  public void test_textSpecial_quot() throws Exception {
    parseSource("<root>&quot;</root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "    T false \"",
        "  >root offset:12 endOffset:19",
        "endDocument");
  }

  public void test_textSpecial_apos() throws Exception {
    parseSource("<root>&apos;</root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "    T false '",
        "  >root offset:12 endOffset:19",
        "endDocument");
  }

  public void test_textSpecial_hexNumber() throws Exception {
    parseSource("<root>&#x41;</root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "    T false A",
        "  >root offset:12 endOffset:19",
        "endDocument");
  }

  public void test_textSpecial_decNumber() throws Exception {
    parseSource("<root>&#65;</root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "    T false A",
        "  >root offset:11 endOffset:18",
        "endDocument");
  }

  /**
   * For use cases of {@link QParser} we don't need to know values of entities.
   */
  public void test_textSpecial_unknownEntity() throws Exception {
    parseSource("<root>&unknown;</root>");
    assertParseEvents(
        "----------------------- filler -----------------------",
        "startDocument",
        "  <root offset:0 length:6",
        "    T false &unknown;",
        "  >root offset:15 endOffset:22",
        "endDocument");
  }
}