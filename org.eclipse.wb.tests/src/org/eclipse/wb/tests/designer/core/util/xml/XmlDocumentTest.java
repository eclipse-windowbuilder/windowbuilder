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

import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentEditContext;
import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentHandler;
import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentObject;
import org.eclipse.wb.internal.core.utils.xml.DocumentAttribute;
import org.eclipse.wb.internal.core.utils.xml.DocumentEditContext;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.DocumentModelVisitor;
import org.eclipse.wb.internal.core.utils.xml.DocumentTextNode;
import org.eclipse.wb.internal.core.utils.xml.FileDocumentEditContext;
import org.eclipse.wb.internal.core.utils.xml.Model;
import org.eclipse.wb.internal.core.utils.xml.parser.QException;
import org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link FileDocumentEditContext}, {@link DocumentElement}, etc.
 * 
 * @author lobas_av
 * @author scheglov_ke
 */
public class XmlDocumentTest extends AbstractJavaProjectTest {
  private boolean m_removeTrailingEOL = false;
  private AbstractDocumentEditContext context;
  private DocumentElement rootElement;

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
    if (context != null) {
      context.disconnect();
      context = null;
    }
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parses given lines of XML and prepares {@link #context} and {@link #rootElement}.
   */
  private void prepareContext(String... lines) throws Exception {
    String source = getXMLSource(lines);
    Document document = new Document(source);
    context = new DocumentEditContext(document) {
      @Override
      protected AbstractDocumentHandler createDocumentHandler() {
        return new TestDocumentHandler();
      }
    };
    rootElement = context.getRoot();
    assertOffsets();
  }

  /**
   * @return the XML source for given lines.
   */
  private String getXMLSource(String... lines) {
    String source = getSourceDQ(lines);
    if (m_removeTrailingEOL) {
      source = StringUtils.chomp(source);
    }
    return source;
  }

  /**
   * Asserts that XML document has expected content and valid offset/length combinations.
   */
  private void assertContext(String... lines) throws Exception {
    String source = getXMLSource(lines);
    assertEquals(source, context.getText());
    assertOffsets();
  }

  /**
   * Asserts that all {@link AbstractDocumentObject}s have valid offset/length combinations.
   */
  private void assertOffsets() {
    rootElement.accept(new DocumentModelVisitor() {
      @Override
      public void endVisit(DocumentElement element) {
        String tag = element.getTag();
        if (element.isClosed()) {
          int offset = element.getOffset();
          int length = element.getLength();
          String text = context.getText(offset, length);
          assertThat(text).startsWith("<" + tag);
          assertThat(text).endsWith("/>");
        } else {
          // open tag
          {
            int offset = element.getOpenTagOffset();
            int length = element.getOpenTagLength();
            String text = context.getText(offset, length);
            assertThat(text).startsWith("<" + tag);
            assertThat(text).endsWith(">");
          }
          // close tag
          {
            int offset = element.getCloseTagOffset();
            int length = element.getCloseTagLength();
            String text = context.getText(offset, length);
            assertThat(text).isEqualTo("</" + tag + ">");
          }
          // all
          {
            int offset = element.getOffset();
            int length = element.getLength();
            String text = context.getText(offset, length);
            assertThat(text).startsWith("<" + tag);
            assertThat(text).endsWith("</" + tag + ">");
          }
        }
        // attributes
        for (DocumentAttribute attribute : element.getDocumentAttributes()) {
          validateAttribute(element, attribute);
        }
        // text node
        validateTextNode(element);
      }

      private void validateAttribute(DocumentElement element, DocumentAttribute attribute) {
        assertSame(element, attribute.getEnclosingElement());
        assertSame(element.getModel(), attribute.getModel());
        // name
        {
          int offset = attribute.getNameOffset();
          int length = attribute.getNameLength();
          String text = context.getText(offset, length);
          assertThat(text).isEqualTo(attribute.getName());
        }
        // value
        {
          int offset = attribute.getValueOffset();
          int length = attribute.getValueLength();
          String text = context.getText(offset, length);
          assertThat(text).isEqualTo(attribute.getValue());
        }
      }

      private void validateTextNode(DocumentElement element) {
        DocumentTextNode textNode = element.getTextNode();
        if (textNode == null) {
          return;
        }
        // references
        assertSame(element, textNode.getEnclosingElement());
        assertSame(element.getModel(), textNode.getModel());
        // content
        String text = context.getText(textNode.getOffset(), textNode.getLength());
        if (textNode.isCDATA()) {
          assertThat(text).isEqualTo(textNode.getRawText());
          assertThat(text).startsWith("<![CDATA[").endsWith("]]>");
          assertThat(text).contains(textNode.getText());
        } else {
          assertThat(text).isEqualTo(textNode.getRawText());
          assertThat(text).isEqualTo(textNode.getText());
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Attempt to parse bad XML throws exception.
   */
  public void test_parse_error() throws Exception {
    try {
      prepareContext("bad");
      fail();
    } catch (Throwable e) {
      Throwable rootCause = DesignerExceptionUtils.getRootCause(e);
      assertInstanceOf(QException.class, rootCause);
    }
  }

  /**
   * Test for basic parsing - attribute and child {@link DocumentElement}.
   */
  public void test_parse() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root name='value'>",
        "  <first/>",
        "</root>");
    // check model
    Model model = rootElement.getModel();
    assertNotNull(model);
    // check "root"
    {
      assertSame(null, rootElement.getParent());
      assertEquals("root", rootElement.getTag());
      assertFalse(rootElement.isClosed());
    }
    // attribute "name" as Object
    {
      List<DocumentAttribute> attributes = rootElement.getDocumentAttributes();
      assertThat(attributes).hasSize(1);
      //
      DocumentAttribute attribute = rootElement.getDocumentAttribute("name");
      assertSame(attribute, attributes.get(0));
      assertEquals("name", attribute.getName());
      assertEquals("value", attribute.getValue());
    }
    // access attribute value
    {
      assertEquals("value", rootElement.getAttribute("name"));
      assertEquals(null, rootElement.getAttribute("name1"));
    }
    // child elements
    List<DocumentElement> rootChildren = rootElement.getChildren();
    assertThat(rootChildren).hasSize(1);
    // check "first"
    {
      DocumentElement firstElement = rootChildren.get(0);
      assertNotNull(firstElement);
      assertSame(model, firstElement.getModel());
      assertSame(rootElement, firstElement.getParent());
      assertEquals("first", firstElement.getTag());
      assertTrue(firstElement.isClosed());
      // no attributes
      List<DocumentAttribute> attributes = firstElement.getDocumentAttributes();
      assertThat(attributes).isEmpty();
    }
    // toString()
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root name='value'>",
        "  <first/>",
        "</root>");
  }

  /**
   * Test for {@link DocumentElement#getRoot()}.
   */
  public void test_getRoot() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root name='value'>",
        "  <first/>",
        "</root>");
    // root itself
    assertSame(rootElement, rootElement.getRoot());
    // child
    {
      DocumentElement child = rootElement.getChildAt(0);
      assertSame(rootElement, child.getRoot());
    }
  }

  /**
   * Test for {@link DocumentElement#getTagLocal()}.
   */
  public void test_getTagLocal() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root xmlns:wbp='someValue'>",
        "  <first/>",
        "  <wbp:second/>",
        "</root>");
    // first
    {
      DocumentElement element = rootElement.getChildAt(0);
      assertEquals("first", element.getTag());
      assertEquals("first", element.getTagLocal());
    }
    // second
    {
      DocumentElement element = rootElement.getChildAt(1);
      assertEquals("wbp:second", element.getTag());
      assertEquals("second", element.getTagLocal());
    }
  }

  /**
   * Test for {@link DocumentElement#getTagNS()}.
   */
  public void test_getTagNS() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root xmlns:wbp='someValue'>",
        "  <first/>",
        "  <wbp:second/>",
        "</root>");
    // first
    {
      DocumentElement element = rootElement.getChildAt(0);
      assertEquals("first", element.getTag());
      assertEquals("", element.getTagNS());
    }
    // second
    {
      DocumentElement element = rootElement.getChildAt(1);
      assertEquals("wbp:second", element.getTag());
      assertEquals("wbp:", element.getTagNS());
    }
  }

  /**
   * Test for {@link DocumentElement#getChild(String)}.
   */
  public void test_getChild_byTag() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root name='value'>",
        "  <first/>",
        "  <second/>",
        "</root>");
    DocumentElement first = rootElement.getChildAt(0);
    DocumentElement second = rootElement.getChildAt(1);
    //
    assertSame(null, rootElement.getChild("noSuch", false));
    assertSame(first, rootElement.getChild("first", false));
    assertSame(second, rootElement.getChild("second", false));
    // case
    assertSame(null, rootElement.getChild("First", false));
    assertSame(first, rootElement.getChild("First", true));
  }

  /**
   * Test for {@link DocumentElement#getDirectChild(DocumentElement)}.
   */
  public void test_getDirectChild() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root name='value'>",
        "  <parent>",
        "    <directChild>",
        "      <indirectChild/>",
        "    </directChild>",
        "  </parent>",
        "  <notChild/>",
        "</root>");
    DocumentElement parent = rootElement.getChildAt(0);
    DocumentElement directChild = parent.getChildAt(0);
    DocumentElement indirectChild = directChild.getChildAt(0);
    DocumentElement notChild = rootElement.getChildAt(1);
    //
    assertSame(directChild, parent.getDirectChild(directChild));
    assertSame(directChild, parent.getDirectChild(indirectChild));
    assertSame(null, parent.getDirectChild(notChild));
    assertSame(null, parent.getDirectChild(rootElement));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Text node
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_textNode_parse() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description>Some text</description>",
        "  <source><![CDATA[Some source]]></source>",
        "</root>");
    // <description> and <source> 
    List<DocumentElement> children = rootElement.getChildren();
    assertThat(children).hasSize(2);
    // <description>
    {
      DocumentElement descriptionElement = children.get(0);
      assertEquals("description", descriptionElement.getTag());
      //
      DocumentTextNode textNode = descriptionElement.getTextNode();
      assertSame(descriptionElement, textNode.getEnclosingElement());
      assertFalse(textNode.isCDATA());
      assertEquals("Some text", textNode.getText());
    }
    // <source>
    {
      DocumentElement sourceElement = children.get(1);
      assertEquals("source", sourceElement.getTag());
      //
      DocumentTextNode textNode = sourceElement.getTextNode();
      assertSame(sourceElement, textNode.getEnclosingElement());
      assertTrue(textNode.isCDATA());
      assertEquals("Some source", textNode.getText());
    }
    // toString()
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description>Some text</description>",
        "  <source><![CDATA[Some source]]></source>",
        "</root>");
  }

  public void test_textNode_edit() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description>Some text</description>",
        "</root>");
    // prepare text node
    DocumentElement parent = rootElement.getChildAt(0);
    DocumentTextNode textNode = parent.getTextNode();
    // initial state
    assertFalse(textNode.isCDATA());
    assertEquals("Some text", textNode.getText());
    // set new text
    {
      String text = "Other text";
      textNode.setText(text);
      assertEquals(text, textNode.getText());
      assertContext(
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<!-- ==================================== -->",
          "<root>",
          "  <description>Other text</description>",
          "</root>");
    }
    // set text with "\n"
    {
      String text = "First line\nSecond line";
      textNode.setText(text);
      assertEquals(text, textNode.getText());
      assertContext(
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<!-- ==================================== -->",
          "<root>",
          "  <description>First line\nSecond line</description>",
          "</root>");
    }
    // set empty text
    {
      String text = "";
      textNode.setText(text);
      assertEquals(text, textNode.getText());
      assertContext(
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<!-- ==================================== -->",
          "<root>",
          "  <description></description>",
          "</root>");
    }
    // set some text
    {
      String text = "the text";
      textNode.setText(text);
      assertEquals(text, textNode.getText());
      assertContext(
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<!-- ==================================== -->",
          "<root>",
          "  <description>the text</description>",
          "</root>");
    }
    // remove text node
    {
      parent.removeTextNode();
      assertSame(null, parent.getTextNode());
      assertContext(
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<!-- ==================================== -->",
          "<root>",
          "  <description/>",
          "</root>");
    }
  }

  public void test_textNode_edit_CDATA() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description><![CDATA[Some text]]></description>",
        "</root>");
    // prepare text node
    DocumentElement parent = rootElement.getChildAt(0);
    DocumentTextNode textNode = parent.getTextNode();
    // initial state
    assertTrue(textNode.isCDATA());
    assertEquals("Some text", textNode.getText());
    // set new text
    {
      String text = "Other text";
      textNode.setText(text);
      assertEquals(text, textNode.getText());
      assertContext(
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<!-- ==================================== -->",
          "<root>",
          "  <description><![CDATA[Other text]]></description>",
          "</root>");
    }
    // remove text node
    {
      parent.removeTextNode();
      assertSame(null, parent.getTextNode());
      assertContext(
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<!-- ==================================== -->",
          "<root>",
          "  <description/>",
          "</root>");
    }
  }

  public void test_textNode_edit_CDATA2() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description><![CDATA[Some text]]></description>",
        "</root>");
    // prepare text node
    DocumentElement parent = rootElement.getChildAt(0);
    DocumentTextNode textNode = parent.getTextNode();
    // initial state
    assertTrue(textNode.isCDATA());
    assertEquals("Some text", textNode.getText());
    // remove CDATA flag
    textNode.setCDATA(false);
    assertFalse(textNode.isCDATA());
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description>Some text</description>",
        "</root>");
  }

  public void test_textNode_add_whenOpen() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description></description>",
        "</root>");
    DocumentElement parent = rootElement.getChildAt(0);
    // initial state
    assertEquals(null, parent.getTextNode());
    // create text node
    DocumentTextNode textNode = new DocumentTextNode(false);
    textNode.setText("Some text");
    // add text node
    parent.setTextNode(textNode);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description>Some text</description>",
        "</root>");
  }

  public void test_textNode_add_whenClosed() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description/>",
        "</root>");
    DocumentElement parent = rootElement.getChildAt(0);
    // initial state
    assertEquals(null, parent.getTextNode());
    // create text node
    DocumentTextNode textNode = new DocumentTextNode(false);
    textNode.setText("Some text");
    // add text node
    parent.setTextNode(textNode);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description>Some text</description>",
        "</root>");
  }

  public void test_textNode_add_CDATA() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description></description>",
        "</root>");
    DocumentElement parent = rootElement.getChildAt(0);
    // initial state
    assertEquals(null, parent.getTextNode());
    // create text node
    DocumentTextNode textNode = new DocumentTextNode(false);
    textNode.setCDATA(true);
    textNode.setText("Some text");
    // add text node
    parent.setTextNode(textNode);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description><![CDATA[Some text]]></description>",
        "</root>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setText()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_textNode_setText_new() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description/>",
        "</root>");
    DocumentElement element = rootElement.getChildAt(0);
    // initial state
    assertEquals(null, element.getTextNode());
    // set text
    element.setText("Some text", false);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description>Some text</description>",
        "</root>");
  }

  public void test_textNode_setText_new_CDATA() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description/>",
        "</root>");
    DocumentElement element = rootElement.getChildAt(0);
    // initial state
    assertEquals(null, element.getTextNode());
    // set text
    element.setText("Some text", true);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description><![CDATA[Some text]]></description>",
        "</root>");
  }

  public void test_textNode_setText_replace() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description>Old text</description>",
        "</root>");
    DocumentElement element = rootElement.getChildAt(0);
    // set text
    element.setText("New text", false);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description>New text</description>",
        "</root>");
  }

  public void test_textNode_setText_remove() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description>Some text</description>",
        "</root>");
    DocumentElement element = rootElement.getChildAt(0);
    // set text
    element.setText(null, false);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <description/>",
        "</root>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Attribute
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DocumentElement#setAttribute(String, String)}.
   */
  public void test_attribute_addFirst() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root/>");
    // no attributes initially
    assertThat(rootElement.getDocumentAttributes()).isEmpty();
    // add attribute as value
    DocumentAttribute newAttribute = rootElement.setAttribute("name", "value");
    assertEquals("name", newAttribute.getName());
    assertEquals("value", newAttribute.getValue());
    // attribute is in element
    List<DocumentAttribute> attributes = rootElement.getDocumentAttributes();
    assertThat(attributes).containsOnly(newAttribute);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root name='value'/>");
  }

  /**
   * Test for {@link DocumentElement#setAttribute(String, String)}.
   */
  public void test_attribute_addSecond() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root other='attr'/>");
    // one attribute initially
    assertThat(rootElement.getDocumentAttributes()).hasSize(1);
    // add attribute as value
    DocumentAttribute newAttribute = rootElement.setAttribute("name", "value");
    assertEquals("name", newAttribute.getName());
    assertEquals("value", newAttribute.getValue());
    // attribute is in element
    List<DocumentAttribute> attributes = rootElement.getDocumentAttributes();
    assertThat(attributes).contains(newAttribute);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root other='attr' name='value'/>");
  }

  /**
   * Test for {@link DocumentElement#setAttribute(String, String)}.
   */
  public void test_attribute_addThird() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root a='1' b='2'/>");
    // two attributes initially
    assertThat(rootElement.getDocumentAttributes()).hasSize(2);
    // add attribute as value
    DocumentAttribute newAttribute = rootElement.setAttribute("name", "value");
    assertEquals("name", newAttribute.getName());
    assertEquals("value", newAttribute.getValue());
    // attribute is in element
    List<DocumentAttribute> attributes = rootElement.getDocumentAttributes();
    assertThat(attributes).contains(newAttribute);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root a='1' b='2' name='value'/>");
  }

  /**
   * Test for {@link DocumentElement#setAttribute(String, String)}.
   */
  public void test_attribute_addNullValue() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root/>");
    // no attributes initially
    assertThat(rootElement.getDocumentAttributes()).isEmpty();
    // try to add, but "null" value
    DocumentAttribute newAttribute = rootElement.setAttribute("name", null);
    assertNull(newAttribute);
    // still no attributes
    assertThat(rootElement.getDocumentAttributes()).isEmpty();
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root/>");
  }

  /**
   * Test for {@link DocumentElement#setAttribute(String, String)}.
   * <p>
   * Depending on charset we should or should not encode non-Latin characters.
   */
  public void test_attribute_charset() throws Exception {
    String rusValue = "\u0410\u0411\u0412";
    prepareContext(
        "<!-- ==================================== -->",
        "<!-- ==================================== -->",
        "<root/>");
    // special characters
    rootElement.setAttribute("name", "\n ' \" < > &");
    assertContext(
        "<!-- ==================================== -->",
        "<!-- ==================================== -->",
        "<root name='&#10; &apos; &quot; &lt; &gt; &amp;'/>");
    // no charset in model, encoded
    assertEquals(null, rootElement.getModel().getCharset());
    rootElement.setAttribute("name", rusValue);
    assertContext(
        "<!-- ==================================== -->",
        "<!-- ==================================== -->",
        "<root name='&#1040;&#1041;&#1042;'/>");
    // UTF-8, plain
    rootElement.getModel().setCharset("UTF-8");
    rootElement.setAttribute("name", rusValue);
    assertContext(
        "<!-- ==================================== -->",
        "<!-- ==================================== -->",
        "<root name='\u0410\u0411\u0412'/>");
    // ISO-8859-1, encoded
    rootElement.getModel().setCharset("ISO-8859-1");
    rootElement.setAttribute("name", rusValue);
    assertContext(
        "<!-- ==================================== -->",
        "<!-- ==================================== -->",
        "<root name='&#1040;&#1041;&#1042;'/>");
  }

  /**
   * Test for {@link DocumentElement#setAttribute(String, String)}.
   */
  public void test_attribute_edit() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root name='value'/>");
    // get attribute
    DocumentAttribute rootAttribute = rootElement.getDocumentAttribute("name");
    assertNotNull(rootAttribute);
    // initial state
    assertEquals("name", rootAttribute.getName());
    assertEquals("value", rootAttribute.getValue());
    // update attribute value
    assertSame(rootAttribute, rootElement.setAttribute("name", "newValue"));
    assertEquals("name", rootAttribute.getName());
    assertEquals("newValue", rootAttribute.getValue());
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root name='newValue'/>");
  }

  /**
   * Test for {@link DocumentAttribute#setValue(String)}.
   */
  public void test_attribute_setValue() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root name='value'/>");
    // get attribute
    DocumentAttribute rootAttribute = rootElement.getDocumentAttribute("name");
    assertNotNull(rootAttribute);
    // initial state
    assertEquals("name", rootAttribute.getName());
    assertEquals("value", rootAttribute.getValue());
    // update attribute value
    rootAttribute.setValue("newValue");
    assertEquals("name", rootAttribute.getName());
    assertEquals("newValue", rootAttribute.getValue());
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root name='newValue'/>");
  }

  /**
   * Test for {@link DocumentElement#removeDocumentAttribute(DocumentAttribute)}.
   */
  public void test_attribute_deleteAsObject() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root name='value'/>");
    // initial state
    List<DocumentAttribute> attributes = rootElement.getDocumentAttributes();
    assertThat(attributes).hasSize(1);
    // remove "name" attribute
    DocumentAttribute attribute = rootElement.getDocumentAttribute("name");
    assertNotNull(attribute);
    assertSame(attribute, attributes.get(0));
    rootElement.removeDocumentAttribute(attribute);
    // no attributes
    attributes = rootElement.getDocumentAttributes();
    assertThat(attributes).hasSize(0);
    assertNull(rootElement.getDocumentAttribute("name"));
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root/>");
  }

  /**
   * Test for using {@link DocumentElement#setAttribute(String, String)} to delete attribute.
   */
  public void test_attribute_deleteAsValue() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root name='value'/>");
    // remove "name" attribute
    rootElement.setAttribute("name", null);
    // no attributes
    List<DocumentAttribute> nodeAttributes = rootElement.getDocumentAttributes();
    assertThat(nodeAttributes).isEmpty();
    assertNull(rootElement.getDocumentAttribute("name"));
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root/>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Elements
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_element_add_whenClosed() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <parent/>",
        "</root>");
    // prepare "parent"
    DocumentElement parent = rootElement.getChildAt(0);
    assertEquals("parent", parent.getTag());
    assertTrue(parent.isClosed());
    // add "child"
    DocumentElement newChild = new DocumentElement();
    newChild.setTag("child");
    parent.addChild(newChild);
    // hierarchy
    assertSame(parent, newChild.getParent());
    assertThat(parent.getChildren()).contains(newChild);
    // state
    assertTrue(newChild.isClosed());
    assertFalse(parent.isClosed());
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <parent>",
        "    <child/>",
        "  </parent>",
        "</root>");
  }

  public void test_element_add_whenSecond() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <first/>",
        "</root>");
    // add "second"
    DocumentElement newChild = new DocumentElement("second");
    rootElement.addChild(newChild);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <first/>",
        "  <second/>",
        "</root>");
  }

  public void test_element_add_whenOnSameLine() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root> <parent></parent> </root>");
    DocumentElement parent = rootElement.getChildAt(0);
    // add "child"
    DocumentElement newChild = new DocumentElement();
    newChild.setTag("child");
    parent.addChild(newChild);
    m_getSource_ignoreSpaces = true;
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root> <parent>",
        " \t<child/></parent> </root>");
  }

  public void test_element_add_whenSingleLine_whenClosed() throws Exception {
    m_removeTrailingEOL = true;
    prepareContext("<root/>");
    // add "child"
    DocumentElement newChild = new DocumentElement();
    newChild.setTag("child");
    rootElement.addChild(newChild);
    assertContext("<root>\n\t<child/>\n</root>");
  }

  public void test_element_add_whenSingleLine_whenOpen() throws Exception {
    m_removeTrailingEOL = true;
    prepareContext("<root></root>");
    // add "child"
    DocumentElement newChild = new DocumentElement();
    newChild.setTag("child");
    rootElement.addChild(newChild);
    assertContext("<root><child/></root>");
  }

  public void test_element_removeFirst() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <first/>",
        "  <second/>",
        "</root>");
    // remove "first"
    DocumentElement first = rootElement.getChildAt(0);
    first.remove();
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <second/>",
        "</root>");
  }

  public void test_element_removeSecond() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <first/>",
        "  <second/>",
        "</root>");
    // remove "second"
    DocumentElement second = rootElement.getChildAt(1);
    second.remove();
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <first/>",
        "</root>");
  }

  /**
   * When remove last child, parent {@link DocumentElement} should be converted into closed.
   */
  public void test_element_removeLast() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <last/>",
        "</root>");
    // remove "last"
    DocumentElement second = rootElement.getChildAt(0);
    second.remove();
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root/>");
    assertTrue(rootElement.isClosed());
  }

  public void test_element_remove_parentHasText() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  text <first/>",
        "</root>");
    // remove "first"
    DocumentElement first = rootElement.getChildAt(0);
    first.remove();
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  text ",
        "</root>");
  }

  /**
   * Test for {@link DocumentElement#removeChildren()}.
   */
  public void test_element_removeChildren() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <first/>",
        "  <second/>",
        "</root>");
    // remove all children
    rootElement.removeChildren();
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root/>");
  }

  public void test_element_setTag() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <tag attrA='1'>",
        "    <inner attrB='2'/>",
        "  </tag>",
        "</root>");
    DocumentElement element = rootElement.getChildAt(0);
    assertEquals("tag", element.getTag());
    // set new tag name
    element.setTag("newTag");
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <newTag attrA='1'>",
        "    <inner attrB='2'/>",
        "  </newTag>",
        "</root>");
  }

  public void test_element_setTagLocal() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root xmlns:wbp='someValue'>",
        "  <first/>",
        "  <wbp:second/>",
        "</root>");
    // "first"
    {
      DocumentElement element = rootElement.getChildAt(0);
      element.setTagLocal("newFirst");
    }
    // "second"
    {
      DocumentElement element = rootElement.getChildAt(1);
      element.setTagLocal("newSecond");
    }
    // verify
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root xmlns:wbp='someValue'>",
        "  <newFirst/>",
        "  <wbp:newSecond/>",
        "</root>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Element move
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_element_move_newParent() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    <element/>",
        "  </source>",
        "  <target>",
        "  </target>",
        "</root>");
    DocumentElement source = rootElement.getChildAt(0);
    DocumentElement element = source.getChildAt(0);
    DocumentElement target = rootElement.getChildAt(1);
    // do move
    target.moveChild(element, 0);
    // hierarchy
    assertSame(target, element.getParent());
    assertThat(target.getChildren()).contains(element);
    assertThat(source.getChildren()).doesNotContain(element);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source/>",
        "  <target>",
        "    <element/>",
        "  </target>",
        "</root>");
  }

  /**
   * We should handle moving out of {@link DocumentElement} from parent with text.
   */
  public void test_element_move_newParent_oldHasText() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    Some text <element/>",
        "  </source>",
        "  <target>",
        "  </target>",
        "</root>");
    DocumentElement source = rootElement.getChildAt(0);
    DocumentElement element = source.getChildAt(0);
    DocumentElement target = rootElement.getChildAt(1);
    // do move
    target.moveChild(element, 0);
    // hierarchy
    assertSame(target, element.getParent());
    assertThat(target.getChildren()).contains(element);
    assertThat(source.getChildren()).doesNotContain(element);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    Some text ",
        "  </source>",
        "  <target>",
        "    <element/>",
        "  </target>",
        "</root>");
  }

  public void test_element_move_newParent_newIndentation() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    <element/>",
        "    <target>",
        "    </target>",
        "  </source>",
        "</root>");
    DocumentElement source = rootElement.getChildAt(0);
    DocumentElement element = source.getChildAt(0);
    DocumentElement target = source.getChildAt(1);
    // do move
    target.moveChild(element, 0);
    // hierarchy
    assertSame(target, element.getParent());
    assertThat(target.getChildren()).contains(element);
    assertThat(source.getChildren()).doesNotContain(element);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    <target>",
        "      <element/>",
        "    </target>",
        "  </source>",
        "</root>");
  }

  public void test_element_move_reorder_asFirst() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    <first/>",
        "    <element/>",
        "    <second/>",
        "  </source>",
        "</root>");
    DocumentElement source = rootElement.getChildAt(0);
    DocumentElement element = source.getChildAt(1);
    // do move
    source.moveChild(element, 0);
    // hierarchy
    assertSame(source, element.getParent());
    assertThat(source.getChildren()).contains(element);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    <element/>",
        "    <first/>",
        "    <second/>",
        "  </source>",
        "</root>");
  }

  public void test_element_move_reorder_asLast() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    <first/>",
        "    <element/>",
        "    <second/>",
        "  </source>",
        "</root>");
    DocumentElement source = rootElement.getChildAt(0);
    DocumentElement element = source.getChildAt(1);
    // do move
    source.moveChild(element, 3);
    // hierarchy
    assertSame(source, element.getParent());
    assertThat(source.getChildren()).contains(element);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    <first/>",
        "    <second/>",
        "    <element/>",
        "  </source>",
        "</root>");
  }

  /**
   * Use <code>-1</code> as target index to add as last.
   */
  public void test_element_move_reorder_asLast2() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    <first/>",
        "    <element/>",
        "    <second/>",
        "  </source>",
        "</root>");
    DocumentElement source = rootElement.getChildAt(0);
    DocumentElement element = source.getChildAt(1);
    // do move
    source.moveChild(element, -1);
    // hierarchy
    assertSame(source, element.getParent());
    assertThat(source.getChildren()).contains(element);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    <first/>",
        "    <second/>",
        "    <element/>",
        "  </source>",
        "</root>");
  }

  public void test_element_move_closedElement() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    <element attr='value'/>",
        "  </source>",
        "  <target>",
        "  </target>",
        "</root>");
    DocumentElement source = rootElement.getChildAt(0);
    DocumentElement element = source.getChildAt(0);
    DocumentElement target = rootElement.getChildAt(1);
    // do move
    target.moveChild(element, 0);
    // hierarchy
    assertSame(target, element.getParent());
    assertThat(target.getChildren()).contains(element);
    assertThat(source.getChildren()).doesNotContain(element);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source/>",
        "  <target>",
        "    <element attr='value'/>",
        "  </target>",
        "</root>");
  }

  public void test_element_move_closedTarget() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    <element attr='value'/>",
        "  </source>",
        "  <target/>",
        "</root>");
    DocumentElement source = rootElement.getChildAt(0);
    DocumentElement element = source.getChildAt(0);
    DocumentElement target = rootElement.getChildAt(1);
    // do move
    target.moveChild(element, 0);
    // hierarchy
    assertSame(target, element.getParent());
    assertThat(target.getChildren()).contains(element);
    assertThat(source.getChildren()).doesNotContain(element);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source/>",
        "  <target>",
        "    <element attr='value'/>",
        "  </target>",
        "</root>");
  }

  public void test_element_move_openWithText() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source>",
        "    <element attr='value'>Some text</element>",
        "  </source>",
        "  <target>",
        "  </target>",
        "</root>");
    DocumentElement source = rootElement.getChildAt(0);
    DocumentElement element = source.getChildAt(0);
    DocumentElement target = rootElement.getChildAt(1);
    // do move
    target.moveChild(element, 0);
    // hierarchy
    assertSame(target, element.getParent());
    assertThat(target.getChildren()).contains(element);
    assertThat(source.getChildren()).doesNotContain(element);
    assertContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root>",
        "  <source/>",
        "  <target>",
        "    <element attr='value'>Some text</element>",
        "  </target>",
        "</root>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DocumentElement#accept(DocumentModelVisitor)}.
   */
  public void test_visitor() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<!-- ==================================== -->",
        "<root name='value'>",
        "  <tag name2='222'/>",
        "</root>");
    assertNotNull(rootElement);
    // visit using default visitor
    rootElement.accept(new DocumentModelVisitor());
    //
    final List<AbstractDocumentObject> visitObjects = new ArrayList<AbstractDocumentObject>();
    rootElement.accept(new DocumentModelVisitor() {
      @Override
      public boolean visit(DocumentElement element) {
        visitObjects.add(element);
        return true;
      }

      @Override
      public void visit(DocumentAttribute attribute) {
        visitObjects.add(attribute);
      }

      @Override
      public void visit(DocumentTextNode node) {
        visitObjects.add(node);
      }

      @Override
      public void endVisit(DocumentElement element) {
        visitObjects.add(element);
      }
    });
    assertThat(visitObjects).hasSize(6);
    assertSame(rootElement, visitObjects.get(0)); // <root>
    assertSame(rootElement.getDocumentAttribute("name"), visitObjects.get(1)); // <root name=""> 
    assertSame(rootElement.getChildAt(0), visitObjects.get(2)); // <tag>
    assertSame(rootElement.getChildAt(0).getDocumentAttribute("name2"), visitObjects.get(3)); // <tag name2="">
    assertSame(rootElement.getChildAt(0), visitObjects.get(4)); // </tag>
    assertSame(rootElement, visitObjects.get(5)); // </root>
    // don't visit children, only <root>
    visitObjects.clear();
    rootElement.accept(new DocumentModelVisitor() {
      @Override
      public boolean visit(DocumentElement element) {
        visitObjects.add(element);
        return false;
      }

      @Override
      public void visit(DocumentAttribute attribute) {
        visitObjects.add(attribute);
      }

      @Override
      public void visit(DocumentTextNode node) {
        visitObjects.add(node);
      }

      @Override
      public void endVisit(DocumentElement element) {
        visitObjects.add(element);
      }
    });
    assertThat(visitObjects).containsExactly(rootElement);
  }

  /**
   * Test for {@link DocumentElement#getChildren(Class)}.
   */
  public void test_getElements() throws Exception {
    prepareContext(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<root>",
        "  <special name='A'/>",
        "  <someOther/>",
        "  <special name='B'/>",
        "</root>");
    //
    List<SpecialDocumentNode> specials = rootElement.getChildren(SpecialDocumentNode.class);
    assertThat(specials).hasSize(2);
    assertEquals("A", specials.get(0).getAttribute("name"));
    assertEquals("B", specials.get(1).getAttribute("name"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // toString()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DocumentElement#toString()}.
   */
  public void test_toString_rootWithoutChildren() throws Exception {
    String content = getDoubleQuotes2("<root/>");
    prepareContext(content);
    assertEquals(content, rootElement.toString());
  }

  /**
   * Test for {@link DocumentElement#toString()}.
   */
  public void test_toString_rootWithAttributes() throws Exception {
    String content = getDoubleQuotes2("<root name='value'/>");
    prepareContext(content);
    assertEquals(content, rootElement.toString());
  }

  /**
   * Test for {@link DocumentElement#toString()}.
   */
  public void test_toString_rootOnlyText() throws Exception {
    String content = getDoubleQuotes2("<root>aaa bbb</root>");
    prepareContext(content);
    assertEquals(content, rootElement.toString());
  }

  /**
   * Test for {@link DocumentElement#toString()}.
   */
  public void test_toString_twoLevels() throws Exception {
    String content =
        getDoubleQuotes2(
            "<root>",
            "  <firstLevelElement>",
            "    <secondLevelElement/>",
            "  </firstLevelElement>",
            "</root>");
    prepareContext(content);
    assertEquals(content, rootElement.toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // writeShort()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DocumentElement#writeShort(PrintWriter)}.
   */
  public void test_writeShort_rootWithAttributes() throws Exception {
    prepareContext("<root name='value'/>");
    assertEquals(getSourceDQ("<root name='value'>"), writeShort(rootElement));
  }

  /**
   * Test for {@link DocumentElement#writeShort(PrintWriter)}.
   */
  public void test_writeShort_twoLevels() throws Exception {
    prepareContext(
        "<root>",
        "  <firstLevelElement>",
        "    <secondLevelElement/>",
        "  </firstLevelElement>",
        "</root>");
    assertEquals("<root>", writeShort(rootElement));
  }

  private static String writeShort(DocumentElement element) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    element.writeShort(writer);
    writer.close();
    return stringWriter.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // File
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_fileContext() throws Exception {
    String filePath = "test/1.xml";
    // parse
    IFile file = setFileContentSrc(filePath, "<root/>");
    file.setCharset("UTF-8", null);
    AbstractDocumentEditContext editContext = new FileDocumentEditContext(file) {
      @Override
      protected AbstractDocumentHandler createDocumentHandler() {
        return new TestDocumentHandler();
      }
    };
    rootElement = editContext.getRoot();
    // model has charset
    assertEquals("UTF-8", rootElement.getModel().getCharset());
    // life cycle
    try {
      // modify
      rootElement.setTag("newTag");
      // not changed yet
      assertEquals("<root/>", getFileContentSrc(filePath));
      // commit
      editContext.commit();
      assertEquals("<newTag/>", getFileContentSrc(filePath));
    } finally {
      editContext.disconnect();
    }
  }
}