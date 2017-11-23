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
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.modern.ModernEclipseSource;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.modern.ModernEclipseSourceNewComposite;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.modern.SourceParameters;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupport;
import org.eclipse.wb.internal.core.nls.edit.StringPropertyInfo;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.StringLiteral;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

import java.util.List;

import javax.swing.JFrame;

/**
 * Tests for {@link ModernEclipseSource}.
 * 
 * @author scheglov_ke
 */
public class SourceEclipseModernTest extends AbstractNlsTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m_testProject.addPlugin("org.eclipse.osgi");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Not a {@link ModernEclipseSource} - just {@link StringLiteral}.
   */
  public void test_notModernEclipse_1() throws Exception {
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
   * Not a {@link ModernEclipseSource} - access to static field in not <code>NLS</code> subclass.
   */
  public void test_notModernEclipse_2() throws Exception {
    setFileContentSrc(
        "test/Strings.java",
        getSourceDQ(
            "package test;",
            "public class Strings {",
            "  public static String MSG = 'Hello!';",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle(Strings.MSG);",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    assertEquals(0, support.getSources().length);
  }

  public void test_parse() throws Exception {
    createAccessorAndProperties();
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle(Messages.frame_title);",
            "    setName(Messages.frame_name);",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    // check that we have DirectSource
    ModernEclipseSource source;
    {
      AbstractSource[] sources = support.getSources();
      assertEquals(1, sources.length);
      source = (ModernEclipseSource) sources[0];
    }
    // check getBundleComment()
    assertEquals(
        "Eclipse modern messages class",
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
   * Use constructor without accessor, only to create {@link IEditableSource} using existing
   * *.properties.
   */
  public void test_constructorWithoutAccessor() throws Exception {
    setFileContentSrc(
        "test/messages.properties",
        getSourceDQ("frame_title=My JFrame", "frame_name=My name"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JFrame {",
            "  public Test() {",
            "  }",
            "}");
    //
    ModernEclipseSource source = new ModernEclipseSource(frame, null, "test.messages");
    IEditableSource editableSource = source.getEditable();
    assertEquals("My JFrame", editableSource.getValue(LocaleInfo.DEFAULT, "frame_title"));
    assertEquals("My name", editableSource.getValue(LocaleInfo.DEFAULT, "frame_name"));
  }

  public void test_possibleSources() throws Exception {
    createAccessorAndProperties();
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JFrame {",
            "  public Test() {",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    //
    List<IEditableSource> editableSources = editableSupport.getEditableSources();
    assertEquals(1, editableSources.size());
    //
    IEditableSource editableSource = editableSources.get(0);
    assertEquals(
        "test.messages (Modern Eclipse messages class test.Messages)",
        editableSource.getLongTitle());
  }

  /**
   * Test for {@link ModernEclipseSource#apply_addKey(String)}.
   */
  public void test_apply_addKey() throws Exception {
    NlsTestUtils.create_EclipseModern_AccessorAndProperties();
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JFrame {",
            "  public Test() {",
            "  }",
            "}");
    String frameSource = m_lastEditor.getSource();
    // add key
    ModernEclipseSource source = new ModernEclipseSource(frame, "test.Messages", "test.messages");
    source.apply_addKey("newKey");
    // checks
    assertEditor(frameSource, m_lastEditor);
    {
      String accessor = getFileContentSrc("test/Messages.java");
      assertThat(accessor).contains("public static String newKey;");
    }
  }

  public void test_externalize() throws Exception {
    // create empty OSGi NLS source
    setFileContentSrc("test/messages.properties", "");
    setFileContentSrc(
        "test/Messages.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.osgi.util.NLS;",
            "public class Messages extends NLS {",
            "  private static final String BUNDLE_NAME = 'test.messages'; //$NON-NLS-1$",
            "  private Messages() {}",
            "  static {",
            "    NLS.initializeMessages(BUNDLE_NAME, Messages.class);",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle('My JFrame');",
            "    setName('My name');",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    // prepare possible source
    IEditableSource editableSource;
    {
      List<IEditableSource> editableSources = editableSupport.getEditableSources();
      assertEquals(1, editableSources.size());
      editableSource = editableSources.get(0);
    }
    // do externalize
    {
      StringPropertyInfo propertyInfo;
      // title
      propertyInfo = new StringPropertyInfo((GenericProperty) frame.getPropertyByTitle("title"));
      editableSupport.externalizeProperty(propertyInfo, editableSource, true);
      // name
      propertyInfo = new StringPropertyInfo((GenericProperty) frame.getPropertyByTitle("name"));
      editableSupport.externalizeProperty(propertyInfo, editableSource, true);
    }
    // apply commands
    support.applyEditable(editableSupport);
    // check
    assertEditor(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    setTitle(Messages.Test_this_title);",
        "    setName(Messages.Test_this_name);",
        "  }",
        "}");
    {
      String messages = getFileContentSrc("test/Messages.java");
      assertEquals(
          getSourceDQ(
              "package test;",
              "import org.eclipse.osgi.util.NLS;",
              "public class Messages extends NLS {",
              "  private static final String BUNDLE_NAME = 'test.messages'; //$NON-NLS-1$",
              "  public static String Test_this_title;",
              "  public static String Test_this_name;",
              "  private Messages() {}",
              "  static {",
              "    NLS.initializeMessages(BUNDLE_NAME, Messages.class);",
              "  }",
              "}"),
          messages);
    }
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertTrue(newProperties.contains("#Eclipse modern messages class"));
      assertTrue(newProperties.contains("Test_this_title=My JFrame"));
      assertTrue(newProperties.contains("Test_this_name=My name"));
    }
  }

  public void test_externalize_qualifiedTypeName() throws Exception {
    // create empty OSGi NLS source
    setFileContentSrc("test/messages.properties", "");
    setFileContentSrc(
        "test/Messages.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.osgi.util.NLS;",
            "public class Messages extends NLS {",
            "  private static final String BUNDLE_NAME = 'test.messages'; //$NON-NLS-1$",
            "  private Messages() {}",
            "  static {",
            "    NLS.initializeMessages(BUNDLE_NAME, Messages.class);",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle('My JFrame');",
            "    setName('My name');",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    // prepare possible source
    IEditableSource editableSource;
    {
      List<IEditableSource> editableSources = editableSupport.getEditableSources();
      assertEquals(1, editableSources.size());
      editableSource = editableSources.get(0);
    }
    // do externalize
    // do externalize
    PreferencesRepairer preferences =
        new PreferencesRepairer(ToolkitProvider.DESCRIPTION.getPreferences());
    try {
      preferences.setValue(IPreferenceConstants.P_NLS_KEY_QUALIFIED_TYPE_NAME, true);
      StringPropertyInfo propertyInfo;
      // title
      propertyInfo = new StringPropertyInfo((GenericProperty) frame.getPropertyByTitle("title"));
      editableSupport.externalizeProperty(propertyInfo, editableSource, true);
      // name
      propertyInfo = new StringPropertyInfo((GenericProperty) frame.getPropertyByTitle("name"));
      editableSupport.externalizeProperty(propertyInfo, editableSource, true);
    } finally {
      preferences.restore();
    }
    // apply commands
    support.applyEditable(editableSupport);
    // check
    assertEditor(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    setTitle(Messages.test_Test_this_title);",
        "    setName(Messages.test_Test_this_name);",
        "  }",
        "}");
    {
      String messages = getFileContentSrc("test/Messages.java");
      assertEquals(
          getSourceDQ(
              "package test;",
              "import org.eclipse.osgi.util.NLS;",
              "public class Messages extends NLS {",
              "  private static final String BUNDLE_NAME = 'test.messages'; //$NON-NLS-1$",
              "  public static String test_Test_this_title;",
              "  public static String test_Test_this_name;",
              "  private Messages() {}",
              "  static {",
              "    NLS.initializeMessages(BUNDLE_NAME, Messages.class);",
              "  }",
              "}"),
          messages);
    }
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertTrue(newProperties.contains("#Eclipse modern messages class"));
      assertTrue(newProperties.contains("test_Test_this_title=My JFrame"));
      assertTrue(newProperties.contains("test_Test_this_name=My name"));
    }
  }

  public void test_renameKey() throws Exception {
    createAccessorAndProperties();
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle(Messages.frame_title);",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    IEditableSource editableSource = editableSupport.getEditableSources().get(0);
    // do rename
    editableSource.renameKey("frame_title", "frame_title2");
    // apply commands
    support.applyEditable(editableSupport);
    // check
    assertEditor(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    setTitle(Messages.frame_title2);",
        "  }",
        "}");
    {
      String messages = getFileContentSrc("test/Messages.java");
      assertEquals(
          getSourceDQ(
              "package test;",
              "import org.eclipse.osgi.util.NLS;",
              "public class Messages extends NLS {",
              "  private static final String BUNDLE_NAME = 'test.messages'; //$NON-NLS-1$",
              "  public static String frame_title2;",
              "  public static String frame_name;",
              "  private Messages() {}",
              "  static {",
              "    NLS.initializeMessages(BUNDLE_NAME, Messages.class);",
              "  }",
              "}"),
          messages);
    }
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertFalse(newProperties.contains("frame_title=My JFrame"));
      assertTrue(newProperties.contains("frame_title2=My JFrame"));
    }
  }

  public void test_internalize() throws Exception {
    createAccessorAndProperties();
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle(Messages.frame_title);",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    IEditableSource editableSource = editableSupport.getEditableSources().get(0);
    // do internalize
    editableSource.internalizeKey("frame_title");
    // apply commands
    support.applyEditable(editableSupport);
    // check
    assertEditor(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    setTitle('My JFrame');",
        "  }",
        "}");
    {
      String messages = getFileContentSrc("test/Messages.java");
      assertEquals(
          getSourceDQ(
              "package test;",
              "import org.eclipse.osgi.util.NLS;",
              "public class Messages extends NLS {",
              "  private static final String BUNDLE_NAME = 'test.messages'; //$NON-NLS-1$",
              "  public static String frame_name;",
              "  private Messages() {}",
              "  static {",
              "    NLS.initializeMessages(BUNDLE_NAME, Messages.class);",
              "  }",
              "}"),
          messages);
    }
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertFalse(newProperties.contains("frame_title=My JFrame"));
      assertTrue(newProperties.contains("frame_name=My name"));
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
    editableSource.setKeyGeneratorStrategy(ModernEclipseSource.MODERN_KEY_GENERATOR);
    // prepare parameters
    SourceParameters parameters = new SourceParameters();
    IJavaProject javaProject = m_lastEditor.getJavaProject();
    {
      parameters.m_accessorSourceFolder =
          javaProject.findPackageFragmentRoot(new Path("/TestProject/src"));
      parameters.m_accessorPackage =
          javaProject.findPackageFragment(new Path("/TestProject/src/test"));
      parameters.m_accessorPackageName = parameters.m_accessorPackage.getElementName();
      parameters.m_accessorClassName = "Messages";
      parameters.m_accessorFullClassName = "test.Messages";
      parameters.m_accessorExists = false;
    }
    {
      parameters.m_propertySourceFolder = parameters.m_accessorSourceFolder;
      parameters.m_propertyPackage = parameters.m_accessorPackage;
      parameters.m_propertyFileName = "messages.properties";
      parameters.m_propertyBundleName = "test.messages";
      parameters.m_propertyFileExists = false;
    }
    // add source
    editableSupport.addSource(editableSource, new SourceDescription(ModernEclipseSource.class,
        ModernEclipseSourceNewComposite.class), parameters);
    // do externalize
    StringPropertyInfo propertyInfo = editableSupport.getProperties(frame).get(0);
    editableSupport.externalizeProperty(propertyInfo, editableSource, true);
    // apply commands
    support.applyEditable(editableSupport);
    // checks
    assertEditor(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    setTitle(Messages.Test_this_title);",
        "  }",
        "}");
    {
      String messages = StringUtils.replace(getFileContentSrc("test/Messages.java"), "\r\n", "\n");
      assertEquals(
          getSourceDQ(
              "package test;",
              "",
              "import org.eclipse.osgi.util.NLS;",
              "",
              "public class Messages extends NLS {",
              "  private static final String BUNDLE_NAME = 'test.messages'; //$NON-NLS-1$",
              "  public static String Test_this_title;",
              "  ////////////////////////////////////////////////////////////////////////////",
              "  //",
              "  // Constructor",
              "  //",
              "  ////////////////////////////////////////////////////////////////////////////",
              "  private Messages() {",
              "    // do not instantiate",
              "  }",
              "  ////////////////////////////////////////////////////////////////////////////",
              "  //",
              "  // Class initialization",
              "  //",
              "  ////////////////////////////////////////////////////////////////////////////",
              "  static {",
              "    // load message values from bundle file",
              "    NLS.initializeMessages(BUNDLE_NAME, Messages.class);",
              "  }",
              "}"),
          messages);
    }
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertTrue(newProperties.contains("#Eclipse modern messages class"));
      assertTrue(newProperties.contains("Test_this_title=My JFrame"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates accessor class and default properties for keys <code>frame_title</code> and
   * <code>frame_name</code>.
   */
  private void createAccessorAndProperties() throws Exception {
    setFileContentSrc(
        "test/messages.properties",
        getSourceDQ("frame_title=My JFrame", "frame_name=My name"));
    setFileContentSrc(
        "test/Messages.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.osgi.util.NLS;",
            "public class Messages extends NLS {",
            "  private static final String BUNDLE_NAME = 'test.messages'; //$NON-NLS-1$",
            "  public static String frame_title;",
            "  public static String frame_name;",
            "  private Messages() {}",
            "  static {",
            "    NLS.initializeMessages(BUNDLE_NAME, Messages.class);",
            "  }",
            "}"));
    waitForAutoBuild();
  }
}
