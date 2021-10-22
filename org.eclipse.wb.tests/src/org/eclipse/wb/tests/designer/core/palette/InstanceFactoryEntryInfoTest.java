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
import org.eclipse.wb.internal.core.editor.palette.model.entry.InstanceFactoryEntryInfo;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.widgets.Table;

import java.util.List;

/**
 * Tests for {@link InstanceFactoryEntryInfo}.
 *
 * @author scheglov_ke
 */
public class InstanceFactoryEntryInfoTest extends AbstractPaletteTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_access() throws Exception {
    InstanceFactoryEntryInfo entry = new InstanceFactoryEntryInfo();
    // factoryClassName
    assertNull(entry.getFactoryClassName());
    entry.setFactoryClassName("test.InstanceFactory");
    assertEquals("test.InstanceFactory", entry.getFactoryClassName());
    // methodSignature
    assertNull(entry.getMethodSignature());
    entry.setMethodSignature("createButton()");
    assertEquals("createButton()", entry.getMethodSignature());
    // toString()
    assertEquals(
        "InstanceFactoryMethod(class='test.InstanceFactory' signature='createButton()')",
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
        "test/InstanceFactory.java",
        getTestSource(
            "public final class InstanceFactory {",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // extend palette
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <instance-factory class='test.InstanceFactory'>",
        "    <method signature='createButton()'/>",
        "  </instance-factory>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare entry
    CategoryInfo category = palette.getCategory("category_1");
    InstanceFactoryEntryInfo entry = (InstanceFactoryEntryInfo) category.getEntries().get(0);
    assertTrue(entry.initialize(null, m_lastParseInfo));
    // check component
    assertSame(category, entry.getCategory());
    assertEquals("test.InstanceFactory", entry.getFactoryClassName());
    assertEquals("createButton()", entry.getMethodSignature());
    assertEquals("category_1 test.InstanceFactory createButton()", entry.getId());
    assertEquals("createButton()", entry.getName());
    assertEquals("Class: test.InstanceFactory<br/>Method: createButton()", entry.getDescription());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tool
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No instance factory instance in class, create new.
   */
  public void test_createTool_new() throws Exception {
    setFileContentSrc(
        "test/InstanceFactory.java",
        getTestSource(
            "public final class InstanceFactory {",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // extend palette
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <instance-factory class='test.InstanceFactory'>",
        "    <method signature='createButton()'/>",
        "  </instance-factory>",
        "</category>"});
    JavaInfo panel = parseEmptyPanel();
    PaletteInfo palette = loadPalette(panel);
    // prepare entry
    CategoryInfo category = palette.getCategory("category_1");
    InstanceFactoryEntryInfo entry = (InstanceFactoryEntryInfo) category.getEntries().get(0);
    // initialize
    assertTrue(entry.initialize(null, panel));
    // create tool
    CreationTool creationTool = (CreationTool) entry.createTool();
    // now we have instance factory instance in CU
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  private final InstanceFactory instanceFactory = new InstanceFactory();",
        "  public Test() {",
        "  }",
        "}");
    InstanceFactoryInfo instanceFactory = getTestInstanceFactories().get(0);
    // check factory
    {
      ICreationFactory factory = creationTool.getFactory();
      factory.activate();
      // check JavaInfo
      JavaInfo javaInfo = (JavaInfo) factory.getNewObject();
      InstanceFactoryCreationSupport creationSupport =
          (InstanceFactoryCreationSupport) javaInfo.getCreationSupport();
      assertEquals(
          TemplateUtils.format("{0}.createButton()", instanceFactory),
          creationSupport.add_getSource(null));
    }
  }

  /**
   * There is already single instance factory instance in class, use it.
   */
  public void test_createTool_single() throws Exception {
    setFileContentSrc(
        "test/InstanceFactory.java",
        getTestSource(
            "public final class InstanceFactory {",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // extend palette
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <instance-factory class='test.InstanceFactory'>",
        "    <method signature='createButton()'/>",
        "  </instance-factory>",
        "</category>"});
    JavaInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final InstanceFactory myFactory = new InstanceFactory();",
            "  public Test() {",
            "  }",
            "}");
    InstanceFactoryInfo instanceFactory = getTestInstanceFactories().get(0);
    String initialSource = m_lastEditor.getSource();
    PaletteInfo palette = loadPalette(panel);
    // prepare entry
    CategoryInfo category = palette.getCategory("category_1");
    InstanceFactoryEntryInfo entry = (InstanceFactoryEntryInfo) category.getEntries().get(0);
    // initialize
    assertTrue(entry.initialize(null, panel));
    // create tool
    CreationTool creationTool = (CreationTool) entry.createTool();
    // no source modification expected
    assertEditor(initialSource, m_lastEditor);
    // check factory
    {
      ICreationFactory factory = creationTool.getFactory();
      factory.activate();
      // check JavaInfo
      JavaInfo javaInfo = (JavaInfo) factory.getNewObject();
      assertSame(Boolean.TRUE, javaInfo.getArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT));
      {
        InstanceFactoryCreationSupport creationSupport =
            (InstanceFactoryCreationSupport) javaInfo.getCreationSupport();
        assertEquals(
            TemplateUtils.format("{0}.createButton()", instanceFactory),
            creationSupport.add_getSource(null));
      }
    }
  }

  /**
   * There are already two instance factory instance in class, select one.
   */
  public void test_createTool_multiSelect() throws Exception {
    setFileContentSrc(
        "test/InstanceFactory.java",
        getTestSource(
            "public final class InstanceFactory {",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // extend palette
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <instance-factory class='test.InstanceFactory'>",
        "    <method signature='createButton()'/>",
        "  </instance-factory>",
        "</category>"});
    JavaInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final InstanceFactory factory_1 = new InstanceFactory();",
            "  private final InstanceFactory factory_2 = new InstanceFactory();",
            "  public Test() {",
            "  }",
            "}");
    String initialSource = m_lastEditor.getSource();
    PaletteInfo palette = loadPalette(panel);
    // prepare entry
    CategoryInfo category = palette.getCategory("category_1");
    final InstanceFactoryEntryInfo entry = (InstanceFactoryEntryInfo) category.getEntries().get(0);
    // initialize
    assertTrue(entry.initialize(null, panel));
    // create tool - select "factory_2"
    CreationTool creationTool;
    InstanceFactoryInfo instanceFactory;
    {
      final CreationTool[] tools = new CreationTool[1];
      new UiContext().executeAndCheck(new UIRunnable() {
        public void run(UiContext context) throws Exception {
          tools[0] = (CreationTool) entry.createTool();
        }
      }, new UIRunnable() {
        public void run(UiContext context) throws Exception {
          context.useShell("Select factory");
          waitEventLoop(0);
          Table table = context.findFirstWidget(Table.class);
          table.select(1);
          // click OK
          context.clickButton("OK");
        }
      });
      creationTool = tools[0];
      instanceFactory = getTestInstanceFactories().get(1);
    }
    // no source modification expected
    assertEditor(initialSource, m_lastEditor);
    // check factory
    {
      ICreationFactory factory = creationTool.getFactory();
      factory.activate();
      // check JavaInfo
      JavaInfo javaInfo = (JavaInfo) factory.getNewObject();
      InstanceFactoryCreationSupport creationSupport =
          (InstanceFactoryCreationSupport) javaInfo.getCreationSupport();
      assertEquals(
          TemplateUtils.format("{0}.createButton()", instanceFactory),
          creationSupport.add_getSource(null));
    }
  }

  /**
   * There are already two instance factory instance in class, cancel selection.
   */
  public void test_createTool_multiCancel() throws Exception {
    setFileContentSrc(
        "test/InstanceFactory.java",
        getTestSource(
            "public final class InstanceFactory {",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // extend palette
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <instance-factory class='test.InstanceFactory'>",
        "    <method signature='createButton()'/>",
        "  </instance-factory>",
        "</category>"});
    JavaInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final InstanceFactory factory_1 = new InstanceFactory();",
            "  private final InstanceFactory factory_2 = new InstanceFactory();",
            "  public Test() {",
            "  }",
            "}");
    String initialSource = m_lastEditor.getSource();
    PaletteInfo palette = loadPalette(panel);
    // prepare entry
    CategoryInfo category = palette.getCategory("category_1");
    final InstanceFactoryEntryInfo entry = (InstanceFactoryEntryInfo) category.getEntries().get(0);
    // initialize
    assertTrue(entry.initialize(null, panel));
    // create tool
    CreationTool creationTool;
    {
      final CreationTool[] tools = new CreationTool[1];
      new UiContext().executeAndCheck(new UIRunnable() {
        public void run(UiContext context) throws Exception {
          tools[0] = (CreationTool) entry.createTool();
        }
      }, new UIRunnable() {
        public void run(UiContext context) throws Exception {
          context.useShell("Select factory");
          context.clickButton("Cancel");
        }
      });
      creationTool = tools[0];
    }
    // no source modification expected
    assertEditor(initialSource, m_lastEditor);
    // no tool
    assertNull(creationTool);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<InstanceFactoryInfo> getTestInstanceFactories() throws Exception {
    return InstanceFactoryInfo.getFactories(
        m_lastParseInfo,
        m_lastLoader.loadClass("test.InstanceFactory"));
  }
}
