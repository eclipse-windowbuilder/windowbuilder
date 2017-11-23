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
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.rcp.model.jface.FieldEditorInfo;
import org.eclipse.wb.internal.rcp.model.jface.FieldEditorPreferencePageInfo;
import org.eclipse.wb.internal.rcp.model.jface.FieldEditorSubComponentCreationSupport;
import org.eclipse.wb.internal.rcp.model.jface.FieldEditorSubComponentVariableSupport;
import org.eclipse.wb.internal.rcp.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Test for {@link FieldEditorPreferencePageInfo}.
 * 
 * @author scheglov_ke
 */
public class FieldEditorPreferencePageTest extends RcpModelTest {
  private static final IPreferenceStore preferences =
      RcpToolkitDescription.INSTANCE.getPreferences();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    preferences.setToDefault(IPreferenceConstants.PREF_FIELD_USUAL_CODE);
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
  /**
   * Parse with implicit {@link FieldEditorPreferencePage#FLAT} flag.
   */
  public void test_FLATimplicit() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "  }",
            "  protected void createFieldEditors() {",
            "    addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()));",
            "    addField(new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent()));",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.preference.FieldEditorPreferencePage} {this} {"
            + "/new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent())/ "
            + "/addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()))/ "
            + "/new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent())/ "
            + "/addField(new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent()))/"
            + "}",
        "  {new: org.eclipse.jface.preference.BooleanFieldEditor} {empty} {/addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()))/}",
        "  {new: org.eclipse.jface.preference.IntegerFieldEditor} {empty} {/addField(new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent()))/}",
        "    {method: getLabelControl} {subComponent} {}",
        "    {method: getTextControl} {subComponent} {}");
    // prepare FieldEditor's
    FieldEditorInfo editor_1;
    FieldEditorInfo editor_2;
    {
      List<FieldEditorInfo> editors = page.getEditors();
      assertThat(editors).hasSize(2);
      editor_1 = editors.get(0);
      editor_2 = editors.get(1);
    }
    // refresh()
    page.refresh();
    assertNoErrors(page);
    // check bounds for FieldEditor's
    Rectangle bounds_1 = editor_1.getBounds();
    Rectangle bounds_2 = editor_2.getBounds();
    assertThat(bounds_1.width).isGreaterThan(300);
    assertThat(bounds_1.height).isGreaterThan(15);
    assertThat(bounds_2.width).isGreaterThan(300);
    assertThat(bounds_2.height).isGreaterThan(15);
    assertFalse(bounds_1.intersects(bounds_2));
  }

  /**
   * Parse with explicit {@link FieldEditorPreferencePage#FLAT} flag.
   */
  public void test_FLATexplicit() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(FLAT);",
            "  }",
            "  protected void createFieldEditors() {",
            "    addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()));",
            "    addField(new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent()));",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.preference.FieldEditorPreferencePage} {this} {"
            + "/new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent())/ "
            + "/addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()))/ "
            + "/new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent())/ "
            + "/addField(new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent()))/"
            + "}",
        "  {new: org.eclipse.jface.preference.BooleanFieldEditor} {empty} {/addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()))/}",
        "  {new: org.eclipse.jface.preference.IntegerFieldEditor} {empty} {/addField(new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent()))/}",
        "    {method: getLabelControl} {subComponent} {}",
        "    {method: getTextControl} {subComponent} {}");
    // refresh()
    page.refresh();
    assertNoErrors(page);
  }

  /**
   * Parse with {@link FieldEditorPreferencePage#GRID} flag.
   */
  public void test_GRID() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(GRID);",
            "  }",
            "  protected void createFieldEditors() {",
            "    addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()));",
            "    addField(new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent()));",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.preference.FieldEditorPreferencePage} {this} {"
            + "/new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent())/ "
            + "/addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()))/ "
            + "/new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent())/ "
            + "/addField(new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent()))/"
            + "}",
        "  {new: org.eclipse.jface.preference.BooleanFieldEditor} {empty} {/addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()))/}",
        "  {new: org.eclipse.jface.preference.IntegerFieldEditor} {empty} {/addField(new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent()))/}",
        "    {method: getLabelControl} {subComponent} {}",
        "    {method: getTextControl} {subComponent} {}");
    // prepare FieldEditor's
    FieldEditorInfo editor_1;
    FieldEditorInfo editor_2;
    {
      List<FieldEditorInfo> editors = page.getEditors();
      assertThat(editors).hasSize(2);
      editor_1 = editors.get(0);
      editor_2 = editors.get(1);
    }
    // refresh()
    page.refresh();
    assertNoErrors(page);
    // check bounds for FieldEditor's
    Rectangle bounds_1 = editor_1.getBounds();
    Rectangle bounds_2 = editor_2.getBounds();
    assertThat(bounds_1.width).isGreaterThanOrEqualTo(90);
    assertThat(bounds_1.height).isGreaterThan(15);
    assertThat(bounds_2.width).isGreaterThan(300);
    assertThat(bounds_2.height).isGreaterThan(15);
    assertFalse(bounds_1.intersects(bounds_2));
  }

  /**
   * {@link RadioGroupFieldEditor} consists of several {@link Control}'s, including
   * {@link Composite}. We should correctly handle this case.
   */
  public void test_complexFieldEditor_bounds() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(GRID);",
            "  }",
            "  protected void createFieldEditors() {",
            "    addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()));",
            "    addField(new RadioGroupFieldEditor('id', 'New RadioGroupFieldEditor', 1, new String[][]{{'name_1', 'value_1'}, {'name_2', 'value_2'}}, getFieldEditorParent(), false));",
            "  }",
            "}");
    assertNoErrors(page);
    // prepare FieldEditor's
    FieldEditorInfo editor_1;
    FieldEditorInfo editor_2;
    {
      List<FieldEditorInfo> editors = page.getEditors();
      assertThat(editors).hasSize(2);
      editor_1 = editors.get(0);
      editor_2 = editors.get(1);
    }
    // refresh()
    page.refresh();
    assertNoErrors(page);
    // check bounds for FieldEditor's
    Rectangle bounds_1 = editor_1.getBounds();
    Rectangle bounds_2 = editor_2.getBounds();
    assertFalse(bounds_1.intersects(bounds_2));
  }

  /**
   * Test for {@link IntegerFieldEditor} properties.
   */
  public void test_IntegerFieldEditor_properties() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  protected void createFieldEditors() {",
            "    addField(new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent()));",
            "  }",
            "}");
    FieldEditorInfo fieldEditor = page.getEditors().get(0);
    // "validRange" complex property
    {
      Property rangeProperty = fieldEditor.getPropertyByTitle("validRange");
      assertNotNull(rangeProperty);
      Property[] subProperties = getSubProperties(rangeProperty);
      assertEquals(2, subProperties.length);
      assertEquals("min", subProperties[0].getTitle());
      assertEquals("max", subProperties[1].getTitle());
    }
    // default value for "textLimit" property
    {
      Property property = fieldEditor.getPropertyByTitle("textLimit");
      assertEquals(10, property.getValue());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette tweaks
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FieldEditor_RootProcessor} logic.<br>
   * Root is {@link FieldEditorPreferencePageInfo}.<br>
   * "System" and "FieldEditors" categories should be visible, and all other - invisible.
   */
  public void test_paletteTweaks_1() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  protected void createFieldEditors() {",
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
      // system/editors visible, other - hidden
      assertTrue(systemCategory.isVisible());
      assertTrue(editorsCategory.isVisible());
      assertFalse(otherCategory.isVisible());
    }
  }

  /**
   * Test for {@link FieldEditor_RootProcessor} logic.<br>
   * Root is <b>not</b> {@link FieldEditorPreferencePageInfo}.<br>
   * "FieldEditors" category should be NOT visible, and all other - untouched.
   */
  public void test_paletteTweaks_2() throws Exception {
    JavaInfo javaInfo =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    // create categories
    CategoryInfo systemCategory = new CategoryInfo("org.eclipse.wb.rcp.system");
    CategoryInfo editorsCategory = new CategoryInfo("org.eclipse.wb.rcp.fieldEditors");
    CategoryInfo otherCategory = new CategoryInfo("some.other.category");
    // prepare List of categories
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
    javaInfo.getBroadcast(PaletteEventListener.class).categories(categories);
    assertThat(categories).hasSize(3);
    {
      // editors - invisible
      assertTrue(systemCategory.isVisible());
      assertFalse(editorsCategory.isVisible());
      assertTrue(otherCategory.isVisible());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FieldEditorPreferencePageInfo#command_CREATE(FieldEditorInfo, FieldEditorInfo)}
   * .<br>
   * Before some existing {@link FieldEditorInfo}.
   */
  public void test_CREATE_1() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(FLAT);",
            "  }",
            "  protected void createFieldEditors() {",
            "    addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()));",
            "  }",
            "}");
    FieldEditorInfo nextEditor = page.getEditors().get(0);
    // do create
    FieldEditorInfo newEditor = createJavaInfo("org.eclipse.jface.preference.IntegerFieldEditor");
    page.command_CREATE(newEditor, nextEditor);
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "    super(FLAT);",
        "  }",
        "  protected void createFieldEditors() {",
        "    addField(new IntegerFieldEditor('id', 'New IntegerFieldEditor', getFieldEditorParent()));",
        "    addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()));",
        "  }",
        "}");
  }

  /**
   * Test for {@link FieldEditorPreferencePageInfo#command_CREATE(FieldEditorInfo, FieldEditorInfo)}
   * .<br>
   * As last {@link FieldEditorInfo}.
   */
  public void test_CREATE_2() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(FLAT);",
            "  }",
            "  protected void createFieldEditors() {",
            "    addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()));",
            "  }",
            "}");
    // do create
    FieldEditorInfo newEditor = createJavaInfo("org.eclipse.jface.preference.IntegerFieldEditor");
    page.command_CREATE(newEditor, null);
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "    super(FLAT);",
        "  }",
        "  protected void createFieldEditors() {",
        "    addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()));",
        "    addField(new IntegerFieldEditor('id', 'New IntegerFieldEditor', getFieldEditorParent()));",
        "  }",
        "}");
  }

  /**
   * Test for {@link FieldEditorPreferencePageInfo#command_CREATE(FieldEditorInfo, FieldEditorInfo)}
   * .
   * <p>
   * Users asked for "usual" code generation, with variables and fields
   */
  public void test_CREATE_withControlCodeStyle() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(FLAT);",
            "  }",
            "  protected void createFieldEditors() {",
            "  }",
            "}");
    // do create
    FieldEditorInfo newEditor = createJavaInfo("org.eclipse.jface.preference.IntegerFieldEditor");
    preferences.setValue(IPreferenceConstants.PREF_FIELD_USUAL_CODE, true);
    page.command_CREATE(newEditor, null);
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "    super(FLAT);",
        "  }",
        "  protected void createFieldEditors() {",
        "    {",
        "      IntegerFieldEditor integerFieldEditor = new IntegerFieldEditor('id', 'New IntegerFieldEditor', getFieldEditorParent());",
        "      addField(integerFieldEditor);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link FieldEditorPreferencePageInfo#command_MOVE(FieldEditorInfo, FieldEditorInfo)}.<br>
   * Move {@link FieldEditorInfo} with {@link EmptyVariableSupport}, should just move
   * {@link Statement}.
   */
  public void test_MOVE_1() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(FLAT);",
            "  }",
            "  protected void createFieldEditors() {",
            "    addField(new BooleanFieldEditor('id_1', 'Boolean editor 1', getFieldEditorParent()));",
            "    addField(new BooleanFieldEditor('id_2', 'Boolean editor 2', getFieldEditorParent()));",
            "  }",
            "}");
    FieldEditorInfo editor_1 = page.getEditors().get(0);
    FieldEditorInfo editor_2 = page.getEditors().get(1);
    // do move
    page.command_MOVE(editor_2, editor_1);
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "    super(FLAT);",
        "  }",
        "  protected void createFieldEditors() {",
        "    addField(new BooleanFieldEditor('id_2', 'Boolean editor 2', getFieldEditorParent()));",
        "    addField(new BooleanFieldEditor('id_1', 'Boolean editor 1', getFieldEditorParent()));",
        "  }",
        "}");
  }

  /**
   * Test for {@link FieldEditorPreferencePageInfo#command_MOVE(FieldEditorInfo, FieldEditorInfo)}.<br>
   * Move {@link FieldEditorInfo} with "real" {@link VariableSupport}, should do general move.
   */
  public void test_MOVE_2() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(FLAT);",
            "  }",
            "  protected void createFieldEditors() {",
            "    {",
            "      IntegerFieldEditor integerFieldEditor = new IntegerFieldEditor('id_1', 'Integer editor', getFieldEditorParent());",
            "      addField(integerFieldEditor);",
            "    }",
            "    addField(new BooleanFieldEditor('id_2', 'Boolean editor 2', getFieldEditorParent()));",
            "  }",
            "}");
    FieldEditorInfo editor_1 = page.getEditors().get(0);
    // do move
    page.command_MOVE(editor_1, null);
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "    super(FLAT);",
        "  }",
        "  protected void createFieldEditors() {",
        "    addField(new BooleanFieldEditor('id_2', 'Boolean editor 2', getFieldEditorParent()));",
        "    {",
        "      IntegerFieldEditor integerFieldEditor = new IntegerFieldEditor('id_1', 'Integer editor', getFieldEditorParent());",
        "      addField(integerFieldEditor);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link FieldEditorPreferencePageInfo#command_MOVE(FieldEditorInfo, FieldEditorInfo)}.<br>
   * Move to last.
   */
  public void test_MOVE_3() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(FLAT);",
            "    setTitle('Some related node');",
            "  }",
            "  protected void createFieldEditors() {",
            "    addField(new BooleanFieldEditor('id_1', 'Boolean editor 1', getFieldEditorParent()));",
            "    addField(new BooleanFieldEditor('id_2', 'Boolean editor 2', getFieldEditorParent()));",
            "  }",
            "}");
    FieldEditorInfo editor_1 = page.getEditors().get(0);
    // do move
    page.command_MOVE(editor_1, null);
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "    super(FLAT);",
        "    setTitle('Some related node');",
        "  }",
        "  protected void createFieldEditors() {",
        "    addField(new BooleanFieldEditor('id_2', 'Boolean editor 2', getFieldEditorParent()));",
        "    addField(new BooleanFieldEditor('id_1', 'Boolean editor 1', getFieldEditorParent()));",
        "  }",
        "}");
  }

  /**
   * When {@link FieldEditorInfo} is created with {@link EmptyVariableSupport}, using single
   * {@link Statement} - this looks good. But if then we will try to set some property, it may
   * require some "real" {@link VariableSupport}, so to keep code good we should move
   * {@link Statement} into {@link Block}.
   */
  public void test_convertToBlock() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(FLAT);",
            "  }",
            "  protected void createFieldEditors() {",
            "    addField(new IntegerFieldEditor('id', 'New IntegerFieldEditor', getFieldEditorParent()));",
            "  }",
            "}");
    FieldEditorInfo editor = page.getEditors().get(0);
    // set property
    editor.getPropertyByTitle("textLimit").setValue(5);
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "    super(FLAT);",
        "  }",
        "  protected void createFieldEditors() {",
        "    {",
        "      IntegerFieldEditor integerFieldEditor = new IntegerFieldEditor('id', 'New IntegerFieldEditor', getFieldEditorParent());",
        "      integerFieldEditor.setTextLimit(5);",
        "      addField(integerFieldEditor);",
        "    }",
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
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "  }",
            "  protected void createFieldEditors() {",
            "    addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()));",
            "    addField(new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent()));",
            "  }",
            "}");
    page.refresh();
    FieldEditorInfo fieldEditor = page.getEditors().get(0);
    // do copy/paste
    {
      JavaInfoMemento memento = JavaInfoMemento.createMemento(fieldEditor);
      FieldEditorInfo newFieldEditor = (FieldEditorInfo) memento.create(page);
      page.command_CREATE(newFieldEditor, null);
      memento.apply();
    }
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "  }",
        "  protected void createFieldEditors() {",
        "    addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()));",
        "    addField(new IntegerFieldEditor('id_2', 'Integer editor', getFieldEditorParent()));",
        "    addField(new BooleanFieldEditor('id_1', 'Boolean editor', getFieldEditorParent()));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sub-components
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for exposed sub-components of {@link FieldEditorInfo}.<br>
   * Mostly for {@link FieldEditorSubComponentCreationSupport} and
   * {@link FieldEditorSubComponentVariableSupport}.
   */
  public void test_subComponents_0() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(FLAT);",
            "  }",
            "  protected void createFieldEditors() {",
            "    {",
            "      StringFieldEditor stringFieldEditor = new StringFieldEditor('id', 'String editor', getFieldEditorParent());",
            "      addField(stringFieldEditor);",
            "    }",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.preference.FieldEditorPreferencePage} {this} {/new StringFieldEditor('id', 'String editor', getFieldEditorParent())/ /addField(stringFieldEditor)/}",
        "  {new: org.eclipse.jface.preference.StringFieldEditor} {local-unique: stringFieldEditor} {/new StringFieldEditor('id', 'String editor', getFieldEditorParent())/ /addField(stringFieldEditor)/}",
        "    {method: getLabelControl} {subComponent} {}",
        "    {method: getTextControl} {subComponent} {}");
    FieldEditorInfo fieldEditor = page.getEditors().get(0);
    ControlInfo labelControl;
    {
      List<ControlInfo> controls = fieldEditor.getChildControls();
      assertThat(controls).hasSize(2);
      assertEquals(
          "org.eclipse.swt.widgets.Label",
          controls.get(0).getDescription().getComponentClass().getName());
      assertEquals(
          "org.eclipse.swt.widgets.Text",
          controls.get(1).getDescription().getComponentClass().getName());
      labelControl = controls.get(0);
    }
    // sub-components are NOT visible on design canvas
    {
      List<ObjectInfo> children = fieldEditor.getPresentation().getChildrenGraphical();
      assertThat(children).isEmpty();
    }
    // check CreationSupport for exposed "labelControl"
    {
      FieldEditorSubComponentCreationSupport creationSupport =
          (FieldEditorSubComponentCreationSupport) labelControl.getCreationSupport();
      assertEquals("method: getLabelControl", creationSupport.toString());
      assertSame(fieldEditor.getCreationSupport().getNode(), creationSupport.getNode());
      assertFalse(creationSupport.canReorder());
      assertFalse(creationSupport.canReparent());
      assertTrue(creationSupport.canDelete());
    }
    // check VariableSupport for exposed "labelControl"
    {
      FieldEditorSubComponentVariableSupport variableSupport =
          (FieldEditorSubComponentVariableSupport) labelControl.getVariableSupport();
      assertEquals("subComponent", variableSupport.toString());
      assertEquals("getLabelControl()", variableSupport.getTitle());
      assertEquals("stringFieldEditorLabelControl", variableSupport.getComponentName());
      // routing to "hostJavaInfo"
      variableSupport.isValidStatementForChild(AstNodeUtils.getEnclosingStatement(fieldEditor.getCreationSupport().getNode()));
      assertEquals(
          fieldEditor.getVariableSupport().getStatementTarget().toString(),
          variableSupport.getStatementTarget().toString());
      assertEquals(
          JavaInfoUtils.getTarget(fieldEditor, null).toString(),
          variableSupport.getChildTarget().toString());
    }
    // check Association for exposed "labelControl"
    assertThat(labelControl.getAssociation()).isInstanceOf(ImplicitObjectAssociation.class);
    // check refresh()
    page.refresh();
    // check that bounds just exist, we will test them carefully in next tests
    assertThat(fieldEditor.getBounds().width).isGreaterThan(300);
    assertThat(fieldEditor.getBounds().height).isGreaterThan(18);
    assertThat(labelControl.getBounds().x).isEqualTo(0);
    assertThat(labelControl.getBounds().width).isGreaterThan(50);
    assertThat(labelControl.getBounds().height).isGreaterThan(10);
  }

  /**
   * Test for exposed sub-components of {@link FieldEditorInfo}.<br>
   * Materializing sub-component on property change.
   */
  public void test_subComponents_1() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(FLAT);",
            "  }",
            "  protected void createFieldEditors() {",
            "    {",
            "      StringFieldEditor stringFieldEditor = new StringFieldEditor('id', 'String editor', getFieldEditorParent());",
            "      addField(stringFieldEditor);",
            "    }",
            "  }",
            "}");
    page.refresh();
    FieldEditorInfo fieldEditor = page.getEditors().get(0);
    ControlInfo labelControl = fieldEditor.getChildControls().get(0);
    // do materialize
    labelControl.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "    super(FLAT);",
        "  }",
        "  protected void createFieldEditors() {",
        "    {",
        "      Composite composite = getFieldEditorParent();",
        "      StringFieldEditor stringFieldEditor = new StringFieldEditor('id', 'String editor', composite);",
        "      stringFieldEditor.getLabelControl(composite).setEnabled(false);",
        "      addField(stringFieldEditor);",
        "    }",
        "  }",
        "}");
    // set one more property
    labelControl.getPropertyByTitle("toolTipText").setValue("Some tooltip");
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "    super(FLAT);",
        "  }",
        "  protected void createFieldEditors() {",
        "    {",
        "      Composite composite = getFieldEditorParent();",
        "      StringFieldEditor stringFieldEditor = new StringFieldEditor('id', 'String editor', composite);",
        "      stringFieldEditor.getLabelControl(composite).setToolTipText('Some tooltip');",
        "      stringFieldEditor.getLabelControl(composite).setEnabled(false);",
        "      addField(stringFieldEditor);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for exposed sub-components of {@link FieldEditorInfo}.<br>
   * Materializing sub-component on property change.<br>
   * Even if {@link FieldEditor} is not in {@link Block}.
   */
  public void test_subComponents_2() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(FLAT);",
            "  }",
            "  protected void createFieldEditors() {",
            "    addField(new StringFieldEditor('id', 'String editor', getFieldEditorParent()));",
            "  }",
            "}");
    page.refresh();
    FieldEditorInfo fieldEditor = page.getEditors().get(0);
    ControlInfo labelControl = fieldEditor.getChildControls().get(0);
    // do materialize
    labelControl.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "    super(FLAT);",
        "  }",
        "  protected void createFieldEditors() {",
        "    {",
        "      Composite composite = getFieldEditorParent();",
        "      StringFieldEditor stringFieldEditor = new StringFieldEditor('id', 'String editor', composite);",
        "      stringFieldEditor.getLabelControl(composite).setEnabled(false);",
        "      addField(stringFieldEditor);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for exposed sub-components of {@link FieldEditorInfo}.<br>
   * Parse materialized sub-components.
   */
  public void test_subComponents_3() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(FLAT);",
            "  }",
            "  protected void createFieldEditors() {",
            "    {",
            "      Composite composite = getFieldEditorParent();",
            "      StringFieldEditor stringFieldEditor = new StringFieldEditor('id', 'String editor', composite);",
            "      stringFieldEditor.getLabelControl(composite).setToolTipText('Some tooltip');",
            "      stringFieldEditor.getLabelControl(composite).setEnabled(false);",
            "      addField(stringFieldEditor);",
            "    }",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.preference.FieldEditorPreferencePage} {this} {/getFieldEditorParent()/ /addField(stringFieldEditor)/}",
        "  {new: org.eclipse.jface.preference.StringFieldEditor} {local-unique: stringFieldEditor} {/new StringFieldEditor('id', 'String editor', composite)/ /stringFieldEditor.getLabelControl(composite)/ /stringFieldEditor.getLabelControl(composite)/ /addField(stringFieldEditor)/}",
        "    {method: getLabelControl} {subComponent} {/stringFieldEditor.getLabelControl(composite).setToolTipText('Some tooltip')/ /stringFieldEditor.getLabelControl(composite).setEnabled(false)/}",
        "    {method: getTextControl} {subComponent} {}");
    // perform refresh()
    page.refresh();
    FieldEditorInfo fieldEditor = page.getEditors().get(0);
    ControlInfo labelControl = fieldEditor.getChildControls().get(0);
    // ensure that properties are applied
    Object labelObject = labelControl.getObject();
    assertEquals("Some tooltip", ReflectionUtils.invokeMethod2(labelObject, "getToolTipText"));
    assertEquals(false, ReflectionUtils.invokeMethod2(labelObject, "isEnabled"));
    // check delete()
    assertTrue(labelControl.canDelete());
    labelControl.delete();
    // source changed, we don't have properties for "labelControl"
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "    super(FLAT);",
        "  }",
        "  protected void createFieldEditors() {",
        "    {",
        "      Composite composite = getFieldEditorParent();",
        "      StringFieldEditor stringFieldEditor = new StringFieldEditor('id', 'String editor', composite);",
        "      addField(stringFieldEditor);",
        "    }",
        "  }",
        "}");
    // ...but "labelControl" is still in "fieldEditor"
    assertThat(fieldEditor.getChildControls()).contains(labelControl);
  }

  /**
   * Test for exposed sub-components of {@link FieldEditorInfo}.<br>
   * Copy/paste materialized sub-components.
   */
  public void test_subComponents_4() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "  }",
            "  protected void createFieldEditors() {",
            "    {",
            "      Composite composite = getFieldEditorParent();",
            "      StringFieldEditor stringFieldEditor = new StringFieldEditor('id', 'String editor', composite);",
            "      stringFieldEditor.getLabelControl(composite).setEnabled(false);",
            "      addField(stringFieldEditor);",
            "    }",
            "  }",
            "}");
    page.refresh();
    FieldEditorInfo fieldEditor = page.getEditors().get(0);
    // do copy/paste
    {
      JavaInfoMemento memento = JavaInfoMemento.createMemento(fieldEditor);
      // ensure that this JavaInfoMemento uses only serializable objects
      {
        ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
        oos.writeObject(memento);
      }
      // do paste
      FieldEditorInfo newFieldEditor = (FieldEditorInfo) memento.create(page);
      page.command_CREATE(newFieldEditor, null);
      memento.apply();
    }
    assertEditor(
        "import org.eclipse.jface.preference.*;",
        "public class Test extends FieldEditorPreferencePage {",
        "  public Test() {",
        "  }",
        "  protected void createFieldEditors() {",
        "    {",
        "      Composite composite = getFieldEditorParent();",
        "      StringFieldEditor stringFieldEditor = new StringFieldEditor('id', 'String editor', composite);",
        "      stringFieldEditor.getLabelControl(composite).setEnabled(false);",
        "      addField(stringFieldEditor);",
        "    }",
        "    {",
        "      Composite composite = getFieldEditorParent();",
        "      StringFieldEditor stringFieldEditor = new StringFieldEditor('id', 'String editor', composite);",
        "      stringFieldEditor.getLabelControl(composite).setEnabled(false);",
        "      addField(stringFieldEditor);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for exposed sub-components of {@link FieldEditorInfo}.<br>
   * Bounds in {@link FieldEditorPreferencePage#GRID} mode.<br>
   * Note: bounds for sub-components are incorrect, because calculated not relative to
   * {@link FieldEditorInfo}, but relative to its parent (as {@link FieldEditorInfo} itself). Not
   * big problem right now, because we don't show these sub-component on design canvas...
   */
  public void test_subComponents_5() throws Exception {
    FieldEditorPreferencePageInfo page =
        parseJavaInfo(
            "import org.eclipse.jface.preference.*;",
            "public class Test extends FieldEditorPreferencePage {",
            "  public Test() {",
            "    super(GRID);",
            "  }",
            "  protected void createFieldEditors() {",
            "    {",
            "      StringFieldEditor stringFieldEditor = new StringFieldEditor('id', 'String editor', getFieldEditorParent());",
            "      addField(stringFieldEditor);",
            "    }",
            "  }",
            "}");
    page.refresh();
    FieldEditorInfo fieldEditor = page.getEditors().get(0);
    ControlInfo labelControl = fieldEditor.getChildControls().get(0);
    // check that bounds just exist
    assertThat(fieldEditor.getBounds().width).isGreaterThan(300);
    assertThat(fieldEditor.getBounds().height).isGreaterThan(18);
    //assertThat(labelControl.getBounds().x).isEqualTo(0);
    assertThat(labelControl.getBounds().width).isGreaterThan(50);
    assertThat(labelControl.getBounds().height).isGreaterThan(10);
  }
}