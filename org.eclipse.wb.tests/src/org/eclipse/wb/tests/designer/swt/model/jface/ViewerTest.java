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
package org.eclipse.wb.tests.designer.swt.model.jface;

import org.eclipse.wb.core.model.IWrapper;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.core.model.association.WrappedObjectAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.WrapperMethodControlCreationSupport;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.InnerClassPropertyEditor;
import org.eclipse.wb.internal.core.model.util.ExposeComponentSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.WrapperMethodControlVariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.FieldUniqueVariableDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.NoEntryPointError;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.TableInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;
import org.eclipse.wb.tests.gef.UIPredicate;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

/**
 * Test for {@link ViewerInfo}.
 * 
 * @author scheglov_ke
 */
public class ViewerTest extends RcpModelTest {
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
  // Expression
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_normalNoControl_hasExpression() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    VariableSupport variableSupport = table.getVariableSupport();
    NodeTarget nodeTarget = getNodeStatementTarget(shell, false, 1);
    // "table" has expression
    {
      String expectedSource = m_lastEditor.getSource();
      assertTrue(variableSupport.hasExpression(nodeTarget));
      assertEditor(expectedSource, m_lastEditor);
    }
  }

  public void test_normalNoControl_getReferenceExpression_viewerAsTarget() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    VariableSupport variableSupport = table.getVariableSupport();
    NodeTarget nodeTarget = getNodeStatementTarget(shell, false, 1);
    // ask for expression, so materialize
    assertEquals("table", variableSupport.getReferenceExpression(nodeTarget));
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
        "    Table table = tableViewer.getTable();",
        "  }",
        "}");
  }

  public void test_normalNoControl_getReferenceExpression_blockAsTarget() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    VariableSupport variableSupport = table.getVariableSupport();
    NodeTarget nodeTarget = getNodeBlockTarget(shell, false);
    // ask for expression, so materialize
    assertEquals("table", variableSupport.getReferenceExpression(nodeTarget));
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
        "    Table table = tableViewer.getTable();",
        "  }",
        "}");
  }

  public void test_normalNoControl_getAccessExpression_blockAsTarget() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    VariableSupport variableSupport = table.getVariableSupport();
    NodeTarget nodeTarget = getNodeBlockTarget(shell, false);
    // ask for expression, so materialize
    assertEquals("table.", variableSupport.getAccessExpression(nodeTarget));
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
        "    Table table = tableViewer.getTable();",
        "  }",
        "}");
  }

  /**
   * We should be able to create {@link Viewer} even if it was subclassed anonymously.
   */
  public void test_parseAnonymous_standalone() throws Exception {
    parseComposite(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER) {",
        "    };",
        "  }",
        "}");
    refresh();
    assertNoErrors(m_lastParseInfo);
  }

  /**
   * We should be able to create {@link Viewer} even if it was subclassed anonymously.
   */
  public void test_parseAnonymous_wrapper() throws Exception {
    parseComposite(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    Table table = new Table(this, SWT.BORDER);",
        "    TableViewer tableViewer = new TableViewer(table) {",
        "    };",
        "  }",
        "}");
    refresh();
    assertNoErrors(m_lastParseInfo);
  }

  /**
   * Test that {@link Viewer} has "Expose viewer..." action.
   */
  public void test_exposeViewer() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    ViewerInfo viewer = (ViewerInfo) table.getChildrenJava().get(0);
    // check for expose action
    {
      IAction exposeAction = findChildAction(getContextMenu(viewer), "Expose viewer...");
      assertNotNull(exposeAction);
    }
    // do expose
    ReflectionUtils.invokeMethod(
        ExposeComponentSupport.class,
        "expose(org.eclipse.wb.core.model.JavaInfo,java.lang.String,java.lang.String)",
        viewer,
        "getTableViewer",
        "public");
    assertEditor(
        "class Test extends Shell {",
        "  private TableViewer tableViewer;",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    tableViewer = new TableViewer(this, SWT.BORDER);",
        "  }",
        "  public TableViewer getTableViewer() {",
        "    return tableViewer;",
        "  }",
        "}");
  }

  public void test_exposedViewer() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  private TableViewer m_viewer;",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new FillLayout());",
            "    m_viewer = new TableViewer(this, SWT.BORDER);",
            "  }",
            "  public TableViewer getViewer() {",
            "    return m_viewer;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseComposite(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    MyComposite composite = new MyComposite(this, SWT.NONE);",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new MyComposite(this, SWT.NONE)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {new: test.MyComposite} {local-unique: composite} {/new MyComposite(this, SWT.NONE)/}",
        "    {implicit-layout: org.eclipse.swt.layout.FillLayout} {implicit-layout} {}",
        "    {viewer: public org.eclipse.swt.widgets.Table org.eclipse.jface.viewers.TableViewer.getTable()} {viewer} {}",
        "      {method: public org.eclipse.jface.viewers.TableViewer test.MyComposite.getViewer()} {property} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "normal" viewer, that creates its control
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_parseNormalNoControl() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "    tableViewer.setUseHashlookup(true);",
            "  }",
            "}");
    assertEquals(2, shell.getChildrenJava().size());
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // check viewer
    ViewerInfo viewer;
    {
      viewer = (ViewerInfo) table.getChildren().get(0);
      assertEquals("tableViewer", viewer.getVariableSupport().getName());
      // association
      {
        Association association = viewer.getAssociation();
        assertInstanceOf(ConstructorParentAssociation.class, association);
      }
    }
    // table association: association of viewer
    {
      Association association = table.getAssociation();
      assertInstanceOf(WrappedObjectAssociation.class, association);
      assertEquals("new TableViewer(this, SWT.BORDER)", association.getSource());
      assertSame(association.getStatement(), viewer.getAssociation().getStatement());
    }
    // check table: variable
    {
      VariableSupport variable = table.getVariableSupport();
      assertInstanceOf(WrapperMethodControlVariableSupport.class, variable);
      assertEquals("viewer", variable.toString());
      assertEquals("tableViewer.getTable()", variable.getTitle());
    }
    // check table: creation
    {
      CreationSupport creation = table.getCreationSupport();
      assertInstanceOf(WrapperMethodControlCreationSupport.class, creation);
      assertEquals(
          "viewer: public org.eclipse.swt.widgets.Table org.eclipse.jface.viewers.TableViewer.getTable()",
          creation.toString());
      assertEquals(viewer.getCreationSupport().getNode(), creation.getNode());
      assertTrue(creation.canReorder());
      assertTrue(creation.canReparent());
    }
    // check delete
    {
      assertTrue(table.canDelete());
      table.delete();
      assertEditor(
          "class Test extends Shell {",
          "  public Test() {",
          "    setLayout(new FillLayout());",
          "  }",
          "}");
    }
  }

  public void test_normalNoControl_materialize() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // materialize Table
    table.addMethodInvocation("setHeaderVisible(boolean)", "true");
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
        "    Table table = tableViewer.getTable();",
        "    table.setHeaderVisible(true);",
        "  }",
        "}");
    // check supports
    assertInstanceOf(WrapperMethodControlCreationSupport.class, table.getCreationSupport());
    assertInstanceOf(LocalUniqueVariableSupport.class, table.getVariableSupport());
    // check isJavaInfo()
    {
      MethodInvocation getTableNode = (MethodInvocation) table.getRelatedNodes().get(0);
      assertTrue(table.isRepresentedBy(getTableNode));
    }
  }

  public void test_normalNoControl_move() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "    Composite composite = new Composite(this, SWT.NONE);",
            "  }",
            "}");
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // do move
    fillLayout.command_MOVE(table, null);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    Composite composite = new Composite(this, SWT.NONE);",
        "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
        "  }",
        "}");
  }

  public void test_normalNoControl_reparent() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    //",
            "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "    //",
            "    Composite composite = new Composite(this, SWT.NONE);",
            "    composite.setLayout(new RowLayout());",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // prepare composite
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(1);
    RowLayoutInfo rowLayout = (RowLayoutInfo) composite.getLayout();
    // do reparent
    rowLayout.command_MOVE(table, null);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    //",
        "    Composite composite = new Composite(this, SWT.NONE);",
        "    composite.setLayout(new RowLayout());",
        "    //",
        "    TableViewer tableViewer = new TableViewer(composite, SWT.BORDER);",
        "  }",
        "}");
  }

  public void test_normalWithControl_reparent() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    //",
            "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "    Table table = tableViewer.getTable();",
            "    //",
            "    Composite composite = new Composite(this, SWT.NONE);",
            "    composite.setLayout(new RowLayout());",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // prepare composite
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(1);
    RowLayoutInfo rowLayout = (RowLayoutInfo) composite.getLayout();
    // do reparent
    rowLayout.command_MOVE(table, null);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    //",
        "    Composite composite = new Composite(this, SWT.NONE);",
        "    composite.setLayout(new RowLayout());",
        "    //",
        "    TableViewer tableViewer = new TableViewer(composite, SWT.BORDER);",
        "    Table table = tableViewer.getTable();",
        "  }",
        "}");
  }

  public void test_materialized_move() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "    {",
            "      TableViewer tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);",
            "      Table table = tableViewer.getTable();",
            "    }",
            "  }",
            "}");
    ControlInfo button = shell.getChildrenControls().get(0);
    ControlInfo table = shell.getChildrenControls().get(1);
    RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
    // do move
    rowLayout.command_MOVE(table, button);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      TableViewer tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);",
        "      Table table = tableViewer.getTable();",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  public void test_newCreated_move() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    ControlInfo button = shell.getChildrenControls().get(0);
    RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
    // do create
    ViewerInfo viewer = createTableViewer(m_lastEditor);
    ControlInfo table = (ControlInfo) viewer.getWrapper().getWrappedInfo();
    rowLayout.command_CREATE(table, null);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      TableViewer tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);",
        "      Table table = tableViewer.getTable();",
        "    }",
        "  }",
        "}");
    // do move
    rowLayout.command_MOVE(table, button);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      TableViewer tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);",
        "      Table table = tableViewer.getTable();",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Viewer around Table
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_parseAroundTable() throws Exception {
    parseComposite(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Table table = new Table(this, SWT.BORDER);",
        "      TableViewer viewer = new TableViewer(table);",
        "      viewer.getTable().setEnabled(false);",
        "    }",
        "  }",
        "}");
    // hierarchy
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new Table(this, SWT.BORDER)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {new: org.eclipse.swt.widgets.Table} {local-unique: table} {/new Table(this, SWT.BORDER)/ /new TableViewer(table)/ /viewer.getTable().setEnabled(false)/}",
        "    {new: org.eclipse.jface.viewers.TableViewer} {local-unique: viewer} {/new TableViewer(table)/ /viewer.getTable()/}");
    TableInfo table = getJavaInfoByName("table");
    ViewerInfo viewer = getJavaInfoByName("viewer");
    assertSame(table, viewer.getParent());
    // IWrapper
    IWrapper wrapper = viewer.getWrapper();
    assertSame(viewer, wrapper.getWrapperInfo());
    assertSame(table, wrapper.getWrappedInfo());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_property_useHashlookup() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    ViewerInfo viewer = (ViewerInfo) table.getChildrenJava().get(0);
    Property property = viewer.getPropertyByTitle("useHashlookup");
    // default value for "useHashlookup"
    assertEquals(Boolean.FALSE, property.getValue());
    // set "true" - add invocation
    property.setValue(Boolean.TRUE);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
        "    tableViewer.setUseHashlookup(true);",
        "  }",
        "}");
    // set "false" - remove invocation
    property.setValue(Boolean.FALSE);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
        "  }",
        "}");
  }

  public void test_property_style() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    ViewerInfo viewer = (ViewerInfo) table.getChildrenJava().get(0);
    //
    Property property = viewer.getPropertyByTitle("Style");
    assertEquals(SWT.BORDER, property.getValue());
  }

  public void test_contentProvider() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    ViewerInfo viewer = (ViewerInfo) table.getChildrenJava().get(0);
    //
    final Property property = viewer.getPropertyByTitle("contentProvider");
    final InnerClassPropertyEditor propertyEditor = (InnerClassPropertyEditor) property.getEditor();
    // no provider
    assertEquals("<double click>", getPropertyText(property));
    // add new provider
    {
      propertyEditor.activate(null, property, null);
      assertEditor(
          "class Test extends Shell {",
          "  private static class ContentProvider implements IStructuredContentProvider {",
          "    public Object[] getElements(Object inputElement) {",
          "      return new Object[0];",
          "    }",
          "    public void dispose() {",
          "    }",
          "    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {",
          "    }",
          "  }",
          "  public Test() {",
          "    setLayout(new FillLayout());",
          "    TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
          "    tableViewer.setContentProvider(new ContentProvider());",
          "  }",
          "}");
      assertEquals("test.Test.ContentProvider", getPropertyText(property));
    }
    // just kick "doubleClick", no result tested
    propertyEditor.doubleClick(property, null);
    // use GUI to set "ArrayContentProvider"
    {
      String expectedSource =
          StringUtils.replace(
              m_lastEditor.getSource(),
              "new ContentProvider",
              "new ArrayContentProvider");
      // open dialog and animate it
      new UiContext().executeAndCheck(new UIRunnable() {
        public void run(UiContext context) throws Exception {
          openPropertyDialog(property);
        }
      }, new UIRunnable() {
        public void run(UiContext context) throws Exception {
          context.useShell("Open type");
          // set filter
          {
            Text filterText = context.findFirstWidget(Text.class);
            filterText.setText("ArrayContentPro");
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
          {
            Button okButton = context.getButtonByText("OK");
            context.click(okButton);
          }
        }
      });
      // check source
      assertEditor(expectedSource, m_lastEditor);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * There was problem that {@link ClassLoader} was not ready during parsing {@link ViewerInfo}
   * code.
   * <p>
   * This code fails in any case, because {@link ViewerInfo} can not be root, and "parent" parameter
   * is not considered as model.
   */
  public void test_noRootComposite() throws Exception {
    try {
      parseComposite(
          "public class Test {",
          "  public Test(Composite parent) {",
          "    TableViewer tableViewer = new TableViewer(parent, SWT.BORDER);",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      Throwable rootCause = DesignerExceptionUtils.getRootCause(e);
      assertThat(rootCause).isExactlyInstanceOf(NoEntryPointError.class);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_liveImage() throws Exception {
    parseComposite(
        "// filler filler filler",
        "class Test extends Shell {",
        "  public Test() {",
        "  }",
        "}");
    // prepare viewer and table
    ViewerInfo viewer = createTableViewer(m_lastEditor);
    TableInfo table = (TableInfo) viewer.getWrapper().getWrappedInfo();
    // check that table has "create" image
    Image image = table.getImage();
    assertNotNull(image);
  }

  /**
   * Test for custom viewers, such as Nebula Viewers (GridTableViewer, RichTextViewer,
   * GridTreeViewer).
   */
  public void test_CREATE_liveImage_forcedSize() throws Exception {
    setFileContentSrc(
        "test/MyTable.java",
        getTestSource(
            "public class MyTable extends Table {",
            "  public MyTable(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  protected void checkSubclass () {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyTableViewer.java",
        getTestSource(
            "public class MyTableViewer extends TableViewer {",
            "  public MyTableViewer(Composite parent) {",
            "    super(new MyTable(parent, SWT.NO_SCROLL));",
            "  }",
            "  public MyTable getMyTable() {",
            "    return (MyTable) getTable();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyTable.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='liveComponent.forcedSize.width'>200</parameter>",
            "    <parameter name='liveComponent.forcedSize.height'>150</parameter>",
            "  </parameters>",
            "</component>"));
    setFileContentSrc(
        "test/MyTableViewer.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyTableViewer(%parent%)]]></source>",
            "  </creation>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='org.eclipse.swt.widgets.Composite' parent='true'/>",
            "    </constructor>",
            "  </constructors>",
            "  <parameters>",
            "    <parameter name='viewer.control.method'>getMyTable</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parseComposite(
        "// filler filler filler",
        "class Test extends Shell {",
        "  public Test() {",
        "  }",
        "}");
    // prepare viewer and table
    ViewerInfo viewer = createViewer(m_lastEditor, "test.MyTableViewer");
    TableInfo table = (TableInfo) viewer.getWrapper().getWrappedInfo();
    // check that table has "create" image 200x150
    {
      Image image = table.getImage();
      assertNotNull(image);
      Rectangle bounds = image.getBounds();
      assertEquals(200, bounds.width);
      assertEquals(150, bounds.height);
    }
  }

  public void test_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    // prepare Viewer
    ViewerInfo viewer = createTableViewer(m_lastEditor);
    // prepare Table
    TableInfo table = (TableInfo) JavaInfoUtils.getWrapped(viewer);
    assertSame(table, JavaInfoUtils.getWrapped(table));
    // check current CreationSupport
    {
      CreationSupport creationSupport = table.getCreationSupport();
      // no node yet, and we can not it, because when it is set, we replace this CreationSupport
      assertNull(creationSupport.getNode());
      // isJavaInfo()
      assertFalse(creationSupport.isJavaInfo(null));
      // permissions
      assertFalse(creationSupport.canReorder());
      assertFalse(creationSupport.canReparent());
    }
    // create
    fillLayout.command_CREATE(table, null);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      TableViewer tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);",
        "      Table table = tableViewer.getTable();",
        "    }",
        "  }",
        "}");
    // check table
    {
      assertSame(shell, table.getParent());
      // creation
      {
        CreationSupport creationSupport = table.getCreationSupport();
        assertInstanceOf(WrapperMethodControlCreationSupport.class, creationSupport);
        assertSame(table, ReflectionUtils.getFieldObject(creationSupport, "m_javaInfo"));
      }
      // variable
      assertInstanceOf(LocalUniqueVariableSupport.class, table.getVariableSupport());
      // toString
      assertEquals(
          "{viewer: public org.eclipse.swt.widgets.Table org.eclipse.jface.viewers.TableViewer.getTable()} {local-unique: table} {/tableViewer.getTable()/}",
          table.toString());
    }
    // check viewer
    {
      assertSame(viewer, table.getChildrenJava().get(0));
      // creation
      assertInstanceOf(ConstructorCreationSupport.class, viewer.getCreationSupport());
      // variable
      assertInstanceOf(LocalUniqueVariableSupport.class, viewer.getVariableSupport());
      assertEquals("tableViewer", viewer.getVariableSupport().getName());
      // toString
      assertEquals(
          "{new: org.eclipse.jface.viewers.TableViewer} {local-unique: tableViewer} {/new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION)/ /tableViewer.getTable()/}",
          viewer.toString());
    }
    // check associations
    {
      Association viewerAssociation = viewer.getAssociation();
      Association tableAssociation = table.getAssociation();
      // viewer association
      assertInstanceOf(ConstructorParentAssociation.class, viewerAssociation);
      assertEquals(
          "new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION)",
          viewerAssociation.getSource());
      // table association
      assertSame(viewerAssociation.getStatement(), tableAssociation.getStatement());
    }
  }

  /**
   * Viewers should use code generation settings.
   */
  public void test_CREATE_useFieldVariable() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    // prepare Viewer and Table
    ViewerInfo viewer = createTableViewer(m_lastEditor);
    TableInfo table = (TableInfo) JavaInfoUtils.getWrapped(viewer);
    // create
    GenerationSettings settings = viewer.getDescription().getToolkit().getGenerationSettings();
    settings.setVariable(FieldUniqueVariableDescription.INSTANCE);
    fillLayout.command_CREATE(table, null);
    assertEditor(
        "class Test extends Shell {",
        "  private Table table;",
        "  private TableViewer tableViewer;",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);",
        "      table = tableViewer.getTable();",
        "    }",
        "  }",
        "}");
  }

  /**
   * @return the new {@link ViewerInfo} instance.
   */
  public static ViewerInfo createTableViewer(AstEditor editor) throws Exception {
    return createViewer(editor, "org.eclipse.jface.viewers.TableViewer");
  }

  private static ViewerInfo createViewer(AstEditor editor, String tableViewerClassName)
      throws Exception {
    CreationSupport creationSupport = new ConstructorCreationSupport();
    return (ViewerInfo) JavaInfoUtils.createJavaInfo(editor, tableViewerClassName, creationSupport);
  }
}