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
import org.eclipse.wb.core.editor.palette.model.entry.ChooseComponentEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.editor.palette.PaletteManager;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.gef.UIPredicate;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests for {@link ChooseComponentEntryInfo}.
 *
 * @author scheglov_ke
 */
public class ChooseComponentEntryInfoTest extends AbstractPaletteTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_access() throws Exception {
    ChooseComponentEntryInfo entry = new ChooseComponentEntryInfo();
    assertNotNull(entry.getIcon());
    assertNotNull(entry.getName());
    assertNotNull(entry.getDescription());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_parse() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <entry id='system.chooseComponent' name='my name'"
            + " class='"
            + ChooseComponentEntryInfo.class.getName()
            + "'/>",
        "</category>"});
    PaletteInfo palette = loadPalette();
    // prepare entry
    CategoryInfo category = palette.getCategory("category_1");
    ChooseComponentEntryInfo entry = (ChooseComponentEntryInfo) category.getEntries().get(0);
    // check component
    assertSame(category, entry.getCategory());
    assertEquals("system.chooseComponent", entry.getId());
    assertEquals("my name", entry.getName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tool
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_createTool_cancel() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <entry id='system.chooseComponent' class='"
            + ChooseComponentEntryInfo.class.getName()
            + "'/>",
        "</category>"});
    JavaInfo panel = parseEmptyPanel();
    final PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
    manager.reloadPalette();
    // set palette site
    IPaletteSite.Helper.setSite(panel, new IPaletteSite.Empty() {
      @Override
      public Shell getShell() {
        return DesignerPlugin.getShell();
      }
    });
    // prepare entry
    final ChooseComponentEntryInfo entry;
    {
      PaletteInfo palette = manager.getPalette();
      CategoryInfo category = palette.getCategory("category_1");
      entry = (ChooseComponentEntryInfo) category.getEntries().get(0);
    }
    // initialize
    assertTrue(entry.initialize(null, panel));
    // create tool
    CreationTool creationTool;
    {
      final CreationTool[] tools = new CreationTool[1];
      new UiContext().executeAndCheck(new UIRunnable() {
        @Override
        public void run(UiContext context) throws Exception {
          tools[0] = (CreationTool) entry.createTool();
        }
      }, new UIRunnable() {
        @Override
        public void run(UiContext context) throws Exception {
          context.useShell("Open type");
          context.clickButton("Cancel");
        }
      });
      creationTool = tools[0];
    }
    // check tool
    assertNull(creationTool);
  }

  public void test_createTool_select() throws Exception {
    addPaletteExtension(new String[]{
        "<category id='category_1' name='category 1'>",
        "  <entry id='system.chooseComponent' class='"
            + ChooseComponentEntryInfo.class.getName()
            + "'/>",
        "</category>"});
    JavaInfo panel = parseEmptyPanel();
    final PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
    manager.reloadPalette();
    // set palette site
    IPaletteSite.Helper.setSite(panel, new IPaletteSite.Empty() {
      @Override
      public Shell getShell() {
        return DesignerPlugin.getShell();
      }

      @Override
      public PaletteInfo getPalette() {
        return manager.getPalette();
      }

      @Override
      public void addCommand(Command command) {
        manager.commands_add(command);
      }
    });
    // prepare entry
    final ChooseComponentEntryInfo entry;
    {
      PaletteInfo palette = manager.getPalette();
      CategoryInfo category = palette.getCategory("category_1");
      entry = (ChooseComponentEntryInfo) category.getEntries().get(0);
    }
    // initialize
    assertTrue(entry.initialize(null, panel));
    // create tool
    CreationTool creationTool;
    {
      final CreationTool[] tools = new CreationTool[1];
      new UiContext().executeAndCheck(new UIRunnable() {
        @Override
        public void run(UiContext context) throws Exception {
          tools[0] = (CreationTool) entry.createTool();
        }
      }, new UIRunnable() {
        @Override
        public void run(UiContext context) throws Exception {
          animateChooseType(context, "JButton");
          context.clickButton("OK");
        }
      });
      creationTool = tools[0];
    }
    // check tool
    {
      ICreationFactory creationFactory = creationTool.getFactory();
      creationFactory.activate();
      // check new object
      JavaInfo javaInfo = (JavaInfo) creationFactory.getNewObject();
      assertEquals(
          "new javax.swing.JButton(\"New button\")",
          javaInfo.getCreationSupport().add_getSource(null));
    }
  }

  /**
   * In GWT it is possible following situation: user opens form, so its {@link ClassLoader} created.
   * Then he creates new <code>Composite</code> and tries to use in already opened form. This does
   * not work, because new component has {@link IType}, but {@link ClassLoader} is already fixed, we
   * can not add new {@link Class} into it.
   * <p>
   * So, we detect {@link IType} presence and ask about reparse.
   */
  @DisposeProjectAfter
  // Test may get stuck on the Linux build...
  public void DISABLE_test_createTool_inProject_butNotInClassLoader() throws Exception {
    JavaInfo panel = parseEmptyPanel();
    // set palette site
    {
      final PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
      manager.reloadPalette();
      IPaletteSite.Helper.setSite(panel, new IPaletteSite.Empty() {
        @Override
        public Shell getShell() {
          return DesignerPlugin.getShell();
        }

        @Override
        public PaletteInfo getPalette() {
          return manager.getPalette();
        }

        @Override
        public void addCommand(Command command) {
          manager.commands_add(command);
        }
      });
    }
    // set page site
    final AtomicBoolean reparsed = new AtomicBoolean();
    DesignPageSite.Helper.setSite(panel, new DesignPageSite() {
      @Override
      public void reparse() {
        reparsed.set(true);
      }
    });
    // add new project, so its IType is visible, but not in ClassLoader
    TestProject newProject = new TestProject("NewProject");
    try {
      setFileContentSrc(
          newProject.getProject(),
          "my/classes/MyClass.java",
          getSource(
              "// filler filler filler filler filler",
              "package my.classes;",
              "public class MyClass {",
              "}"));
      m_testProject.addRequiredProject(newProject);
      waitForAutoBuild();
      // prepare entry
      final ChooseComponentEntryInfo entry = new ChooseComponentEntryInfo();
      assertTrue(entry.initialize(null, panel));
      // create tool
      {
        final CreationTool[] tools = new CreationTool[1];
        new UiContext().executeAndCheck(new UIRunnable() {
          @Override
          public void run(UiContext context) throws Exception {
            tools[0] = (CreationTool) entry.createTool();
          }
        }, new UIRunnable() {
          @Override
          public void run(UiContext context) throws Exception {
            animateChooseType(context, "MyClass");
            clickOK_andConfirmReparse(context);
          }

          private void clickOK_andConfirmReparse(final UiContext okContext) throws Exception {
            new UiContext().executeAndCheck(new UIRunnable() {
              @Override
              public void run(UiContext context) throws Exception {
                okContext.clickButton("OK");
              }
            }, new UIRunnable() {
              @Override
              public void run(UiContext context) throws Exception {
                context.useShell("Unable to load component");
                context.clickButton("Yes");
              }
            });
          }
        });
      }
    } finally {
      newProject.dispose();
    }
    // should be reparsed
    assertTrue(reparsed.get());
  }

  private static void animateChooseType(UiContext context, String className) throws Exception {
    context.useShell("Open type");
    // set filter
    {
      Text filterText = context.findFirstWidget(Text.class);
      filterText.setText(className);
    }
    // wait for types
    {
      final Table typesTable = context.findFirstWidget(Table.class);
      context.waitFor(new UIPredicate() {
        @Override
        public boolean check() {
          return typesTable.getItems().length != 0;
        }
      });
    }
  }
}
