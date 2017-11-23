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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildGraphical;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.model.jface.FieldEditorInfo;
import org.eclipse.wb.internal.rcp.model.jface.FieldLayoutPreferencePageInfo;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link FieldLayoutPreferencePageInfo}.
 * 
 * @author scheglov_ke
 */
public class FieldLayoutPreferencePageTest extends RcpModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureNewProject() throws Exception {
    super.configureNewProject();
    ProjectUtils.ensureResourceType(
        m_testProject.getJavaProject(),
        Activator.getDefault().getBundle(),
        "org.eclipse.wb.swt.FieldLayoutPreferencePage");
  }

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
  public void test_parse() throws Exception {
    FieldLayoutPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "import org.eclipse.wb.swt.*;",
            "public class Test extends FieldLayoutPreferencePage {",
            "  public Test() {",
            "  }",
            "  public Control createPageContents(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "    {",
            "      Composite composite = new Composite(container, SWT.NONE);",
            "      composite.setBounds(0, 0, 200, 25);",
            "      addField(new BooleanFieldEditor('', 'Boolean editor', composite));",
            "    }",
            "    {",
            "      Composite composite = new Composite(container, SWT.NONE);",
            "      composite.setBounds(50, 100, 200, 25);",
            "      addField(new StringFieldEditor('', 'String editor', composite));",
            "    }",
            "    return container;",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.wb.swt.FieldLayoutPreferencePage} {this} {/addField(new BooleanFieldEditor('', 'Boolean editor', composite))/ /addField(new StringFieldEditor('', 'String editor', composite))/}",
        "  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
        "    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NULL)/ /new Composite(container, SWT.NONE)/ /new Composite(container, SWT.NONE)/ /container/}",
        "      {implicit-layout: absolute} {implicit-layout} {}",
        "      {new: org.eclipse.swt.widgets.Composite} {local-unique: composite} {/new Composite(container, SWT.NONE)/ /composite.setBounds(0, 0, 200, 25)/ /new BooleanFieldEditor('', 'Boolean editor', composite)/}",
        "        {new: org.eclipse.jface.preference.BooleanFieldEditor} {empty} {/addField(new BooleanFieldEditor('', 'Boolean editor', composite))/}",
        "      {new: org.eclipse.swt.widgets.Composite} {local-unique: composite} {/new Composite(container, SWT.NONE)/ /composite.setBounds(50, 100, 200, 25)/ /new StringFieldEditor('', 'String editor', composite)/}",
        "        {new: org.eclipse.jface.preference.StringFieldEditor} {empty} {/addField(new StringFieldEditor('', 'String editor', composite))/}",
        "          {method: getLabelControl} {subComponent} {}",
        "          {method: getTextControl} {subComponent} {}");
    CompositeInfo parentComposite = (CompositeInfo) page.getChildrenJava().get(0);
    CompositeInfo containerComposite = (CompositeInfo) parentComposite.getChildrenControls().get(0);
    CompositeInfo composite_1 = (CompositeInfo) containerComposite.getChildrenControls().get(0);
    CompositeInfo composite_2 = (CompositeInfo) containerComposite.getChildrenControls().get(1);
    FieldEditorInfo editor_1 = (FieldEditorInfo) composite_1.getChildrenJava().get(0);
    FieldEditorInfo editor_2 = (FieldEditorInfo) composite_2.getChildrenJava().get(0);
    // check association
    assertInstanceOf(EmptyAssociation.class, editor_1.getAssociation());
    assertInstanceOf(EmptyAssociation.class, editor_2.getAssociation());
    // check bounds
    page.refresh();
    assertNoErrors(page);
    assertThat(composite_1.getBounds().width).isEqualTo(200);
    assertThat(composite_2.getBounds().width).isEqualTo(200);
    assertThat(editor_1.getBounds().width).isGreaterThanOrEqualTo(90);
    assertThat(editor_2.getBounds().width).isEqualTo(200);
    // FieldEditor's are not visible on FieldLayoutPreferencePage (on design canvas)
    {
      boolean[] visible = new boolean[]{true};
      page.getBroadcast(ObjectInfoChildGraphical.class).invoke(editor_1, visible);
      assertFalse(visible[0]);
    }
  }

  /**
   * Test for {@link FieldLayoutPreferencePageInfo#schedule_CREATE(FieldEditorInfo)}.
   */
  public void test_CREATE() throws Exception {
    FieldLayoutPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "import org.eclipse.wb.swt.*;",
            "public class Test extends FieldLayoutPreferencePage {",
            "  public Test() {",
            "  }",
            "  public Control createPageContents(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "    return container;",
            "  }",
            "}");
    CompositeInfo parentComposite = (CompositeInfo) page.getChildrenJava().get(0);
    CompositeInfo containerComposite = (CompositeInfo) parentComposite.getChildrenControls().get(0);
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) containerComposite.getLayout();
    // do create
    FieldEditorInfo newEditor = createJavaInfo("org.eclipse.jface.preference.IntegerFieldEditor");
    CompositeInfo newComposite = page.schedule_CREATE(newEditor);
    layout.commandCreate(newComposite, null);
    layout.commandChangeBounds(newComposite, new Point(0, 0), new Dimension(200, 25));
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "import org.eclipse.wb.swt.*;",
        "public class Test extends FieldLayoutPreferencePage {",
        "  public Test() {",
        "  }",
        "  public Control createPageContents(Composite parent) {",
        "    Composite container = new Composite(parent, SWT.NULL);",
        "    {",
        "      Composite composite = new Composite(container, SWT.NONE);",
        "      composite.setBounds(0, 0, 200, 25);",
        "      addField(new IntegerFieldEditor('id', 'New IntegerFieldEditor', composite));",
        "    }",
        "    return container;",
        "  }",
        "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.wb.swt.FieldLayoutPreferencePage} {this} {/addField(new IntegerFieldEditor('id', 'New IntegerFieldEditor', composite))/}",
        "  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
        "    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NULL)/ /container/ /new Composite(container, SWT.NONE)/}",
        "      {implicit-layout: absolute} {implicit-layout} {}",
        "      {new: org.eclipse.swt.widgets.Composite} {local-unique: composite} {/new Composite(container, SWT.NONE)/ /new IntegerFieldEditor('id', 'New IntegerFieldEditor', composite)/ /composite.setBounds(0, 0, 200, 25)/}",
        "        {new: org.eclipse.jface.preference.IntegerFieldEditor} {empty} {/addField(new IntegerFieldEditor('id', 'New IntegerFieldEditor', composite))/}",
        "          {method: getLabelControl} {subComponent} {}",
        "          {method: getTextControl} {subComponent} {}");
    assertInstanceOf(EmptyAssociation.class, newEditor.getAssociation());
    // set property, so convert to Block 
    newEditor.getPropertyByTitle("textLimit").setValue(5);
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "import org.eclipse.wb.swt.*;",
        "public class Test extends FieldLayoutPreferencePage {",
        "  public Test() {",
        "  }",
        "  public Control createPageContents(Composite parent) {",
        "    Composite container = new Composite(parent, SWT.NULL);",
        "    {",
        "      Composite composite = new Composite(container, SWT.NONE);",
        "      composite.setBounds(0, 0, 200, 25);",
        "      {",
        "        IntegerFieldEditor integerFieldEditor = new IntegerFieldEditor('id', 'New IntegerFieldEditor', composite);",
        "        integerFieldEditor.setTextLimit(5);",
        "        addField(integerFieldEditor);",
        "      }",
        "    }",
        "    return container;",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FieldEditorInfo} copy/paste.
   */
  public void test_copyPaste() throws Exception {
    FieldLayoutPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "import org.eclipse.wb.swt.*;",
            "public class Test extends FieldLayoutPreferencePage {",
            "  public Test() {",
            "  }",
            "  public Control createPageContents(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "    {",
            "      Composite composite = new Composite(container, SWT.NONE);",
            "      composite.setBounds(0, 0, 200, 25);",
            "      addField(new BooleanFieldEditor('', 'Boolean editor', composite));",
            "    }",
            "    {",
            "      Composite composite = new Composite(container, SWT.NONE);",
            "      composite.setBounds(50, 100, 200, 25);",
            "      addField(new StringFieldEditor('', 'String editor', composite));",
            "    }",
            "    return container;",
            "  }",
            "}");
    page.refresh();
    CompositeInfo parentComposite = (CompositeInfo) page.getChildrenJava().get(0);
    CompositeInfo containerComposite = (CompositeInfo) parentComposite.getChildrenControls().get(0);
    CompositeInfo composite = (CompositeInfo) containerComposite.getChildrenControls().get(0);
    FieldEditorInfo fieldEditor = (FieldEditorInfo) composite.getChildrenJava().get(0);
    // do copy/paste
    {
      JavaInfoMemento memento = JavaInfoMemento.createMemento(fieldEditor);
      FieldEditorInfo newFieldEditor = (FieldEditorInfo) memento.create(page);
      {
        AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) containerComposite.getLayout();
        CompositeInfo newComposite = page.schedule_CREATE(newFieldEditor);
        layout.commandCreate(newComposite, null);
        layout.commandChangeBounds(newComposite, new Point(50, 150), new Dimension(200, 25));
      }
      memento.apply();
    }
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "import org.eclipse.wb.swt.*;",
        "public class Test extends FieldLayoutPreferencePage {",
        "  public Test() {",
        "  }",
        "  public Control createPageContents(Composite parent) {",
        "    Composite container = new Composite(parent, SWT.NULL);",
        "    {",
        "      Composite composite = new Composite(container, SWT.NONE);",
        "      composite.setBounds(0, 0, 200, 25);",
        "      addField(new BooleanFieldEditor('', 'Boolean editor', composite));",
        "    }",
        "    {",
        "      Composite composite = new Composite(container, SWT.NONE);",
        "      composite.setBounds(50, 100, 200, 25);",
        "      addField(new StringFieldEditor('', 'String editor', composite));",
        "    }",
        "    {",
        "      Composite composite = new Composite(container, SWT.NONE);",
        "      composite.setBounds(50, 150, 200, 25);",
        "      addField(new BooleanFieldEditor('', 'Boolean editor', composite));",
        "    }",
        "    return container;",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette tweaks
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FieldEditor_RootProcessor} logic.<br>
   * Root is {@link FieldLayoutPreferencePageInfo}.<br>
   * "System" and "FieldEditors" categories should be visible and open, and all other - also
   * visible.
   */
  public void test_paletteTweaks_1() throws Exception {
    FieldLayoutPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "import org.eclipse.wb.swt.*;",
            "public class Test extends FieldLayoutPreferencePage {",
            "  public Test() {",
            "  }",
            "  public Control createPageContents(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "    return container;",
            "  }",
            "}");
    CategoryInfo systemCategory = new CategoryInfo("org.eclipse.wb.rcp.system");
    CategoryInfo editorsCategory = new CategoryInfo("org.eclipse.wb.rcp.fieldEditors");
    CategoryInfo otherCategory = new CategoryInfo("some.other.category");
    // prepare categories
    List<CategoryInfo> categories =
        Lists.newArrayList(systemCategory, editorsCategory, otherCategory);
    assertThat(categories).hasSize(3);
    {
      // all visible
      assertTrue(systemCategory.isVisible());
      assertTrue(editorsCategory.isVisible());
      assertTrue(otherCategory.isVisible());
    }
    // update categories
    page.getBroadcast(PaletteEventListener.class).categories(categories);
    assertThat(categories).hasSize(3);
    {
      // all visible
      assertTrue(systemCategory.isVisible());
      assertTrue(editorsCategory.isVisible());
      assertTrue(otherCategory.isVisible());
    }
  }
}