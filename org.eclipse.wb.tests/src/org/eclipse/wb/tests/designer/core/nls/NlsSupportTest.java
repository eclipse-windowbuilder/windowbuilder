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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.old.EclipseSource;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.old.EclipseSourceNewComposite;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupport;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.nls.ui.AbstractSourceNewComposite;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;

/**
 * Tests for {@link NlsSupport} and basic NLS operations.
 * 
 * @author scheglov_ke
 */
public class NlsSupportTest extends SwingModelTest {
  private ContainerInfo m_frame;
  private NlsSupport m_support;

  ////////////////////////////////////////////////////////////////////////////
  //
  // setUp
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    waitEventLoop(0);
    if (m_frame != null) {
      m_frame.refresh_dispose();
      m_frame = null;
      m_support = null;
    }
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Fixture
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepareUsualState() throws Exception {
    createUsualAccessorProperties();
    parseUsualJFrame();
  }

  private void createUsualAccessorProperties() throws Exception {
    createUsualAccessor();
    setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
    setFileContentSrc("test/messages_it.properties", getSourceDQ("frame.title=My JFrame IT"));
    waitForAutoBuild();
  }

  private void createUsualAccessor() throws Exception {
    NlsTestUtils.create_EclipseOld_Accessor(this, false);
  }

  private void parseUsualJFrame() throws Exception {
    m_frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
            "    setName('Some name');",
            "  }",
            "}");
    //
    m_support = NlsSupport.get(m_frame);
    AbstractSource.setLocaleInfo(m_frame, LocaleInfo.DEFAULT);
  }

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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link NlsSupport#getKeySource(String)}.
   */
  public void test_getKeySource() throws Exception {
    prepareUsualState();
    NlsSupport nlsSupport = NlsSupport.get(m_frame);
    // prepare only source
    AbstractSource source;
    {
      AbstractSource[] sources = nlsSupport.getSources();
      assertThat(sources).hasSize(1);
      source = sources[0];
    }
    //
    assertSame(source, nlsSupport.getKeySource("frame.title"));
    assertSame(null, nlsSupport.getKeySource("no.such.key"));
  }

  public void test_getValue_default() throws Exception {
    prepareUsualState();
    // check with default locale
    m_frame.refresh();
    assertEquals("My JFrame", ((JFrame) m_frame.getObject()).getTitle());
  }

  public void test_getValue_null() throws Exception {
    prepareUsualState();
    AbstractSource.setLocaleInfo(m_frame, null);
    m_frame.refresh();
    assertEquals("My JFrame", ((JFrame) m_frame.getObject()).getTitle());
  }

  public void test_getValue_it() throws Exception {
    prepareUsualState();
    // check with Italian locale (I don't use Russian because it will be used as default in my case...)
    AbstractSource.setLocaleInfo(m_frame, new LocaleInfo(new Locale("it")));
    m_frame.refresh();
    assertEquals("My JFrame IT", ((JFrame) m_frame.getObject()).getTitle());
  }

  public void test_getValue_it_IT() throws Exception {
    prepareUsualState();
    AbstractSource.setLocaleInfo(m_frame, new LocaleInfo(new Locale("it_IT")));
    m_frame.refresh();
    assertEquals("My JFrame IT", ((JFrame) m_frame.getObject()).getTitle());
  }

  /**
   * Check for "bad expression" when there are no bundle files.
   */
  public void test_getValue_removeResources() throws Exception {
    createUsualAccessor();
    //
    m_frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title'));",
            "  }",
            "}");
    m_frame.refresh();
    //
    m_support = NlsSupport.get(m_frame);
    {
      Expression expression =
          ((GenericProperty) m_frame.getPropertyByTitle("title")).getExpression();
      assertTrue(NlsSupport.isBadExpression(expression));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // More rendering
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Users ask why chosen {@link Locale} is not used for binary components.
   * <p>
   * Note, that this feature works only for "old Eclipse" source, with "Messages" class.
   */
  public void test_useSelectedLocale_forBinaryComponents() throws Exception {
    createUsualAccessorProperties();
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setName(Messages.getString('frame.title')); //$NON-NLS-1$",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "}");
    panel.refresh();
    // check that "name" is set
    assertEquals("My JFrame", panel.getComponent().getName());
    // set new locale
    AbstractSource.setLocaleInfo(panel, new LocaleInfo(new Locale("it")));
    panel.refresh();
    assertEquals("My JFrame IT", panel.getComponent().getName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // NLSSource
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_SourceDescription() throws Exception {
    prepareUsualState();
    SourceDescription[] sourceDescriptions = NlsSupport.getSourceDescriptions(m_frame);
    for (int i = 0; i < sourceDescriptions.length; i++) {
      SourceDescription sourceDescription = sourceDescriptions[i];
      if (sourceDescription.getSourceClass() == EclipseSource.class) {
        assertSame(EclipseSourceNewComposite.class, sourceDescription.getNewCompositeClass());
        assertEquals("Classic Eclipse messages class", sourceDescription.getTitle());
        // test createNewComposite()
        Shell shell = new Shell();
        try {
          AbstractSourceNewComposite composite =
              sourceDescription.createNewComposite(shell, m_frame);
          assertNotNull(composite);
        } finally {
          shell.dispose();
        }
      }
    }
  }

  public void test_NLSSource_getRoot() throws Exception {
    prepareUsualState();
    assertSame(m_frame, m_support.getRoot());
  }

  public void test_NLSSource_getSources() throws Exception {
    prepareUsualState();
    AbstractSource[] sources = m_support.getSources();
    assertEquals(1, sources.length);
    assertInstanceOf(EclipseSource.class, sources[0]);
  }

  public void test_NLSSource_getLocales() throws Exception {
    prepareUsualState();
    LocaleInfo[] locales = m_support.getLocales();
    assertThat(locales).hasSize(2);
    assertEquals("(default)", locales[0].getTitle());
    assertEquals("it", locales[1].getTitle());
  }

  /**
   * User asked to provide global configuration to set locales which always will exists.
   */
  public void test_NLSSource_getLocales_alwaysVisibleLocales() throws Exception {
    prepareUsualState();
    ToolkitDescription toolkit = m_frame.getDescription().getToolkit();
    PreferencesRepairer preferencesRepairer = new PreferencesRepairer(toolkit.getPreferences());
    try {
      preferencesRepairer.setValue(IPreferenceConstants.P_NLS_ALWAYS_VISIBLE_LOCALES, "de, ru_RU");
      LocaleInfo[] locales = m_support.getLocales();
      List<String> localeNames =
          Lists.transform(ImmutableList.copyOf(locales), new Function<LocaleInfo, String>() {
            public String apply(LocaleInfo from) {
              return from.toString();
            }
          });
      assertThat(localeNames).hasSize(4).containsOnly("(default)", "it", "de", "ru_RU");
    } finally {
      preferencesRepairer.restore();
    }
  }

  public void test_NLSSource_isExternalized() throws Exception {
    prepareUsualState();
    // externalized expression
    {
      GenericProperty titleProperty = (GenericProperty) m_frame.getPropertyByTitle("title");
      assertTrue(m_support.isExternalized(titleProperty.getExpression()));
    }
    // not externalized expression
    {
      GenericProperty nameProperty = (GenericProperty) m_frame.getPropertyByTitle("name");
      assertFalse(m_support.isExternalized(nameProperty.getExpression()));
    }
  }

  public void test_NLSSource_isExternallyChanged() throws Exception {
    prepareUsualState();
    assertFalse(m_support.isExternallyChanged());
    setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame2"));
    assertTrue(m_support.isExternallyChanged());
  }

  public void test_NLSSource_setValue() throws Exception {
    prepareUsualState();
    // set value
    GenericProperty titleProperty = (GenericProperty) m_frame.getPropertyByTitle("title");
    m_support.setValue(titleProperty.getExpression(), "New title");
    // check *.properties file
    String newProperties = getFileContentSrc("test/messages.properties");
    assertTrue(newProperties.contains("frame.title=New title"));
  }

  public void test_NLSSource_externalize() throws Exception {
    prepareUsualState();
    // set value
    GenericProperty nameProperty = (GenericProperty) m_frame.getPropertyByTitle("name");
    m_support.externalize(m_frame, nameProperty);
    // check source
    assertEditor(new String[]{
        "class Test extends JFrame {",
        "  Test() {",
        "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
        "    setName(Messages.getString('Test.this.name')); //$NON-NLS-1$",
        "  }",
        "}"});
    // check *.properties file
    String newProperties = getFileContentSrc("test/messages.properties");
    assertTrue(newProperties.contains("Test.this.name=Some name"));
  }

  /**
   * Test for {@link NlsSupport#getAttachedSource(IEditableSupport, IEditableSource)}.
   */
  public void test_getAttachedSource() throws Exception {
    setFileContentSrc("test/messages.properties", getSourceDQ("#Field ResourceBundle: m_bundle"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JFrame {",
            "  public Test() {",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    // check that we have (possible) editable source
    List<IEditableSource> editableSources = editableSupport.getEditableSources();
    assertThat(editableSources).hasSize(1);
    IEditableSource editableSource = editableSources.get(0);
    // not attached, so to source
    {
      AbstractSource source = editableSupport.getSource(editableSource);
      assertNull(source);
    }
    // attach
    AbstractSource source = support.getAttachedSource(editableSupport, editableSource);
    assertNotNull(source);
    assertEditor(
        "import java.util.ResourceBundle;",
        "// filler filler filler",
        "public class Test extends JFrame {",
        "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle(\"test.messages\"); //$NON-NLS-1$",
        "  public Test() {",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenericProperty.setValue()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_GenericProperty_setValue_clearProperty() throws Exception {
    prepareUsualState();
    m_frame.refresh();
    // set value
    Property titleProperty = m_frame.getPropertyByTitle("title");
    titleProperty.setValue(Property.UNKNOWN_VALUE);
    // check that JFrame was refreshed
    assertEquals("", ((JFrame) m_frame.getObject()).getTitle());
    // check source
    assertEditor(new String[]{
        "class Test extends JFrame {",
        "  Test() {",
        "    setName('Some name');",
        "  }",
        "}"});
    // check *.properties file
    String newProperties = getFileContentSrc("test/messages.properties");
    assertFalse(newProperties.contains("Test.this.name=myName"));
  }

  public void test_GenericProperty_setValue_forAlreadyExternalized() throws Exception {
    prepareUsualState();
    m_frame.refresh();
    // set value
    {
      GenericProperty property = (GenericProperty) m_frame.getPropertyByTitle("title");
      property.setValue("New title");
    }
    // check *.properties file
    String newProperties = getFileContentSrc("test/messages.properties");
    assertTrue(newProperties.contains("frame.title=New title"));
    // check that JFrame was refreshed
    assertEquals("New title", ((JFrame) m_frame.getObject()).getTitle());
  }

  public void test_GenericProperty_setValue_setKey_forNewProperty() throws Exception {
    createUsualAccessorProperties();
    m_frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setName(Messages.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    m_frame.refresh();
    // set "name" property value
    {
      Property property = m_frame.getPropertyByTitle("title");
      property.setValue("*frame.title");
      assertEquals("My JFrame", property.getValue());
    }
    // check source
    {
      String expectedSource =
          getTestSource(
              "public class Test extends JFrame {",
              "  public Test() {",
              "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
              "    setName(Messages.getString('frame.name')); //$NON-NLS-1$",
              "  }",
              "}");
      assertEditor(expectedSource, m_lastEditor);
      assertEquals(expectedSource, m_lastEditor.getModelUnit().getSource());
    }
    // check *.properties file
    String newProperties = getFileContentSrc("test/messages.properties");
    assertTrue(newProperties.contains("frame.title=My JFrame"));
  }

  public void test_GenericProperty_setValue_setKey_forExistingProperty() throws Exception {
    createUsualAccessorProperties();
    m_frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setName(Messages.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    m_frame.refresh();
    // set "name" property value
    {
      Property property = m_frame.getPropertyByTitle("name");
      property.setValue("*frame.title");
      assertEquals("My JFrame", property.getValue());
    }
    // check source
    {
      String expectedSource =
          getTestSource(
              "public class Test extends JFrame {",
              "  public Test() {",
              "    setName(Messages.getString('frame.title')); //$NON-NLS-1$",
              "  }",
              "}");
      assertEditor(expectedSource, m_lastEditor);
      assertEquals(expectedSource, m_lastEditor.getModelUnit().getSource());
    }
    // check *.properties file
    String newProperties = getFileContentSrc("test/messages.properties");
    assertTrue(newProperties.contains("frame.title=My JFrame"));
  }

  public void test_GenericProperty_setValue_autoExternalizeEnabled() throws Exception {
    createUsualAccessorProperties();
    m_frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
            "  }",
            "}");
    m_frame.refresh();
    // set "name" property value
    Property property = m_frame.getPropertyByTitle("name");
    property.setValue("myName");
    assertEquals("myName", property.getValue());
    // check source
    {
      String expectedSource =
          getTestSource(
              "class Test extends JFrame {",
              "  Test() {",
              "    setName(Messages.getString('Test.this.name')); //$NON-NLS-1$",
              "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
              "  }",
              "}");
      assertEditor(expectedSource, m_lastEditor);
      assertEquals(expectedSource, m_lastEditor.getModelUnit().getSource());
    }
    // check *.properties file
    String newProperties = getFileContentSrc("test/messages.properties");
    assertTrue(newProperties.contains("Test.this.name=myName"));
  }

  public void test_GenericProperty_setValue_autoExternalizeDisable() throws Exception {
    createUsualAccessorProperties();
    m_frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
            "  }",
            "}");
    m_frame.refresh();
    // set "name" property value
    PreferencesRepairer preferences =
        new PreferencesRepairer(ToolkitProvider.DESCRIPTION.getPreferences());
    try {
      preferences.setValue(IPreferenceConstants.P_NLS_AUTO_EXTERNALIZE, false);
      m_frame.getPropertyByTitle("name").setValue("myName");
    } finally {
      preferences.restore();
    }
    // check source
    assertEditor(new String[]{
        "class Test extends JFrame {",
        "  Test() {",
        "    setName('myName');",
        "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
        "  }",
        "}"});
    // check *.properties file
    String newProperties = getFileContentSrc("test/messages.properties");
    assertFalse(newProperties.contains("Test.this.name=myName"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Live component" and NLS
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class MyButtonInfo extends ComponentInfo {
    public MyButtonInfo(AstEditor editor,
        ComponentDescription description,
        CreationSupport creationSupport) throws Exception {
      super(editor, description, creationSupport);
      addBroadcastListener(new JavaEventListener() {
        @Override
        public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
          getPropertyByTitle("text").setValue("txt");
        }
      });
    }
  }

  public void test_liveImage_whenExternalized() throws Exception {
    createUsualAccessorProperties();
    // use model that sets value for Property
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <model class='" + MyButtonInfo.class.getName() + "'/>",
            "</component>"));
    waitForAutoBuild();
    // parse
    m_frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
            "  }",
            "}");
    m_frame.refresh();
    // remember old NLS state
    String properties = getFileContentSrc("test/messages.properties");
    String messages = getFileContentSrc("test/Messages.java");
    // ask image
    {
      ComponentInfo button = createComponent("test.MyButton");
      Image image = button.getImage();
      assertNotNull(image);
      assertThat(image.getBounds().width).isGreaterThan(40);
      assertThat(image.getBounds().height).isGreaterThan(20);
    }
    // check that NLS state is not changed
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      String newMessages = getFileContentSrc("test/Messages.java");
      assertEquals(properties, newProperties);
      assertEquals(messages, newMessages);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Variable
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When user renames component, we should rename associated NLS keys.
   */
  public void test_renameKeysWhenVariable() throws Exception {
    setFileContentSrc(
        "test/messages.properties",
        getSourceDQ("Test.frame.title=My JFrame", "frame.name=My name", "foo.bar=baz"));
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
            "public class Test {",
            "  private static final ResourceBundle m_bundle = ResourceBundle.getBundle('test.messages'); //$NON-NLS-1$",
            "  public static void main(String[] args) {",
            "    JFrame frame = new JFrame();",
            "    frame.setTitle(m_bundle.getString('Test.frame.title')); //$NON-NLS-1$",
            "  }",
            "}");
    frame.refresh();
    // set "rename" preference flag
    PreferencesRepairer preferencesRepairer =
        new PreferencesRepairer(frame.getDescription().getToolkit().getPreferences());
    try {
      preferencesRepairer.setValue(IPreferenceConstants.P_NLS_KEY_RENAME_WITH_VARIABLE, true);
      frame.getVariableSupport().setName("newName");
      // "Test.frame.title" renamed, because it has "frame" and used
      // "frame.name" renamed because has "frame", but ignored, because it is not used in this form
      // "foo.bar" not renamed, because no "frame"
      {
        String newProperties = getFileContentSrc("test/messages.properties");
        assertTrue(newProperties.contains("Test.newName.title=My JFrame"));
        assertTrue(newProperties.contains("frame.name=My name"));
        assertTrue(newProperties.contains("foo.bar=baz"));
      }
    } finally {
      preferencesRepairer.restore();
    }
    // don't enable "rename" flag
    {
      frame.getVariableSupport().setName("frame2");
      // no changes
      {
        String newProperties = getFileContentSrc("test/messages.properties");
        assertTrue(newProperties.contains("Test.newName.title=My JFrame"));
      }
    }
  }
}
