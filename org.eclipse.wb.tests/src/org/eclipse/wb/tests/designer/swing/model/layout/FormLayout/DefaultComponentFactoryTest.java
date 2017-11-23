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
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.swing.FormLayout.palette.DefaultComponentFactoryCreateLabelEntryInfo;
import org.eclipse.wb.internal.swing.FormLayout.palette.DefaultComponentFactoryCreateTitleEntryInfo;
import org.eclipse.wb.internal.swing.FormLayout.parser.DefaultComponentFactoryCreationSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.swt.graphics.Image;

import com.jgoodies.forms.factories.DefaultComponentFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for components from {@link DefaultComponentFactory}.
 * 
 * @author scheglov_ke
 */
public class DefaultComponentFactoryTest extends AbstractFormLayoutTest {
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
  // createLabel()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DefaultComponentFactory#createLabel(String)}.
   */
  public void test_createLabel() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import com.jgoodies.forms.factories.DefaultComponentFactory;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JLabel label = DefaultComponentFactory.getInstance().createLabel('A');",
            "      add(label);",
            "    }",
            "  }",
            "  // filler filler filler",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(label)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {opaque} {local-unique: label} {/DefaultComponentFactory.getInstance().createLabel('A')/ /add(label)/}");
    panel.refresh();
    ComponentInfo component = panel.getChildrenComponents().get(0);
    // CreationSupport
    assertThat(component.getCreationSupport()).isInstanceOf(
        DefaultComponentFactoryCreationSupport.class);
    // permissions
    assertTrue(JavaInfoUtils.canMove(component));
    assertTrue(JavaInfoUtils.canReparent(component));
    // "text" property
    component.getPropertyByTitle("text").setValue("B");
    assertEditor(
        "import com.jgoodies.forms.factories.DefaultComponentFactory;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JLabel label = DefaultComponentFactory.getInstance().createLabel('B');",
        "      add(label);",
        "    }",
        "  }",
        "  // filler filler filler",
        "}");
    // delete
    assertTrue(component.canDelete());
    component.delete();
    assertEditor(
        "import com.jgoodies.forms.factories.DefaultComponentFactory;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "  // filler filler filler",
        "}");
  }

  /**
   * Test for {@link DefaultComponentFactoryCreateLabelEntryInfo}.
   */
  @DisposeProjectAfter
  public void test_createLabel_tool() throws Exception {
    do_projectDispose();
    do_projectCreate();
    m_useFormsImports = false;
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    ToolEntryInfo entry = new DefaultComponentFactoryCreateLabelEntryInfo();
    assertEquals(entry.getClass().getName(), entry.getId());
    assertNotNull(entry.getIcon());
    // do initialize
    assertTrue(entry.initialize(null, m_lastParseInfo));
    // check tool
    CreationTool creationTool = (CreationTool) entry.createTool();
    ICreationFactory creationFactory = creationTool.getFactory();
    creationFactory.activate();
    // get object
    ComponentInfo newComponent = (ComponentInfo) creationFactory.getNewObject();
    assertInstanceOf(
        DefaultComponentFactoryCreationSupport.class,
        newComponent.getCreationSupport());
    // live image
    {
      Image image = newComponent.getImage();
      assertNotNull(image);
      org.eclipse.swt.graphics.Rectangle bounds = image.getBounds();
      assertThat(bounds.width).isGreaterThan(50).isLessThan(100);
      assertThat(bounds.height).isGreaterThan(10).isLessThan(20);
      image.dispose();
    }
    // add object
    ((FlowLayoutInfo) panel.getLayout()).add(newComponent, null);
    assertEditor(
        "import com.jgoodies.forms.factories.DefaultComponentFactory;",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JLabel label = DefaultComponentFactory.getInstance().createLabel('New JGoodies label');",
        "      add(label);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // createTitle()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DefaultComponentFactory#createTitle(String)}.
   */
  public void test_createTitle() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import com.jgoodies.forms.factories.DefaultComponentFactory;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JLabel label = DefaultComponentFactory.getInstance().createTitle('A');",
            "      add(label);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(label)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {opaque} {local-unique: label} {/DefaultComponentFactory.getInstance().createTitle('A')/ /add(label)/}");
    panel.refresh();
    ComponentInfo component = panel.getChildrenComponents().get(0);
    // CreationSupport
    assertThat(component.getCreationSupport()).isInstanceOf(
        DefaultComponentFactoryCreationSupport.class);
    // "text" property
    component.getPropertyByTitle("text").setValue("B");
    assertEditor(
        "import com.jgoodies.forms.factories.DefaultComponentFactory;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JLabel label = DefaultComponentFactory.getInstance().createTitle('B');",
        "      add(label);",
        "    }",
        "  }",
        "}");
  }

  public void test_createTitle_tool() throws Exception {
    do_projectDispose();
    do_projectCreate();
    m_useFormsImports = false;
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    ToolEntryInfo entry = new DefaultComponentFactoryCreateTitleEntryInfo();
    assertEquals(entry.getClass().getName(), entry.getId());
    assertNotNull(entry.getIcon());
    // do initialize
    assertTrue(entry.initialize(null, m_lastParseInfo));
    // check tool
    CreationTool creationTool = (CreationTool) entry.createTool();
    ICreationFactory creationFactory = creationTool.getFactory();
    creationFactory.activate();
    // get object
    ComponentInfo newComponent = (ComponentInfo) creationFactory.getNewObject();
    assertInstanceOf(
        DefaultComponentFactoryCreationSupport.class,
        newComponent.getCreationSupport());
    // add object
    ((FlowLayoutInfo) panel.getLayout()).add(newComponent, null);
    assertEditor(
        "import com.jgoodies.forms.factories.DefaultComponentFactory;",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JLabel label = DefaultComponentFactory.getInstance().createTitle('New JGoodies title');",
        "      add(label);",
        "    }",
        "  }",
        "}");
  }
}
