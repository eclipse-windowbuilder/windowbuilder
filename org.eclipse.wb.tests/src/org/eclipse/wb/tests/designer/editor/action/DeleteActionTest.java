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
package org.eclipse.wb.tests.designer.editor.action;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.actions.DeleteAction;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.core.model.TestObjectInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Test for {@link DeleteAction}.
 * 
 * @author mitin_aa
 */
public class DeleteActionTest extends SwingGefTest {
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
   * Test for deleting selected parent and its child.
   */
  public void test_ParentChild() throws Exception {
    ContainerInfo thisPanel =
        openContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "	public Test() {",
            "		{",
            "			JPanel panel = new JPanel();",
            "			add(panel);",
            "			{",
            "				JButton button = new JButton('New button');",
            "				panel.add(button);",
            "			}",
            "		}",
            "	}",
            "}");
    ContainerInfo panel = (ContainerInfo) thisPanel.getChildrenComponents().get(0);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // select
    canvas.select(panel, button);
    // delete
    {
      IAction deleteAction = getDeleteAction();
      assertTrue(deleteAction.isEnabled());
      deleteAction.run();
      assertEditor(
          "// filler filler filler",
          "public class Test extends JPanel {",
          "	public Test() {",
          "	}",
          "}");
    }
  }

  /**
   * We can delete even "root" component, but this works as clearing it.
   */
  public void test_canRootComponent() throws Exception {
    ContainerInfo panel =
        openContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "	public Test() {",
            "	  setEnabled(false);",
            "	}",
            "}");
    // select "panel"
    canvas.select(panel);
    // delete "panel"
    IAction deleteAction = getDeleteAction();
    assertTrue(deleteAction.isEnabled());
    deleteAction.run();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "	public Test() {",
        "	}",
        "}");
  }

  /**
   * We should be able to select and delete {@link ObjectInfo} that is displayed only in components
   * tree, even if it does not exist on design canvas.
   */
  public void test_componentInTree_butNotOnDesign() throws Exception {
    ContainerInfo panel =
        openContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "	public Test() {",
            "	}",
            "}");
    // add artificial ObjectInfo
    ObjectInfo object = new TestObjectInfo("myObject") {
      @Override
      public boolean canDelete() {
        return true;
      }

      @Override
      public void delete() throws Exception {
        getParent().removeChild(this);
      }
    };
    panel.addChild(object);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  myObject");
    // show "object"
    panel.refresh();
    tree.setExpanded(panel, true);
    // set "object"
    tree.select(object);
    // "object" can be deleted, so "delete" action is enabled
    {
      IAction deleteAction = getDeleteAction();
      assertTrue(deleteAction.isEnabled());
      deleteAction.run();
    }
    // "object" was deleted
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * Test that when "Design" page is first, then actions are still correctly mapped to Designer
   * handlers.
   */
  public void test_DesignPageFirst() throws Exception {
    IPreferenceStore preferences = DesignerPlugin.getPreferences();
    preferences.setValue(
        IPreferenceConstants.P_EDITOR_LAYOUT,
        IPreferenceConstants.V_EDITOR_LAYOUT_PAGES_DESIGN);
    try {
      ContainerInfo panel =
          openContainer(
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "public class Test extends JPanel {",
              "	public Test() {",
              "	  add(new JButton('Some button'));",
              "	}",
              "}");
      // select JButton
      {
        ComponentInfo button = panel.getChildrenComponents().get(0);
        canvas.select(button);
      }
      // use "Delete" action
      IAction deleteAction = getDeleteAction();
      assertTrue(deleteAction.isEnabled());
      deleteAction.run();
      assertEquals(
          getTestSource(
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "public class Test extends JPanel {",
              "	public Test() {",
              "	}",
              "}"),
          m_lastEditor.getModelUnit().getSource());
    } finally {
      preferences.setToDefault(IPreferenceConstants.P_EDITOR_LAYOUT);
    }
  }
}
