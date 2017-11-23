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
package org.eclipse.wb.tests.designer.core.nls;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.bundle.pure.direct.DirectSource;
import org.eclipse.wb.internal.core.nls.bundle.pure.field.FieldSource;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.BorderLayout;

/**
 * Tests for special methods of {@link AbstractSource} for support {@link StringPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class SourceAbstractSpecialTest extends AbstractNlsTest {
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
  // Errors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link BorderLayout#NORTH} has {@link String} type, but we don't expect that
   * {@link NLSStringEvaluator} will return any value here, so no parse error expected.
   */
  public void test_parseErrors() throws Exception {
    parseContainer(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    getContentPane().add(new JButton(), BorderLayout.NORTH);",
        "  }",
        "}");
    assertEquals(0, m_lastState.getBadParserNodes().nodes().size());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replace_toStringLiteral
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_replace_toStringLiteral() throws Exception {
    setFileContentSrc(
        "test/messages.properties",
        getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "import java.util.ResourceBundle;",
            "public class Test extends JFrame {",
            "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
            "  public Test() {",
            "    setTitle(m_bundle.getString('frame.title')); //$NON-NLS-1$",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    // prepare FieldSource
    FieldSource fieldSource;
    {
      AbstractSource[] sources = support.getSources();
      assertEquals(1, sources.length);
      fieldSource = (FieldSource) sources[0];
    }
    // replace with StringLiteral
    GenericProperty titleProperty = (GenericProperty) frame.getPropertyByTitle("title");
    fieldSource.replace_toStringLiteral(titleProperty, "my title");
    // check source code
    assertEditor(
        "import java.util.ResourceBundle;",
        "public class Test extends JFrame {",
        "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
        "  public Test() {",
        "    setTitle('my title');",
        "  }",
        "}");
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertTrue(newProperties.contains("frame.title=My JFrame"));
      assertTrue(newProperties.contains("frame.name=My name"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replace_externalizedSourceKey
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_replace_externalizedSourceKey_1() throws Exception {
    String properties = getSource("frame.title=My JFrame", "frame.name=My name");
    setFileContentSrc("test/messages.properties", properties);
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "import java.util.ResourceBundle;",
            "public class Test extends JFrame {",
            "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
            "  public Test() {",
            "    setTitle(ResourceBundle.getBundle('test.messages').getString('frame.title')); //$NON-NLS-1$ //$NON-NLS-2$",
            "    setName(m_bundle.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    // check that we have DirectSource and FieldSource
    DirectSource directSource;
    {
      AbstractSource[] sources = support.getSources();
      assertEquals(2, sources.length);
      directSource = (DirectSource) sources[1];
      assertInstanceOf(FieldSource.class, sources[0]);
    }
    // do replace source/key
    GenericProperty titleProperty = (GenericProperty) frame.getPropertyByTitle("title");
    directSource.useKey(titleProperty, "frame.name");
    // check source code
    assertEditor(
        "import java.util.ResourceBundle;",
        "public class Test extends JFrame {",
        "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
        "  public Test() {",
        "    setTitle(ResourceBundle.getBundle('test.messages').getString('frame.name')); //$NON-NLS-1$ //$NON-NLS-2$",
        "    setName(m_bundle.getString('frame.name')); //$NON-NLS-1$",
        "  }",
        "}");
    assertEquals(properties, getFileContentSrc("test/messages.properties"));
  }

  public void test_replace_externalizedSourceKey_2() throws Exception {
    String properties = getSource("frame.title=My JFrame", "frame.name=My name");
    setFileContentSrc("test/messages.properties", properties);
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "import java.util.ResourceBundle;",
            "public class Test extends JFrame {",
            "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
            "  public Test() {",
            "    setTitle(ResourceBundle.getBundle('test.messages').getString('frame.title')); //$NON-NLS-1$ //$NON-NLS-2$",
            "    setName(m_bundle.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    // check that we have DirectSource and FieldSource
    FieldSource fieldSource;
    {
      AbstractSource[] sources = support.getSources();
      assertEquals(2, sources.length);
      assertInstanceOf(DirectSource.class, sources[1]);
      fieldSource = (FieldSource) sources[0];
    }
    // do replace source/key
    GenericProperty titleProperty = (GenericProperty) frame.getPropertyByTitle("title");
    fieldSource.useKey(titleProperty, "frame.name");
    // check source code
    assertEditor(
        "import java.util.ResourceBundle;",
        "public class Test extends JFrame {",
        "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
        "  public Test() {",
        "    setTitle(m_bundle.getString('frame.name')); //$NON-NLS-1$",
        "    setName(m_bundle.getString('frame.name')); //$NON-NLS-1$",
        "  }",
        "}");
    assertEquals(properties, getFileContentSrc("test/messages.properties"));
  }

  /**
   * Replace {@link StringLiteral}.
   */
  public void test_replace_externalizedSourceKey_3() throws Exception {
    String properties = getSource("frame.title=My JFrame", "frame.name=My name");
    setFileContentSrc("test/messages.properties", properties);
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "import java.util.ResourceBundle;",
            "public class Test extends JFrame {",
            "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
            "  public Test() {",
            "    setTitle('Some title');",
            "    setName(m_bundle.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    // check that we have FieldSource
    FieldSource fieldSource;
    {
      AbstractSource[] sources = support.getSources();
      assertEquals(1, sources.length);
      fieldSource = (FieldSource) sources[0];
    }
    // do replace source/key
    GenericProperty titleProperty = (GenericProperty) frame.getPropertyByTitle("title");
    fieldSource.useKey(titleProperty, "frame.name");
    // check source code
    assertEditor(
        "import java.util.ResourceBundle;",
        "public class Test extends JFrame {",
        "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
        "  public Test() {",
        "    setTitle(m_bundle.getString('frame.name')); //$NON-NLS-1$",
        "    setName(m_bundle.getString('frame.name')); //$NON-NLS-1$",
        "  }",
        "}");
    assertEquals(properties, getFileContentSrc("test/messages.properties"));
  }

  /**
   * Replace when {@link GenericProperty} is not modified, i.e. no {@link Expression}.
   */
  public void test_replace_externalizedSourceKey_4() throws Exception {
    String properties = getSource("frame.title=My JFrame", "frame.name=My name");
    setFileContentSrc("test/messages.properties", properties);
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "import java.util.ResourceBundle;",
            "public class Test extends JFrame {",
            "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
            "  public Test() {",
            "    setName(m_bundle.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    // check that we have FieldSource
    FieldSource fieldSource;
    {
      AbstractSource[] sources = support.getSources();
      assertEquals(1, sources.length);
      fieldSource = (FieldSource) sources[0];
    }
    // do replace source/key
    GenericProperty titleProperty = (GenericProperty) frame.getPropertyByTitle("title");
    fieldSource.useKey(titleProperty, "frame.name");
    // check source code
    assertEditor(
        "import java.util.ResourceBundle;",
        "public class Test extends JFrame {",
        "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
        "  public Test() {",
        "    setTitle(m_bundle.getString('frame.name')); //$NON-NLS-1$",
        "    setName(m_bundle.getString('frame.name')); //$NON-NLS-1$",
        "  }",
        "}");
    assertEquals(properties, getFileContentSrc("test/messages.properties"));
  }

  /**
   * Different key in same {@link AbstractSource}.
   */
  public void test_replace_externalizedSourceKey_5() throws Exception {
    String properties = getSource("frame.title=My JFrame", "frame.name=My name");
    setFileContentSrc("test/messages.properties", properties);
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "import java.util.ResourceBundle;",
            "public class Test extends JFrame {",
            "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
            "  public Test() {",
            "    setTitle(m_bundle.getString('frame.title')); //$NON-NLS-1$",
            "    setName(m_bundle.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    // check that we have DirectSource and FieldSource
    FieldSource fieldSource;
    {
      AbstractSource[] sources = support.getSources();
      assertEquals(1, sources.length);
      fieldSource = (FieldSource) sources[0];
    }
    // do replace source/key
    GenericProperty titleProperty = (GenericProperty) frame.getPropertyByTitle("title");
    fieldSource.useKey(titleProperty, "frame.name");
    // check source code
    assertEditor(
        "import java.util.ResourceBundle;",
        "public class Test extends JFrame {",
        "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
        "  public Test() {",
        "    setTitle(m_bundle.getString('frame.name')); //$NON-NLS-1$",
        "    setName(m_bundle.getString('frame.name')); //$NON-NLS-1$",
        "  }",
        "}");
    assertEquals(properties, getFileContentSrc("test/messages.properties"));
  }

  /**
   * Test for {@link AbstractSource#replace_externalizedSourceKey(GenericProperty, String, String)}.
   * We should not add new keys into source.
   */
  public void test_replace_externalizedSourceKey_6() throws Exception {
    m_testProject.addPlugin("org.eclipse.osgi");
    NlsTestUtils.create_EclipseModern_AccessorAndProperties();
    String accessor = getFileContentSrc("test/Messages.java");
    String properties = getFileContentSrc("test/messages.properties");
    String[] lines1 =
        {
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle(Messages.frame_title);",
            "  }",
            "}"};
    //
    ContainerInfo frame = parseContainer(lines1);
    NlsSupport support = NlsSupport.get(frame);
    AbstractSource fieldSource = support.getSources()[0];
    // do replace source/key
    GenericProperty nameProperty = (GenericProperty) frame.getPropertyByTitle("name");
    fieldSource.useKey(nameProperty, "frame_name");
    String[] lines =
        {
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setName(Messages.frame_name);",
            "    setTitle(Messages.frame_title);",
            "  }",
            "}"};
    // check source code
    assertEditor(lines);
    // no new keys expected
    assertEquals(accessor, getFileContentSrc("test/Messages.java"));
    assertEquals(properties, getFileContentSrc("test/messages.properties"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // useKey()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AbstractSource#useKey(GenericProperty, String)}.
   */
  public void test_useKey() throws Exception {
    setFileContentSrc(
        "test/messages.properties",
        getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "import java.util.ResourceBundle;",
            "public class Test extends JFrame {",
            "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
            "  public Test() {",
            "    setName(m_bundle.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    // check that we have DirectSource and FieldSource
    FieldSource fieldSource;
    {
      AbstractSource[] sources = support.getSources();
      assertThat(sources).hasSize(1);
      fieldSource = (FieldSource) sources[0];
    }
    // do replace source/key
    GenericProperty titleProperty = (GenericProperty) frame.getPropertyByTitle("title");
    fieldSource.useKey(titleProperty, "frame.title");
    // check source code
    assertEditor(
        "import java.util.ResourceBundle;",
        "public class Test extends JFrame {",
        "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle(\"test.messages\"); //$NON-NLS-1$",
        "  public Test() {",
        "    setTitle(m_bundle.getString(\"frame.title\")); //$NON-NLS-1$",
        "    setName(m_bundle.getString(\"frame.name\")); //$NON-NLS-1$",
        "  }",
        "}");
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertTrue(newProperties.contains("frame.title=My JFrame"));
      assertTrue(newProperties.contains("frame.name=My name"));
    }
  }

  /**
   * Test for {@link AbstractSource#useKey(GenericProperty, String)}.
   */
  public void test_useKey_alreadyExternalized() throws Exception {
    setFileContentSrc(
        "test/messages.properties",
        getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "import java.util.ResourceBundle;",
            "public class Test extends JFrame {",
            "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
            "  public Test() {",
            "    setName(m_bundle.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    // check that we have DirectSource and FieldSource
    FieldSource fieldSource;
    {
      AbstractSource[] sources = support.getSources();
      assertThat(sources).hasSize(1);
      fieldSource = (FieldSource) sources[0];
    }
    // do replace source/key
    GenericProperty nameProperty = (GenericProperty) frame.getPropertyByTitle("name");
    fieldSource.useKey(nameProperty, "frame.title");
    // check source code
    assertEditor(
        "import java.util.ResourceBundle;",
        "public class Test extends JFrame {",
        "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle(\"test.messages\"); //$NON-NLS-1$",
        "  public Test() {",
        "    setName(m_bundle.getString(\"frame.title\")); //$NON-NLS-1$",
        "  }",
        "}");
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertTrue(newProperties.contains("frame.title=My JFrame"));
      assertTrue(newProperties.contains("frame.name=My name"));
    }
  }
}
