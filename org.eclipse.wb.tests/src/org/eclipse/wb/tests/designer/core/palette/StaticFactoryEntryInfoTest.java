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
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.editor.palette.model.entry.StaticFactoryEntryInfo;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.tests.designer.tests.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StaticFactoryEntryInfo}.
 * 
 * @author scheglov_ke
 */
public class StaticFactoryEntryInfoTest extends AbstractPaletteTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_access() throws Exception {
    StaticFactoryEntryInfo entry = new StaticFactoryEntryInfo();
    // factoryClassName
    assertNull(entry.getFactoryClassName());
    entry.setFactoryClassName("test.StaticFactory");
    assertEquals("test.StaticFactory", entry.getFactoryClassName());
    // methodSignature
    assertNull(entry.getMethodSignature());
    entry.setMethodSignature("createButton()");
    assertEquals("createButton()", entry.getMethodSignature());
    // toString()
    assertEquals(
        "StaticFactoryMethod(class='test.StaticFactory' signature='createButton()')",
        entry.toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Only absolutely required values are specified in XML, all other values should be derived from
   * them.
   */
  public void test_parse_defaults() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // extend palette
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1' description='category 1'>",
        "  <static-factory class='test.StaticFactory'>",
        "    <method signature='createButton()'/>",
        "  </static-factory>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare entry
    CategoryInfo category = palette.getCategory("category_1");
    StaticFactoryEntryInfo entry = (StaticFactoryEntryInfo) category.getEntries().get(0);
    assertTrue(entry.initialize(null, m_lastParseInfo));
    // check entry
    assertSame(category, entry.getCategory());
    assertEquals("test.StaticFactory", entry.getFactoryClassName());
    assertEquals("createButton()", entry.getMethodSignature());
    assertEquals("category_1 test.StaticFactory createButton()", entry.getId());
    assertEquals("createButton()", entry.getName());
    assertEquals("Class: test.StaticFactory<br/>Method: createButton()", entry.getDescription());
  }

  /**
   * All explicit values are specified.
   */
  public void test_parse_values() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // extend palette
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1' description='category 1'>",
        "  <static-factory class='test.StaticFactory'>",
        "    <method signature='createButton()' id='my id' name='my name' description='my description'/>",
        "  </static-factory>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare entry
    CategoryInfo category = palette.getCategory("category_1");
    StaticFactoryEntryInfo entry = (StaticFactoryEntryInfo) category.getEntries().get(0);
    // check entry
    assertSame(category, entry.getCategory());
    assertEquals("test.StaticFactory", entry.getFactoryClassName());
    assertEquals("createButton()", entry.getMethodSignature());
    assertEquals("my id", entry.getId());
    assertEquals("my name", entry.getName());
    assertEquals("my description", entry.getDescription());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name in wbp-factory.xml
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If "name" attribute is empty, then name from {@link FactoryMethodDescription} should be used.
   */
  public void test_parse_presentationName_no() throws Exception {
    assertPresentationName_fromFactoryDescription(null);
  }

  /**
   * If "name" attribute is empty, then name from {@link FactoryMethodDescription} should be used.
   */
  public void test_parse_presentationName_emptyString() throws Exception {
    assertPresentationName_fromFactoryDescription("");
  }

  /**
   * If "name" attribute is "default", i.e. signature, then name from
   * {@link FactoryMethodDescription} should be used.
   */
  public void test_parse_presentationName_signature() throws Exception {
    assertPresentationName_fromFactoryDescription("createButton()");
  }

  private void assertPresentationName_fromFactoryDescription(String nameAttribute) throws Exception {
    setFileContentSrc(
        "test/MyFactory.java",
        getTestSource(
            "public class MyFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyFactory.wbp-factory.xml",
        getSource(
            "<factory>",
            "  <method name='createButton'>",
            "    <name>Name in XML</name>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    //
    String nameAttributeSrc = nameAttribute != null ? "name='" + nameAttribute + "'" : "";
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "    <static-factory class='test.MyFactory'>",
        "      <method signature='createButton()' " + nameAttributeSrc + "/>",
        "    </static-factory>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare category/entry
    CategoryInfo category = palette.getCategory("category_1");
    StaticFactoryEntryInfo entry = (StaticFactoryEntryInfo) category.getEntries().get(0);
    // initialize and check values
    assertTrue(entry.initialize(null, m_lastParseInfo));
    assertEquals("Name in XML", entry.getName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Description attribute
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If "description" text is empty, then description from {@link FactoryMethodDescription} should
   * be used.
   */
  public void test_parse_descriptionText_emptyString() throws Exception {
    assertDescriptionText_fromFactoryDescription("");
  }

  /**
   * If "description" text is empty, then description from {@link FactoryMethodDescription} should
   * be used.
   */
  public void test_parse_descriptionText_spacesString() throws Exception {
    assertDescriptionText_fromFactoryDescription(" \t");
  }

  /**
   * If "description" text is exactly name of class (we generate such description when user adds
   * component using UI), then description from {@link FactoryMethodDescription} should be used.
   */
  public void test_parse_descriptionText_classAndSignature() throws Exception {
    assertDescriptionText_fromFactoryDescription("Class: test.MyFactory Method: createButton()");
  }

  private void assertDescriptionText_fromFactoryDescription(String descriptionAttribute)
      throws Exception {
    setFileContentSrc(
        "test/MyFactory.java",
        getTestSource(
            "public class MyFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyFactory.wbp-factory.xml",
        getSource(
            "<factory>",
            "  <method name='createButton'>",
            "    <description>Description in XML</description>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    //
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "    <static-factory class='test.MyFactory'>",
        "      <method signature='createButton()' name='f' description='"
            + descriptionAttribute
            + "'/>",
        "    </static-factory>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare category/entry
    CategoryInfo category = palette.getCategory("category_1");
    StaticFactoryEntryInfo entry = (StaticFactoryEntryInfo) category.getEntries().get(0);
    // initialize and check values
    assertTrue(entry.initialize(null, m_lastParseInfo));
    assertEquals("Description in XML", entry.getDescription());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When no factory class, we should ignore and don't add warning.
   */
  public void test_initialize_noFactoryClass() throws Exception {
    waitForAutoBuild();
    JavaInfo panel = parseEmptyPanel();
    // prepare entry
    StaticFactoryEntryInfo entry = new StaticFactoryEntryInfo();
    entry.setFactoryClassName("test.StaticFactory");
    entry.setMethodSignature("createButton()");
    // initialize
    assertEquals(0, m_lastState.getWarnings().size());
    assertFalse(entry.initialize(null, panel));
    // no warnings
    assertThat(m_lastState.getWarnings()).isEmpty();
  }

  /**
   * When no factory method, we should add warning.
   */
  public void test_initialize_noFactoryMethod() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public final class StaticFactory {",
            "}"));
    waitForAutoBuild();
    // parse
    JavaInfo panel = parseEmptyPanel();
    // prepare entry
    StaticFactoryEntryInfo entry = new StaticFactoryEntryInfo();
    entry.setFactoryClassName("test.StaticFactory");
    entry.setMethodSignature("noSuchMethod()");
    // initialize
    assertEquals(0, m_lastState.getWarnings().size());
    assertFalse(entry.initialize(null, panel));
    // no warnings
    assertThat(m_lastState.getWarnings()).isEmpty();
  }

  /**
   * Good situation - existing factory class, and existing method signature.
   */
  public void test_initialize() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    JavaInfo panel = parseEmptyPanel();
    // prepare entry
    StaticFactoryEntryInfo entry = new StaticFactoryEntryInfo();
    entry.setFactoryClassName("test.StaticFactory");
    entry.setMethodSignature("createButton()");
    // initialize
    assertTrue(entry.initialize(null, panel));
    // after successful initialize we can ask for icon
    {
      Image icon = entry.getIcon();
      assertNotNull(icon);
      assertEquals(16, icon.getBounds().width);
      assertEquals(16, icon.getBounds().height);
    }
  }

  /**
   * Test that we can use icon for factory method and description from XML, i.e.
   * {@link FactoryMethodDescription#getIcon()} and
   * {@link FactoryMethodDescription#getDescription()}.
   */
  public void test_initialize_iconAndDescription() throws Exception {
    // prepare factory
    {
      setFileContentSrc(
          "test/StaticFactory.java",
          getTestSource(
              "public final class StaticFactory {",
              "  public static JButton createButton() {",
              "    return new JButton();",
              "  }",
              "}"));
      setFileContentSrc(
          "test/StaticFactory.wbp-factory.xml",
          getSourceDQ(
              "<?xml version='1.0' encoding='UTF-8'?>",
              "<factory>",
              "  <method name='createButton'>",
              "    <description>Some textual description of method.</description>",
              "  </method>",
              "</factory>"));
      // create icon for "createButton()"
      {
        IFile iconFile = getFileSrc("test", "StaticFactory.createButton__.png");
        iconFile.create(Activator.getFile("icons/test.png"), true, null);
      }
      // build
      waitForAutoBuild();
    }
    // parse
    JavaInfo panel = parseEmptyPanel();
    // prepare entry
    StaticFactoryEntryInfo entry;
    {
      entry = new StaticFactoryEntryInfo();
      entry.setFactoryClassName("test.StaticFactory");
      entry.setMethodSignature("createButton()");
    }
    // initialize
    assertTrue(entry.initialize(null, panel));
    assertEquals("Some textual description of method.", entry.getDescription());
    assertSame(entry.getMethodDescription().getIcon(), entry.getIcon());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tool
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_createTool() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    waitEventLoop(10);
    waitForAutoBuild();
    // parse
    JavaInfo panel = parseEmptyPanel();
    // prepare entry
    StaticFactoryEntryInfo entry = new StaticFactoryEntryInfo();
    entry.setFactoryClassName("test.StaticFactory");
    entry.setMethodSignature("createButton()");
    // initialize
    assertTrue(entry.initialize(null, panel));
    // create tool
    CreationTool creationTool = (CreationTool) entry.createTool();
    ICreationFactory factory = creationTool.getFactory();
    factory.activate();
    // check JavaInfo
    JavaInfo javaInfo = (JavaInfo) factory.getNewObject();
    assertSame(Boolean.TRUE, javaInfo.getArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT));
    {
      StaticFactoryCreationSupport creationSupport =
          (StaticFactoryCreationSupport) javaInfo.getCreationSupport();
      assertEquals("test.StaticFactory.createButton()", creationSupport.add_getSource(null));
    }
  }

  /**
   * Users want factory-specific tweaks for properties.
   */
  public void test_createTool_factoryMethodSpecific_ComponentDescription() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory.createButton__.wbp-component.xml",
        getSource(
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <properties-preferred names='enabled'/>",
            "</component>"));
    waitForAutoBuild();
    // parse
    JavaInfo panel = parseEmptyPanel();
    // prepare entry
    StaticFactoryEntryInfo entry = new StaticFactoryEntryInfo();
    entry.setFactoryClassName("test.StaticFactory");
    entry.setMethodSignature("createButton()");
    // initialize
    assertTrue(entry.initialize(null, panel));
    // create tool
    CreationTool creationTool = (CreationTool) entry.createTool();
    ICreationFactory factory = creationTool.getFactory();
    factory.activate();
    // check JavaInfo
    JavaInfo javaInfo = (JavaInfo) factory.getNewObject();
    // setEnabled(boolean) marked as "preferred"
    {
      GenericPropertyDescription propertyDescription =
          javaInfo.getDescription().getProperty("setEnabled(boolean)");
      assertSame(PropertyCategory.PREFERRED, propertyDescription.getCategory());
    }
  }
}
