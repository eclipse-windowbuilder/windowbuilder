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
package org.eclipse.wb.tests.designer.core.palette;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.IPaletteSite;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.palette.PaletteManager;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ComponentPresentation;
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentPresentationHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.ImageUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JPanelInfo;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.core.TestBundle;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Tests for {@link ComponentEntryInfo}.
 *
 * @author scheglov_ke
 */
public class ComponentEntryInfoTest extends AbstractPaletteTest {
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
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Check for {@link ComponentEntryInfo} access methods.
   */
  public void test_access() throws Exception {
    ComponentEntryInfo componentEntry = new ComponentEntryInfo();
    componentEntry.setComponentClassName("javax.swing.JPanel");
    // toString()
    assertEquals("Component(class='javax.swing.JPanel')", componentEntry.toString());
    // get/setComponentClassName
    assertEquals("javax.swing.JPanel", componentEntry.getClassName());
    componentEntry.setComponentClassName("javax.swing.JButton");
    assertEquals("javax.swing.JButton", componentEntry.getClassName());
    // get/setCreationId
    assertNull(componentEntry.getCreationId());
    componentEntry.setCreationId("my creation id");
    assertEquals("my creation id", componentEntry.getCreationId());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Only "class" specified in extension, so other things are deduced.
   */
  public void test_parse_onlyClass() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='javax.swing.JPanel'/>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare category/entry
    CategoryInfo category = palette.getCategory("category_1");
    ComponentEntryInfo entry = (ComponentEntryInfo) category.getEntries().get(0);
    // check entry
    {
      assertSame(category, entry.getCategory());
      assertEquals("category_1 javax.swing.JPanel", entry.getId());
      assertEquals("javax.swing.JPanel", entry.getClassName());
      assertNull(entry.getCreationId());
      assertNull(entry.getDescription());
      // before initialize
      assertEquals("javax.swing.JPanel", entry.getName());
      assertSame(ComponentEntryInfo.DEFAULT_ICON, entry.getIcon());
      // initialize and check rest values
      assertTrue(entry.initialize(null, m_lastParseInfo));
      assertEquals("JPanel", entry.getName());
      assertNotNull(entry.getIcon());
      // toString()
      assertEquals("Component(class='javax.swing.JPanel')", entry.toString());
    }
  }

  /**
   * "id", "name", "description" and "icon" specified in extension.
   */
  public void test_parse_valuesFromExtension() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='javax.swing.JPanel' creationId='creation id' id='my id' name='my name' description='my description' icon='icons/true.gif' visible='false'/>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare category/entry
    CategoryInfo category = palette.getCategory("category_1");
    ComponentEntryInfo entry = (ComponentEntryInfo) category.getEntries().get(0);
    // check entry
    assertSame(category, entry.getCategory());
    assertEquals("my id", entry.getId());
    assertEquals("javax.swing.JPanel", entry.getClassName());
    assertEquals("creation id", entry.getCreationId());
    assertEquals("my description", entry.getDescription());
    assertEquals("my name", entry.getName());
    // we have icon in palette entry, so it is not "null"
    {
      Image icon = entry.getIcon();
      assertNotNull(icon);
      assertEquals(16, icon.getBounds().width);
      assertEquals(16, icon.getBounds().height);
    }
    assertFalse(entry.isVisible());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Description attribute
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If "description" text is empty, then description from {@link ComponentDescription} should be
   * used.
   */
  public void test_parse_descriptionText_emptyString() throws Exception {
    assertDescriptionText_fromComponentDescription("");
  }

  /**
   * If "description" text is empty, then description from {@link ComponentDescription} should be
   * used.
   */
  public void test_parse_descriptionText_spacesString() throws Exception {
    assertDescriptionText_fromComponentDescription(" \t");
  }

  /**
   * If "description" text is exactly name of class (we generate such description when user adds
   * component using UI), then description from {@link ComponentDescription} should be used.
   */
  public void test_parse_descriptionText_className() throws Exception {
    assertDescriptionText_fromComponentDescription("javax.swing.JPanel");
  }

  private void assertDescriptionText_fromComponentDescription(String descriptionAttribute)
      throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='javax.swing.JPanel' description='" + descriptionAttribute + "'/>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare category/entry
    CategoryInfo category = palette.getCategory("category_1");
    ComponentEntryInfo entry = (ComponentEntryInfo) category.getEntries().get(0);
    // initialize and check values
    assertTrue(entry.initialize(null, m_lastParseInfo));
    assertEquals("A generic lightweight container.", entry.getDescription());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Standard {@link JPanel}, description and icon from {@link ComponentDescription}.
   */
  public void test_initialize_1_allDefaults() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='javax.swing.JPanel'/>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare component
    CategoryInfo category = palette.getCategory("category_1");
    ComponentEntryInfo entry = (ComponentEntryInfo) category.getEntries().get(0);
    assertNull(entry.getDescription());
    // do initialize
    assertTrue(entry.initialize(null, m_lastParseInfo));
    // check that after initialize description/icon from CreationDescription is provided
    {
      ComponentDescription componentDescription =
          ComponentDescriptionHelper.getDescription(m_lastEditor, JPanel.class);
      CreationDescription creation = componentDescription.getCreation(null);
      assertEquals(creation.getDescription(), entry.getDescription());
      assertTrue("Same icons.", UiUtils.equals(creation.getIcon(), entry.getIcon()));
    }
  }

  /**
   * Bad component class, failed to initialize.
   */
  public void test_initialize_2_badComponentClass() throws Exception {
    JavaInfo panel = parseEmptyPanel();
    assertEquals(0, m_lastState.getWarnings().size());
    //
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='no.such.Component'/>",
        "</category>"});
    PaletteInfo palette = loadPalette(panel);
    // prepare component
    CategoryInfo category = palette.getCategory("category_1");
    ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
    // no presentation
    assertEquals("no.such.Component", componentEntry.getName());
    assertSame(ComponentEntryInfo.DEFAULT_ICON, componentEntry.getIcon());
    // do initialize, failed
    assertFalse(componentEntry.initialize(null, panel));
    // no warnings
    assertThat(m_lastState.getWarnings()).isEmpty();
  }

  /**
   * Bad component description, failed to initialize.
   */
  public void test_initialize_2_badComponentDescription() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel(boolean value) {",
            "  }",
            "}"));
    setFileContentSrc("test/MyPanel.wbp-component.xml", getSourceDQ("something bad"));
    waitForAutoBuild();
    waitEventLoop(10);
    waitForAutoBuild();
    // parse
    JavaInfo panel =
        parseSource(
            "test",
            "Test.java",
            getTestSource(
                "// filler filler filler filler filler",
                "// filler filler filler filler filler",
                "public class Test extends JPanel {",
                "  public Test() {",
                "  }",
                "}"));
    assertEquals(0, m_lastState.getWarnings().size());
    //
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='test.MyPanel'/>",
        "</category>"});
    PaletteInfo palette = loadPalette(panel);
    // prepare component
    CategoryInfo category = palette.getCategory("category_1");
    ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
    // do initialize, we have warning, because we still load description (as slow) and it is invalid
    assertFalse(componentEntry.initialize(null, panel));
    assertThat(m_lastState.getWarnings()).hasSize(1);
  }

  /**
   * Object without description.
   */
  public void test_initialize_noDescription() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='java.lang.String'/>",
        "</category>"});
    JavaInfo panel = parseEmptyPanel();
    PaletteInfo palette = loadPalette(panel);
    // prepare component
    CategoryInfo category = palette.getCategory("category_1");
    ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
    assertNull(componentEntry.getDescription());
    // do initialize
    assertTrue(componentEntry.initialize(null, panel));
    // when no "real" description, component class name is used
    assertEquals("java.lang.String", componentEntry.getDescription());
  }

  /**
   * Several "creation" sections.
   */
  public void test_initialize_severalCreations() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel(boolean value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation id='true'>",
            "    <source><![CDATA[new test.MyPanel(true)]]></source>",
            "  </creation>",
            "  <creation id='false'>",
            "    <source><![CDATA[new test.MyPanel(false)]]></source>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // parse
    JavaInfo panel =
        parseSource(
            "test",
            "Test2.java",
            getTestSource(
                "// filler filler filler filler filler",
                "// filler filler filler filler filler",
                "public class Test2 extends JPanel {",
                "  public Test2() {",
                "  }",
                "}"));
    PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
    // load palette
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='test.MyPanel' creationId='false'/>",
        "</category>"});
    manager.reloadPalette();
    PaletteInfo palette = manager.getPalette();
    // prepare component
    CategoryInfo category = palette.getCategory("category_1");
    ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
    // check component
    assertTrue(componentEntry.initialize(null, panel));
    assertTrue(componentEntry.isEnabled());
    assertEquals("test.MyPanel", componentEntry.getClassName());
    assertEquals("category_1 test.MyPanel false", componentEntry.getId());
    // check tool
    {
      CreationTool creationTool = (CreationTool) componentEntry.createTool();
      ICreationFactory creationFactory = creationTool.getFactory();
      creationFactory.activate();
      // check new object
      JavaInfo javaInfo = (JavaInfo) creationFactory.getNewObject();
      assertEquals("new test.MyPanel(false)", javaInfo.getCreationSupport().add_getSource(null));
    }
  }

  /**
   * No "creation" with requested "id", and also no "description", so no "fast" presentation.
   */
  public void test_initialize_noSuchCreation() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    JavaInfo panel = parseEmptyPanel();
    PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
    // load palette
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='test.MyPanel' creationId='noSuchCreation'/>",
        "</category>"});
    manager.reloadPalette();
    PaletteInfo palette = manager.getPalette();
    // prepare component
    CategoryInfo category = palette.getCategory("category_1");
    ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
    // check component
    assertThat(m_lastState.getWarnings()).hasSize(0);
    assertFalse(componentEntry.initialize(null, panel));
    assertThat(m_lastState.getWarnings()).hasSize(1);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Enabled
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that "enabled" script can be used to disable.
   */
  public void test_isEnabled_enabledScript() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='javax.swing.JPanel' name='my name' enabled='1 == 2'/>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare category/entry
    CategoryInfo category = palette.getCategory("category_1");
    ComponentEntryInfo entry = (ComponentEntryInfo) category.getEntries().get(0);
    // check entry
    assertSame(category, entry.getCategory());
    assertEquals("javax.swing.JPanel", entry.getClassName());
    assertEquals("my name", entry.getName());
    // initialize
    assertTrue(entry.initialize(null, m_lastParseInfo));
    // disabled
    assertFalse(entry.isEnabled());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tool
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_createTool() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='javax.swing.JPanel'/>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare component
    CategoryInfo category = palette.getCategory("category_1");
    ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
    // do initialize
    assertTrue(componentEntry.initialize(null, m_lastParseInfo));
    // check tool
    CreationTool creationTool = (CreationTool) componentEntry.createTool();
    ICreationFactory creationFactory = creationTool.getFactory();
    creationFactory.activate();
    // check new object
    JavaInfo javaInfo = (JavaInfo) creationFactory.getNewObject();
    assertInstanceOf(JPanelInfo.class, javaInfo);
    assertInstanceOf(ConstructorCreationSupport.class, javaInfo.getCreationSupport());
    assertSame(Boolean.TRUE, javaInfo.getArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT));
    // activate again, new object should be created
    {
      creationFactory.activate();
      assertNotSame(javaInfo, creationFactory.getNewObject());
    }
  }

  /**
   * We should not allow to drop abstract components.
   */
  public void test_createTool_abstractClass() throws Exception {
    setFileContentSrc(
        "test/MyAbstractButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public abstract class MyAbstractButton extends JButton {",
            "}"));
    waitForAutoBuild();
    // prepare palette
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='test.MyAbstractButton'/>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare component
    final CategoryInfo category = palette.getCategory("category_1");
    final ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
    // set palette site
    IPaletteSite.Helper.setSite(m_lastParseInfo, new IPaletteSite.Empty() {
      @Override
      public Shell getShell() {
        return DesignerPlugin.getShell();
      }
    });
    // do initialize
    assertTrue(componentEntry.initialize(null, m_lastParseInfo));
    // create tool
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        CreationTool creationTool = (CreationTool) componentEntry.createTool();
        assertNull(creationTool);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Error");
        context.clickButton("OK");
      }
    });
  }

  public void test_createTool_abstractClass_ignored() throws Exception {
    setFileContentSrc(
        "test/MyAbstractButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public abstract class MyAbstractButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyAbstractButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyAbstractButton(){}]]></source>",
            "    <tag name='createAnonymous' value='true'/>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // prepare palette
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='test.MyAbstractButton'/>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare component
    final CategoryInfo category = palette.getCategory("category_1");
    final ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
    // set palette site
    IPaletteSite.Helper.setSite(m_lastParseInfo, new IPaletteSite.Empty() {
      @Override
      public Shell getShell() {
        return DesignerPlugin.getShell();
      }
    });
    // do initialize
    assertTrue(componentEntry.initialize(null, m_lastParseInfo));
    // create tool
    CreationTool creationTool = (CreationTool) componentEntry.createTool();
    assertNotNull(creationTool);
  }

  /**
   * Simulate case when component was initialized, but at loading {@link ComponentDescription} some
   * problem happened.
   */
  public void test_createTool_badClass() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component class='no.such.Class'/>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare component
    CategoryInfo category = palette.getCategory("category_1");
    ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
    // do initialize
    m_lastParseInfo.putArbitraryValue(ComponentEntryInfo.KEY_SIMULATE_PRESENTATION, true);
    assertTrue(componentEntry.initialize(null, m_lastParseInfo));
    // create tool
    assertThat(m_lastState.getWarnings()).hasSize(0);
    CreationTool creationTool = (CreationTool) componentEntry.createTool();
    assertNull(creationTool);
    assertThat(m_lastState.getWarnings()).hasSize(1);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Library
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When component has "library" tag, this library should be added to classpath.
   */
  @DisposeProjectAfter
  public void test_createTool_withLibrary() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      String className = ClassForBundle.class.getName();
      testBundle.addJar("myClasses.jar").addClass(ClassForBundle.class).close();
      testBundle.addJar("myClasses.zip").add(className.replace('.', '/') + ".java", "src").close();
      testBundle.addExtension("org.eclipse.wb.core.toolkits", new String[]{
          "<toolkit id='org.eclipse.wb.swing'>",
          "  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses.jar'/>",
          "</toolkit>"});
      testBundle.install();
      try {
        addPaletteExtension(new String[]{
            "<category id='category_1' name='category 1'>",
            "  <component class='" + className + "'>",
            "    <library type='"
                + className
                + "'"
                + " bundle='"
                + testBundle.getId()
                + "'"
                + " jar='myClasses.jar' src='myClasses.zip'/>",
            "  </component>",
            "</category>"});
        ContainerInfo panel =
            parseContainer(
                "// filler filler filler",
                "public class Test extends JPanel {",
                "  public Test() {",
                "  }",
                "}");
        PaletteInfo palette = loadPalette(panel);
        // initially to layout type in project
        assertNull(m_javaProject.findType(className));
        // prepare component
        CategoryInfo category = palette.getCategory("category_1");
        ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
        // do initialize
        assertTrue(componentEntry.initialize(null, panel));
        // create tool
        ICreationFactory creationFactory;
        {
          CreationTool creationTool = (CreationTool) componentEntry.createTool();
          creationFactory = creationTool.getFactory();
          creationFactory.activate();
        }
        // check new object
        ComponentInfo newComponent = (ComponentInfo) creationFactory.getNewObject();
        assertEquals(className, newComponent.getDescription().getComponentClass().getName());
        // now ClassForBundle type should be in project
        assertNotNull(m_javaProject.findType(className));
      } finally {
        testBundle.uninstall();
        waitEventLoop(0);
      }
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Two "library" tags.
   */
  @DisposeProjectAfter
  public void test_createTool_withLibrary2() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      String className = ClassForBundle.class.getName();
      String className2 = ClassForBundle2.class.getName();
      testBundle.addJar("myClasses.jar").addClass(ClassForBundle.class).close();
      testBundle.addJar("myClasses.zip").add(className.replace('.', '/') + ".java", "src").close();
      testBundle.addJar("myClasses2.jar").addClass(ClassForBundle2.class).close();
      testBundle.addJar("myClasses2.zip").add(className2.replace('.', '/') + ".java", "src").close();
      testBundle.addExtension("org.eclipse.wb.core.toolkits", new String[]{
          "<toolkit id='org.eclipse.wb.swing'>",
          "  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses.jar'/>",
          "  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses2.jar'/>",
          "</toolkit>"});
      testBundle.install();
      try {
        addPaletteExtension(new String[]{
            "<category id='category_1' name='category 1'>",
            "  <component class='" + className2 + "'>",
            "    <library type='"
                + className
                + "'"
                + " bundle='"
                + testBundle.getId()
                + "'"
                + " jar='myClasses.jar' src='myClasses.zip'/>",
            "    <library type='"
                + className2
                + "'"
                + " bundle='"
                + testBundle.getId()
                + "'"
                + " jar='myClasses2.jar' src='myClasses2.zip'/>",
            "  </component>",
            "</category>"});
        ContainerInfo panel =
            parseContainer(
                "// filler filler filler",
                "public class Test extends JPanel {",
                "  public Test() {",
                "  }",
                "}");
        PaletteInfo palette = loadPalette(panel);
        // initially to layout type in project
        assertNull(m_javaProject.findType(className));
        // prepare component
        CategoryInfo category = palette.getCategory("category_1");
        ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
        // do initialize
        assertTrue(componentEntry.initialize(null, panel));
        // create tool
        ICreationFactory creationFactory;
        {
          CreationTool creationTool = (CreationTool) componentEntry.createTool();
          creationFactory = creationTool.getFactory();
          creationFactory.activate();
        }
        // check new object
        ComponentInfo newComponent = (ComponentInfo) creationFactory.getNewObject();
        assertEquals(className2, newComponent.getDescription().getComponentClass().getName());
        // now ClassForBundle/ClassForBundle2 types should be in project
        assertNotNull(m_javaProject.findType(className2));
        assertNotNull(m_javaProject.findType(className));
      } finally {
        testBundle.uninstall();
        waitEventLoop(0);
      }
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * When component has "library" tag, this library should be added to classpath.
   * <p>
   * Project is also PDE project, so manifest also should be updated.
   */
  @DisposeProjectAfter
  public void test_createTool_withLibrary_whenPDE() throws Exception {
    PdeProjectConversionUtils.convertToPDE(m_project, null);
    TestBundle testBundle = new TestBundle();
    try {
      String className = ClassForBundle.class.getName();
      testBundle.addJar("myClasses.jar").addClass(ClassForBundle.class).close();
      testBundle.addJar("myClasses.zip").add(className.replace('.', '/') + ".java", "src").close();
      testBundle.addExtension("org.eclipse.wb.core.toolkits", new String[]{
          "<toolkit id='org.eclipse.wb.swing'>",
          "  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses.jar'/>",
          "</toolkit>"});
      testBundle.install();
      {
        addPaletteExtension(new String[]{
            "<category id='category_1' name='category 1'>",
            "  <component class='" + className + "'>",
            "    <library type='"
                + className
                + "'"
                + " bundle='"
                + testBundle.getId()
                + "'"
                + " jar='myClasses.jar' src='myClasses.zip'/>",
            "  </component>",
            "</category>"});
        PaletteInfo palette = loadPalette();
        // initially to layout type in project
        assertNull(m_javaProject.findType(className));
        // prepare component
        CategoryInfo category = palette.getCategory("category_1");
        ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
        // do initialize
        assertTrue(componentEntry.initialize(null, m_lastParseInfo));
        // create tool
        componentEntry.createTool();
        // now ClassForBundle type should be in project
        assertNotNull(m_javaProject.findType(className));
        // PDE manifest also updated
        {
          String manifest = getFileContent("META-INF/MANIFEST.MF");
          assertThat(manifest).contains("Bundle-ClassPath: .,\n myClasses.jar");
        }
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // hasClass()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No special "component" rule.
   */
  public void test_hasClass_noSpecialRule() throws Exception {
    // prepare MyComponent
    prepareMyComponent();
    // load palette
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component id='t' class='test.MyComponent'/>",
        "</category>"});
    ComponentEntryInfo entry = loadSingleComponent("t");
    assertHasClass(entry, true);
  }

  /**
   * If component is in known package of some standard (or supported) library, we can avoid loading
   * all classes and just check that this library exists, by loading only one "witness" class.
   * <p>
   * Here "witness" class exists, so we don't even check class itself.
   */
  public void test_hasClass_useWitness_true() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.addExtension(
          "org.eclipse.wb.core.paletteComponentExists",
          new String[]{"<component package='test.' witness='test2.Witness'/>"});
      testBundle.install(true);
      // prepare Witness
      setFileContentSrc(
          "test2/Witness.java",
          getSource(
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "package test2;",
              "public class Witness {",
              "}"));
      waitForAutoBuild();
      // load palette
      addPaletteExtension(new String[]{
          "<category id='category_1' name='category 1'>",
          "  <component id='t' class='test.NoSuchComponent'/>",
          "</category>"});
      ComponentEntryInfo entry = loadSingleComponent("t");
      assertHasClass(entry, true);
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * If component is in known package of some standard (or supported) library, we can avoid loading
   * all classes and just check that this library exists, by loading only one "witness" class.
   * <p>
   * Here "witness" class does not exist, so no matter even if class itself exists.
   */
  public void test_hasClass_useWitness_false() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.addExtension(
          "org.eclipse.wb.core.paletteComponentExists",
          new String[]{"<component package='test.' witness='test2.Witness'/>"});
      testBundle.install(true);
      // prepare MyComponent
      prepareMyComponent();
      // load palette
      addPaletteExtension(new String[]{
          "<category id='category_1' name='category 1'>",
          "  <component id='t' class='test.MyComponent'/>",
          "</category>"});
      ComponentEntryInfo entry = loadSingleComponent("t");
      assertHasClass(entry, false);
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Sometimes we always mix {@link ClassLoader} with class into editor {@link ClassLoader}, even
   * {@link IJavaProject} if does not include it. So, we should check {@link IType} existence in
   * {@link IJavaProject}.
   * <p>
   * Here {@link Class} exists, but we remove corresponding Java file (without building project), so
   * {@link IJavaProject} sees that {@link IType} does not exist.
   */
  public void test_hasClass_hasType_false() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.addExtension(
          "org.eclipse.wb.core.paletteComponentExists",
          new String[]{"<component package='test.' hasType='true'/>"});
      testBundle.install(true);
      // prepare MyComponent
      prepareMyComponent();
      // has MyComponent.class, but we delete Java source, so no IType
      assertTrue(ProjectUtils.hasType(m_javaProject, "test.MyComponent"));
      assertTrue(getFile("bin/test/MyComponent.class").exists());
      getFileSrc("test/MyComponent.java").delete(true, null);
      assertFalse(ProjectUtils.hasType(m_javaProject, "test.MyComponent"));
      // load palette
      addPaletteExtension(new String[]{
          "<category id='category_1' name='category 1'>",
          "  <component id='t' class='test.MyComponent'/>",
          "</category>"});
      ComponentEntryInfo entry = loadSingleComponent("t");
      assertHasClass(entry, false);
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Sometimes we always mix {@link ClassLoader} with class into editor {@link ClassLoader}, even
   * {@link IJavaProject} if does not include it. So, we should check {@link IType} existence in
   * {@link IJavaProject}.
   * <p>
   * Both {@link Class} and {@link IType} exist.
   */
  public void test_hasClass_hasType_true() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.addExtension(
          "org.eclipse.wb.core.paletteComponentExists",
          new String[]{"<component package='test.' hasType='true'/>"});
      testBundle.install(true);
      // prepare MyComponent
      prepareMyComponent();
      assertTrue(ProjectUtils.hasType(m_javaProject, "test.MyComponent"));
      // load palette
      addPaletteExtension(new String[]{
          "<category id='category_1' name='category 1'>",
          "  <component id='t' class='test.MyComponent'/>",
          "</category>"});
      ComponentEntryInfo entry = loadSingleComponent("t");
      assertHasClass(entry, true);
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Sometimes we always mix {@link ClassLoader} with class into editor {@link ClassLoader} and
   * automatically add corresponding jar file into {@link IJavaProject} classpath. So, this
   * component can be considered as always accessible.
   */
  public void test_hasClass_always() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.addExtension(
          "org.eclipse.wb.core.paletteComponentExists",
          new String[]{"<component package='test.' always='true'/>"});
      testBundle.install(true);
      // no MyComponent
      assertFalse(ProjectUtils.hasType(m_javaProject, "test.MyComponent"));
      // load palette
      addPaletteExtension(new String[]{
          "<category id='category_1' name='category 1'>",
          "  <component id='t' class='test.MyComponent'/>",
          "</category>"});
      ComponentEntryInfo entry = loadSingleComponent("t");
      assertHasClass(entry, true);
    } finally {
      testBundle.dispose();
    }
  }

  private void assertHasClass(ComponentEntryInfo entry, boolean expected) throws Exception {
    // initialize, without check
    entry.initialize(null, m_lastParseInfo);
    // but hasClass() is OK
    assertEquals(expected, ReflectionUtils.invokeMethod(entry, "hasClass()"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // pre-loading palette
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for loading palette usually done in earlyStartup().
   * <p>
   * This requires for -DFLAG_NO_PALETTE flag.
   */
  public void test_preloadingCache() throws Exception {
    TestBundle testBundle = new TestBundle();
    Image image = new Image(null, 11, 29);
    try {
      // prepare
      String className = ClassForBundle.class.getName();
      String descriptionsPath =
          "wbp-meta/" + CodeUtils.getPackage(className).replace('.', '/') + "/";
      testBundle.addClass(ClassForBundle.class);
      testBundle.setFile(
          descriptionsPath + ".wbp-cache-descriptions",
          "Please, cache this package.");
      String componentPath = descriptionsPath + StringUtils.substringAfterLast(className, ".");
      testBundle.setFile(
          componentPath + ".wbp-component.xml",
          getSourceDQ(
              "<?xml version='1.0' encoding='UTF-8'?>",
              "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
              "  <description>test1 test2 <p attr='val'>test3</p> test4 test5</description>",
              "</component>"));
      testBundle.setFile(componentPath + ".png", ImageUtils.getBytesPNG(image));
      testBundle.addExtension(
          "org.eclipse.wb.core.toolkits",
          "<toolkit id='org.eclipse.wb.swing'>",
          "  <palette>",
          "    <category id='org.eclipse.wb.tests.testBundle.components' name='Test' description='Test'>",
          "      <component class='" + className + "'/>",
          "    </category>",
          "  </palette>",
          "</toolkit>");
      testBundle.install();
      try {
        String toolkitId = "org.eclipse.wb.swing";
        // get cache
        Object cache =
            ReflectionUtils.invokeMethod(
                ComponentPresentationHelper.class,
                "getCache(java.lang.String)",
                toolkitId);
        // do the job
        ReflectionUtils.invokeMethod(
            ComponentPresentationHelper.class,
            "fillPresentations("
                + cache.getClass().getName().replace("$", ".")
                + ",java.lang.String,org.eclipse.core.runtime.IProgressMonitor)",
            cache,
            toolkitId,
            new NullProgressMonitor());
        // get presentation and check
        ComponentPresentation presentation =
            (ComponentPresentation) ReflectionUtils.invokeMethod(
                cache,
                "get(java.lang.String)",
                className + " null");
        assertNotNull(presentation);
        assertEquals(
            "test1 test2 <p attr=\"val\">test3</p> test4 test5",
            presentation.getDescription());
        Image icon = presentation.getIcon();
        assertNotNull(icon);
        assertEquals(image.getBounds().width, 11);
        assertEquals(image.getBounds().height, 29);
      } finally {
        testBundle.uninstall();
      }
    } finally {
      image.dispose();
      testBundle.dispose();
    }
  }

  /**
   * We use this class to put it into new {@link Bundle}.
   */
  public static class ClassForBundle extends JButton {
    private static final long serialVersionUID = 0L;
  }
  /**
   * We use this class to check referencing other classes, from separate JAR's.
   */
  public static class ClassForBundle2 extends ClassForBundle {
    private static final long serialVersionUID = 0L;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for "typeParameter" support.
   */
  public void test_typeParameters_chooseType() throws Exception {
    final ComponentEntryInfo componentEntry = prepare_typeParameters();
    // animate createTool()
    CreationTool creationTool;
    {
      final AtomicReference<CreationTool> creationToolResult = new AtomicReference<CreationTool>();
      new UiContext().executeAndCheck(new UIRunnable() {
        public void run(UiContext context) throws Exception {
          CreationTool result = (CreationTool) componentEntry.createTool();
          creationToolResult.set(result);
        }
      }, new UIRunnable() {
        public void run(UiContext context) throws Exception {
          context.useShell("Generic component creation");
          // initial type
          Text textWidget = context.getTextByLabel("Row type:");
          assertEquals("java.lang.Object", textWidget.getText());
          // animate "..." button
          {
            Button chooseButton = context.getButtonByText("...");
            animateChooseButton(chooseButton);
          }
          // chosen type
          assertEquals("java.lang.String", textWidget.getText());
          // OK
          context.clickButton("OK");
        }

        public void animateChooseButton(final Button chooseButton) throws Exception {
          new UiContext().executeAndCheck(new UIRunnable() {
            public void run(UiContext context) throws Exception {
              context.click(chooseButton);
            }
          }, new UIRunnable() {
            public void run(UiContext context) throws Exception {
              animateOpenTypeSelection(context, "java.lang.String");
              context.clickButton("OK");
            }
          });
        }
      });
      creationTool = creationToolResult.get();
      assertNotNull(creationTool);
    }
    // prepare new component
    ComponentInfo newComponent;
    {
      ICreationFactory creationFactory = creationTool.getFactory();
      creationFactory.activate();
      newComponent = (ComponentInfo) creationFactory.getNewObject();
    }
    // new component has "template arguments"
    {
      Map<String, String> templateArguments = newComponent.getTemplateArguments();
      assertThat(templateArguments).contains(entry("rowType", "java.lang.String"));
    }
  }

  /**
   * Test for "typeParameter" support.
   * <p>
   * Cancel type choosing, and type parameters dialog then.
   */
  public void test_typeParameters_cancel() throws Exception {
    final ComponentEntryInfo componentEntry = prepare_typeParameters();
    // animate createTool()
    CreationTool creationTool;
    {
      final AtomicReference<CreationTool> creationToolResult = new AtomicReference<CreationTool>();
      new UiContext().executeAndCheck(new UIRunnable() {
        public void run(UiContext context) throws Exception {
          CreationTool result = (CreationTool) componentEntry.createTool();
          creationToolResult.set(result);
        }
      }, new UIRunnable() {
        public void run(UiContext context) throws Exception {
          context.useShell("Generic component creation");
          // initial type
          Text textWidget = context.getTextByLabel("Row type:");
          assertEquals("java.lang.Object", textWidget.getText());
          // animate "..." button
          {
            Button chooseButton = context.getButtonByText("...");
            animateChooseButton(chooseButton);
          }
          // no changes
          assertEquals("java.lang.Object", textWidget.getText());
          // cancel
          context.clickButton("Cancel");
        }

        public void animateChooseButton(final Button chooseButton) throws Exception {
          new UiContext().executeAndCheck(new UIRunnable() {
            public void run(UiContext context) throws Exception {
              context.click(chooseButton);
            }
          }, new UIRunnable() {
            public void run(UiContext context) throws Exception {
              animateOpenTypeSelection(context, "java.lang.String");
              context.clickButton("Cancel");
            }
          });
        }
      });
      creationTool = creationToolResult.get();
    }
    // type parameter dialog was cancelled, so no tool
    assertNull(creationTool);
  }

  /**
   * Test for "typeParameter" support.
   * <p>
   * Attempt to choose type which is not subclass of required.
   */
  public void test_typeParameters_chooseBadType() throws Exception {
    final ComponentEntryInfo componentEntry;
    {
      String line = "<typeParameter name='rowType' type='java.util.List' title='My type'/>";
      componentEntry = prepare_typeParameters(line);
    }
    // animate createTool()
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        componentEntry.createTool();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Generic component creation");
        // animate "..." button
        {
          Button chooseButton = context.getButtonByText("...");
          animateChooseButton(chooseButton);
        }
        // Cancel
        context.clickButton("Cancel");
      }

      public void animateChooseButton(final Button chooseButton) throws Exception {
        new UiContext().executeAndCheck(new UIRunnable() {
          public void run(UiContext context) throws Exception {
            context.click(chooseButton);
          }
        }, new UIRunnable() {
          public void run(final UiContext openTypeContext) throws Exception {
            animateOpenTypeSelection(openTypeContext, "java.lang.String");
            // click OK and close "Error" dialog
            new UiContext().executeAndCheck(new UIRunnable() {
              public void run(UiContext context) throws Exception {
                openTypeContext.clickButton("OK");
              }
            }, new UIRunnable() {
              public void run(UiContext context) throws Exception {
                context.useShell("Error");
                context.clickButton("OK");
              }
            });
          }
        });
      }
    });
  }

  private ComponentEntryInfo prepare_typeParameters() throws Exception {
    String line = "<typeParameter name='rowType' type='java.lang.Object' title='Row type'/>";
    return prepare_typeParameters(line);
  }

  private ComponentEntryInfo prepare_typeParameters(String typeParameterLine) throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel<T> extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyPanel<%rowType%>()]]></source>",
            "    <typeParameters>",
            "      " + typeParameterLine,
            "    </typeParameters>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // prepare palette
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <component id='myId' class='test.MyPanel'/>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare component
    ComponentEntryInfo componentEntry = (ComponentEntryInfo) palette.getEntry("myId");
    assertTrue(componentEntry.initialize(null, m_lastParseInfo));
    return componentEntry;
  }
}
