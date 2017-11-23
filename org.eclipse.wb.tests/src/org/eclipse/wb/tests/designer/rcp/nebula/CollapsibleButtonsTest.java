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
package org.eclipse.wb.tests.designer.rcp.nebula;

import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.rcp.nebula.collapsiblebuttons.CollapsibleButtonsInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link CollapsibleButtonsInfo}.
 * 
 * @author sablin_aa
 */
public class CollapsibleButtonsTest extends AbstractNebulaTest {
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
   * General test {@link CollapsibleButtonsInfo}.
   */
  public void test_General() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.nebula.widgets.collapsiblebuttons.*;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    CollapsibleButtons collapsibleButtons = new CollapsibleButtons(this, SWT.NONE, IColorManager.SKIN_AUTO_DETECT);",
            "    {",
            "    	CustomButton customButton = collapsibleButtons.addButton('Copy', 'New CollapsibleButton', null, null);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new CollapsibleButtons(this, SWT.NONE, IColorManager.SKIN_AUTO_DETECT)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {new: org.eclipse.nebula.widgets.collapsiblebuttons.CollapsibleButtons} {local-unique: collapsibleButtons} {/new CollapsibleButtons(this, SWT.NONE, IColorManager.SKIN_AUTO_DETECT)/ /collapsibleButtons.addButton('Copy', 'New CollapsibleButton', null, null)/}",
        "    {implicit-factory} {local-unique: customButton} {/collapsibleButtons.addButton('Copy', 'New CollapsibleButton', null, null)/}");
    shell.refresh();
    CollapsibleButtonsInfo collapsibleButtons =
        shell.getChildren(CollapsibleButtonsInfo.class).get(0);
    // check button
    {
      ControlInfo button = collapsibleButtons.getChildrenControls().get(0);
      assertEquals(
          "org.eclipse.nebula.widgets.collapsiblebuttons.CustomButton",
          button.getDescription().getComponentClass().getName());
      assertInstanceOf(ImplicitFactoryCreationSupport.class, button.getCreationSupport());
      assertInstanceOf(InvocationVoidAssociation.class, button.getAssociation());
      // "button" should have some not empty bounds (test for CollapsibleButtons_Info.makeAddedButtonsVisible())
      {
        Rectangle bounds = button.getBounds();
        assertThat(bounds.width).isGreaterThan(100);
        assertThat(bounds.height).isGreaterThan(20);
      }
    }
  }

  /**
   * Test adding button on {@link CollapsibleButtonsInfo}.
   */
  public void test_createButton() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.nebula.widgets.collapsiblebuttons.*;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    CollapsibleButtons collapsibleButtons = new CollapsibleButtons(this, SWT.NONE, IColorManager.SKIN_AUTO_DETECT);",
            "    {",
            "    	CustomButton customButton = collapsibleButtons.addButton('Copy', 'New CollapsibleButton', null, null);",
            "    }",
            "  }",
            "}");
    CollapsibleButtonsInfo collapsibleButtons =
        shell.getChildren(CollapsibleButtonsInfo.class).get(0);
    assertEquals(1, collapsibleButtons.getChildrenControls().size());
    // add new button
    ControlInfo button_new = CollapsibleButtonsInfo.createButton(collapsibleButtons, null);
    // check source
    assertEditor(
        "import org.eclipse.nebula.widgets.collapsiblebuttons.*;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    CollapsibleButtons collapsibleButtons = new CollapsibleButtons(this, SWT.NONE, IColorManager.SKIN_AUTO_DETECT);",
        "    {",
        "    	CustomButton customButton = collapsibleButtons.addButton('Copy', 'New CollapsibleButton', null, null);",
        "    }",
        "    {",
        "    	CustomButton customButton = collapsibleButtons.addButton('New Button', 'New CollapsibleButton', null, null);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new CollapsibleButtons(this, SWT.NONE, IColorManager.SKIN_AUTO_DETECT)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {new: org.eclipse.nebula.widgets.collapsiblebuttons.CollapsibleButtons} {local-unique: collapsibleButtons} {/new CollapsibleButtons(this, SWT.NONE, IColorManager.SKIN_AUTO_DETECT)/ /collapsibleButtons.addButton('Copy', 'New CollapsibleButton', null, null)/ /collapsibleButtons.addButton('New Button', 'New CollapsibleButton', null, null)/}",
        "    {implicit-factory} {local-unique: customButton} {/collapsibleButtons.addButton('Copy', 'New CollapsibleButton', null, null)/}",
        "    {implicit-factory} {local-unique: customButton} {/collapsibleButtons.addButton('New Button', 'New CollapsibleButton', null, null)/}");
    // check new button
    assertInstanceOf(ImplicitFactoryCreationSupport.class, button_new.getCreationSupport());
    assertInstanceOf(InvocationVoidAssociation.class, button_new.getAssociation());
  }

  /**
   * Test moving button on {@link CollapsibleButtonsInfo}.
   */
  public void test_moveButton() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.nebula.widgets.collapsiblebuttons.*;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    CollapsibleButtons collapsibleButtons = new CollapsibleButtons(this, SWT.NONE, IColorManager.SKIN_AUTO_DETECT);",
            "    {",
            "    	CustomButton customButton = collapsibleButtons.addButton('B1', 'New CollapsibleButton', null, null);",
            "    }",
            "    {",
            "    	CustomButton customButton = collapsibleButtons.addButton('B2', 'New CollapsibleButton', null, null);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new CollapsibleButtons(this, SWT.NONE, IColorManager.SKIN_AUTO_DETECT)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {new: org.eclipse.nebula.widgets.collapsiblebuttons.CollapsibleButtons} {local-unique: collapsibleButtons} {/new CollapsibleButtons(this, SWT.NONE, IColorManager.SKIN_AUTO_DETECT)/ /collapsibleButtons.addButton('B1', 'New CollapsibleButton', null, null)/ /collapsibleButtons.addButton('B2', 'New CollapsibleButton', null, null)/}",
        "    {implicit-factory} {local-unique: customButton} {/collapsibleButtons.addButton('B1', 'New CollapsibleButton', null, null)/}",
        "    {implicit-factory} {local-unique: customButton} {/collapsibleButtons.addButton('B2', 'New CollapsibleButton', null, null)/}");
    CollapsibleButtonsInfo collapsibleButtons =
        shell.getChildren(CollapsibleButtonsInfo.class).get(0);
    // extract buttons
    assertEquals(2, collapsibleButtons.getChildrenControls().size());
    ControlInfo button_1 = collapsibleButtons.getChildrenControls().get(0);
    ControlInfo button_2 = collapsibleButtons.getChildrenControls().get(1);
    // move buttons
    CollapsibleButtonsInfo.moveButton(button_2, button_1);
    assertEquals(0, collapsibleButtons.getChildrenControls().indexOf(button_2));
    assertEquals(1, collapsibleButtons.getChildrenControls().indexOf(button_1));
    assertEditor(
        "import org.eclipse.nebula.widgets.collapsiblebuttons.*;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    CollapsibleButtons collapsibleButtons = new CollapsibleButtons(this, SWT.NONE, IColorManager.SKIN_AUTO_DETECT);",
        "    {",
        "    	CustomButton customButton = collapsibleButtons.addButton('B2', 'New CollapsibleButton', null, null);",
        "    }",
        "    {",
        "    	CustomButton customButton = collapsibleButtons.addButton('B1', 'New CollapsibleButton', null, null);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new CollapsibleButtons(this, SWT.NONE, IColorManager.SKIN_AUTO_DETECT)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {new: org.eclipse.nebula.widgets.collapsiblebuttons.CollapsibleButtons} {local-unique: collapsibleButtons} {/new CollapsibleButtons(this, SWT.NONE, IColorManager.SKIN_AUTO_DETECT)/ /collapsibleButtons.addButton('B1', 'New CollapsibleButton', null, null)/ /collapsibleButtons.addButton('B2', 'New CollapsibleButton', null, null)/}",
        "    {implicit-factory} {local-unique: customButton} {/collapsibleButtons.addButton('B2', 'New CollapsibleButton', null, null)/}",
        "    {implicit-factory} {local-unique: customButton} {/collapsibleButtons.addButton('B1', 'New CollapsibleButton', null, null)/}");
  }
}