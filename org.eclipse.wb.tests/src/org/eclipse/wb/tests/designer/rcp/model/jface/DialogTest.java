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
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.model.association.FactoryParentAssociation;
import org.eclipse.wb.core.model.association.UnknownAssociation;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.jface.DialogButtonIdPropertyEditor;
import org.eclipse.wb.internal.rcp.model.jface.DialogInfo;
import org.eclipse.wb.internal.rcp.model.jface.WindowInfo;
import org.eclipse.wb.internal.rcp.palette.DialogButtonEntryInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.InsValue;
import org.eclipse.wb.tests.designer.Expectations.RectValue;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link DialogInfo}.
 * 
 * @author scheglov_ke
 */
public class DialogTest extends RcpModelTest {
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
   * {@link Dialog} with {@link Dialog#createDialogArea(Composite)} method.<br>
   * Parameter "parent" in <code>createDialogArea()</code> should not have layout.
   */
  public void test_0() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "  protected Control createDialogArea(Composite parent) {",
            "    Composite container = (Composite) super.createDialogArea(parent);",
            "    {",
            "      Button button = new Button(container, SWT.NONE);",
            "    }",
            "    return container;",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.dialogs.Dialog} {this} {}",
        "  {parameter} {parent} {/super.createDialogArea(parent)/}",
        "    {casted-superInvocation: (Composite)super.createDialogArea(parent)} {local-unique: container} {/(Composite) super.createDialogArea(parent)/ /new Button(container, SWT.NONE)/ /container/}",
        "      {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
        "      {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(container, SWT.NONE)/}",
        "        {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}");
    CompositeInfo dialogAreaParent = dialog.getChildren(CompositeInfo.class).get(0);
    CompositeInfo dialogAreaSuper = dialogAreaParent.getChildren(CompositeInfo.class).get(0);
    // refresh() also should be successful
    dialog.refresh();
    assertNotNull(dialog.getImage());
    assertEquals(450, dialog.getBounds().width);
    assertEquals(300, dialog.getBounds().height);
    // check "parent" in createDialogArea(), may change with time
    {
      assertFalse(dialogAreaParent.hasLayout());
      {
        Insets expected =
            Expectations.get(new Insets(25, 3, 3, 3), new InsValue[]{
                new InsValue("flanker-windows", new Insets(25, 3, 3, 3)),
                new InsValue("scheglov-win", new Insets(25, 3, 3, 3))});
        assertEquals(expected, dialog.getClientAreaInsets());
      }
      {
        Rectangle expected =
            Expectations.get(new Rectangle(0, 0, 444, 272), new RectValue[]{
                new RectValue("flanker-windows", new Rectangle(0, 0, 444, 272)),
                new RectValue("scheglov-win", new Rectangle(0, 0, 444, 272))});
        assertEquals(expected, dialogAreaParent.getModelBounds());
      }
      {
        Rectangle expected =
            Expectations.get(new Rectangle(3, 25, 444, 272), new RectValue[]{
                new RectValue("flanker-windows", new Rectangle(3, 25, 444, 272)),
                new RectValue("scheglov-win", new Rectangle(3, 25, 444, 272))});
        assertEquals(expected, dialogAreaParent.getBounds());
      }
    }
    // check "container" in createDialogArea()
    {
      assertTrue(dialogAreaSuper.hasLayout());
      Rectangle bounds = dialogAreaSuper.getModelBounds();
      assertThat(bounds.width).isEqualTo(dialogAreaParent.getModelBounds().width);
      assertThat(bounds.height).isGreaterThan(200);
    }
  }

  /**
   * {@link Dialog} with {@link Dialog#createDialogArea(Composite)} and
   * {@link Dialog#configureShell(Shell)} methods.
   */
  public void test_1() throws Exception {
    parseJavaInfo(
        "import org.eclipse.jface.dialogs.*;",
        "public class Test extends org.eclipse.jface.dialogs.Dialog {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "  }",
        "  protected Control createDialogArea(Composite parent) {",
        "    Composite container = (Composite) super.createDialogArea(parent);",
        "    return container;",
        "  }",
        "  protected void configureShell(Shell newShell) {",
        "    super.configureShell(newShell);",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.jface.dialogs.Dialog} {this} {}",
        "  {parameter} {newShell} {/super.configureShell(newShell)/}",
        "    {parameter} {parent} {/super.createDialogArea(parent)/}",
        "      {casted-superInvocation: (Composite)super.createDialogArea(parent)} {local-unique: container} {/(Composite) super.createDialogArea(parent)/ /container/}",
        "        {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}");
  }

  /**
   * Test that even if "createButtonBar" is overridden, we still don't visit it, because this causes
   * bad hierarchy.
   */
  public void test_createDialogArea_createButtonBar() throws Exception {
    parseJavaInfo(
        "import org.eclipse.jface.dialogs.*;",
        "public class Test extends org.eclipse.jface.dialogs.Dialog {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "  }",
        "  protected Control createDialogArea(Composite parent) {",
        "    Composite container = (Composite) super.createDialogArea(parent);",
        "    return container;",
        "  }",
        "  protected Control createButtonBar(Composite parent) {",
        "    return super.createButtonBar(parent);",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.jface.dialogs.Dialog} {this} {}",
        "  {parameter} {parent} {/super.createDialogArea(parent)/}",
        "    {casted-superInvocation: (Composite)super.createDialogArea(parent)} {local-unique: container} {/(Composite) super.createDialogArea(parent)/ /container/}",
        "      {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}");
  }

  /**
   * Source code returns <code>null</code> as initial size (hard coded here, but this may happen for
   * other reasons). But this is invalid value, so {@link WindowInfo} model should intercept it and
   * return some reasonable value.
   */
  public void test_bad_getInitialSize() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "  protected Point getInitialSize() {",
            "    return null;",
            "  }",
            "}");
    dialog.refresh();
    assertNoErrors(dialog);
  }

  /**
   * If we pass <code>null</code> as parent {@link Shell}, than {@link Dialog} uses active
   * {@link Shell} as parent. This is not good, because during parsing we show
   * {@link ProgressMonitorDialog}, so we dispose it with {@link Dialog} instance.
   */
  public void test_passNullParentShell() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test() {",
            "    super((Shell) null);",
            "  }",
            "}");
    dialog.refresh();
    assertNoErrors(dialog);
    //
    Shell dialogShell = (Shell) ReflectionUtils.invokeMethod(dialog, "getShell()");
    assertNotSame(DesignerPlugin.getShell(), dialogShell.getParent());
  }

  /**
   * We should be able to set "null" layout for "container". This should not cause problems with
   * some dangling {@link GridDataInfo}.
   */
  public void test_setAbsoluteLayout() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "  protected Control createDialogArea(Composite parent) {",
            "    Composite container = (Composite) super.createDialogArea(parent);",
            "    container.setLayout(null);",
            "    new Button(container, SWT.NONE);",
            "    return container;",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.jface.dialogs.Dialog} {this} {}",
        "  {parameter} {parent} {/super.createDialogArea(parent)/}",
        "    {casted-superInvocation: (Composite)super.createDialogArea(parent)} {local-unique: container} {/(Composite) super.createDialogArea(parent)/ /container.setLayout(null)/ /new Button(container, SWT.NONE)/ /container/}",
        "      {new: org.eclipse.swt.widgets.Button} {empty} {/new Button(container, SWT.NONE)/}",
        "      {null} {absolute} {}");
    // refresh
    dialog.refresh();
    assertNoErrors(dialog);
  }

  /**
   * {@link Dialog} that uses local static factory method.
   * <p>
   * We can not test this problem in core, because we need to test that factory flag is checked for
   * all methods, not only for methods in execution flow. And, at least now, "rendering" support in
   * hard coded into each component, such as {@link DialogInfo}.
   */
  public void test_withLocalStaticFactory() throws Exception {
    m_waitForAutoBuild = true;
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "  protected Control createDialogArea(Composite parent) {",
            "    Composite container = (Composite) super.createDialogArea(parent);",
            "    {",
            "      Button button = createButton(container);",
            "      button.setText('A');",
            "    }",
            "    {",
            "      Button button = createButton(container);",
            "      button.setText('B');",
            "    }",
            "    return container;",
            "  }",
            "  /**",
            "  * @wbp.factory",
            "  */",
            "  public static Button createButton(Composite parent) {",
            "    return new Button(parent, SWT.NONE);",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.dialogs.Dialog} {this} {}",
        "  {parameter} {parent} {/super.createDialogArea(parent)/}",
        "    {casted-superInvocation: (Composite)super.createDialogArea(parent)} {local-unique: container} {/(Composite) super.createDialogArea(parent)/ /createButton(container)/ /createButton(container)/ /container/}",
        "      {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
        "      {static factory: test.Test createButton(org.eclipse.swt.widgets.Composite)} {local-unique: button} {/createButton(container)/ /button.setText('A')/}",
        "        {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}",
        "      {static factory: test.Test createButton(org.eclipse.swt.widgets.Composite)} {local-unique: button} {/createButton(container)/ /button.setText('B')/}",
        "        {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}");
    // do refresh()
    dialog.refresh();
    assertNoErrors(dialog);
    {
      CompositeInfo parent = dialog.getChildren(CompositeInfo.class).get(0);
      CompositeInfo container = parent.getChildren(CompositeInfo.class).get(0);
      Composite containerObject = (Composite) container.getObject();
      assertThat(containerObject.getChildren()).hasSize(2);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Button bar
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link Dialog} is active, it contributes {@link DialogButtonEntryInfo} to JFace palette.
   */
  public void test_buttonBar_buttonOnPalette() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "}");
    // prepare category/entries
    CategoryInfo category = new CategoryInfo();
    category.setId("org.eclipse.wb.rcp.jface");
    List<EntryInfo> entries = Lists.newArrayList();
    // send palette broadcast
    PaletteEventListener listener = dialog.getBroadcast(PaletteEventListener.class);
    listener.entries(category, entries);
    // we should have exactly one entry
    assertEquals(1, entries.size());
    assertInstanceOf(DialogButtonEntryInfo.class, entries.get(0));
  }

  /**
   * Test for {@link DialogInfo#getButtonBar()}.<br>
   * No "button bar".
   */
  public void test_buttonBar_getButtonBar_0() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "}");
    // check hierarchy
    assertNull(dialog.getButtonBar());
  }

  /**
   * Test for {@link DialogInfo#getButtonBar()}.<br>
   * Has "button bar".
   */
  public void test_buttonBar_getButtonBar_1() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "  protected void createButtonsForButtonBar(Composite parent) {",
            "    createButton(parent, 0, '0', false);",
            "    createButton(parent, 1, '1', false);",
            "  }",
            "}");
    // check hierarchy
    CompositeInfo buttonBar = dialog.getButtonBar();
    assertEquals(2, buttonBar.getChildrenControls().size());
  }

  /**
   * {@link Dialog} with {@link Dialog#createButtonsForButtonBar(Composite)} method.<br>
   * Move buttons on button bar.
   */
  public void test_buttonBar_MOVE() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "  protected void createButtonsForButtonBar(Composite parent) {",
            "    createButton(parent, 0, '0', false);",
            "    createButton(parent, 1, '1', false);",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.dialogs.Dialog} {this} {/createButton(parent, 0, '0', false)/ /createButton(parent, 1, '1', false)/}",
        "  {parameter} {parent} {/createButton(parent, 0, '0', false)/ /createButton(parent, 1, '1', false)/}",
        "    {implicit-factory} {empty} {/createButton(parent, 0, '0', false)/}",
        "    {implicit-factory} {empty} {/createButton(parent, 1, '1', false)/}");
    // check "buttonBar"
    CompositeInfo buttonBar;
    {
      buttonBar = dialog.getChildren(CompositeInfo.class).get(0);
      assertFalse(buttonBar.hasLayout());
      assertInstanceOf(UnknownAssociation.class, buttonBar.getAssociation());
    }
    // prepare buttons
    assertEquals(2, buttonBar.getChildrenControls().size());
    ControlInfo button_1 = buttonBar.getChildrenControls().get(0);
    ControlInfo button_2 = buttonBar.getChildrenControls().get(1);
    // check "button_1"
    {
      assertInstanceOf(ImplicitFactoryCreationSupport.class, button_1.getCreationSupport());
      assertInstanceOf(FactoryParentAssociation.class, button_1.getAssociation());
    }
    // move "button_2" before "button_1"
    DialogInfo.moveButtonOnButtonBar(button_2, button_1);
    assertEditor(
        "import org.eclipse.jface.dialogs.*;",
        "public class Test extends org.eclipse.jface.dialogs.Dialog {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "  }",
        "  protected void createButtonsForButtonBar(Composite parent) {",
        "    createButton(parent, 1, '1', false);",
        "    createButton(parent, 0, '0', false);",
        "  }",
        "}");
  }

  /**
   * Create new button on button bar.
   */
  public void test_buttonBar_CREATE() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "  protected Control createDialogArea(Composite parent) {",
            "    return (Composite) super.createDialogArea(parent);",
            "  }",
            "  protected void createButtonsForButtonBar(Composite parent) {",
            "    createButton(parent, 0, '0', false);",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.dialogs.Dialog} {this} {/createButton(parent, 0, '0', false)/}",
        "  {parameter} {parent} {/super.createDialogArea(parent)/}",
        "    {casted-superInvocation: (Composite)super.createDialogArea(parent)} {empty} {/(Composite) super.createDialogArea(parent)/}",
        "      {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
        "    {parameter} {parent} {/createButton(parent, 0, '0', false)/}",
        "      {implicit-factory} {empty} {/createButton(parent, 0, '0', false)/}");
    CompositeInfo dialogAreaParent = dialog.getChildren(CompositeInfo.class).get(0);
    CompositeInfo dialogAreaSuper = dialogAreaParent.getChildren(CompositeInfo.class).get(0);
    CompositeInfo buttonBar = dialogAreaParent.getChildren(CompositeInfo.class).get(1);
    // check isButtonBar()
    assertFalse(DialogInfo.isButtonBar(dialogAreaParent));
    assertFalse(DialogInfo.isButtonBar(dialogAreaSuper));
    assertTrue(DialogInfo.isButtonBar(buttonBar));
    // add new dialog button
    ControlInfo newButton = DialogInfo.createButtonOnButtonBar(buttonBar, null);
    assertEditor(
        "import org.eclipse.jface.dialogs.*;",
        "public class Test extends org.eclipse.jface.dialogs.Dialog {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "  }",
        "  protected Control createDialogArea(Composite parent) {",
        "    return (Composite) super.createDialogArea(parent);",
        "  }",
        "  protected void createButtonsForButtonBar(Composite parent) {",
        "    createButton(parent, 0, '0', false);",
        "    createButton(parent, 0, 'New button', false);",
        "  }",
        "}");
    check_case40661(newButton);
  }

  /**
   * We should check that {@link InstanceFactoryCreationSupport} is correctly initialized and we can
   * ask properties.
   */
  private static void check_case40661(ControlInfo newButton) throws Exception {
    ImplicitFactoryCreationSupport creationSupport =
        (ImplicitFactoryCreationSupport) newButton.getCreationSupport();
    // check MethodDescription
    {
      MethodDescription description = creationSupport.getDescription();
      assertNotNull(description);
      assertEquals(
          "createButton({org.eclipse.swt.widgets.Composite,parent},{int},{java.lang.String},{boolean})",
          description.toString());
    }
    // ...and check properties
    {
      Property[] properties = newButton.getProperties();
      assertThat(properties.length).isGreaterThan(10);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Button's
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for "button" on "button bar".<br>
   * Test for "ID", "Text" and "Default" top level {@link Property}'s.
   */
  public void test_buttonBarButton_0() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "  protected void createButtonsForButtonBar(Composite parent) {",
            "    createButton(parent, 0, '0', false);",
            "  }",
            "}");
    ControlInfo button = dialog.getButtonBar().getChildrenControls().get(0);
    // check for properties "ID", "Text" and "Default"
    assertNotNull(button.getPropertyByTitle("ID"));
    assertNotNull(button.getPropertyByTitle("Text"));
    assertNotNull(button.getPropertyByTitle("Default"));
    // each time same Property should be returned
    {
      Property idProperty = button.getPropertyByTitle("ID");
      assertSame(idProperty, button.getPropertyByTitle("ID"));
    }
  }

  /**
   * Test for "button" on "button bar".<br>
   * Test for {@link DialogButtonIdPropertyEditor}.
   */
  public void test_buttonBarButton_1() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  private static final int CUSTOM_1 = IDialogConstants.CLIENT_ID + 1;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "  protected void createButtonsForButtonBar(Composite parent) {",
            "    createButton(parent, 0, '0', false);",
            "    createButton(parent, IDialogConstants.YES_ID, '1', false);",
            "    createButton(parent, CUSTOM_1, '1', false);",
            "  }",
            "}");
    List<ControlInfo> buttons = dialog.getButtonBar().getChildrenControls();
    // button_0
    {
      Property idProperty = buttons.get(0).getPropertyByTitle("ID");
      assertEquals("0", getPropertyText(idProperty));
    }
    // button_1
    {
      Property idProperty = buttons.get(1).getPropertyByTitle("ID");
      assertEquals("YES_ID", getPropertyText(idProperty));
    }
    // button_2
    {
      Property idProperty = buttons.get(2).getPropertyByTitle("ID");
      assertEquals("CUSTOM_1", getPropertyText(idProperty));
    }
  }

  /**
   * Test for {@link DialogButtonIdPropertyEditor#getCustomIDs(GenericProperty)}.
   */
  @SuppressWarnings("unchecked")
  public void test_getCustomIDs() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  private int NOT_FINAL_STATIC;",
            "  private static int NOT_FINAL;",
            "  private final int NOT_STATIC = 0;",
            "  private static final int TWO = 0, FRAGMENTS = 1;",
            "  private static final int NOT_INFIX = 0;",
            "  private static final int NOT_DIALOG_CONSTANTS = 1 + 2;",
            "  private static final int NOT_CLIENT = IDialogConstants.YES_ID + 2;",
            "  private static final int NOT_RIGHT_NUMBER = IDialogConstants.CLIENT_ID + (2);",
            "  private static final int GOOD = IDialogConstants.CLIENT_ID + 2;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "}");
    List<FieldDeclaration> idList =
        (List<FieldDeclaration>) ReflectionUtils.invokeMethod2(
            DialogButtonIdPropertyEditor.class,
            "getCustomIDs",
            GenericProperty.class,
            dialog.getPropertyByTitle("blockOnOpen"));
    assertThat(idList).hasSize(1);
    FieldDeclaration fieldDeclaration = idList.get(0);
    assertEquals("GOOD", DomGenerics.fragments(fieldDeclaration).get(0).getName().getIdentifier());
    {
      String nameKey =
          (String) ReflectionUtils.getFieldObject(
              DialogButtonIdPropertyEditor.class,
              "BUTTON_NAME_PROPERTY");
      assertEquals("GOOD", fieldDeclaration.getProperty(nameKey));
    }
    {
      String offsetKey =
          (String) ReflectionUtils.getFieldObject(
              DialogButtonIdPropertyEditor.class,
              "BUTTON_OFFSET_PROPERTY");
      assertEquals(2, fieldDeclaration.getProperty(offsetKey));
    }
  }

  /**
   * Test for problem that two {@link Button}-s created for single <code>createButton()</code>.
   */
  public void test_buttonBarButton_noExtraButtonObjects() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "  protected void createButtonsForButtonBar(Composite parent) {",
            "    createButton(parent, 0, 'btn', false);",
            "  }",
            "}");
    dialog.refresh();
    CompositeInfo buttonBar = dialog.getButtonBar();
    Composite buttonBarObject = (Composite) buttonBar.getObject();
    assertThat(buttonBarObject.getChildren()).hasSize(1);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_contextMenu_remove_getInitialSize() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "  protected Control createDialogArea(Composite parent) {",
            "    return (Composite) super.createDialogArea(parent);",
            "  }",
            "  protected Point getInitialSize() {",
            "    return new Point(500, 300);",
            "  }",
            "}");
    dialog.refresh();
    //
    {
      IMenuManager contextMenu = getContextMenu(dialog);
      IAction action = findChildAction(contextMenu, "Remove getInitialSize()");
      assertNotNull(action);
      action.run();
    }
    assertEditor(
        "import org.eclipse.jface.dialogs.*;",
        "public class Test extends org.eclipse.jface.dialogs.Dialog {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "  }",
        "  protected Control createDialogArea(Composite parent) {",
        "    return (Composite) super.createDialogArea(parent);",
        "  }",
        "}");
  }

  public void test_contextMenu_usePreferredSize() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "import org.eclipse.jface.dialogs.*;",
            "public class Test extends org.eclipse.jface.dialogs.Dialog {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "  protected Control createDialogArea(Composite parent) {",
            "    return (Composite) super.createDialogArea(parent);",
            "  }",
            "}");
    dialog.refresh();
    Dimension preferredSize = dialog.getPreferredSize().getCopy();
    //
    {
      IMenuManager contextMenu = getContextMenu(dialog);
      IAction action = findChildAction(contextMenu, "Set minimal size, as after pack()");
      assertNotNull(action);
      action.run();
    }
    assertEditor(
        "import org.eclipse.jface.dialogs.*;",
        "public class Test extends org.eclipse.jface.dialogs.Dialog {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "  }",
        "  protected Control createDialogArea(Composite parent) {",
        "    return (Composite) super.createDialogArea(parent);",
        "  }",
        "  protected Point getInitialSize() {",
        "    return new Point(" + preferredSize.width + ", " + preferredSize.height + ");",
        "  }",
        "}");
  }
}