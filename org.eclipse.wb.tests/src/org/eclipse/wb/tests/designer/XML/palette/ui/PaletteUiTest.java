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
package org.eclipse.wb.tests.designer.XML.palette.ui;

import org.eclipse.wb.core.controls.palette.PaletteComposite;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.editor.palette.PaletteEventListener;
import org.eclipse.wb.internal.core.xml.editor.palette.command.CategoryRemoveCommand;
import org.eclipse.wb.internal.core.xml.editor.palette.command.ComponentAddCommand;
import org.eclipse.wb.internal.core.xml.editor.palette.model.CategoryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.ComponentEntryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.EntryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.PaletteInfo;
import org.eclipse.wb.tests.gef.UIPredicate;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

/**
 * Test for palette UI.
 * 
 * @author scheglov_ke
 */
public class PaletteUiTest extends AbstractPaletteUiTest {
  private String m_newId;

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
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Touch no-op implementations of {@link PaletteEventListener}.
   */
  public void test_PaletteEventListener() throws Exception {
    PaletteEventListener listener = new PaletteEventListener() {
    };
    listener.categories(null);
    listener.categories2(null);
    listener.entries(null, null);
  }

  public void test_restorePalette() throws Exception {
    openEditor("<Shell/>");
    // remove "System"
    {
      CategoryInfo category = m_paletteManager.getPalette().getCategories().get(0);
      m_paletteManager.commands_add(new CategoryRemoveCommand(category));
    }
    // no "System"
    {
      PaletteInfo palette = m_paletteManager.getPalette();
      CategoryInfo category = palette.getCategories().get(0);
      assertThat(category.getName()).isNotEqualTo("System");
    }
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Restore default palette...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Confirm");
        context.clickButton("OK");
      }
    });
    // "System" is again in palette
    {
      PaletteInfo palette = m_paletteManager.getPalette();
      CategoryInfo category = palette.getCategories().get(0);
      assertThat(category.getName()).isEqualTo("System");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Settings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for configuring palette settings.
   */
  public void test_settings() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Settings...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette settings");
        context.clickButton("OK");
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Category
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_addCategory_beforeFirst() throws Exception {
    openEditor("<Shell/>");
    // use "System" as target
    Object target;
    {
      target = m_paletteManager.getPalette().getCategories().get(0);
      setTargetObject(target);
    }
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Add category...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("New palette category");
        m_newId = context.getTextByLabel("&ID:").getText();
        context.getTextByLabel("&Name:").setText("New category");
        context.clickButton("OK");
      }
    });
    // check added category
    {
      PaletteInfo palette = m_paletteManager.getPalette();
      CategoryInfo newCategory = palette.getCategory(m_newId);
      assertNotNull(newCategory);
      assertEquals("New category", newCategory.getName());
      assertThat(newCategory.getEntries()).isEmpty();
      // it is first
      assertSame(newCategory, palette.getCategories().get(0));
      assertSame(target, palette.getCategories().get(1));
    }
  }

  public void test_addCategory_asLast() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Add category...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("New palette category");
        m_newId = context.getTextByLabel("&ID:").getText();
        context.getTextByLabel("&Name:").setText("New category");
        context.clickButton("OK");
      }
    });
    // check added category
    {
      PaletteInfo palette = m_paletteManager.getPalette();
      CategoryInfo newCategory = palette.getCategory(m_newId);
      assertNotNull(newCategory);
      // it is last (not first)
      List<CategoryInfo> categories = palette.getCategories();
      assertNotSame(newCategory, categories.get(0));
    }
  }

  public void test_editCategory() throws Exception {
    openEditor("<Shell/>");
    // use "System" as target
    {
      CategoryInfo category = m_paletteManager.getPalette().getCategories().get(0);
      setTargetObject(category);
      // initial name
      assertEquals("System", category.getName());
    }
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Edit...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Edit palette category");
        context.getTextByLabel("&Name:").setText("System2");
        context.clickButton("OK");
      }
    });
    // updated name
    PaletteInfo palette = m_paletteManager.getPalette();
    CategoryInfo category = palette.getCategories().get(0);
    assertEquals("System2", category.getName());
  }

  public void test_removeCategory() throws Exception {
    openEditor("<Shell/>");
    // use "System" as target
    {
      CategoryInfo category = m_paletteManager.getPalette().getCategories().get(0);
      setTargetObject(category);
    }
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Remove");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Confirm");
        context.clickButton("OK");
      }
    });
    // no "System"
    PaletteInfo palette = m_paletteManager.getPalette();
    CategoryInfo category = palette.getCategories().get(0);
    assertThat(category.getName()).isNotEqualTo("System");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Component
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_addComponent() throws Exception {
    openEditor("<Shell/>");
    // use "System" as target
    {
      CategoryInfo category = m_paletteManager.getPalette().getCategories().get(0);
      setTargetObject(category);
      // 3 entry initially
      assertThat(category.getEntries()).hasSize(3);
    }
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Add component...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Add component");
        chooseComponentClass(context, "org.eclipse.swt.widgets.Button");
        // default name
        assertEquals("Button", context.getTextByLabel("&Name:").getText());
        assertThat(context.getTextByLabel("&Description:").getText()).contains(
            "pressed and released");
        // set custom name
        context.getTextByLabel("&Name:").setText("My name");
        // done
        m_newId = context.getTextByLabel("&ID:").getText();
        context.clickButton("OK");
      }
    });
    // new entry added
    CategoryInfo category = m_paletteManager.getPalette().getCategories().get(0);
    List<EntryInfo> entries = category.getEntries();
    assertThat(entries).hasSize(4);
    {
      ComponentEntryInfo newComponent = (ComponentEntryInfo) entries.get(3);
      assertEquals("org.eclipse.swt.widgets.Button", newComponent.getClassName());
      assertEquals("My name", newComponent.getName());
    }
  }

  public void test_editComponent() throws Exception {
    openEditor("<Shell/>");
    // add Button component to "System" and use it as target
    {
      CategoryInfo category = m_paletteManager.getPalette().getCategories().get(0);
      // add Button component
      m_paletteManager.commands_add(new ComponentAddCommand("myID",
          "My name",
          "My desc",
          true,
          "org.eclipse.swt.widgets.Button",
          category));
      assertThat(category.getEntries()).hasSize(4);
      ReflectionUtils.invokeMethod(m_designerPalette, "showPalette()");
      setTargetObject(category.getEntries().get(3));
    }
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Edit...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Edit component");
        assertEquals("My name", context.getTextByLabel("&Name:").getText());
        assertEquals("My desc", context.getTextByLabel("&Description:").getText());
        // choose Text widget
        chooseComponentClass(context, "org.eclipse.swt.widgets.Text");
        // done
        context.clickButton("OK");
      }
    });
    // entry was updated
    CategoryInfo category = m_paletteManager.getPalette().getCategories().get(0);
    ComponentEntryInfo editedComponent = (ComponentEntryInfo) category.getEntries().get(3);
    assertEquals("org.eclipse.swt.widgets.Text", editedComponent.getClassName());
    assertEquals("Text", editedComponent.getName());
    assertThat(editedComponent.getDescription()).contains("enter and modify text");
  }

  private void chooseComponentClass(final UiContext context_, final String className)
      throws Exception {
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context_.clickButton("Ch&oose...");
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        // set filter
        {
          context.useShell("Open type");
          Text filterText = context.findFirstWidget(Text.class);
          filterText.setText(className);
        }
        // wait for types
        {
          final Table typesTable = context.findFirstWidget(Table.class);
          context.waitFor(new UIPredicate() {
            public boolean check() {
              return typesTable.getItems().length != 0;
            }
          });
        }
        // click OK
        context.clickButton("OK");
      }
    });
  }

  public void test_removeEntry() throws Exception {
    openEditor("<Shell/>");
    // use "System" as target
    {
      CategoryInfo category = m_paletteManager.getPalette().getCategories().get(0);
      EntryInfo entry = category.getEntries().get(0);
      assertEquals("Selection", entry.getName());
      setTargetObject(entry);
    }
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Remove");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Confirm");
        context.clickButton("OK");
      }
    });
    // no "Selection" in "System"
    PaletteInfo palette = m_paletteManager.getPalette();
    CategoryInfo category = palette.getCategories().get(0);
    EntryInfo entry = category.getEntries().get(0);
    assertThat(entry.getName()).isNotEqualTo("Selection");
  }

  public void test_paletteManager_dragEntry_onCategory() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        TreeItem selectionItem = context.getTreeItem("Selection");
        TreeItem compositesItem = context.getTreeItem("Composites");
        Tree paletteTree = selectionItem.getParent();
        // do DND
        TreeDragHelper dragHelper = new TreeDragHelper(paletteTree);
        dragHelper.startDrag(selectionItem);
        dragHelper.dragOn(compositesItem);
        dragHelper.endDrag();
        // done
        context.clickButton("OK");
      }
    });
    // "Selection" is last in "Composites"
    CategoryInfo compositesCategory = m_paletteManager.getPalette().getCategories().get(1);
    assertEquals("Composites", compositesCategory.getName());
    List<EntryInfo> entries = compositesCategory.getEntries();
    assertEquals("Selection", entries.get(entries.size() - 1).getName());
  }

  public void test_paletteManager_dragEntry_beforeOther() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        TreeItem selectionItem = context.getTreeItem("Selection");
        TreeItem groupItem = context.getTreeItem("Group");
        Tree paletteTree = selectionItem.getParent();
        // do DND
        TreeDragHelper dragHelper = new TreeDragHelper(paletteTree);
        dragHelper.startDrag(selectionItem);
        dragHelper.dragBefore(groupItem);
        dragHelper.endDrag();
        // done
        context.clickButton("OK");
      }
    });
    // "Selection" is 1-th in "Composites"
    CategoryInfo compositesCategory = m_paletteManager.getPalette().getCategories().get(1);
    List<EntryInfo> entries = compositesCategory.getEntries();
    assertEquals("Selection", entries.get(1).getName());
  }

  public void test_paletteManager_dragEntry_afterOther() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        TreeItem selectionItem = context.getTreeItem("Selection");
        TreeItem groupItem = context.getTreeItem("Group");
        Tree paletteTree = selectionItem.getParent();
        // do DND
        TreeDragHelper dragHelper = new TreeDragHelper(paletteTree);
        dragHelper.startDrag(selectionItem);
        dragHelper.dragAfter(groupItem);
        dragHelper.endDrag();
        // done
        context.clickButton("OK");
      }
    });
    // "Selection" is 2-th in "Composites"
    CategoryInfo compositesCategory = m_paletteManager.getPalette().getCategories().get(1);
    List<EntryInfo> entries = compositesCategory.getEntries();
    assertEquals("Selection", entries.get(2).getName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette manager dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_paletteManager_filter() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        // filter
        {
          assertNotNull(context.getTreeItem("Button"));
          assertNotNull(context.getTreeItem("Table"));
          // set filer
          Text filterText = context.findFirstWidget(Text.class);
          assertNotNull(filterText);
          filterText.setText("Button");
          // only Button
          assertNotNull(context.getTreeItem("Button"));
          assertNull(context.getTreeItem("Table"));
          // clear filer
          context.click(context.getToolItem("Clear"), 0);
          assertNotNull(context.getTreeItem("Button"));
          assertNotNull(context.getTreeItem("Table"));
        }
        // done
        context.clickButton("OK");
      }
    });
  }

  public void test_paletteManager_expandCollapse() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        // "System" initially collapsed
        TreeItem systemItem = context.getTreeItem("System");
        assertFalse(systemItem.getExpanded());
        // expand all
        context.clickButton("Expand All");
        assertTrue(systemItem.getExpanded());
        // collapse all
        context.clickButton("Collapse All");
        assertFalse(systemItem.getExpanded());
        // done
        context.clickButton("OK");
      }
    });
  }

  public void test_paletteManager_dragCategory() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        TreeItem systemItem = context.getTreeItem("System");
        TreeItem compositesItem = context.getTreeItem("Composites");
        Tree paletteTree = systemItem.getParent();
        // do DND
        TreeDragHelper dragHelper = new TreeDragHelper(paletteTree);
        dragHelper.startDrag(systemItem);
        dragHelper.dragAfter(compositesItem);
        dragHelper.endDrag();
        // done
        context.clickButton("OK");
      }
    });
    // "System" is with index "1"
    assertEquals("Composites", m_paletteManager.getPalette().getCategories().get(0).getName());
    assertEquals("System", m_paletteManager.getPalette().getCategories().get(1).getName());
  }

  public void test_paletteManager_addCategory() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        // prepare palette Tree
        Tree paletteTree = context.findFirstWidget(Tree.class);
        int oldItemCount = paletteTree.getItemCount();
        // add new category using dialog
        animateAddCategoryDialog(context);
        // new category was added as last
        int newItemCount = paletteTree.getItemCount();
        assertEquals(oldItemCount + 1, newItemCount);
        assertEquals("New category", paletteTree.getItem(newItemCount - 1).getText());
        // done
        context.clickButton("OK");
      }

      private void animateAddCategoryDialog(final UiContext _context) throws Exception {
        new UiContext().executeAndCheck(new UIRunnable() {
          public void run(UiContext context) throws Exception {
            _context.clickButton("Add Category...");
          }
        }, new UIRunnable() {
          public void run(UiContext context) throws Exception {
            context.useShell("New palette category");
            m_newId = context.getTextByLabel("&ID:").getText();
            context.getTextByLabel("&Name:").setText("New category");
            context.clickButton("OK");
          }
        });
      }
    });
    // check added category
    {
      PaletteInfo palette = m_paletteManager.getPalette();
      CategoryInfo newCategory = palette.getCategory(m_newId);
      assertNotNull(newCategory);
    }
  }

  public void test_paletteManager_addComponent() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        // prepare palette Tree
        TreeItem systemItem = context.getTreeItem("System");
        int oldItemCount = systemItem.getItemCount();
        UiContext.setSelection(systemItem);
        // add new component using dialog
        Button addEntryButton = context.getButtonByText("Add Entry >>");
        context.click(addEntryButton);
        MenuManager menuManager = (MenuManager) addEntryButton.getData("menuManager");
        IAction action = findChildAction(menuManager, "Component...");
        assertNotNull(action);
        animateAddComponentDialog(action);
        // new component was added as last
        int newItemCount = systemItem.getItemCount();
        assertEquals(oldItemCount + 1, newItemCount);
        assertEquals("Button", systemItem.getItem(newItemCount - 1).getText());
        // done
        context.clickButton("OK");
      }

      private void animateAddComponentDialog(final IAction action) throws Exception {
        new UiContext().executeAndCheck(new UIRunnable() {
          public void run(UiContext context) throws Exception {
            action.run();
          }
        }, new UIRunnable() {
          public void run(UiContext context) throws Exception {
            context.useShell("Add component");
            chooseComponentClass(context, "org.eclipse.swt.widgets.Button");
            context.clickButton("OK");
          }
        });
      }
    });
    // check added component
    {
      PaletteInfo palette = m_paletteManager.getPalette();
      CategoryInfo category = palette.getCategories().get(0);
      List<EntryInfo> entries = category.getEntries();
      ComponentEntryInfo newEntry = (ComponentEntryInfo) entries.get(entries.size() - 1);
      assertEquals("org.eclipse.swt.widgets.Button", newEntry.getClassName());
    }
  }

  public void test_paletteManager_editCategory() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        // set selection
        TreeItem systemItem = context.getTreeItem("System");
        UiContext.setSelection(systemItem);
        // edit category
        animateEditCategoryDialog(context);
        // name was changed
        assertEquals("System edited", systemItem.getText());
        // done
        context.clickButton("OK");
      }

      private void animateEditCategoryDialog(final UiContext _context) throws Exception {
        new UiContext().executeAndCheck(new UIRunnable() {
          public void run(UiContext context) throws Exception {
            _context.clickButton("Edit...");
          }
        }, new UIRunnable() {
          public void run(UiContext context) throws Exception {
            context.useShell("Edit palette category");
            context.getTextByLabel("&Name:").setText("System edited");
            context.clickButton("OK");
          }
        });
      }
    });
    // check edited category
    {
      PaletteInfo palette = m_paletteManager.getPalette();
      CategoryInfo category = palette.getCategories().get(0);
      assertEquals("System edited", category.getName());
    }
  }

  public void test_paletteManager_editComponent() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        // prepare palette Tree
        TreeItem compositesItem = context.getTreeItem("Composites");
        TreeItem compositeItem = compositesItem.getItem(0);
        UiContext.setSelection(compositeItem);
        // edit component
        animateEditComponentDialog(context);
        // name was changed
        assertEquals("Composite edited", compositeItem.getText());
        // done
        context.clickButton("OK");
      }

      private void animateEditComponentDialog(final UiContext _context) throws Exception {
        new UiContext().executeAndCheck(new UIRunnable() {
          public void run(UiContext context) throws Exception {
            _context.clickButton("Edit...");
          }
        }, new UIRunnable() {
          public void run(UiContext context) throws Exception {
            context.useShell("Edit component");
            context.getTextByLabel("&Name:").setText("Composite edited");
            context.clickButton("OK");
          }
        });
      }
    });
    // check edited component
    {
      PaletteInfo palette = m_paletteManager.getPalette();
      CategoryInfo category = palette.getCategory("org.eclipse.wb.rcp.composites");
      EntryInfo component = category.getEntries().get(0);
      assertEquals("Composite edited", component.getName());
    }
  }

  public void test_paletteManager_moveCategory() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        // set selection
        Tree paletteTree = context.findFirstWidget(Tree.class);
        {
          TreeItem systemItem = paletteTree.getItem(0);
          assertEquals("System", systemItem.getText());
          UiContext.setSelection(systemItem);
        }
        // move category
        context.clickButton("Down");
        assertEquals("System", paletteTree.getItem(1).getText());
        // done
        context.clickButton("OK");
      }
    });
    // "System" moved
    {
      PaletteInfo palette = m_paletteManager.getPalette();
      CategoryInfo category = palette.getCategories().get(1);
      assertEquals("System", category.getName());
    }
  }

  public void test_paletteManager_moveEntry() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        // select "Selection"
        TreeItem systemItem = context.getTreeItem("System");
        {
          TreeItem selectionItem = systemItem.getItem(0);
          assertEquals("Selection", selectionItem.getText());
          UiContext.setSelection(selectionItem);
        }
        // move entry
        context.clickButton("Down");
        assertEquals("Selection", systemItem.getItem(1).getText());
        // done
        context.clickButton("OK");
      }
    });
    // "Selection" moved in "System"
    {
      PaletteInfo palette = m_paletteManager.getPalette();
      CategoryInfo category = palette.getCategories().get(0);
      EntryInfo entry = category.getEntries().get(1);
      assertEquals("Selection", entry.getName());
    }
  }

  public void test_paletteManager_removeCategory() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        // set selection
        TreeItem systemItem = context.getTreeItem("System");
        UiContext.setSelection(systemItem);
        // edit category
        animateRemoveDialog(context);
        // no "System"
        assertNull(context.getTreeItem("System"));
        // done
        context.clickButton("OK");
      }

      private void animateRemoveDialog(final UiContext _context) throws Exception {
        new UiContext().executeAndCheck(new UIRunnable() {
          public void run(UiContext context) throws Exception {
            _context.clickButton("Remove...");
          }
        }, new UIRunnable() {
          public void run(UiContext context) throws Exception {
            context.useShell("Confirm");
            context.clickButton("OK");
          }
        });
      }
    });
    // no "System"
    {
      PaletteInfo palette = m_paletteManager.getPalette();
      CategoryInfo category = palette.getCategories().get(0);
      assertThat(category.getName()).isNotEqualTo("System");
    }
  }

  public void test_paletteManager_removeEntry() throws Exception {
    openEditor("<Shell/>");
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        IAction action = getContextMenuAction("Palette manager...");
        assertNotNull(action);
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Palette Manager");
        // set selection
        TreeItem selectionItem = context.getTreeItem("Selection");
        assertNotNull(selectionItem);
        UiContext.setSelection(selectionItem);
        // edit category
        animateRemoveDialog(context);
        // no "Selection"
        assertNull(context.getTreeItem("Selection"));
        // done
        context.clickButton("OK");
      }

      private void animateRemoveDialog(final UiContext _context) throws Exception {
        new UiContext().executeAndCheck(new UIRunnable() {
          public void run(UiContext context) throws Exception {
            _context.clickButton("Remove...");
          }
        }, new UIRunnable() {
          public void run(UiContext context) throws Exception {
            context.useShell("Confirm");
            context.clickButton("OK");
          }
        });
      }
    });
    // no "Selection" in "System"
    {
      PaletteInfo palette = m_paletteManager.getPalette();
      CategoryInfo category = palette.getCategories().get(0);
      EntryInfo entry = category.getEntries().get(0);
      assertThat(entry.getName()).isNotEqualTo("Selection");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets visual object for given non-visual as target.
   */
  private void setTargetObject(Object nonVisual) throws Exception {
    Object visual = getTargetObjectVisual(nonVisual);
    ReflectionUtils.setField(m_paletteComposite, "m_forcedTargetObject", visual);
  }

  /**
   * {@link PaletteComposite} works with "visual" objects, so we need to convert non-visual into
   * visual.
   */
  private Object getTargetObjectVisual(Object nonVisual) {
    if (nonVisual instanceof CategoryInfo) {
      return ((Map<?, ?>) ReflectionUtils.getFieldObject(
          m_designerPalette,
          "m_categoryInfoToVisual")).get(nonVisual);
    }
    if (nonVisual instanceof EntryInfo) {
      return ((Map<?, ?>) ReflectionUtils.getFieldObject(m_designerPalette, "m_entryInfoToVisual")).get(nonVisual);
    }
    return null;
  }

  /**
   * @return the {@link IAction} from context menu of palette.
   */
  private IAction getContextMenuAction(String text) throws Exception {
    MenuManager contextMenu =
        (MenuManager) ReflectionUtils.getFieldObject(m_paletteComposite, "m_menuManager");
    ReflectionUtils.invokeMethod(
        contextMenu,
        "fireAboutToShow(org.eclipse.jface.action.IMenuManager)",
        contextMenu);
    return findChildAction(contextMenu, text);
  }
}
