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
package org.eclipse.wb.tests.css;

import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentObject;
import org.eclipse.wb.internal.css.model.CssDeclarationNode;
import org.eclipse.wb.internal.css.model.CssDocument;
import org.eclipse.wb.internal.css.model.CssErrorNode;
import org.eclipse.wb.internal.css.model.CssFactory;
import org.eclipse.wb.internal.css.model.CssNode;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.model.CssVisitor;
import org.eclipse.wb.internal.css.model.at.CssCharsetNode;
import org.eclipse.wb.internal.css.model.punctuation.CssColonNode;
import org.eclipse.wb.internal.css.model.punctuation.CssCurlyBraceNode;
import org.eclipse.wb.internal.css.model.punctuation.CssSemiColonNode;
import org.eclipse.wb.internal.css.model.root.Model;
import org.eclipse.wb.internal.css.model.string.CssPropertyNode;
import org.eclipse.wb.internal.css.model.string.CssSelectorNode;
import org.eclipse.wb.internal.css.model.string.CssStringNode;
import org.eclipse.wb.internal.css.model.string.CssValueNode;
import org.eclipse.wb.internal.css.parser.CssEditContext;
import org.eclipse.wb.internal.css.parser.CssParser;
import org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;

/**
 * Test for {@link CssParser} and {@link CssEditContext}.
 * 
 * @author scheglov_ke
 */
public class CssDocumentTest extends AbstractJavaProjectTest {
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

  // XXX
  ////////////////////////////////////////////////////////////////////////////
  //
  // CSSDocument
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for "@charset" parsing into {@link CssCharsetNode}.
   */
  public void test_CSSCharsetNode_parse() throws Exception {
    prepareContext(
        "@charset 'windows-1251';",
        "a {",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    // charset
    CssCharsetNode charset = rootElement.getCharset();
    assertNotNull(charset);
    assertSame(rootElement, charset.getParent());
    assertNotNull(charset.getSemiColon());
    // charset value
    CssStringNode stringNode = charset.getString();
    assertEquals("\"windows-1251\"", stringNode.getValue());
  }

  /**
   * Test for {@link CssDocument#setCharset(CssCharsetNode)}.
   */
  public void test_CSSCharsetNode_remove() throws Exception {
    prepareContext(
        "@charset 'windows-1251';",
        "a {",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    // remove
    rootElement.setCharset(null);
    assertSame(null, rootElement.getCharset());
    assertContext(
        "a {",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
  }

  /**
   * Test for {@link CssDocument#setCharset(CssCharsetNode)}.
   */
  public void test_CSSCharsetNode_add() throws Exception {
    prepareContext(
        "a {",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    // add
    CssCharsetNode newCharset = CssFactory.newCharset("Latin");
    rootElement.setCharset(newCharset);
    assertSame(newCharset, rootElement.getCharset());
    assertContext(
        "@charset 'Latin';",
        "a {",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CSSRuleNode
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Try to call method which causes exception from {@link CssRuleNode}.
   */
  public void test_CSSRuleNode_causeException() throws Exception {
    prepareContext("abc {}");
    // setLeftBrace() should be called only during parsing
    try {
      firstRule.setLeftBrace(null);
      fail();
    } catch (IllegalStateException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for basic parsing.
   */
  public void test_parse() throws Exception {
    prepareContext(
        "a {",
        "  nameA: 1;",
        "  nameB: 22;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    // selector
    assertEquals("a", firstRule.getSelector().getValue());
    // curly braces
    assertNotNull(firstRule.getLeftBrace());
    assertNotNull(firstRule.getRightBrace());
    // declarations
    List<CssDeclarationNode> declarations = firstRule.getDeclarations();
    assertThat(declarations).hasSize(2);
    {
      CssDeclarationNode declaration = declarations.get(0);
      assertSame(declaration, firstRule.getDeclaration(0));
      assertEquals(0, firstRule.getIndex(declaration));
      // property
      CssPropertyNode property = declaration.getProperty();
      assertEquals("nameA", property.getValue());
      // value
      CssValueNode value = declaration.getValue();
      assertEquals("1", value.getValue());
    }
    {
      CssDeclarationNode declaration = declarations.get(1);
      assertSame(declaration, firstRule.getDeclaration(1));
      assertEquals(1, firstRule.getIndex(declaration));
      // property
      CssPropertyNode property = declaration.getProperty();
      assertEquals("nameB", property.getValue());
      // value
      CssValueNode value = declaration.getValue();
      assertEquals("22", value.getValue());
    }
    // just empty visitor
    rootElement.accept(new CssVisitor());
  }

  /**
   * Test for basic parsing.
   */
  public void test_parse_classSelector() throws Exception {
    prepareContext(
        ".myClass {",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    // selector
    assertEquals(".myClass", firstRule.getSelector().getValue());
    // no errors
    assertThat(errors).isEmpty();
  }

  /**
   * Value with several words.
   */
  public void test_parse_longValue() throws Exception {
    prepareContext(
        "a {",
        "  name: 0 11 222;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    //
    List<CssDeclarationNode> declarations = firstRule.getDeclarations();
    assertThat(declarations).hasSize(1);
    assertEquals("0 11 222", declarations.get(0).getValue().getValue());
  }

  public void test_badToken_noLeftCurlyBrace_beforeEOF() throws Exception {
    prepareContext("a foo");
    // no rules
    assertThat(rules).isEmpty();
    // has error
    assertThat(errors).hasSize(1);
    {
      CssErrorNode error = errors.get(0);
      assertEquals(0, error.getOffset());
      assertEquals(5, error.getLength());
      assertEquals("eof", error.getMessage());
    }
  }

  public void test_badToken_noRightCurlyBrace_beforeEOF() throws Exception {
    prepareContext("a {");
    // no rules
    assertThat(rules).isEmpty();
    // has error
    assertThat(errors).hasSize(1);
    {
      CssErrorNode error = errors.get(0);
      assertEquals(0, error.getOffset());
      assertEquals(3, error.getLength());
      assertEquals("eof", error.getMessage());
    }
  }

  /**
   * Skip to then end of current rule, i.e. to "}" character.
   */
  public void test_badToken_badCharacterInSelector() throws Exception {
    prepareContext("a< } b {}");
    // has single rule
    {
      assertThat(rules).hasSize(1);
      assertEquals("b", firstRule.getSelector().getValue());
    }
    // has error
    assertThat(errors).hasSize(1);
    {
      CssErrorNode error = errors.get(0);
      assertEquals(0, error.getOffset());
      assertEquals(5, error.getLength());
      assertEquals("character", error.getMessage());
    }
  }

  /**
   * We should ignore bad tokens.
   */
  public void test_badToken_atBeginning() throws Exception {
    prepareContext(
        "$PP_OFF",
        "b {}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    // has 1 rule
    {
      assertThat(rules).hasSize(1);
      CssRuleNode rule = rules.get(0);
      assertEquals("b", rule.getSelector().getValue());
    }
    // has error
    assertThat(errors).hasSize(1);
    {
      CssErrorNode error = errors.get(0);
      assertEquals(0, error.getOffset());
      assertEquals(8, error.getLength());
      assertEquals("identifier.character", error.getMessage());
    }
  }

  /**
   * We should ignore bad tokens.
   */
  public void test_badToken_inDeclaration_noColon() throws Exception {
    prepareContext(
        "a {",
        "  name_1: value_1;",
        "  something very bad;",
        "  name_2: value_2;",
        "}",
        "/* filler filler filler filler */",
        "/* filler filler filler filler */");
    // selector
    assertEquals("a", firstRule.getSelector().getValue());
    // declarations
    {
      List<CssDeclarationNode> declarations = firstRule.getDeclarations();
      assertThat(declarations).hasSize(2);
      {
        CssDeclarationNode declaration = declarations.get(0);
        assertEquals("name_1", declaration.getProperty().getValue());
        assertEquals("value_1", declaration.getValue().getValue());
      }
      {
        CssDeclarationNode declaration = declarations.get(1);
        assertEquals("name_2", declaration.getProperty().getValue());
        assertEquals("value_2", declaration.getValue().getValue());
      }
    }
    // check errors
    assertThat(errors).hasSize(1);
    {
      CssErrorNode error = errors.get(0);
      assertEquals(23, error.getOffset());
      assertEquals(21, error.getLength());
      assertEquals("colon", error.getMessage());
    }
  }

  /**
   * Identifier expected as beginning of declaration (property name).
   */
  public void test_badToken_inDeclaration_notIdentifier_1() throws Exception {
    prepareContext(
        "a {",
        "  name_1: value_1;",
        "  : no matter;",
        "  name_2: value_2;",
        "}",
        "/* filler filler filler filler */",
        "/* filler filler filler filler */");
    // selector
    assertEquals("a", firstRule.getSelector().getValue());
    // declarations
    {
      List<CssDeclarationNode> declarations = firstRule.getDeclarations();
      assertThat(declarations).hasSize(2);
      {
        CssDeclarationNode declaration = declarations.get(0);
        assertEquals("name_1", declaration.getProperty().getValue());
        assertEquals("value_1", declaration.getValue().getValue());
      }
      {
        CssDeclarationNode declaration = declarations.get(1);
        assertEquals("name_2", declaration.getProperty().getValue());
        assertEquals("value_2", declaration.getValue().getValue());
      }
    }
    // check errors
    assertThat(errors).hasSize(1);
    {
      CssErrorNode error = errors.get(0);
      assertEquals(23, error.getOffset());
      assertEquals(14, error.getLength());
      assertEquals("identifier", error.getMessage());
    }
  }

  /**
   * Identifier expected as beginning of declaration (property name).
   */
  public void test_badToken_inDeclaration_notIdentifier_2() throws Exception {
    prepareContext(
        "a {",
        "  name_1: value_1;",
        "  : no matter",
        "}",
        "b {",
        "  name_2: value_2;",
        "}",
        "/* filler filler filler filler */",
        "/* filler filler filler filler */");
    // two rules
    {
      assertThat(rules).hasSize(2);
      {
        CssRuleNode rule = rules.get(0);
        assertEquals("a", rule.getSelector().getValue());
        {
          List<CssDeclarationNode> declarations = rule.getDeclarations();
          assertThat(declarations).hasSize(1);
          assertEquals("name_1", declarations.get(0).getProperty().getValue());
        }
      }
      {
        CssRuleNode rule = rules.get(1);
        assertEquals("b", rule.getSelector().getValue());
        {
          List<CssDeclarationNode> declarations = rule.getDeclarations();
          assertThat(declarations).hasSize(1);
          assertEquals("name_2", declarations.get(0).getProperty().getValue());
        }
      }
    }
    // has error
    assertThat(errors).hasSize(1);
    {
      CssErrorNode error = errors.get(0);
      assertEquals(23, error.getOffset());
      assertEquals(12, error.getLength());
      assertEquals("identifier", error.getMessage());
    }
  }

  /**
   * Expected ":" after property name, but EOF found.
   */
  public void test_badToken_inDeclaration_unexpectedEOF_noColon() throws Exception {
    prepareContext("a { property");
    // no successfully parsed rules
    assertThat(rules).isEmpty();
    // check errors
    {
      assertThat(errors).hasSize(2);
      // for declaration
      {
        CssErrorNode error = errors.get(0);
        assertEquals(4, error.getOffset());
        assertEquals(8, error.getLength());
        assertEquals("colon", error.getMessage());
      }
      // for rule
      {
        CssErrorNode error = errors.get(1);
        assertEquals(0, error.getOffset());
        assertEquals(12, error.getLength());
        assertEquals("eof", error.getMessage());
      }
    }
  }

  /**
   * Unexpected EOF during value reading.
   */
  public void test_badToken_inDeclaration_unexpectedEOF_inValue() throws Exception {
    prepareContext("a { property: value");
    // no successfully parsed rules
    assertThat(rules).isEmpty();
    // check errors
    {
      assertThat(errors).hasSize(2);
      // for declaration
      {
        CssErrorNode error = errors.get(0);
        assertEquals(4, error.getOffset());
        assertEquals(15, error.getLength());
        assertEquals("eof", error.getMessage());
      }
      // for rule
      {
        CssErrorNode error = errors.get(1);
        assertEquals(0, error.getOffset());
        assertEquals(19, error.getLength());
        assertEquals("eof", error.getMessage());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CSSRuleNode#remove()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CssRuleNode#remove()}.
   */
  public void test_updateValue() throws Exception {
    prepareContext(
        "oldName {",
        "  nameA: 1;",
        "  nameB: 22;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    //
    firstRule.getSelector().setValue("myNewSelector");
    assertContext(
        "myNewSelector {",
        "  nameA: 1;",
        "  nameB: 22;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CSSRuleNode#remove()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CssRuleNode#remove()}.
   */
  public void test_removeRule_first() throws Exception {
    prepareContext(
        "a {",
        "  nameA: 1;",
        "}",
        "b {",
        "  nameB: 2;",
        "}",
        "c {",
        "  nameC: 3;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    //
    CssRuleNode rule = rules.get(0);
    rule.remove();
    assertContext(
        "b {",
        "  nameB: 2;",
        "}",
        "c {",
        "  nameC: 3;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
  }

  /**
   * Test for {@link CssRuleNode#remove()}.
   */
  public void test_removeRule_middle() throws Exception {
    prepareContext(
        "a {",
        "  nameA: 1;",
        "}",
        "b {",
        "  nameB: 2;",
        "}",
        "c {",
        "  nameC: 3;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    //
    CssRuleNode rule = rules.get(1);
    rule.remove();
    assertContext(
        "a {",
        "  nameA: 1;",
        "}",
        "c {",
        "  nameC: 3;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
  }

  /**
   * Test for {@link CssRuleNode#remove()}.
   */
  public void test_removeRule_last() throws Exception {
    prepareContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "}",
        "b {",
        "  nameB: 2;",
        "}",
        "c {",
        "  nameC: 3;",
        "}");
    //
    CssRuleNode rule = rules.get(2);
    rule.remove();
    assertContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "}",
        "b {",
        "  nameB: 2;",
        "}");
  }

  /**
   * Test for {@link CssRuleNode#remove()}.
   */
  public void test_removeRule_withSpaces() throws Exception {
    prepareContext(
        "a {",
        "  nameA: 1;",
        "} \t",
        "",
        "",
        "b {",
        "  nameB: 2;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    //
    CssRuleNode rule = rules.get(0);
    rule.remove();
    assertContext(
        "b {",
        "  nameB: 2;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CSSDocument#addRule()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CssDocument#addRule(CssRuleNode)}.
   */
  public void test_addRule_last() throws Exception {
    prepareContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "}");
    //
    rootElement.addRule(CssFactory.newRule("newRule"));
    assertContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "}",
        "newRule {",
        "}");
  }

  /**
   * Test for {@link CssDocument#addRule(CssRuleNode)}.
   */
  public void test_addRule_first_noComment() throws Exception {
    prepareContext(
        "a {",
        "  nameA: 1;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    //
    rootElement.addRule(0, CssFactory.newRule("newRule"));
    assertContext(
        "newRule {",
        "}",
        "a {",
        "  nameA: 1;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
  }

  /**
   * Test for {@link CssDocument#addRule(CssRuleNode)}.
   */
  public void test_addRule_first_withComment() throws Exception {
    prepareContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "}");
    //
    rootElement.addRule(0, CssFactory.newRule("newRule"));
    assertContext(
        "newRule {",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "}");
  }

  /**
   * Test for {@link CssDocument#addRule(CssRuleNode)}.
   */
  public void test_addRule_first_only() throws Exception {
    prepareContext();
    //
    rootElement.addRule(0, CssFactory.newRule("newRule"));
    assertContext("newRule {", "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CSSDeclarationNode#remove()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CssDeclarationNode#remove()}.
   */
  public void test_removeDeclaration_first() throws Exception {
    prepareContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "  nameB: 22;",
        "  nameC: 333;",
        "}");
    //
    CssDeclarationNode declaration = firstRule.getDeclaration(0);
    declaration.remove();
    assertContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameB: 22;",
        "  nameC: 333;",
        "}");
  }

  /**
   * Test for {@link CssDeclarationNode#remove()}.
   */
  public void test_removeDeclaration_middle() throws Exception {
    prepareContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "  nameB: 22;",
        "  nameC: 333;",
        "}");
    //
    CssDeclarationNode declaration = firstRule.getDeclaration(1);
    declaration.remove();
    assertContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "  nameC: 333;",
        "}");
  }

  /**
   * Test for {@link CssDeclarationNode#remove()}.
   */
  public void test_removeDeclaration_last() throws Exception {
    prepareContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "  nameB: 22;",
        "  nameC: 333;",
        "}");
    //
    CssDeclarationNode declaration = firstRule.getDeclaration(2);
    declaration.remove();
    assertContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "  nameB: 22;",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CSSRuleNode#addDeclaration(CSSDeclarationNode)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CssRuleNode#addDeclaration(CssDeclarationNode)}.
   */
  public void test_addDeclaration_first() throws Exception {
    prepareContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "  nameB: 22;",
        "}");
    //
    CssDeclarationNode newDeclaration = CssFactory.newDeclaration("newProperty", "newValue");
    firstRule.addDeclaration(0, newDeclaration);
    assertContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  newProperty: newValue;",
        "  nameA: 1;",
        "  nameB: 22;",
        "}");
  }

  /**
   * Test for {@link CssRuleNode#addDeclaration(CssDeclarationNode)}.
   */
  public void test_addDeclaration_last() throws Exception {
    prepareContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "  nameB: 22;",
        "}");
    //
    CssDeclarationNode newDeclaration = CssFactory.newDeclaration("newProperty", "newValue");
    firstRule.addDeclaration(newDeclaration);
    assertContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "  nameB: 22;",
        "  newProperty: newValue;",
        "}");
  }

  /**
   * Test for {@link CssRuleNode#addDeclaration(CssDeclarationNode)}.
   */
  public void test_addDeclaration_last_noSemicolon() throws Exception {
    prepareContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "  nameB: 22",
        "}");
    //
    CssDeclarationNode newDeclaration = CssFactory.newDeclaration("newProperty", "newValue");
    firstRule.addDeclaration(newDeclaration);
    assertContext(
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "a {",
        "  nameA: 1;",
        "  nameB: 22;",
        "  newProperty: newValue;",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CSSEditContext
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CssEditContext#getText(int, int)}.
   */
  public void test_CSSEditContext_getText() throws Exception {
    prepareContext("abc {}");
    // success
    assertEquals("abc", context.getText(0, 3));
    assertEquals("bc", context.getText(1, 2));
    // exception
    try {
      context.getText(-1, 0);
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  /**
   * Test for {@link CssEditContext#replaceRule(CssRuleNode, String)}.
   */
  public void test_CSSEditContext_replaceRule() throws Exception {
    prepareContext(
        "a {",
        "  nameA: 1;",
        "  nameB: 2;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    //
    context.replaceRule(
        firstRule,
        getSource(
            "newSelector {",
            "  property_1: value_1;",
            "  property_2: value_2;",
            "  property_3: value_3;",
            "}").trim());
    assertContext(
        "newSelector {",
        "  property_1: value_1;",
        "  property_2: value_2;",
        "  property_3: value_3;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
  }

  /**
   * Test for {@link CssEditContext#replaceRule(CssRuleNode, String)}.
   */
  public void test_CSSEditContext_replaceRule_whenMoreThenOneRule() throws Exception {
    prepareContext(
        "a {",
        "  nameA: 1;",
        "  nameB: 2;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    //
    try {
      context.replaceRule(
          firstRule,
          getSource(
              "firstSelector {",
              "  property_1: value_1;",
              "  property_2: value_2;",
              "  property_3: value_3;",
              "}",
              "secondSelector {",
              "  property_1: value_1;",
              "  property_2: value_2;",
              "  property_3: value_3;",
              "}").trim());
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).contains("one rule");
    }
  }

  /**
   * Test for {@link CssEditContext#sortRules(Comparator)}.
   */
  public void test_CSSEditContext_sortRules() throws Exception {
    prepareContext(
        "c {",
        "  nameCa: 3a;",
        "  nameCb: 3b;",
        "  nameCc: 3c;",
        "}",
        "b {",
        "  nameBa: 2a;",
        "  nameBb: 2b;",
        "  nameBc: 2c;",
        "}",
        "a {",
        "  nameA: 1;",
        "}",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */",
        "/* filler filler filler filler filler */");
    //
    context.sortRules(new Comparator<CssRuleNode>() {
      public int compare(CssRuleNode o1, CssRuleNode o2) {
        String selector_1 = o1.getSelector().getValue();
        String selector_2 = o2.getSelector().getValue();
        return selector_1.compareTo(selector_2);
      }
    });
    assertContext(
        "a {",
        "  nameA: 1;",
        "}",
        "",
        "b {",
        "  nameBa: 2a;",
        "  nameBb: 2b;",
        "  nameBc: 2c;",
        "}",
        "",
        "c {",
        "  nameCa: 3a;",
        "  nameCb: 3b;",
        "  nameCc: 3c;",
        "}");
  }

  /**
   * Test for {@link CssEditContext#CSSEditContext(IFile)}.
   */
  public void test_CSSEditContext_forIFile() throws Exception {
    String content = "abc {}";
    IFile file = setFileContent("test/Test.css", content);
    context = new CssEditContext(file);
    firstRule = context.getCssDocument().getRule(0);
    // get text
    assertEquals(content, context.getText());
    // do change
    firstRule.getSelector().setValue("newSelector");
    // file is not changed yet
    assertEquals(content, getFileContent(file));
    // do commit, file changed
    context.commit();
    assertEquals("newSelector {}", getFileContent(file));
  }

  /**
   * Test for edit operation which causes exception.
   */
  public void test_CSSEditContext_causeException() throws Exception {
    prepareContext("abc {}");
    // use "null", so cause NullPointerException
    try {
      firstRule.getSelector().setValue(null);
    } catch (NullPointerException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private CssEditContext context;
  private CssDocument rootElement;
  private List<CssRuleNode> rules;
  private List<CssErrorNode> errors;
  private CssRuleNode firstRule;

  /**
   * Parses given lines of XML and prepares {@link #context} and {@link #rootElement}.
   */
  private void prepareContext(String... lines) throws Exception {
    String source = getXMLSource(lines);
    Document document = new Document(source);
    context = new CssEditContext(document);
    // just touch
    assertSame(document, context.getDocument());
    // assign model elements
    rootElement = context.getCssDocument();
    rules = rootElement.getRules();
    errors = rootElement.getErrors();
    // validate initial state
    assertOffsets();
    // if possible get first rule
    if (!rules.isEmpty()) {
      firstRule = rootElement.getRule(0);
    }
  }

  /**
   * @return the XML source for given lines.
   */
  private String getXMLSource(String... lines) {
    return getSourceDQ(lines);
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
    // has model
    final Model model = rootElement.getModel();
    assertNotNull(model);
    // check nodes
    rootElement.accept(new CssVisitor() {
      @Override
      public void postVisit(CssNode node) {
        // all nodes have same model
        assertSame(model, node.getModel());
      }

      @Override
      public void endVisit(CssCharsetNode node) {
        int offset = node.getOffset();
        int length = node.getLength();
        String text = context.getText(offset, length);
        assertThat(text).startsWith("@charset");
        assertThat(text).endsWith(";");
      }

      @Override
      public void endVisit(CssStringNode node) {
        int offset = node.getOffset();
        int length = node.getLength();
        String text = context.getText(offset, length);
        String value = node.getValue();
        assertThat(text).isEqualTo(value);
        assertThat(text).startsWith("\"").endsWith("\"");
      }

      @Override
      public void endVisit(CssRuleNode node) {
        int offset = node.getOffset();
        int length = node.getLength();
        String text = context.getText(offset, length);
        assertThat(text).contains("{");
        assertThat(text).endsWith("}");
      }

      @Override
      public void endVisit(CssSelectorNode node) {
        int offset = node.getOffset();
        int length = node.getLength();
        String text = context.getText(offset, length);
        String value = node.getValue();
        assertThat(text).isEqualTo(value);
      }

      @Override
      public void endVisit(CssCurlyBraceNode node) {
        int offset = node.getOffset();
        int length = node.getLength();
        String text = context.getText(offset, length);
        if (node.isLeft()) {
          assertThat(text).isEqualTo("{");
        }
        if (node.isRight()) {
          assertThat(text).isEqualTo("}");
        }
      }

      @Override
      public void endVisit(CssPropertyNode node) {
        int offset = node.getOffset();
        int length = node.getLength();
        String text = context.getText(offset, length);
        String name = node.getValue();
        assertThat(text).isEqualTo(name);
      }

      @Override
      public void endVisit(CssColonNode node) {
        int offset = node.getOffset();
        int length = node.getLength();
        String text = context.getText(offset, length);
        assertThat(text).isEqualTo(":");
      }

      @Override
      public void endVisit(CssValueNode node) {
        int offset = node.getOffset();
        int length = node.getLength();
        String text = context.getText(offset, length);
        String value = node.getValue();
        assertThat(text).isEqualTo(value);
      }

      @Override
      public void endVisit(CssSemiColonNode node) {
        int offset = node.getOffset();
        int length = node.getLength();
        String text = context.getText(offset, length);
        assertThat(text).isEqualTo(";");
      }
    });
  }
}
