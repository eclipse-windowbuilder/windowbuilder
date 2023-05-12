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
package org.eclipse.wb.tests.designer.swt.model;

import org.eclipse.wb.internal.core.model.clipboard.ComponentInfoMemento;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Test for {@link JavaInfoMemento} and other clipboard related operations.
 *
 * @author scheglov_ke
 */
public class ClipboardTest extends RcpModelTest {
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
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_properties_String() throws Exception {
    check_propertiesPaste("setText(\"My button\")");
  }

  public void test_properties_boolean() throws Exception {
    check_propertiesPaste("setEnabled(false)");
  }

  public void test_properties_Color_new() throws Exception {
    check_propertiesPaste("setBackground(new Color(null, 1, 2, 3))");
  }

  public void test_properties_Color_system() throws Exception {
    check_propertiesPaste("setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED))");
  }

  public void test_properties_Color_systemInt() throws Exception {
    check_propertiesPaste(
        "setBackground(Display.getCurrent().getSystemColor(3))",
        "setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED))");
  }

  public void test_properties_Font_new() throws Exception {
    check_propertiesPaste("setFont(new Font(null, \"Arial\", 12, SWT.BOLD))");
  }

  public void test_properties_Font_JFaceResources() throws Exception {
    check_propertiesPaste("setFont(JFaceResources.getHeaderFont())");
  }

  public void test_properties_Image_null() throws Exception {
    check_propertiesPaste("setImage(null)");
  }

  public void test_properties_Image_newFile() throws Exception {
    check_propertiesPaste("setImage(new Image(null, \"c:/1.png\"))");
  }

  public void test_properties_Image_newClasspath() throws Exception {
    check_propertiesPaste("setImage(new Image(null, Test.class.getResourceAsStream(\"/resources/1.png\")))");
  }

  public void test_properties_Image_newClasspath_otherClass() throws Exception {
    check_propertiesPaste(
        "setImage(new Image(null, test.Test.class.getResourceAsStream(\"/resources/1.png\")))",
        "setImage(new Image(null, Test.class.getResourceAsStream(\"/resources/1.png\")))");
  }

  /**
   * Same as {@link #check_propertiesPaste(String, String)}, but with same source/target invocation.
   */
  private void check_propertiesPaste(String invocationCode) throws Exception {
    check_propertiesPaste(invocationCode, invocationCode);
  }

  /**
   * Checks that with given source invocation, given resulting invocation will be generated during
   * copy/paste.
   *
   * @param invocationCode_1
   *          the source invocation
   * @param invocationCode_2
   *          the resulting invocation
   */
  private void check_propertiesPaste(String invocationCode_1, String invocationCode_2)
      throws Exception {
    m_waitForAutoBuild = true;
    final CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button(this, SWT.BORDER | SWT.CHECK);",
            "      button." + invocationCode_1 + ";",
            "    }",
            "  }",
            "}");
    final RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
    shell.refresh();
    // create memento for "button"
    final JavaInfoMemento memento;
    {
      ControlInfo button = shell.getChildrenControls().get(0);
      memento = JavaInfoMemento.createMemento(button);
    }
    // do paste
    {
      ExecutionUtils.run(shell, new RunnableEx() {
        @Override
        public void run() throws Exception {
          ControlInfo button = (ControlInfo) memento.create(shell);
          rowLayout.command_CREATE(button, null);
          memento.apply();
        }
      });
      assertEditor(
          "class Test extends Shell {",
          "  public Test() {",
          "    setLayout(new RowLayout());",
          "    {",
          "      Button button = new Button(this, SWT.BORDER | SWT.CHECK);",
          "      button." + invocationCode_1 + ";",
          "    }",
          "    {",
          "      Button button = new Button(this, SWT.BORDER | SWT.CHECK);",
          "      button." + invocationCode_2 + ";",
          "    }",
          "  }",
          "}");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "live" image and preferred size
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for live image, preferred size during paste.
   */
  public void test_liveImageSize() throws Exception {
    final CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setBounds(10, 20, 200, 100);",
            "    }",
            "  }",
            "}");
    final AbsoluteLayoutInfo absoluteLayout = (AbsoluteLayoutInfo) shell.getLayout();
    shell.refresh();
    // create memento for "control"
    final ControlInfo sourceControl;
    final ComponentInfoMemento memento;
    {
      sourceControl = shell.getChildrenControls().get(0);
      memento = (ComponentInfoMemento) JavaInfoMemento.createMemento(sourceControl);
    }
    // we can ask for "live" image
    {
      assertNotNull(memento.getImage());
      assertFalse(memento.getImage().isDisposed());
    }
    // do paste
    ExecutionUtils.run(shell, new RunnableEx() {
      @Override
      public void run() throws Exception {
        ControlInfo control = (ControlInfo) memento.create(shell);
        // check that we support "live" image during paste
        assertNotNull(control.getImage());
        /*{
        	ImageLoader imageLoader = new ImageLoader();
        	imageLoader.data = new ImageData[]{control.getImage().getImageData()};
        	imageLoader.save("C:/1.png", SWT.IMAGE_PNG);
        }*/
        // check preferred size
        assertEquals(new Dimension(200, 100), control.getPreferredSize());
        // check absolute bounds
        {
          Rectangle bounds = control.getBounds();
          assertEquals(new Dimension(200, 100), bounds.getSize());
          assertEquals(10 + shell.getClientAreaInsets().left, bounds.x);
          assertEquals(20 + shell.getClientAreaInsets().top, bounds.y);
          assertEquals(sourceControl.getAbsoluteBounds(), bounds);
        }
        // add
        absoluteLayout.commandCreate(control, null);
        memento.apply();
        // now "live" image should be disposed
        assertTrue(memento.getImage().isDisposed());
      }
    });
  }

  /**
   * Test assertions in {@link JavaInfoMemento}.
   */
  public void test_asserts() throws Exception {
    final CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setBounds(10, 20, 200, 100);",
            "    }",
            "  }",
            "}");
    final AbsoluteLayoutInfo absoluteLayout = (AbsoluteLayoutInfo) shell.getLayout();
    shell.refresh();
    // create memento for "control"
    final ControlInfo sourceControl;
    final JavaInfoMemento memento;
    {
      sourceControl = shell.getChildrenControls().get(0);
      memento = JavaInfoMemento.createMemento(sourceControl);
    }
    // do paste
    ExecutionUtils.run(shell, new RunnableEx() {
      @Override
      public void run() throws Exception {
        // can not apply() before create()
        try {
          memento.apply();
          fail();
        } catch (AssertionFailedException e) {
        }
        // create control
        ControlInfo control = (ControlInfo) memento.create(shell);
        // can not apply() before adding to hierarchy
        try {
          memento.apply();
          fail();
        } catch (IllegalArgumentException e) {
        }
        // add
        absoluteLayout.commandCreate(control, null);
        memento.apply();
        // can not apply() second time
        try {
          memento.apply();
          fail();
        } catch (IllegalArgumentException e) {
        }
        // can not create() after apply()
        try {
          memento.create(shell);
          fail();
        } catch (IllegalArgumentException e) {
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layouts
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Does copy/paste for first child of parsed {@link CompositeInfo} with {@link RowLayoutInfo}.
   */
  private void layouts_doCopy(String[] sourceLines, String[] targetLines) throws Exception {
    final CompositeInfo shell = parseComposite(sourceLines);
    final RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
    shell.refresh();
    // do copy/paste
    {
      ControlInfo control = shell.getChildrenControls().get(0);
      doCopyPaste(control, new PasteProcedure<ControlInfo>() {
        @Override
        public void run(ControlInfo p) throws Exception {
          rowLayout.command_CREATE(p, null);
        }
      });
    }
    // check result
    assertEditor(targetLines);
  }

  public void test_factoryStatic() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static Button createButton(Composite parent, String text) {",
            "    Button button = new Button(parent, SWT.NONE);",
            "    button.setText(text);",
            "    return button;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    String[] sourceLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = StaticFactory.createButton(this, \"button\");",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = StaticFactory.createButton(this, \"button\");",
            "    }",
            "    {",
            "      Button button = StaticFactory.createButton(this, \"button\");",
            "    }",
            "  }",
            "}"};
    layouts_doCopy(sourceLines, targetLines);
  }

  public void DISABLE_test_viewer_1() throws Exception {
    String[] sourceLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      TableViewer viewer = new TableViewer(this, SWT.BORDER);",
            "      viewer.setUseHashlookup(true);",
            "      viewer.getTable().setHeaderVisible(true);",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      TableViewer viewer = new TableViewer(this, SWT.BORDER);",
            "      viewer.setUseHashlookup(true);",
            "      viewer.getTable().setHeaderVisible(true);",
            "    }",
            "    {",
            "      TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "      tableViewer.setUseHashlookup(true);",
            "      Table table = tableViewer.getTable();",
            "      table.setHeaderVisible(true);",
            "    }",
            "  }",
            "}"};
    layouts_doCopy(sourceLines, targetLines);
  }

  public void test_exposedSubComponent() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  private Button m_button;",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new RowLayout());",
            "    m_button = new Button(this, SWT.NONE);",
            "  }",
            "  public Button getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // do copy/paste
    String[] sourceLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      MyComposite myComposite = new MyComposite(this, SWT.NONE);",
            "      myComposite.getButton().setText(\"button\");",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      MyComposite myComposite = new MyComposite(this, SWT.NONE);",
            "      myComposite.getButton().setText(\"button\");",
            "    }",
            "    {",
            "      MyComposite myComposite = new MyComposite(this, SWT.NONE);",
            "      myComposite.getButton().setText(\"button\");",
            "    }",
            "  }",
            "}"};
    layouts_doCopy(sourceLines, targetLines);
  }

  public void DISABLE_test_viewer_2() throws Exception {
    String[] sourceLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Table table = new Table(this, SWT.BORDER);",
            "      table.setHeaderVisible(true);",
            "      TableViewer viewer = new TableViewer(table);",
            "      viewer.setUseHashlookup(true);",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Table table = new Table(this, SWT.BORDER);",
            "      table.setHeaderVisible(true);",
            "      TableViewer viewer = new TableViewer(table);",
            "      viewer.setUseHashlookup(true);",
            "    }",
            "    {",
            "      Table table = new Table(this, SWT.BORDER);",
            "      table.setHeaderVisible(true);",
            "      TableViewer tableViewer = new TableViewer(table);",
            "      tableViewer.setUseHashlookup(true);",
            "    }",
            "  }",
            "}"};
    layouts_doCopy(sourceLines, targetLines);
  }

  public void test_Table() throws Exception {
    String[] sourceLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Table table = new Table(this, SWT.BORDER);",
            "      {",
            "        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
            "        tableColumn.setWidth(100);",
            "        tableColumn.setText(\"AAA\");",
            "      }",
            "      {",
            "        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
            "        tableColumn.setWidth(100);",
            "        tableColumn.setText(\"BBB\");",
            "      }",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Table table = new Table(this, SWT.BORDER);",
            "      {",
            "        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
            "        tableColumn.setWidth(100);",
            "        tableColumn.setText(\"AAA\");",
            "      }",
            "      {",
            "        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
            "        tableColumn.setWidth(100);",
            "        tableColumn.setText(\"BBB\");",
            "      }",
            "    }",
            "    {",
            "      Table table = new Table(this, SWT.BORDER);",
            "      {",
            "        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
            "        tableColumn.setWidth(100);",
            "        tableColumn.setText(\"AAA\");",
            "      }",
            "      {",
            "        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
            "        tableColumn.setWidth(100);",
            "        tableColumn.setText(\"BBB\");",
            "      }",
            "    }",
            "  }",
            "}"};
    layouts_doCopy(sourceLines, targetLines);
  }

  public void test_layouts_AbsoluteLayout_implicit() throws Exception {
    String[] sourceLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "    }",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "    }",
            "  }",
            "}"};
    layouts_doCopy(sourceLines, targetLines);
  }

  public void test_layouts_AbsoluteLayout_null() throws Exception {
    String[] sourceLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(null);",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(null);",
            "    }",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(null);",
            "    }",
            "  }",
            "}"};
    layouts_doCopy(sourceLines, targetLines);
  }

  public void test_layouts_AbsoluteLayout_withComponent() throws Exception {
    String[] sourceLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      {",
            "        Button button = new Button(composite, SWT.CHECK);",
            "        button.setText(\"button\");",
            "        button.setBounds(10, 20, 200, 100);",
            "      }",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      {",
            "        Button button = new Button(composite, SWT.CHECK);",
            "        button.setText(\"button\");",
            "        button.setBounds(10, 20, 200, 100);",
            "      }",
            "    }",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      {",
            "        Button button = new Button(composite, SWT.CHECK);",
            "        button.setText(\"button\");",
            "        button.setBounds(10, 20, 200, 100);",
            "      }",
            "    }",
            "  }",
            "}"};
    layouts_doCopy(sourceLines, targetLines);
  }

  public void test_layouts_FillLayout() throws Exception {
    String[] sourceLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new FillLayout());",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "      }",
            "      {",
            "        Button button = new Button(composite, SWT.CHECK);",
            "        button.setText(\"second button\");",
            "      }",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new FillLayout());",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "      }",
            "      {",
            "        Button button = new Button(composite, SWT.CHECK);",
            "        button.setText(\"second button\");",
            "      }",
            "    }",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new FillLayout());",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "      }",
            "      {",
            "        Button button = new Button(composite, SWT.CHECK);",
            "        button.setText(\"second button\");",
            "      }",
            "    }",
            "  }",
            "}"};
    layouts_doCopy(sourceLines, targetLines);
  }

  public void test_layouts_RowLayout() throws Exception {
    String[] sourceLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new RowLayout());",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "        button.setLayoutData(new RowData(100, 200));",
            "      }",
            "      {",
            "        Button button = new Button(composite, SWT.CHECK);",
            "      }",
            "      {",
            "        Button button = new Button(composite, SWT.RADIO);",
            "        RowData rowData = new RowData();",
            "        rowData.width = 100;",
            "        button.setLayoutData(rowData);",
            "      }",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new RowLayout());",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "        button.setLayoutData(new RowData(100, 200));",
            "      }",
            "      {",
            "        Button button = new Button(composite, SWT.CHECK);",
            "      }",
            "      {",
            "        Button button = new Button(composite, SWT.RADIO);",
            "        RowData rowData = new RowData();",
            "        rowData.width = 100;",
            "        button.setLayoutData(rowData);",
            "      }",
            "    }",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new RowLayout());",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "        button.setLayoutData(new RowData(100, 200));",
            "      }",
            "      {",
            "        Button button = new Button(composite, SWT.CHECK);",
            "      }",
            "      {",
            "        Button button = new Button(composite, SWT.RADIO);",
            "        button.setLayoutData(new RowData(100, SWT.DEFAULT));",
            "      }",
            "    }",
            "  }",
            "}"};
    layouts_doCopy(sourceLines, targetLines);
  }

  public void test_layouts_GridLayout() throws Exception {
    String[] sourceLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new GridLayout(2, false));",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "        GridData gridData = new GridData();",
            "        gridData.horizontalAlignment = GridData.FILL;",
            "        button.setLayoutData(gridData);",
            "      }",
            "      new Label(composite, SWT.NONE);",
            "      new Label(composite, SWT.NONE);",
            "      {",
            "        Button button = new Button(composite, SWT.CHECK);",
            "      }",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new GridLayout(2, false));",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "        GridData gridData = new GridData();",
            "        gridData.horizontalAlignment = GridData.FILL;",
            "        button.setLayoutData(gridData);",
            "      }",
            "      new Label(composite, SWT.NONE);",
            "      new Label(composite, SWT.NONE);",
            "      {",
            "        Button button = new Button(composite, SWT.CHECK);",
            "      }",
            "    }",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new GridLayout(2, false));",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "        button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));",
            "      }",
            "      new Label(composite, SWT.NONE);",
            "      new Label(composite, SWT.NONE);",
            "      {",
            "        Button button = new Button(composite, SWT.CHECK);",
            "      }",
            "    }",
            "  }",
            "}"};
    layouts_doCopy(sourceLines, targetLines);
  }
}
