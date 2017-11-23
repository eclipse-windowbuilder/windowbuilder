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

import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.palette.PaletteManager;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryAddCommand;
import org.eclipse.wb.internal.core.editor.palette.model.entry.InstanceFactoryEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.StaticFactoryEntryInfo;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link PaletteInfo}.
 * 
 * @author scheglov_ke
 */
public class PaletteManagerTest extends AbstractPaletteTest {
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
  // PaletteManager
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link PaletteManager} can load palette, including dynamically added extensions.
   */
  public void test_dynamic() throws Exception {
    JavaInfo panel = parseEmptyPanel();
    PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
    // no palette before loading
    assertNull(manager.getPalette());
    // no palette contributions for this toolkit yet
    {
      // load palette
      manager.reloadPalette();
      // check palette
      PaletteInfo palette = manager.getPalette();
      assertNotNull(palette);
      assertEquals(0, palette.getCategories().size());
    }
    // add dynamic extension and check that palette uses it
    {
      addPaletteExtension(new String[]{
          "<category id='category_1' name='category 1' description='description 1'>",
          "  <component class='javax.swing.JPanel'/>",
          "</category>"});
      try {
        // load palette
        manager.reloadPalette();
        // check palette
        PaletteInfo palette = manager.getPalette();
        assertNotNull(palette);
        // check category
        assertEquals(1, palette.getCategories().size());
        CategoryInfo category = palette.getCategory("category_1");
        // check component
        assertEquals(1, category.getEntries().size());
        ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
        assertEquals("category_1 javax.swing.JPanel", componentEntry.getId());
      } finally {
        removeToolkitExtension();
      }
    }
    // dynamic extension was removed, so again empty palette
    {
      // load palette
      manager.reloadPalette();
      // check palette
      PaletteInfo palette = manager.getPalette();
      assertNotNull(palette);
      assertEquals(0, palette.getCategories().size());
    }
  }

  /**
   * When two {@link CategoryInfo}s declared with same ID, silently use first one.
   * <p>
   * http://www.eclipse.org/forums/index.php?t=rview&goto=839174#msg_839174
   */
  public void test_duplicateCategory() throws Exception {
    JavaInfo panel = parseEmptyPanel();
    PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
    // add dynamic palette
    {
      addPaletteExtension(new String[]{
          "<category id='category_1' name='name 1' description='desc 1'>",
          "  <component class='javax.swing.JButton'/>",
          "</category>",
          "<category id='category_1' name='name 2' description='desc 2'>",
          "  <component class='javax.swing.JTextField'/>",
          "</category>",});
      try {
        manager.reloadPalette();
        PaletteInfo palette = manager.getPalette();
        assertEquals(1, palette.getCategories().size());
        // check category
        CategoryInfo category = palette.getCategory("category_1");
        assertEquals("name 1", category.getName());
        assertEquals("desc 1", category.getDescription());
        // check components
        {
          assertEquals(2, category.getEntries().size());
          {
            ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(0);
            assertEquals("javax.swing.JButton", componentEntry.getClassName());
          }
          {
            ComponentEntryInfo componentEntry = (ComponentEntryInfo) category.getEntries().get(1);
            assertEquals("javax.swing.JTextField", componentEntry.getClassName());
          }
        }
      } finally {
        removeToolkitExtension();
      }
    }
  }

  /**
   * Test for {@link PaletteManager#getPalette()}.
   */
  public void test_getPaletteCopy() throws Exception {
    addPaletteExtension(new String[]{"<category id='category_1' name='category 1'/>"});
    JavaInfo panel = parseEmptyPanel();
    PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
    // load palette
    PaletteInfo palette;
    {
      manager.reloadPalette();
      palette = manager.getPalette();
      assertEquals(1, palette.getCategories().size());
      assertNotNull(palette.getCategory("category_1"));
    }
    // get palette copy, it is equal, but not same
    PaletteInfo paletteCopy = manager.getPaletteCopy();
    assertNotSame(palette, paletteCopy);
    assertEquals(1, paletteCopy.getCategories().size());
    assertNotNull(paletteCopy.getCategory("category_1"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Outside of category
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Entry outside of category, has "category" attribute - OK.
   */
  public void test_parse_outsideCategory() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'/>",
        "<component class='javax.swing.JButton' category='category_1'/>"});
    PaletteInfo palette = loadPalette();
    // check for entry
    CategoryInfo category = palette.getCategory("category_1");
    assertEquals(1, category.getEntries().size());
  }

  /**
   * Entry outside of category, and no category - error.
   */
  public void test_parse_outsideCategory_noCategory() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'/>",
        "<component class='javax.swing.JButton'/>"});
    DesignerPlugin.setDisplayExceptionOnConsole(false);
    try {
      PaletteInfo palette = loadPalette();
      // no entry expected
      CategoryInfo category = palette.getCategory("category_1");
      assertEquals(0, category.getEntries().size());
    } finally {
      DesignerPlugin.setDisplayExceptionOnConsole(true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_command_1() throws Exception {
    PaletteManager manager = loadManager();
    // use loaded palette
    {
      PaletteInfo palette = manager.getPalette();
      // initially palette is empty
      assertEquals(0, palette.getCategories().size());
      // add category using command
      manager.commands_add(new CategoryAddCommand("new id",
          "new name",
          "new description",
          true,
          true,
          null));
      manager.commands_write();
      // check new category
      assertEquals(1, palette.getCategories().size());
      CategoryInfo category = palette.getCategory("new id");
      assertEquals("new name", category.getName());
      assertEquals("new description", category.getDescription());
    }
    // reload palette, but command was written, so palette still has new category
    {
      manager.reloadPalette();
      PaletteInfo palette = manager.getPalette();
      assertEquals(1, palette.getCategories().size());
      assertNotNull(palette.getCategory("new id"));
    }
  }

  /**
   * This method is invoked after {@link #test_command_2()}, so we check here that commands are not
   * kept between tests (test methods).
   */
  public void test_command_2() throws Exception {
    PaletteManager manager = loadManager();
    PaletteInfo palette = manager.getPalette();
    assertEquals(0, palette.getCategories().size());
  }

  /**
   * Sometimes users want to use standard palette for all developers of project. So, they want to
   * share palette commands. Best way to do this - save them into project and use CVS.
   */
  public void test_commandsInProject() throws Exception {
    String commandsFilePath = "wbp-meta/" + TOOLKIT_ID + ".wbp-palette-commands.xml";
    setFileContent(commandsFilePath, "");
    // prepare manager
    PaletteManager manager = loadManager();
    // use loaded palette
    {
      PaletteInfo palette = manager.getPalette();
      // initially palette is empty
      assertEquals(0, palette.getCategories().size());
      // add category using command
      manager.commands_add(new CategoryAddCommand("new id",
          "new name",
          "new description",
          true,
          true,
          null));
      manager.commands_write();
      // check new category
      assertEquals(1, palette.getCategories().size());
      CategoryInfo category = palette.getCategory("new id");
      assertEquals("new name", category.getName());
      assertEquals("new description", category.getDescription());
    }
    // check that palette commands saved into project
    {
      String commands = getFileContent(commandsFilePath);
      assertThat(commands).isNotEqualTo("<commands/>");
    }
    // reload palette, but command was written, so palette still has new category
    {
      manager.reloadPalette();
      PaletteInfo palette = manager.getPalette();
      assertEquals(1, palette.getCategories().size());
      assertNotNull(palette.getCategory("new id"));
    }
  }

  /**
   * We should apply command files from Jar files.
   */
  @DisposeProjectAfter
  public void test_applyCommandsFromJar() throws Exception {
    // add JAR with commands
    {
      String jarPath =
          TestUtils.createTemporaryJar(
              "wbp-meta/" + TOOLKIT_ID + ".wbp-palette-commands.xml",
              getSourceDQ(
                  "<commands>",
                  "  <addCategory id='myID' name='myName' description='myDescription'/>",
                  "</commands>"));
      ProjectUtils.addExternalJar(m_javaProject, jarPath, null);
    }
    // load palette
    PaletteInfo palette = loadPalette();
    // check categories
    List<CategoryInfo> categories = palette.getCategories();
    assertThat(categories).hasSize(1);
    {
      CategoryInfo category = categories.get(0);
      assertEquals("myID", category.getId());
      assertEquals("myName", category.getName());
      assertEquals("myDescription", category.getDescription());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Category order
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for "next" attribute for "category" tag.
   * <p>
   * Simple case, when order of contributions is good, i.e. we encounter category before its "next"
   * reference.
   */
  public void test_categoryOrder_nextCategory() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'/>",
        "<category id='category_2' name='category 2'/>",
        "<category id='category_3' name='category 3' next='category_2'/>"});
    PaletteInfo palette = loadPalette();
    List<CategoryInfo> categories = palette.getCategories();
    assertThat(categories).hasSize(3);
    assertEquals("category_1", categories.get(0).getId());
    assertEquals("category_3", categories.get(1).getId());
    assertEquals("category_2", categories.get(2).getId());
  }

  /**
   * Test for "next" attribute for "category" tag.
   * <p>
   * Complex case, when we first see category with "next" attribute and only then referenced
   * category.
   */
  public void test_categoryOrder_nextCategory_2() throws Exception {
    addPaletteExtension(
        "palette_1",
        new String[]{"<category id='category_3' name='category 3' next='category_2'/>"});
    addPaletteExtension("palette_2", new String[]{
        "<category id='category_1' name='category 1'/>",
        "<category id='category_2' name='category 2'/>"});
    try {
      PaletteInfo palette = loadPalette();
      List<CategoryInfo> categories = palette.getCategories();
      assertThat(categories).hasSize(3);
      assertEquals("category_1", categories.get(0).getId());
      assertEquals("category_3", categories.get(1).getId());
      assertEquals("category_2", categories.get(2).getId());
    } finally {
      removeToolkitExtension("palette_1");
      removeToolkitExtension("palette_2");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette contributions from "wbp-meta"
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parsing for custom palette from project: default values for {@link ComponentEntryInfo}.
   */
  public void test_customPalette_project_1() throws Exception {
    setFileContent(
        "wbp-meta",
        TOOLKIT_ID + ".wbp-palette.xml",
        getSourceDQ(
            "<palette>",
            "  <category id='categoryId.1' name='name 1' description='desc 1'>",
            "    <component class='javax.swing.JButton'/>",
            "    <component class='javax.swing.JTextField'/>",
            "  </category>",
            "  <category id='categoryId.2' name='name 1' description='desc 1' open='false'/>",
            "</palette>"));
    // load palette
    PaletteInfo palette = loadPalette();
    // categories
    assertEquals(2, palette.getCategories().size());
    {
      CategoryInfo category = palette.getCategories().get(0);
      assertEquals("categoryId.1", category.getId());
      assertEquals("name 1", category.getName());
      assertEquals("desc 1", category.getDescription());
      assertTrue(category.isOpen());
      // components
      {
        assertEquals(2, category.getEntries().size());
        {
          ComponentEntryInfo component = (ComponentEntryInfo) category.getEntries().get(0);
          assertEquals("javax.swing.JButton", component.getClassName());
          assertEquals("categoryId.1 javax.swing.JButton", component.getId());
          assertTrue(component.isVisible());
          // attributes that are updated only after initialize()
          assertTrue(component.initialize(null, m_lastParseInfo));
          assertEquals("JButton", component.getName());
          assertEquals("An implementation of a \"push\" button.", component.getDescription());
        }
        {
          ComponentEntryInfo component = (ComponentEntryInfo) category.getEntries().get(1);
          assertEquals("javax.swing.JTextField", component.getClassName());
        }
      }
    }
    {
      CategoryInfo category = palette.getCategories().get(1);
      assertEquals("categoryId.2", category.getId());
      assertFalse(category.isOpen());
    }
  }

  /**
   * When two {@link CategoryInfo}s declared with same ID, silently use first one.
   * <p>
   * http://www.eclipse.org/forums/index.php?t=rview&goto=839174#msg_839174
   */
  public void test_customPalette_project_duplicateCategoryDeclaration() throws Exception {
    setFileContent(
        "wbp-meta",
        TOOLKIT_ID + ".wbp-palette.xml",
        getSourceDQ(
            "<palette>",
            "  <category id='categoryId.1' name='name 1' description='desc 1'>",
            "    <component class='javax.swing.JButton'/>",
            "  </category>",
            "  <category id='categoryId.1' name='name 2' description='desc 2'>",
            "    <component class='javax.swing.JTextField'/>",
            "  </category>",
            "</palette>"));
    // load palette
    PaletteInfo palette = loadPalette();
    // categories
    assertEquals(1, palette.getCategories().size());
    {
      CategoryInfo category = palette.getCategories().get(0);
      assertEquals("categoryId.1", category.getId());
      assertEquals("name 1", category.getName());
      assertEquals("desc 1", category.getDescription());
      assertTrue(category.isOpen());
      // components
      {
        assertEquals(2, category.getEntries().size());
        {
          ComponentEntryInfo component = (ComponentEntryInfo) category.getEntries().get(0);
          assertEquals("javax.swing.JButton", component.getClassName());
        }
        {
          ComponentEntryInfo component = (ComponentEntryInfo) category.getEntries().get(1);
          assertEquals("javax.swing.JTextField", component.getClassName());
        }
      }
    }
  }

  /**
   * Parsing for custom palette from project: specific values for {@link ComponentEntryInfo}.
   */
  public void test_customPalette_project_2() throws Exception {
    setFileContent(
        "wbp-meta",
        TOOLKIT_ID + ".wbp-palette.xml",
        getSourceDQ(
            "<palette>",
            "  <category id='categoryId.1' name='name 1' description='desc 1'>",
            "    <component id='myButton.id' class='javax.swing.JButton' name='my name' description='my desc' visible='false'/>",
            "  </category>",
            "</palette>"));
    // load palette
    PaletteInfo palette = loadPalette();
    CategoryInfo category = palette.getCategories().get(0);
    // component
    {
      ComponentEntryInfo component = (ComponentEntryInfo) category.getEntries().get(0);
      assertEquals("javax.swing.JButton", component.getClassName());
      assertEquals("myButton.id", component.getId());
      assertEquals("my name", component.getName());
      assertEquals("my desc", component.getDescription());
      assertFalse(component.isVisible());
    }
  }

  /**
   * Parsing for custom palette from project: specific "creation id", using it for generated "id".
   */
  public void test_customPalette_project_3() throws Exception {
    setFileContent(
        "wbp-meta",
        TOOLKIT_ID + ".wbp-palette.xml",
        getSourceDQ(
            "<palette>",
            "  <category id='categoryId.1' name='name 1' description='desc 1'>",
            "    <component creationId='creationId' class='javax.swing.JButton'/>",
            "  </category>",
            "</palette>"));
    // load palette
    PaletteInfo palette = loadPalette();
    CategoryInfo category = palette.getCategories().get(0);
    // component
    {
      ComponentEntryInfo component = (ComponentEntryInfo) category.getEntries().get(0);
      assertEquals("javax.swing.JButton", component.getClassName());
      assertEquals("categoryId.1 javax.swing.JButton creationId", component.getId());
    }
  }

  /**
   * Parsing for custom palette from project: no component class, so exception.
   */
  public void test_customPalette_project_4() throws Exception {
    setFileContent(
        "wbp-meta",
        TOOLKIT_ID + ".wbp-palette.xml",
        getSourceDQ(
            "<palette>",
            "  <category id='categoryId.1' name='name 1' description='desc 1'>",
            "    <component no-class-attribute='javax.swing.JButton'/>",
            "  </category>",
            "</palette>"));
    // load palette, exception expected :-)
    PaletteInfo palette;
    {
      final boolean[] exceptionHappened = new boolean[1];
      ILogListener logListener = new ILogListener() {
        public void logging(IStatus status, String plugin) {
          exceptionHappened[0] = true;
          assertEquals(IStatus.ERROR, status.getSeverity());
          assertEquals(DesignerPlugin.PLUGIN_ID, status.getPlugin());
          assertEquals(IStatus.ERROR, status.getCode());
        }
      };
      //
      ILog log = DesignerPlugin.getDefault().getLog();
      try {
        log.addLogListener(logListener);
        DesignerPlugin.setDisplayExceptionOnConsole(false);
        //
        palette = loadPalette();
      } finally {
        log.removeLogListener(logListener);
        DesignerPlugin.setDisplayExceptionOnConsole(true);
      }
      // check that exception happened
      assertTrue(exceptionHappened[0]);
    }
    // no class, so component entry not loaded
    CategoryInfo category = palette.getCategories().get(0);
    assertEquals(0, category.getEntries().size());
  }

  /**
   * Parsing for custom palette from project: "component" outside of "category".
   */
  public void test_customPalette_project_5() throws Exception {
    setFileContent(
        "wbp-meta",
        TOOLKIT_ID + ".wbp-palette.xml",
        getSourceDQ(
            "<palette>",
            "  <category id='categoryId.1' name='name 1' description='desc 1'/>",
            "  <component class='javax.swing.JButton' category='categoryId.1'/>",
            "</palette>"));
    // load palette
    PaletteInfo palette = loadPalette();
    CategoryInfo category = palette.getCategories().get(0);
    // component
    {
      ComponentEntryInfo component = (ComponentEntryInfo) category.getEntries().get(0);
      assertEquals("javax.swing.JButton", component.getClassName());
    }
  }

  /**
   * Parsing for custom palette from project: use also required projects.
   */
  @DisposeProjectAfter
  public void test_customPalette_project_6() throws Exception {
    // create new project "myProject"
    TestProject myTestProject;
    IJavaProject myJavaProject;
    {
      myTestProject = new TestProject("myProject");
      myJavaProject = myTestProject.getJavaProject();
      // create wbp-meta/palette
      Path path = new Path("wbp-meta/" + TOOLKIT_ID + ".wbp-palette.xml");
      setFileContent(
          myJavaProject.getProject().getFile(path),
          getSourceDQ(
              "<palette>",
              "  <category id='categoryId.1' name='name 1' description='desc 1'/>",
              "</palette>"));
    }
    try {
      // add reference of "myProject" from "TestProject"
      ProjectUtils.requireProject(m_javaProject, myJavaProject);
      // create palette for "TestProject"
      setFileContent(
          "wbp-meta",
          TOOLKIT_ID + ".wbp-palette.xml",
          getSourceDQ(
              "<palette>",
              "  <category id='categoryId.2' name='name 2' description='desc 2'/>",
              "</palette>"));
      // load palette from "TestProject", so "myProject" also used
      PaletteInfo palette = loadPalette();
      // check categories
      assertEquals(2, palette.getCategories().size());
      {
        CategoryInfo category = palette.getCategories().get(0);
        assertEquals("categoryId.1", category.getId());
      }
      {
        CategoryInfo category = palette.getCategories().get(1);
        assertEquals("categoryId.2", category.getId());
      }
    } finally {
      myTestProject.dispose();
    }
  }

  /**
   * Parsing for custom palette from project: use "jar".
   */
  @DisposeProjectAfter
  public void test_customPalette_project_7() throws Exception {
    // add JAR with palette
    {
      String jarPath =
          TestUtils.createTemporaryJar(
              "wbp-meta/" + TOOLKIT_ID + ".wbp-palette.xml",
              getSourceDQ(
                  "<palette>",
                  "  <category id='categoryId.1' name='name 1' description='desc 1'/>",
                  "</palette>"));
      ProjectUtils.addClasspathEntry(
          m_javaProject,
          JavaCore.newLibraryEntry(new Path(jarPath), null, null));
    }
    // palette in project
    setFileContent(
        "wbp-meta",
        TOOLKIT_ID + ".wbp-palette.xml",
        getSourceDQ(
            "<palette>",
            "  <category id='categoryId.2' name='name 2' description='desc 2'/>",
            "</palette>"));
    // load palette
    PaletteInfo palette = loadPalette();
    // check categories
    assertEquals(2, palette.getCategories().size());
    {
      CategoryInfo category = palette.getCategories().get(0);
      assertEquals("categoryId.1", category.getId());
    }
    {
      CategoryInfo category = palette.getCategories().get(1);
      assertEquals("categoryId.2", category.getId());
    }
  }

  /**
   * Test for "next" attribute for "category" tag.
   */
  public void test_customPalette_project_nextCategory() throws Exception {
    addPaletteExtension("palette_1", new String[]{
        "<category id='category_1' name='category_1'/>",
        "<category id='category_2' name='category_2'/>"});
    setFileContent(
        "wbp-meta",
        TOOLKIT_ID + ".wbp-palette.xml",
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<palette>",
            "  <category id='fromProject' name='fromProject' next='category_2'/>",
            "</palette>"));
    try {
      PaletteInfo palette = loadPalette();
      List<CategoryInfo> categories = palette.getCategories();
      assertThat(categories).hasSize(3);
      assertEquals("category_1", categories.get(0).getId());
      assertEquals("fromProject", categories.get(1).getId());
      assertEquals("category_2", categories.get(2).getId());
    } finally {
      removeToolkitExtension("palette_1");
    }
  }

  /**
   * We should ignore exceptions during parsing custom palettes, just log them as warnings.
   */
  @DisposeProjectAfter
  public void test_customPalette_project_whenOneFileMissing() throws Exception {
    // create new project "myProject"
    IJavaProject myJavaProject;
    TestProject myTestProject;
    {
      myTestProject = new TestProject("myProject");
      myJavaProject = myTestProject.getJavaProject();
      // create wbp-meta/palette
      {
        Path path = new Path("wbp-meta/" + TOOLKIT_ID + ".wbp-palette.xml");
        IFile file = myJavaProject.getProject().getFile(path);
        setFileContent(
            file,
            getSource(
                "<palette>",
                "  <category id='categoryId.1' name='name 1' description='desc 1'/>",
                "</palette>"));
        // delete!
        file.getLocation().toFile().delete();
      }
    }
    try {
      // add reference of "myProject" from "TestProject"
      ProjectUtils.requireProject(m_javaProject, myJavaProject);
      // create palette for "TestProject"
      setFileContent(
          "wbp-meta",
          TOOLKIT_ID + ".wbp-palette.xml",
          getSourceDQ(
              "<palette>",
              "  <category id='categoryId.2' name='name 2' description='desc 2'/>",
              "</palette>"));
      // load palette from "TestProject", so "myProject" also used
      PaletteInfo palette = loadPalette();
      // check categories: only ".2" category, because file with ".1" was deleted 
      assertThat(palette.getCategories()).hasSize(1);
      {
        CategoryInfo category = palette.getCategories().get(0);
        assertEquals("categoryId.2", category.getId());
      }
      // ... file with ".1" was deleted in FS, but Eclipse does not know about this, so warning
      {
        List<EditorWarning> warnings = m_lastState.getWarnings();
        assertThat(warnings).hasSize(1);
        EditorWarning warning = warnings.get(0);
        assertThat(warning.getMessage()).contains("myProject/wbp-meta/test.toolkit.wbp-palette.xml");
      }
    } finally {
      myTestProject.dispose();
    }
  }

  /**
   * Parsing for custom palette from project: no description in {@link ComponentEntryInfo}, so
   * description from *.wbp-component.xml should be used.
   */
  public void test_customPalette_project_descriptionFromProject() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  // filler filler filler filler",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <description>My description</description>",
            "</component>"));
    setFileContent(
        "wbp-meta",
        TOOLKIT_ID + ".wbp-palette.xml",
        getSourceDQ(
            "<palette>",
            "  <category id='categoryId.1' name='name 1'>",
            "    <component class='test.MyButton'/>",
            "  </category>",
            "</palette>"));
    waitForAutoBuild();
    // load palette
    PaletteInfo palette = loadPalette();
    CategoryInfo category = palette.getCategories().get(0);
    // component
    {
      ComponentEntryInfo component = (ComponentEntryInfo) category.getEntries().get(0);
      assertEquals("test.MyButton", component.getClassName());
      assertEquals("categoryId.1 test.MyButton", component.getId());
      assertTrue(component.isVisible());
      // attributes that are updated only after initialize()
      assertTrue(component.initialize(null, m_lastParseInfo));
      assertEquals("MyButton", component.getName());
      assertEquals("My description", component.getDescription());
    }
  }

  /**
   * Parsing for custom palette from project: "static-factory" and "method" elements.
   */
  public void test_customPalette_project_staticFactory() throws Exception {
    setFileContentSrc(
        "test/MyFactory.java",
        getTestSource(
            "public class MyFactory {",
            "  public static JButton createFirst() {",
            "    return new JButton();",
            "  }",
            "  public static JButton createSecond() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContent(
        "wbp-meta",
        TOOLKIT_ID + ".wbp-palette.xml",
        getSourceDQ(
            "<palette>",
            "  <category id='categoryId.1' name='name 1' description='desc 1'>",
            "    <static-factory class='test.MyFactory'>",
            "      <method signature='createFirst()'/>",
            "      <method signature='createSecond()' name='Some name' description='Some description'/>",
            "    </static-factory>",
            "  </category>",
            "</palette>"));
    waitForAutoBuild();
    // load palette
    PaletteInfo palette = loadPalette();
    CategoryInfo category = palette.getCategories().get(0);
    assertThat(category.getEntries()).hasSize(2);
    // createFirst()
    {
      StaticFactoryEntryInfo entry = (StaticFactoryEntryInfo) category.getEntries().get(0);
      assertTrue(entry.initialize(null, m_lastParseInfo));
      assertEquals("test.MyFactory", entry.getFactoryClassName());
      assertEquals("categoryId.1 test.MyFactory createFirst()", entry.getId());
      assertEquals("createFirst()", entry.getMethodSignature());
      assertEquals("createFirst()", entry.getName());
      assertEquals("Class: test.MyFactory<br/>Method: createFirst()", entry.getDescription());
    }
    // createSecond()
    {
      StaticFactoryEntryInfo entry = (StaticFactoryEntryInfo) category.getEntries().get(1);
      assertTrue(entry.initialize(null, m_lastParseInfo));
      assertEquals("test.MyFactory", entry.getFactoryClassName());
      assertEquals("categoryId.1 test.MyFactory createSecond()", entry.getId());
      assertEquals("createSecond()", entry.getMethodSignature());
      assertEquals("Some name", entry.getName());
      assertEquals("Some description", entry.getDescription());
    }
  }

  /**
   * Parsing for custom palette from project: "instance-factory" and "method" elements.
   */
  public void test_customPalette_project_instanceFactory() throws Exception {
    setFileContentSrc(
        "test/MyFactory.java",
        getTestSource(
            "public class MyFactory {",
            "  public JButton createFirst() {",
            "    return new JButton();",
            "  }",
            "  public JButton createSecond() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContent(
        "wbp-meta",
        TOOLKIT_ID + ".wbp-palette.xml",
        getSourceDQ(
            "<palette>",
            "  <category id='categoryId.1' name='name 1' description='desc 1'>",
            "    <instance-factory class='test.MyFactory'>",
            "      <method signature='createFirst()'/>",
            "      <method signature='createSecond()' name='Some name' description='Some description'/>",
            "    </instance-factory>",
            "  </category>",
            "</palette>"));
    waitForAutoBuild();
    // load palette
    PaletteInfo palette = loadPalette();
    CategoryInfo category = palette.getCategories().get(0);
    assertThat(category.getEntries()).hasSize(2);
    // createFirst()
    {
      InstanceFactoryEntryInfo entry = (InstanceFactoryEntryInfo) category.getEntries().get(0);
      assertTrue(entry.initialize(null, m_lastParseInfo));
      assertEquals("test.MyFactory", entry.getFactoryClassName());
      assertEquals("categoryId.1 test.MyFactory createFirst()", entry.getId());
      assertEquals("createFirst()", entry.getMethodSignature());
      assertEquals("createFirst()", entry.getName());
      assertEquals("Class: test.MyFactory<br/>Method: createFirst()", entry.getDescription());
    }
    // createSecond()
    {
      InstanceFactoryEntryInfo entry = (InstanceFactoryEntryInfo) category.getEntries().get(1);
      assertTrue(entry.initialize(null, m_lastParseInfo));
      assertEquals("test.MyFactory", entry.getFactoryClassName());
      assertEquals("categoryId.1 test.MyFactory createSecond()", entry.getId());
      assertEquals("createSecond()", entry.getMethodSignature());
      assertEquals("Some name", entry.getName());
      assertEquals("Some description", entry.getDescription());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conditions support
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_condition_true() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category' name='category'>",
        "  <component class='javax.swing.JButton' condition='version > 3.0'/>",
        "</category>"});
    // parse and configure EditorState
    JavaInfo panel = parseEmptyPanel();
    m_lastState.addVersions(ImmutableMap.<String, Object>of("version", "3.5"));
    //
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors(panel);
    CategoryInfo category = palette.getCategory("category");
    assertThat(category.getEntries()).hasSize(1);
  }

  public void test_condition_false() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category' name='category'>",
        "  <component class='javax.swing.JButton' condition='version > 3.0'/>",
        "</category>"});
    // parse and configure EditorState
    JavaInfo panel = parseEmptyPanel();
    m_lastState.addVersions(ImmutableMap.<String, Object>of("version", "2.1"));
    //
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors(panel);
    CategoryInfo category = palette.getCategory("category");
    assertThat(category.getEntries()).isEmpty();
  }

  public void test_condition_notBoolean() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category' name='category'>",
        "  <component class='javax.swing.JButton' condition='42'/>",
        "</category>"});
    // parse and configure EditorState
    JavaInfo panel = parseEmptyPanel();
    m_lastState.addVersions(ImmutableMap.<String, Object>of("version", "2.1"));
    //
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors(panel);
    CategoryInfo category = palette.getCategory("category");
    assertThat(category.getEntries()).isEmpty();
  }

  /**
   * Some entries are useful only in specific context.
   */
  public void test_condition_useRootModel() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category' name='category'>",
        "  <component class='javax.swing.JButton' condition='rootModel.getObject() is javax.swing.JPanel'/>",
        "</category>"});
    // parse
    JavaInfo panel = parseEmptyPanel();
    refresh();
    // load palette
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors(panel);
    CategoryInfo category = palette.getCategory("category");
    assertThat(category.getEntries()).hasSize(1);
  }

  public void test_condition_invalid() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category' name='category'>",
        "  <component class='javax.swing.JButton' condition='!@#$%'/>",
        "</category>"});
    // parse and configure EditorState
    JavaInfo panel = parseEmptyPanel();
    // invalid "condition", so no entry
    DesignerPlugin.setDisplayExceptionOnConsole(false);
    try {
      PaletteInfo palette = loadPalette(panel);
      CategoryInfo category = palette.getCategory("category");
      assertThat(category.getEntries()).isEmpty();
    } finally {
      DesignerPlugin.setDisplayExceptionOnConsole(true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "condition" for "category"
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_categoryCondition_true() throws Exception {
    addPaletteExtension(new String[]{"<category id='category' name='category' condition='true'/>"});
    // parse
    JavaInfo panel = parseEmptyPanel();
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors(panel);
    // one category
    assertThat(palette.getCategories()).hasSize(1);
  }

  public void test_categoryCondition_false() throws Exception {
    addPaletteExtension(new String[]{"<category id='category' name='category' condition='false'/>"});
    // parse
    JavaInfo panel = parseEmptyPanel();
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors(panel);
    // no category
    assertThat(palette.getCategories()).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "condition" for "palette"
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_paletteCondition_true() throws Exception {
    addToolkitExtension(PALETTE_EXTENSION_ID, new String[]{
        "<palette condition='true'>",
        "  <category id='theID' name='category'/>",
        "</palette>"});
    // parse
    JavaInfo panel = parseEmptyPanel();
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors(panel);
    // one category
    assertThat(palette.getCategories()).hasSize(1);
  }

  public void test_paletteCondition_false() throws Exception {
    addToolkitExtension(PALETTE_EXTENSION_ID, new String[]{
        "<palette condition='false'>",
        "  <category id='theID' name='category'/>",
        "</palette>"});
    // parse
    JavaInfo panel = parseEmptyPanel();
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors(panel);
    // no categories
    assertThat(palette.getCategories()).isEmpty();
  }
}
