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
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.bundle.pure.field.FieldSource;
import org.eclipse.wb.internal.core.nls.bundle.pure.field.FieldSourceNewComposite;
import org.eclipse.wb.internal.core.nls.bundle.pure.field.SourceParameters;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupport;
import org.eclipse.wb.internal.core.nls.edit.StringPropertyInfo;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.StringLiteral;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.swing.JFrame;

/**
 * Tests for {@link FieldSource}.
 * 
 * @author scheglov_ke
 */
public class SourceFieldTest extends AbstractNlsTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Not externalized.
   */
  public void test_notDirectCases_1() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle('title');",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    assertEquals(0, support.getSources().length);
  }

  /**
   * Bundle is not assigned on declaration.
   */
  public void test_notDirectCases_2() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo frame =
        parseContainer(
            "import java.util.ResourceBundle;",
            "public class Test extends JFrame {",
            "  public Test() {",
            "    ResourceBundle bundle;",
            "    bundle = ResourceBundle.getBundle('test.messages');",
            "    setTitle(bundle.getString('frame.title'));",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    assertEquals(0, support.getSources().length);
  }

  /**
   * Not "getBundle()" invocation.
   */
  public void test_notDirectCases_3() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo frame =
        parseContainer(
            "import java.util.ResourceBundle;",
            "public class Test extends JFrame {",
            "  ResourceBundle m_bundle = new java.util.PropertyResourceBundle((java.io.Reader)null);",
            "  public Test() throws Exception {",
            "    setTitle(m_bundle.getString('frame.title'));",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    assertEquals(0, support.getSources().length);
  }

  /**
   * Not {@link StringLiteral} as argument of "getBundle()".
   */
  public void test_notDirectCases_4() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo frame =
        parseContainer(
            "import java.util.ResourceBundle;",
            "public class Test extends JFrame {",
            "  ResourceBundle m_bundle = ResourceBundle.getBundle(null);",
            "  public Test() throws Exception {",
            "    setTitle(m_bundle.getString('frame.title'));",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    assertEquals(0, support.getSources().length);
  }

  public void test_parse() throws Exception {
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
            "    setName(m_bundle.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    assertNoErrors(frame);
    NlsSupport support = NlsSupport.get(frame);
    // check that we have FieldSource
    FieldSource source;
    {
      AbstractSource[] sources = support.getSources();
      assertEquals(1, sources.length);
      source = (FieldSource) sources[0];
    }
    // check getBundleComment()
    assertEquals(
        "Field ResourceBundle: m_bundle",
        ReflectionUtils.invokeMethod(source, "getBundleComment()"));
    // check that "title" is correct
    frame.refresh();
    try {
      JFrame jFrame = (JFrame) frame.getObject();
      assertEquals("My JFrame", jFrame.getTitle());
      assertEquals("My name", jFrame.getName());
    } finally {
      frame.refresh_dispose();
    }
  }

  /**
   * In general case we can not know which resource bundle to use. But if "wnp.nls.resourceBundle"
   * is specified, we can parse it and read/update it.
   */
  public void test_useSpecificCreation_butWithWbpTag() throws Exception {
    setFileContentSrc(
        "test/messages.properties",
        getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
    setFileContentSrc(
        "test/MyResourceBundleFactory.java",
        getSourceDQ(
            "package test;",
            "import java.util.ResourceBundle;",
            "public class MyResourceBundleFactory {",
            "  public static ResourceBundle getMainBundle() {",
            "    return ResourceBundle.getBundle('test.messages');",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "import java.util.ResourceBundle;",
            "public class Test extends JFrame {",
            "  /**",
            "  * @wbp.nls.resourceBundle test.messages",
            "  */",
            "  private static final ResourceBundle m_bundle = MyResourceBundleFactory.getMainBundle();",
            "  public Test() {",
            "    setTitle(m_bundle.getString('frame.title')); //$NON-NLS-1$",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    NlsSupport support = NlsSupport.get(frame);
    // check that we have FieldSource
    {
      AbstractSource[] sources = support.getSources();
      assertThat(sources).hasSize(1);
      FieldSource source = (FieldSource) sources[0];
      assertNotNull(source);
    }
    // check that "title" is correct
    JFrame jFrame = (JFrame) frame.getObject();
    assertEquals("My JFrame", jFrame.getTitle());
  }

  public void test_parse_getWithLocale() throws Exception {
    setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "import java.util.ResourceBundle;",
            "import java.util.Locale;",
            "public class Test extends JFrame {",
            "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages', Locale.getDefault()); //$NON-NLS-1$",
            "  public Test() {",
            "    setTitle(m_bundle.getString('frame.title')); //$NON-NLS-1$",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    // check that we have FieldSource
    {
      AbstractSource[] sources = support.getSources();
      assertEquals(1, sources.length);
      assertInstanceOf(FieldSource.class, sources[0]);
    }
    // check that "title" is correct
    frame.refresh();
    try {
      JFrame jFrame = (JFrame) frame.getObject();
      assertEquals("My JFrame", jFrame.getTitle());
    } finally {
      frame.refresh_dispose();
    }
  }

  public void test_setValue() throws Exception {
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
    AbstractSource.setLocaleInfo(frame, LocaleInfo.DEFAULT);
    NlsSupport support = NlsSupport.get(frame);
    //
    GenericProperty titleProperty = (GenericProperty) frame.getPropertyByTitle("title");
    support.setValue(titleProperty.getExpression(), "New title");
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertTrue(newProperties.contains("#Field ResourceBundle: m_bundle"));
      assertTrue(newProperties.contains("frame.title=New title"));
    }
  }

  public void test_setValue2() throws Exception {
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
    AbstractSource.setLocaleInfo(frame, LocaleInfo.DEFAULT);
    //
    Property titleProperty = frame.getPropertyByTitle("title");
    titleProperty.setValue("New title");
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertTrue(newProperties.contains("#Field ResourceBundle: m_bundle"));
      assertTrue(newProperties.contains("frame.title=New title"));
    }
  }

  public void test_possibleSources() throws Exception {
    setFileContentSrc("test/not-a-properties.text", "");
    setFileContentSrc(
        "test/messages2.properties",
        getSourceDQ("#Invalid comment for Direct ResourceBundle"));
    setFileContentSrc("test/messages.properties", getSourceDQ("#Field ResourceBundle: m_bundle"));
    setFileContentSrc(
        "test/messages_it.properties",
        getSourceDQ("#We need only default *.properties file"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle('My JFrame');",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    // check that we have (possible) editable source
    List<IEditableSource> editableSources = editableSupport.getEditableSources();
    assertEquals(1, editableSources.size());
    // check this single editable source
    IEditableSource editableSource = editableSources.get(0);
    assertEquals(
        "test.messages (ResourceBundle in field 'm_bundle')",
        editableSource.getLongTitle());
    // use possible source
    StringPropertyInfo propertyInfo = editableSupport.getProperties(frame).get(0);
    editableSupport.externalizeProperty(propertyInfo, editableSource, true);
    // apply commands
    support.applyEditable(editableSupport);
    // check
    assertEditor(
        "import java.util.ResourceBundle;",
        "public class Test extends JFrame {",
        "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
        "  public Test() {",
        "    setTitle(m_bundle.getString('Test.this.title')); //$NON-NLS-1$",
        "  }",
        "}");
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertTrue(newProperties.contains("#Field ResourceBundle: m_bundle"));
      assertTrue(newProperties.contains("Test.this.title=My JFrame"));
    }
    {
      String newProperties = getFileContentSrc("test/messages_it.properties");
      assertTrue(newProperties.contains("#Field ResourceBundle: m_bundle"));
      assertTrue(newProperties.contains("Test.this.title=My JFrame"));
    }
  }

  public void test_renameKey() throws Exception {
    setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
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
    IEditableSupport editableSupport = support.getEditable();
    IEditableSource editableSource = editableSupport.getEditableSources().get(0);
    // do rename
    editableSource.renameKey("frame.title", "frame.title2");
    // apply commands
    support.applyEditable(editableSupport);
    // check
    assertEditor(
        "import java.util.ResourceBundle;",
        "public class Test extends JFrame {",
        "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
        "  public Test() {",
        "    setTitle(m_bundle.getString('frame.title2')); //$NON-NLS-1$",
        "  }",
        "}");
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertFalse(newProperties.contains("frame.title=My JFrame"));
      assertTrue(newProperties.contains("frame.title2=My JFrame"));
    }
  }

  public void test_internalize() throws Exception {
    setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
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
    IEditableSupport editableSupport = support.getEditable();
    IEditableSource editableSource = editableSupport.getEditableSources().get(0);
    // do internalize
    editableSource.internalizeKey("frame.title");
    // apply commands
    support.applyEditable(editableSupport);
    // check
    assertEditor(
        "import java.util.ResourceBundle;",
        "public class Test extends JFrame {",
        "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
        "  public Test() {",
        "    setTitle('My JFrame');",
        "  }",
        "}");
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertFalse(newProperties.contains("frame.title=My JFrame"));
    }
  }

  public void test_create() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle('My JFrame');",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    // prepare editable source
    IEditableSource editableSource = NlsTestUtils.createEmptyEditable("test.messages");
    // prepare parameters
    SourceParameters parameters = new SourceParameters();
    IJavaProject javaProject = m_lastEditor.getJavaProject();
    {
      parameters.m_propertySourceFolder =
          javaProject.findPackageFragmentRoot(new Path("/TestProject/src"));
      parameters.m_propertyPackage =
          javaProject.findPackageFragment(new Path("/TestProject/src/test"));
      parameters.m_propertyFileName = "messages.properties";
      parameters.m_propertyBundleName = "test.messages";
      parameters.m_propertyFileExists = false;
      //
      parameters.m_fieldName = "m_bundle";
    }
    // add source
    editableSupport.addSource(editableSource, new SourceDescription(FieldSource.class,
        FieldSourceNewComposite.class), parameters);
    // do externalize
    StringPropertyInfo propertyInfo = editableSupport.getProperties(frame).get(0);
    editableSupport.externalizeProperty(propertyInfo, editableSource, true);
    // apply commands
    support.applyEditable(editableSupport);
    // checks
    assertEditor(
        "import java.util.ResourceBundle;",
        "public class Test extends JFrame {",
        "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
        "  public Test() {",
        "    setTitle(m_bundle.getString('Test.this.title')); //$NON-NLS-1$",
        "  }",
        "}");
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertTrue(newProperties.contains("#Field ResourceBundle: m_bundle"));
      assertTrue(newProperties.contains("Test.this.title=My JFrame"));
    }
  }
}
