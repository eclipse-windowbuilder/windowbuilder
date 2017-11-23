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
package org.eclipse.wb.tests.designer.XML.palette;

import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.xml.editor.palette.PaletteManager;
import org.eclipse.wb.internal.core.xml.editor.palette.command.CategoryAddCommand;
import org.eclipse.wb.internal.core.xml.editor.palette.model.CategoryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.ComponentEntryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.EntryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.PaletteInfo;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
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

import java.io.File;
import java.util.List;

/**
 * Tests for {@link PaletteManager}.
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
    XmlObjectInfo panel = parseEmptyPanel();
    PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
    // access
    assertEquals(TOOLKIT_ID, manager.getToolkitId());
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
          "  <component class='org.eclipse.swt.widgets.Button'/>",
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
        assertEquals("category_1 org.eclipse.swt.widgets.Button", componentEntry.getId());
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
   * Test for {@link PaletteManager#getPalette()}.
   */
  public void test_getPaletteCopy() throws Exception {
    addPaletteExtension(new String[]{"<category id='category_1' name='category 1'/>"});
    XmlObjectInfo panel = parseEmptyPanel();
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

  /**
   * Test for parsing "x-entry" element.
   */
  public void test_parse_xEntry() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <x-entry id='myId' name='myName' "
            + "class='org.eclipse.wb.internal.core.xml.editor.palette.model.SelectionToolEntryInfo'/>",
        "</category>"});
    EntryInfo entry = loadSingleEntry("myId");
    assertEquals("myName", entry.getName());
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
        "<component class='org.eclipse.swt.widgets.Button' category='category_1'/>"});
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
        "<component class='org.eclipse.swt.widgets.Button'/>"});
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
  // Export/Import
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link PaletteManager#exportTo(String)}.
   */
  public void test_exportTo() throws Exception {
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
      // has category
      assertEquals(1, palette.getCategories().size());
    }
    // export commands
    String exportPath;
    {
      File exportFile = File.createTempFile("wbpTests", ".xml");
      exportPath = exportFile.getAbsolutePath();
      manager.exportTo(exportPath);
    }
    // clear commands
    {
      manager.commands_clear();
      manager.commands_write();
      manager.reloadPalette();
      // no categories
      PaletteInfo palette = manager.getPalette();
      assertEquals(0, palette.getCategories().size());
    }
    // import commands
    {
      manager.importFrom(exportPath);
      manager.reloadPalette();
      // no categories
      PaletteInfo palette = manager.getPalette();
      assertEquals(1, palette.getCategories().size());
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
            "    <component class='org.eclipse.swt.widgets.Button'/>",
            "    <component class='org.eclipse.swt.widgets.Text'/>",
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
          assertEquals("org.eclipse.swt.widgets.Button", component.getClassName());
          assertEquals("categoryId.1 org.eclipse.swt.widgets.Button", component.getId());
          assertTrue(component.isVisible());
          // attributes that are updated only after initialize()
          assertTrue(component.initialize(null, m_lastObject));
          assertEquals("Button", component.getName());
          assertEquals("Instances of this class represent a selectable user interface object"
              + " that issues notification when pressed and released.", component.getDescription());
        }
        {
          ComponentEntryInfo component = (ComponentEntryInfo) category.getEntries().get(1);
          assertEquals("org.eclipse.swt.widgets.Text", component.getClassName());
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
   * Parsing for custom palette from project: specific values for {@link ComponentEntryInfo}.
   */
  public void test_customPalette_project_2() throws Exception {
    setFileContent(
        "wbp-meta",
        TOOLKIT_ID + ".wbp-palette.xml",
        getSourceDQ(
            "<palette>",
            "  <category id='categoryId.1' name='name 1' description='desc 1'>",
            "    <component id='myButton.id' class='org.eclipse.swt.widgets.Button' name='my name' description='my desc' visible='false'/>",
            "  </category>",
            "</palette>"));
    // load palette
    PaletteInfo palette = loadPalette();
    CategoryInfo category = palette.getCategories().get(0);
    // component
    {
      ComponentEntryInfo component = (ComponentEntryInfo) category.getEntries().get(0);
      assertEquals("org.eclipse.swt.widgets.Button", component.getClassName());
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
            "    <component creationId='creationId' class='org.eclipse.swt.widgets.Button'/>",
            "  </category>",
            "</palette>"));
    // load palette
    PaletteInfo palette = loadPalette();
    CategoryInfo category = palette.getCategories().get(0);
    // component
    {
      ComponentEntryInfo component = (ComponentEntryInfo) category.getEntries().get(0);
      assertEquals("org.eclipse.swt.widgets.Button", component.getClassName());
      assertEquals("categoryId.1 org.eclipse.swt.widgets.Button creationId", component.getId());
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
            "    <component no-class-attribute='org.eclipse.swt.widgets.Button'/>",
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
            "  <component class='org.eclipse.swt.widgets.Button' category='categoryId.1'/>",
            "</palette>"));
    // load palette
    PaletteInfo palette = loadPalette();
    CategoryInfo category = palette.getCategories().get(0);
    // component
    {
      ComponentEntryInfo component = (ComponentEntryInfo) category.getEntries().get(0);
      assertEquals("org.eclipse.swt.widgets.Button", component.getClassName());
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
        List<EditorWarning> warnings = m_lastContext.getWarnings();
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
    prepareMyComponent(new String[]{}, new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<description>My description</description>"});
    setFileContent(
        "wbp-meta",
        TOOLKIT_ID + ".wbp-palette.xml",
        getSourceDQ(
            "<palette>",
            "  <category id='categoryId.1' name='name 1'>",
            "    <component class='test.MyComponent'/>",
            "  </category>",
            "</palette>"));
    waitForAutoBuild();
    // load palette
    PaletteInfo palette = loadPalette();
    CategoryInfo category = palette.getCategories().get(0);
    // component
    {
      ComponentEntryInfo component = (ComponentEntryInfo) category.getEntries().get(0);
      assertEquals("test.MyComponent", component.getClassName());
      assertEquals("categoryId.1 test.MyComponent", component.getId());
      assertTrue(component.isVisible());
      // attributes that are updated only after initialize()
      assertTrue(component.initialize(null, m_lastObject));
      assertEquals("MyComponent", component.getName());
      assertEquals("My description", component.getDescription());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conditions support
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_componentCondition_true() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category' name='category'>",
        "  <component class='org.eclipse.swt.widgets.Button' condition='version > 3.0'/>",
        "</category>"});
    // parse and configure EditorState
    XmlObjectInfo panel = parseEmptyPanel();
    m_lastContext.addVersions(ImmutableMap.<String, Object>of("version", "3.5"));
    //
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors();
    CategoryInfo category = palette.getCategory("category");
    assertThat(category.getEntries()).hasSize(1);
  }

  public void test_componentCondition_false() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category' name='category'>",
        "  <component class='org.eclipse.swt.widgets.Button' condition='version > 3.0'/>",
        "</category>"});
    // parse and configure EditorState
    XmlObjectInfo panel = parseEmptyPanel();
    m_lastContext.addVersions(ImmutableMap.<String, Object>of("version", "2.1"));
    //
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors();
    CategoryInfo category = palette.getCategory("category");
    assertThat(category.getEntries()).isEmpty();
  }

  public void test_componentCondition_notBoolean() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category' name='category'>",
        "  <component class='org.eclipse.swt.widgets.Button' condition='42'/>",
        "</category>"});
    // parse and configure EditorState
    XmlObjectInfo panel = parseEmptyPanel();
    m_lastContext.addVersions(ImmutableMap.of("version", "2.1"));
    //
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors();
    CategoryInfo category = palette.getCategory("category");
    assertThat(category.getEntries()).isEmpty();
  }

  /**
   * Some entries are useful only in specific context.
   */
  public void test_componentCondition_useRootModel() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category' name='category'>",
        "  <component class='org.eclipse.swt.widgets.Button'"
            + " condition='rootModel.getObject() is org.eclipse.swt.widgets.Shell'/>",
        "</category>"});
    // parse
    XmlObjectInfo panel = parseEmptyPanel();
    refresh();
    // load palette
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors();
    CategoryInfo category = palette.getCategory("category");
    assertThat(category.getEntries()).hasSize(1);
  }

  public void test_componentCondition_invalid() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category' name='category'>",
        "  <component class='org.eclipse.swt.widgets.Button' condition='!@#$%'/>",
        "</category>"});
    // parse and configure EditorState
    XmlObjectInfo panel = parseEmptyPanel();
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
    XmlObjectInfo panel = parseEmptyPanel();
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors();
    // one category
    assertThat(palette.getCategories()).hasSize(1);
  }

  public void test_categoryCondition_false() throws Exception {
    addPaletteExtension(new String[]{"<category id='category' name='category' condition='false'/>"});
    // parse
    XmlObjectInfo panel = parseEmptyPanel();
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors();
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
    XmlObjectInfo panel = parseEmptyPanel();
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors();
    // one category
    assertThat(palette.getCategories()).hasSize(1);
  }

  public void test_paletteCondition_false() throws Exception {
    addToolkitExtension(PALETTE_EXTENSION_ID, new String[]{
        "<palette condition='false'>",
        "  <category id='theID' name='category'/>",
        "</palette>"});
    // parse
    XmlObjectInfo panel = parseEmptyPanel();
    PaletteInfo palette = loadPalette(panel);
    assertNoErrors();
    // no categories
    assertThat(palette.getCategories()).isEmpty();
  }
}
