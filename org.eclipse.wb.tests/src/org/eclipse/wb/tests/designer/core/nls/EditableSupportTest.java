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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.old.EclipseSource;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.old.SourceParameters;
import org.eclipse.wb.internal.core.nls.commands.InternalizeKeyCommand;
import org.eclipse.wb.internal.core.nls.commands.RemoveLocaleCommand;
import org.eclipse.wb.internal.core.nls.commands.RenameKeyCommand;
import org.eclipse.wb.internal.core.nls.edit.EditableSupport;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSourceListener;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupport;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupportListener;
import org.eclipse.wb.internal.core.nls.edit.StringPropertyInfo;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JFrame;

/**
 * Tests for {@link IEditableSupport}.
 * 
 * @author scheglov_ke
 */
public class EditableSupportTest extends AbstractNlsTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_possibleSource() throws Exception {
    NlsTestUtils.create_EclipseOld_Accessor(this, false);
    setFileContentSrc("test/messages.properties", getSourceDQ("# some comment"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle('My JFrame');",
            "  }",
            "}");
    //
    NlsSupport support = NlsSupport.get(frame);
    EditableSupport editableSupport = (EditableSupport) support.getEditable();
    // getEditableSources(), should return "possible" sources
    {
      List<IEditableSource> editableSources = editableSupport.getEditableSources();
      assertEquals(1, editableSources.size());
      IEditableSource editableSource = editableSources.get(0);
      assertEquals("test.messages", editableSource.getShortTitle());
      assertNull(editableSupport.getSource(editableSource));
      assertInstanceOf(EclipseSource.class, editableSupport.getPossibleSource(editableSource));
    }
  }

  /**
   * Test for {@link RenameKeyCommand}.
   */
  public void test_RenameKeyCommand() throws Exception {
    NlsTestUtils.create_EclipseOld_Accessor(this, false);
    setFileContentSrc(
        "test/messages.properties",
        getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
            "    setName(Messages.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    // prepare NLS
    NlsSupport support = NlsSupport.get(frame);
    EditableSupport editableSupport = (EditableSupport) support.getEditable();
    // prepare editable source
    IEditableSource editableSource;
    {
      List<IEditableSource> editableSources = editableSupport.getEditableSources();
      assertEquals(1, editableSources.size());
      editableSource = editableSources.get(0);
    }
    // check keys
    assertStringSet(editableSource.getKeys(), new String[]{"frame.title", "frame.name"});
    // rename - two times
    editableSource.renameKey("frame.name", "frame.name2");
    editableSource.renameKey("frame.title", "frame.title2");
    editableSource.renameKey("frame.title2", "frame.title3");
    // apply commands
    support.applyEditable(editableSupport);
    // check
    assertStringSet(editableSource.getKeys(), new String[]{"frame.title3", "frame.name2"});
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      //
      assertFalse(newProperties.contains("frame.name=My name"));
      assertTrue(newProperties.contains("frame.name2=My name"));
      //
      assertFalse(newProperties.contains("frame.title=My JFrame"));
      assertFalse(newProperties.contains("frame.title2=My JFrame"));
      assertTrue(newProperties.contains("frame.title3=My JFrame"));
    }
    assertEditor(
        "class Test extends JFrame {",
        "  Test() {",
        "    setTitle(Messages.getString('frame.title3')); //$NON-NLS-1$",
        "    setName(Messages.getString('frame.name2')); //$NON-NLS-1$",
        "  }",
        "}");
  }

  /**
   * Test for {@link InternalizeKeyCommand}.
   */
  public void test_InternalizeKeyCommand() throws Exception {
    NlsTestUtils.create_EclipseOld_Accessor(this, false);
    setFileContentSrc(
        "test/messages.properties",
        getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
            "    setName(Messages.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    // prepare NLS
    NlsSupport support = NlsSupport.get(frame);
    EditableSupport editableSupport = (EditableSupport) support.getEditable();
    // prepare editable source
    IEditableSource editableSource;
    {
      List<IEditableSource> editableSources = editableSupport.getEditableSources();
      assertEquals(1, editableSources.size());
      editableSource = editableSources.get(0);
    }
    // check keys
    assertStringSet(editableSource.getKeys(), new String[]{"frame.title", "frame.name"});
    // internalize
    editableSource.internalizeKey("frame.title");
    editableSource.internalizeKey("frame.name");
    // apply commands
    support.applyEditable(editableSupport);
    // check
    assertStringSet(editableSource.getKeys(), new String[]{});
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertFalse(newProperties.contains("frame.name=My name"));
      assertFalse(newProperties.contains("frame.title=My JFrame"));
    }
    assertEditor(
        "class Test extends JFrame {",
        "  Test() {",
        "    setTitle('My JFrame');",
        "    setName('My name');",
        "  }",
        "}");
  }

  /**
   * Test for {@link RemoveLocaleCommand}.
   */
  public void test_RemoveLocaleCommand() throws Exception {
    NlsTestUtils.create_EclipseOld_Accessor(this, false);
    setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
            "  }",
            "}");
    // prepare NLS
    NlsSupport support = NlsSupport.get(frame);
    EditableSupport editableSupport = (EditableSupport) support.getEditable();
    // prepare editable source
    IEditableSource editableSource;
    {
      List<IEditableSource> editableSources = editableSupport.getEditableSources();
      assertEquals(1, editableSources.size());
      editableSource = editableSources.get(0);
    }
    // add locale
    LocaleInfo newLocale = new LocaleInfo(new Locale("it"));
    {
      // check locales
      assertEquals(1, editableSource.getLocales().length);
      // do add
      editableSource.addLocale(newLocale, LocaleInfo.DEFAULT);
      // check locales
      assertEquals(2, editableSource.getLocales().length);
      // no changes in file system until applyEditable()
      assertFalse(getFileSrc("/test/messages_it.properties").exists());
    }
    // update value in new locale
    editableSource.setValue(newLocale, "frame.title", "My JFrame IT");
    // remove locale
    {
      editableSource.removeLocale(editableSource.getLocales()[1]);
      assertEquals(1, editableSource.getLocales().length);
    }
    // apply commands
    support.applyEditable(editableSupport);
    // check
    assertTrue(getFileSrc("/test/messages.properties").exists());
    assertFalse(getFileSrc("/test/messages_it.properties").exists());
  }

  /**
   * Test for {@link IEditableSource#addKey(String, String)}.
   */
  public void test_addKey() throws Exception {
    m_testProject.addPlugin("org.eclipse.osgi");
    NlsTestUtils.create_EclipseModern_AccessorAndProperties();
    String[] lines =
        {
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.frame_title);",
            "  }",
            "}"};
    ContainerInfo frame = parseContainer(lines);
    String frameSource = m_lastEditor.getSource();
    // prepare NLS
    NlsSupport support = NlsSupport.get(frame);
    EditableSupport editableSupport = (EditableSupport) support.getEditable();
    // prepare editable source
    IEditableSource editableSource;
    {
      List<IEditableSource> editableSources = editableSupport.getEditableSources();
      assertEquals(1, editableSources.size());
      editableSource = editableSources.get(0);
    }
    // add key
    editableSource.addKey("newKey", "newValue");
    assertThat(editableSource.getKeys()).contains("newKey");
    {
      LocaleInfo[] locales = editableSource.getLocales();
      assertThat(locales).hasSize(2);
      assertEquals("newValue", editableSource.getValue(locales[0], "newKey"));
      assertEquals("newValue", editableSource.getValue(locales[1], "newKey"));
    }
    // apply commands
    support.applyEditable(editableSupport);
    assertEditor(frameSource, m_lastEditor);
    // checks
    {
      String accessor = getFileContentSrc("test/Messages.java");
      assertThat(accessor).contains("public static String newKey;");
    }
    {
      String newProperties = getFileContentSrc("test/messages.properties");
      assertThat(newProperties).contains("newKey=newValue");
    }
    {
      String newProperties = getFileContentSrc("test/messages_it.properties");
      assertThat(newProperties).contains("newKey=newValue");
    }
  }

  public void test_existingSource() throws Exception {
    NlsTestUtils.create_EclipseOld_Accessor(this, false);
    setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
            "    setName('Some name');",
            "    //",
            "    JButton button = new JButton('abc');",
            "    getContentPane().add(button);",
            "    //",
            "    JButton button2 = new JButton();",
            "    getContentPane().add(button2);",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    ComponentInfo button = contentPane.getChildrenComponents().get(0);
    ComponentInfo button2 = contentPane.getChildrenComponents().get(1);
    //
    NlsSupport support = NlsSupport.get(frame);
    EditableSupport editableSupport = (EditableSupport) support.getEditable();
    // check simple access
    assertSame(frame, editableSupport.getRoot());
    assertTrue(editableSupport.hasExistingSources());
    // check components
    {
      List<JavaInfo> components = editableSupport.getComponents();
      assertEquals(4, components.size());
      assertTrue(components.contains(frame));
      assertTrue(components.contains(contentPane));
      assertTrue(components.contains(button));
      assertTrue(components.contains(button2));
    }
    // check properties to externalize
    {
      List<StringPropertyInfo> properties = editableSupport.getProperties(frame);
      assertEquals(1, properties.size());
      StringPropertyInfo stringPropertyInfo = properties.get(0);
      assertEquals("name", stringPropertyInfo.getProperty().getTitle());
    }
    // hasPropertiesInTree()
    {
      assertTrue(editableSupport.hasPropertiesInTree(frame));
      assertTrue(editableSupport.hasPropertiesInTree(contentPane));
      assertTrue(editableSupport.hasPropertiesInTree(button));
      assertFalse(editableSupport.hasPropertiesInTree(button2));
    }
    // getEditableSources()
    {
      List<IEditableSource> editableSources = editableSupport.getEditableSources();
      assertEquals(1, editableSources.size());
      IEditableSource editableSource = editableSources.get(0);
      assertEquals("test.messages", editableSource.getShortTitle());
      assertInstanceOf(EclipseSource.class, editableSupport.getSource(editableSource));
    }
  }

  public void test_addSource() throws Exception {
    waitForAutoBuild();
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle('My JFrame');",
            "    //",
            "    JButton button = new JButton('abc');",
            "    getContentPane().add(button);",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    ComponentInfo button = contentPane.getChildrenComponents().get(0);
    NlsSupport support = NlsSupport.get(frame);
    //
    // STAGE #1
    //
    {
      EditableSupport editableSupport = (EditableSupport) support.getEditable();
      // add listener
      final StringBuffer buffer = new StringBuffer();
      IEditableSupportListener editableSupportListener = createEditableSupportListener(buffer);
      editableSupport.addListener(editableSupportListener);
      // addSource()
      IEditableSource editableSource;
      {
        editableSource = NlsTestUtils.createEmptyEditable("messages");
        SourceDescription sourceDescription = NlsSupport.getSourceDescriptions(frame)[0];
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
        parameters.m_withDefaultValue = true;
        // do add
        editableSupport.addSource(editableSource, sourceDescription, parameters);
        assertContains(buffer, "sourceAdded");
      }
      // no key before externalizing
      {
        assertTrue(editableSource.getKeys().isEmpty());
        assertTrue(editableSource.getFormKeys().isEmpty());
      }
      // externalize
      {
        List<StringPropertyInfo> properties = Lists.newArrayList();
        properties.addAll(editableSupport.getProperties(frame));
        properties.addAll(editableSupport.getProperties(button));
        for (StringPropertyInfo propertyInfo : properties) {
          editableSupport.externalizeProperty(propertyInfo, editableSource, true);
          assertContains(buffer, "externalizedPropertiesChanged");
        }
      }
      // check keys
      {
        assertStringSet(editableSource.getKeys(), new String[]{
            "Test.this.title",
            "Test.button.text"});
        assertStringSet(editableSource.getFormKeys(), new String[]{
            "Test.this.title",
            "Test.button.text"});
        checkComponentsMap(
            editableSource,
            new String[]{"Test.this.title", "Test.button.text"},
            new JavaInfo[][]{new JavaInfo[]{frame}, new JavaInfo[]{button}});
      }
      // check values
      {
        assertEquals("My JFrame", editableSource.getValue(LocaleInfo.DEFAULT, "Test.this.title"));
        assertEquals("abc", editableSource.getValue(LocaleInfo.DEFAULT, "Test.button.text"));
      }
      // check for value in not existing locale
      {
        String value = editableSource.getValue(new LocaleInfo(Locale.GERMAN), "Test.this.title");
        assertNull(value);
      }
      // rename key
      {
        IEditableSourceListener listener = createEditableSourceListener(buffer);
        editableSource.addListener(listener);
        // don't change key, ignored, no log expected
        editableSource.renameKey("Test.button.text", "Test.button.text");
        assertTrue(buffer.length() == 0);
        // change wrong key, ignored, no log expected
        editableSource.renameKey("no-such-key", "some-other-key");
        assertTrue(buffer.length() == 0);
        // rename key with listener
        editableSource.renameKey("Test.button.text", "Test.button.text3");
        assertContains(buffer, "keyRenamed: Test.button.text -> Test.button.text3");
        // remove listener, no log in buffer expected
        editableSource.removeListener(listener);
        editableSource.renameKey("Test.button.text3", "Test.button.text2");
        assertTrue(buffer.length() == 0);
        // check keys
        {
          assertStringSet(editableSource.getKeys(), new String[]{
              "Test.this.title",
              "Test.button.text2"});
          assertStringSet(editableSource.getFormKeys(), new String[]{
              "Test.this.title",
              "Test.button.text2"});
          checkComponentsMap(
              editableSource,
              new String[]{"Test.this.title", "Test.button.text2"},
              new JavaInfo[][]{new JavaInfo[]{frame}, new JavaInfo[]{button}});
        }
        // check values
        {
          assertEquals("My JFrame", editableSource.getValue(LocaleInfo.DEFAULT, "Test.this.title"));
          assertEquals("abc", editableSource.getValue(LocaleInfo.DEFAULT, "Test.button.text2"));
        }
      }
      // apply commands
      support.applyEditable(editableSupport);
      editableSupport.removeListener(editableSupportListener);
      // checks
      {
        {
          String newProperties = getFileContentSrc("test/messages.properties");
          assertTrue(newProperties.contains("#Eclipse messages class"));
          assertTrue(newProperties.contains("Test.this.title=My JFrame"));
          assertTrue(newProperties.contains("Test.button.text2=abc"));
        }
        assertEditor(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle(Messages.getString(\"Test.this.title\", \"My JFrame\")); //$NON-NLS-1$ //$NON-NLS-2$",
            "    //",
            "    JButton button = new JButton(Messages.getString(\"Test.button.text2\", \"abc\")); //$NON-NLS-1$ //$NON-NLS-2$",
            "    getContentPane().add(button);",
            "  }",
            "}");
      }
    }
    //
    // STAGE #2
    //
    {
      final StringBuffer buffer = new StringBuffer();
      // prepare editable support
      EditableSupport editableSupport = (EditableSupport) support.getEditable();
      editableSupport.addListener(createEditableSupportListener(buffer));
      // prepare editable source
      IEditableSource editableSource = getSingleExistingEditableSource(editableSupport, buffer);
      // internalize
      {
        assertStringSet(editableSource.getKeys(), new String[]{
            "Test.this.title",
            "Test.button.text2"});
        //
        editableSource.internalizeKey("Test.button.text2");
        assertContains(buffer, "externalizedPropertiesChanged", false);
        assertContains(buffer, "keyRemoved: Test.button.text2");
        //
        assertStringSet(editableSource.getKeys(), new String[]{"Test.this.title"});
        checkComponentsMap(
            editableSource,
            new String[]{"Test.this.title"},
            new JavaInfo[][]{new JavaInfo[]{frame}});
      }
      // apply commands
      support.applyEditable(editableSupport);
      // checks
      {
        {
          String newProperties = getFileContentSrc("test/messages.properties");
          assertTrue(newProperties.contains("Test.this.title=My JFrame"));
          assertFalse(newProperties.contains("Test.button.text2=abc"));
        }
        assertEditor(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle(Messages.getString(\"Test.this.title\", \"My JFrame\")); //$NON-NLS-1$ //$NON-NLS-2$",
            "    //",
            "    JButton button = new JButton(\"abc\");",
            "    getContentPane().add(button);",
            "  }",
            "}");
      }
    }
    frame.refresh();
    //
    // STAGE #3
    //
    {
      final StringBuffer buffer = new StringBuffer();
      // prepare editable support
      EditableSupport editableSupport = (EditableSupport) support.getEditable();
      editableSupport.addListener(createEditableSupportListener(buffer));
      // prepare editable source
      IEditableSource editableSource = getSingleExistingEditableSource(editableSupport, buffer);
      // externalize
      {
        assertStringSet(editableSource.getKeys(), new String[]{"Test.this.title"});
        //
        StringPropertyInfo property = editableSupport.getProperties(button).get(0);
        editableSource.externalize(property, true);
        assertContains(buffer, "externalizedPropertiesChanged", false);
        assertContains(buffer, "keyAdded: Test.button.text");
        //
        assertStringSet(editableSource.getKeys(), new String[]{
            "Test.this.title",
            "Test.button.text"});
        checkComponentsMap(
            editableSource,
            new String[]{"Test.this.title", "Test.button.text"},
            new JavaInfo[][]{new JavaInfo[]{frame}, new JavaInfo[]{button}});
      }
      // set "empty" value for unknown key, ignored
      {
        String key = "no-such-key";
        editableSource.setValue(LocaleInfo.DEFAULT, key, "");
        assertNull(editableSource.getValue(LocaleInfo.DEFAULT, key));
      }
      // set same value, ignored
      {
        String key = "Test.this.title";
        String oldValue = editableSource.getValue(LocaleInfo.DEFAULT, key);
        editableSource.setValue(LocaleInfo.DEFAULT, key, new String("My JFrame"));
        assertSame(oldValue, editableSource.getValue(LocaleInfo.DEFAULT, key));
      }
      // locales
      {
        {
          LocaleInfo[] locales = editableSource.getLocales();
          assertEquals(1, locales.length);
          assertEquals("(default)", locales[0].getTitle());
        }
        // add locales
        editableSource.addLocale(new LocaleInfo(new Locale("it")), LocaleInfo.DEFAULT);
        editableSource.addLocale(new LocaleInfo(new Locale("fr")), null);
        {
          LocaleInfo[] locales = editableSource.getLocales();
          assertEquals(3, locales.length);
          assertEquals("(default)", locales[0].getTitle());
          assertEquals("fr", locales[1].getTitle());
          assertEquals("it", locales[2].getTitle());
        }
      }
      // apply commands
      support.applyEditable(editableSupport);
      // checks
      {
        // *.properties: default
        {
          String newProperties = getFileContentSrc("test/messages.properties");
          assertTrue(newProperties.contains("Test.this.title=My JFrame"));
          assertTrue(newProperties.contains("Test.button.text=abc"));
        }
        // *.properties: it
        {
          String newProperties = getFileContentSrc("test/messages_it.properties");
          assertTrue(newProperties.contains("Test.this.title=My JFrame"));
          assertTrue(newProperties.contains("Test.button.text=abc"));
        }
        // *.properties: fr
        {
          String newProperties = getFileContentSrc("test/messages_fr.properties");
          assertFalse(newProperties.contains("Test.this.title=My JFrame"));
          assertFalse(newProperties.contains("Test.button.text=abc"));
        }
        assertEditor(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setTitle(Messages.getString(\"Test.this.title\", \"My JFrame\")); //$NON-NLS-1$ //$NON-NLS-2$",
            "    //",
            "    JButton button = new JButton(Messages.getString(\"Test.button.text\", \"abc\")); //$NON-NLS-1$ //$NON-NLS-2$",
            "    getContentPane().add(button);",
            "  }",
            "}");
      }
    }
    frame.refresh();
    //
    // STAGE #4
    //
    {
      final StringBuffer buffer = new StringBuffer();
      // prepare editable support
      EditableSupport editableSupport = (EditableSupport) support.getEditable();
      editableSupport.addListener(createEditableSupportListener(buffer));
      // prepare editable source
      IEditableSource editableSource = getSingleExistingEditableSource(editableSupport, buffer);
      // rename key (in 'fr' we don't have value)
      {
        editableSource.renameKey("Test.button.text", "Test.button.text3");
        assertContains(buffer, "keyRenamed: Test.button.text -> Test.button.text3");
      }
      // remove locale
      {
        {
          LocaleInfo[] locales = editableSource.getLocales();
          assertEquals(3, locales.length);
          assertEquals("fr", locales[1].toString());
          assertEquals("it", locales[2].toString());
        }
        editableSource.removeLocale(editableSource.getLocales()[1]);
        {
          LocaleInfo[] locales = editableSource.getLocales();
          assertEquals(2, locales.length);
          assertEquals("(default)", locales[0].getTitle());
          assertEquals("it", locales[1].getTitle());
        }
      }
      // apply commands
      support.applyEditable(editableSupport);
      // checks
      {
        assertTrue(getFileSrc("test", "messages.properties").exists());
        assertTrue(getFileSrc("test", "messages_it.properties").exists());
        // *.properties: fr - should be removed
        assertFalse(getFileSrc("test", "messages_fr.properties").exists());
      }
    }
    frame.refresh();
    // clean up
    frame.refresh_dispose();
  }

  /**
   * @return the single {@link IEditableSource} from given {@link EditableSupport}.
   */
  private static IEditableSource getSingleExistingEditableSource(EditableSupport editableSupport,
      StringBuffer buffer) {
    List<IEditableSource> editableSources = editableSupport.getEditableSources();
    assertEquals(1, editableSources.size());
    IEditableSource editableSource = editableSources.get(0);
    // add listener
    editableSource.addListener(createEditableSourceListener(buffer));
    return editableSource;
  }

  public void test_EclipseSource_defaultValue() throws Exception {
    NlsTestUtils.create_EclipseOld_Accessor(this, true);
    setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
    setFileContentSrc("test/messages_it.properties", getSourceDQ("frame.title=My JFrame IT"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title', 'My JFrame')); //$NON-NLS-1$ //$NON-NLS-2$",
            "    setName('Some name');",
            "  }",
            "}");
    AbstractSource.setLocaleInfo(frame, LocaleInfo.DEFAULT);
    //
    NlsSupport support = NlsSupport.get(frame);
    AbstractSource source = support.getSources()[0];
    GenericProperty titleProperty = (GenericProperty) frame.getPropertyByTitle("title");
    //
    frame.refresh();
    try {
      assertEquals("My JFrame", ((JFrame) frame.getObject()).getTitle());
      //
      source.setValue(titleProperty.getExpression(), "qwerty");
      assertEditor(
          "class Test extends JFrame {",
          "  Test() {",
          "    setTitle(Messages.getString(\"frame.title\", \"qwerty\")); //$NON-NLS-1$ //$NON-NLS-2$",
          "    setName(\"Some name\");",
          "  }",
          "}");
      // *.properties: default
      {
        String newProperties = getFileContentSrc("test/messages.properties");
        assertTrue(newProperties.contains("frame.title=qwerty"));
      }
    } finally {
      frame.refresh_dispose();
    }
  }

  /**
   * Default value can not be changed when use {@link IEditableSupport}.
   */
  public void test_EclipseSource_defaultValue2() throws Exception {
    NlsTestUtils.create_EclipseOld_Accessor(this, true);
    setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
    setFileContentSrc("test/messages_it.properties", getSourceDQ("frame.title=My JFrame IT"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title', 'My JFrame')); //$NON-NLS-1$ //$NON-NLS-2$",
            "    setName('Some name');",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    IEditableSource editableSource = editableSupport.getEditableSources().get(0);
    //
    frame.refresh();
    try {
      assertEquals("My JFrame", ((JFrame) frame.getObject()).getTitle());
      editableSource.setValue(LocaleInfo.DEFAULT, "frame.title", "qwerty");
      support.applyEditable(editableSupport);
      //
      assertEditor(
          "class Test extends JFrame {",
          "  Test() {",
          "    setTitle(Messages.getString(\"frame.title\", \"My JFrame\")); //$NON-NLS-1$ //$NON-NLS-2$",
          "    setName(\"Some name\");",
          "  }",
          "}");
      // *.properties: default
      {
        String newProperties = getFileContentSrc("test/messages.properties");
        assertTrue(newProperties.contains("frame.title=qwerty"));
      }
    } finally {
      frame.refresh_dispose();
    }
  }

  /**
   * Test for attaching possible source and for generating unique keys.
   */
  public void test_externalize_uniqueKeys() throws Exception {
    NlsTestUtils.create_EclipseOld_Accessor(this, false);
    setFileContentSrc(
        "test/messages.properties",
        getSourceDQ("#Some comment", "not-a-form-key=value"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle('My JFrame');",
            "    {",
            "      JButton button = new JButton('111');",
            "      getContentPane().add(button);",
            "    }",
            "    {",
            "      JButton button = new JButton('222');",
            "      getContentPane().add(button);",
            "    }",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    ComponentInfo button1 = contentPane.getChildrenComponents().get(0);
    ComponentInfo button2 = contentPane.getChildrenComponents().get(1);
    //
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    IEditableSource editableSource = editableSupport.getEditableSources().get(0);
    // check for "not a form key"
    {
      assertStringSet(editableSource.getKeys(), new String[]{"not-a-form-key"});
      assertStringSet(editableSource.getFormKeys(), new String[]{});
      assertSame(Collections.EMPTY_SET, editableSource.getComponentsByKey("not-a-form-key"));
    }
    // externalize (with same base key, but unique should be generated)
    {
      StringPropertyInfo propertyInfo;
      //
      propertyInfo = editableSupport.getProperties(button1).get(0);
      editableSupport.externalizeProperty(propertyInfo, editableSource, true);
      //
      propertyInfo = editableSupport.getProperties(button2).get(0);
      editableSupport.externalizeProperty(propertyInfo, editableSource, true);
    }
    support.applyEditable(editableSupport);
    // check
    {
      assertEditor(
          "class Test extends JFrame {",
          "  Test() {",
          "    setTitle(\"My JFrame\");",
          "    {",
          "      JButton button = new JButton(Messages.getString(\"Test.button.text\")); //$NON-NLS-1$",
          "      getContentPane().add(button);",
          "    }",
          "    {",
          "      JButton button = new JButton(Messages.getString(\"Test.button.text_1\")); //$NON-NLS-1$",
          "      getContentPane().add(button);",
          "    }",
          "  }",
          "}");
      // *.properties: default
      {
        String newProperties = getFileContentSrc("test/messages.properties");
        assertTrue(newProperties.contains("Test.button.text=111"));
        assertTrue(newProperties.contains("Test.button.text_1=222"));
      }
    }
  }

  public void test_StringPropertyInfo() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle('My JFrame');",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    //
    List<StringPropertyInfo> properties = editableSupport.getProperties(frame);
    assertEquals(1, properties.size());
    StringPropertyInfo propertyInfo = properties.get(0);
    //
    assertSame(frame, propertyInfo.getComponent());
    assertSame(frame.getPropertyByTitle("title"), propertyInfo.getProperty());
    assertEquals("My JFrame", propertyInfo.getValue());
    assertEquals("title: My JFrame", propertyInfo.getTitle());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // renameKey() conflict
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_renameConflict_cancel() throws Exception {
    NlsTestUtils.create_EclipseOld_Accessor(this, false);
    setFileContentSrc(
        "test/messages.properties",
        getSourceDQ("frame.title=title", "frame.name=name"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
            "    setName(Messages.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    final IEditableSource editableSource = editableSupport.getEditableSources().get(0);
    // initial check
    {
      assertStringSet(editableSource.getKeys(), new String[]{"frame.title", "frame.name"});
      assertEquals("title", editableSource.getValue(LocaleInfo.DEFAULT, "frame.title"));
      assertEquals("name", editableSource.getValue(LocaleInfo.DEFAULT, "frame.name"));
      checkComponentsMap(
          editableSource,
          new String[]{"frame.title", "frame.name"},
          new JavaInfo[][]{new JavaInfo[]{frame}, new JavaInfo[]{frame}});
    }
    // dispose shell, so cancel dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        editableSource.renameKey("frame.name", "frame.title");
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        Shell shell = context.useShell("Confirm");
        shell.notifyListeners(SWT.Close, new Event());
      }
    });
    // no changes expected
    {
      assertStringSet(editableSource.getKeys(), new String[]{"frame.title", "frame.name"});
      assertEquals("title", editableSource.getValue(LocaleInfo.DEFAULT, "frame.title"));
      assertEquals("name", editableSource.getValue(LocaleInfo.DEFAULT, "frame.name"));
      checkComponentsMap(
          editableSource,
          new String[]{"frame.title", "frame.name"},
          new JavaInfo[][]{new JavaInfo[]{frame}, new JavaInfo[]{frame}});
    }
  }

  public void test_renameConflict_keep() throws Exception {
    NlsTestUtils.create_EclipseOld_Accessor(this, false);
    setFileContentSrc(
        "test/messages.properties",
        getSourceDQ("frame.title=title", "frame.name=name"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
            "    setName(Messages.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    final IEditableSource editableSource = editableSupport.getEditableSources().get(0);
    // yes, keep existing value
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        editableSource.renameKey("frame.name", "frame.title");
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.clickButton("Yes, keep existing value");
      }
    });
    {
      assertStringSet(editableSource.getKeys(), new String[]{"frame.title"});
      assertEquals("title", editableSource.getValue(LocaleInfo.DEFAULT, "frame.title"));
      checkComponentsMap(
          editableSource,
          new String[]{"frame.title"},
          new JavaInfo[][]{new JavaInfo[]{frame}});
    }
  }

  public void test_renameConflict_useSourceValue() throws Exception {
    NlsTestUtils.create_EclipseOld_Accessor(this, false);
    setFileContentSrc(
        "test/messages.properties",
        getSourceDQ("frame.title=title", "frame.name=name"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    setTitle(Messages.getString('frame.title')); //$NON-NLS-1$",
            "    setName(Messages.getString('frame.name')); //$NON-NLS-1$",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    final IEditableSource editableSource = editableSupport.getEditableSources().get(0);
    // no, use value of renaming key
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        editableSource.renameKey("frame.name", "frame.title");
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.clickButton("No, use value of renaming key");
      }
    });
    {
      assertStringSet(editableSource.getKeys(), new String[]{"frame.title"});
      assertEquals("name", editableSource.getValue(LocaleInfo.DEFAULT, "frame.title"));
      checkComponentsMap(
          editableSource,
          new String[]{"frame.title"},
          new JavaInfo[][]{new JavaInfo[]{frame}});
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Logging
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the implementation of {@link IEditableSupportListener} that logs events into given
   *         {@link StringBuffer}.
   */
  private static IEditableSupportListener createEditableSupportListener(final StringBuffer buffer) {
    return new IEditableSupportListener() {
      @Override
      public void sourceAdded(IEditableSource source) {
        buffer.append("sourceAdded: " + source.getLongTitle() + "\n");
      }

      @Override
      public void externalizedPropertiesChanged() {
        buffer.append("externalizedPropertiesChanged\n");
      }
    };
  }

  /**
   * @return the implementation of {@link IEditableSourceListener} that logs events into given
   *         {@link StringBuffer}.
   */
  private static IEditableSourceListener createEditableSourceListener(final StringBuffer buffer) {
    return new IEditableSourceListener() {
      public void keyAdded(String key, Object o) {
        buffer.append("keyAdded: " + key + "\n");
      }

      public void keyRemoved(String key) {
        buffer.append("keyRemoved: " + key + "\n");
      }

      public void keyRenamed(String oldKey, String newKey) {
        buffer.append("keyRenamed: " + oldKey + " -> " + newKey + "\n");
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts the given {@link Set} contains only given {@link String}'s.
   */
  private void assertStringSet(Set<String> set, String[] expectedValues) {
    assertEquals(expectedValues.length, set.size());
    for (String expectedValue : expectedValues) {
      assertTrue(expectedValue + " not found in " + set, set.contains(expectedValue));
    }
  }

  /**
   * Asserts that given {@link IEditableSource} has expected keys -> components map.
   */
  private void checkComponentsMap(IEditableSource editableSource,
      String[] keys,
      JavaInfo[][] components) {
    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];
      JavaInfo[] expectedComponents = components[i];
      Set<JavaInfo> componentsByKey = editableSource.getComponentsByKey(key);
      assertEquals(expectedComponents.length, componentsByKey.size());
      for (int j = 0; j < expectedComponents.length; j++) {
        JavaInfo expectedComponent = expectedComponents[j];
        assertTrue(componentsByKey.contains(expectedComponent));
      }
    }
  }

  /**
   * Asserts that given {@link StringBuilder} contains required string.<br>
   * Clears {@link StringBuffer}.
   */
  private void assertContains(StringBuffer buffer, String s) {
    assertContains(buffer, s, true);
  }

  /**
   * Asserts that given {@link StringBuilder} contains required string.<br>
   * Clears {@link StringBuffer}.
   */
  private void assertContains(StringBuffer buffer, String s, boolean clear) {
    if (buffer.indexOf(s) == -1) {
      fail("|" + s + "| expected, but |" + buffer.toString() + "| found.");
    }
    if (clear) {
      buffer.setLength(0);
    }
  }
}
