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
package org.eclipse.wb.tests.designer.XWT.model.property;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.xwt.model.jface.TableViewerInfo;
import org.eclipse.wb.internal.xwt.model.jface.ViewerColumnInfo;
import org.eclipse.wb.internal.xwt.model.property.editor.InnerClassPropertyEditor;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.gef.UIPredicate;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link InnerClassPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class InnerClassPropertyEditorTest extends XwtModelTest {
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
  // getText()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getText_noValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'/>",
        "</Shell>");
    refresh();
    TableViewerInfo viewer = getObjectByName("viewer");
    Property property = viewer.getPropertyByTitle("labelProvider");
    // check state
    assertFalse(property.isModified());
    assertEquals("<double click>", getPropertyText(property));
  }

  public void test_getText_hasValue() throws Exception {
    prepareMyLabelProvider();
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.labelProvider>",
        "      <t:MyLabelProvider/>",
        "    </TableViewer.labelProvider>",
        "  </TableViewer>",
        "</Shell>");
    refresh();
    TableViewerInfo viewer = getObjectByName("viewer");
    Property property = viewer.getPropertyByTitle("labelProvider");
    // check state
    assertTrue(property.isModified());
    assertEquals("MyLabelProvider", getPropertyText(property));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  @DisposeProjectAfter
  public void test_generateInner() throws Exception {
    // create existing class, to generate other name
    setFileContentSrc(
        "test/TableLabelProvider_1.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class TableLabelProvider_1 {",
            "}"));
    waitForAutoBuild();
    // parse
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'/>",
        "</Shell>");
    refresh();
    TableViewerInfo viewer = getObjectByName("viewer");
    Property property = viewer.getPropertyByTitle("labelProvider");
    // no value initially
    assertFalse(property.isModified());
    // activate
    activateProperty(property, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.labelProvider>",
        "      <t:TableLabelProvider_2/>",
        "    </TableViewer.labelProvider>",
        "  </TableViewer>",
        "</Shell>");
    // new state
    assertTrue(property.isModified());
    assertEquals("TableLabelProvider_2", getPropertyText(property));
    // class test.TableLabelProvider_2 created
    assertTrue(ProjectUtils.hasType(m_javaProject, "test.TableLabelProvider_2"));
    // ...and opened
    {
      IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
      assertNotNull(activeEditor);
      activeEditor.getTitle().equals("TableLabelProvider_2.java");
    }
  }

  /**
   * "contentProvider" uses "private static" modifier in template.
   */
  @DisposeProjectAfter
  public void test_generateInner_contentProvider() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'/>",
        "</Shell>");
    refresh();
    TableViewerInfo viewer = getObjectByName("viewer");
    Property property = viewer.getPropertyByTitle("contentProvider");
    // no value initially
    assertFalse(property.isModified());
    // activate
    activateProperty(property, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.contentProvider>",
        "      <t:ContentProvider_1/>",
        "    </TableViewer.contentProvider>",
        "  </TableViewer>",
        "</Shell>");
    // new state
    assertTrue(property.isModified());
    assertEquals("ContentProvider_1", getPropertyText(property));
    // class test.ContentProvider_1 created
    {
      IType type = m_javaProject.findType("test.ContentProvider_1");
      assertNotNull(type);
      assertThat(type.getSource()).contains("public class");
    }
    // ...and opened
    {
      IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
      assertNotNull(activeEditor);
      activeEditor.getTitle().equals("ContentProvider_1.java");
    }
  }

  @DisposeProjectAfter
  public void test_generateAnonymous() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer>",
        "    <TableViewer.table headerVisible='true'/>",
        "    <TableViewerColumn wbp:name='column'/>",
        "  </TableViewer>",
        "</Shell>");
    refresh();
    ViewerColumnInfo column = getObjectByName("column");
    Property property = column.getPropertyByTitle("labelProvider");
    // no value initially
    assertFalse(property.isModified());
    // activate
    activateProperty(property, null);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer>",
        "    <TableViewer.table headerVisible='true'/>",
        "    <TableViewerColumn wbp:name='column'>",
        "      <TableViewerColumn.labelProvider>",
        "        <t:ViewerColumnCellLabelProvider_1/>",
        "      </TableViewerColumn.labelProvider>",
        "    </TableViewerColumn>",
        "  </TableViewer>",
        "</Shell>");
    // new state
    assertTrue(property.isModified());
    assertEquals("ViewerColumnCellLabelProvider_1", getPropertyText(property));
    // class test.ContentProvider_1 created
    {
      IType type = m_javaProject.findType("test.ViewerColumnCellLabelProvider_1");
      assertNotNull(type);
      assertThat(type.getSource()).contains("public class");
    }
    // ...and opened
    {
      IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
      assertNotNull(activeEditor);
      activeEditor.getTitle().equals("ViewerColumnCellLabelProvider_1.java");
    }
  }

  @DisposeProjectAfter
  public void test_generateAnonymous_disabled() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer>",
        "    <TableViewer.table headerVisible='true'/>",
        "    <TableViewerColumn wbp:name='column'/>",
        "  </TableViewer>",
        "</Shell>");
    refresh();
    ViewerColumnInfo column = getObjectByName("column");
    Property property = column.getPropertyByTitle("editingSupport");
    // no value initially
    assertFalse(property.isModified());
    // activate, disabled, so nothing happens
    activateProperty(property, null);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer>",
        "    <TableViewer.table headerVisible='true'/>",
        "    <TableViewerColumn wbp:name='column'/>",
        "  </TableViewer>",
        "</Shell>");
    // still not modified
    assertFalse(property.isModified());
  }

  public void test_openOnDoubleClick() throws Exception {
    prepareMyLabelProvider();
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.labelProvider>",
        "      <t:MyLabelProvider/>",
        "    </TableViewer.labelProvider>",
        "  </TableViewer>",
        "</Shell>");
    refresh();
    TableViewerInfo viewer = getObjectByName("viewer");
    Property property = viewer.getPropertyByTitle("labelProvider");
    // do double click
    doPropertyDoubleClick(property, null);
    // class test.MyLabelProvider opened
    {
      IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
      assertNotNull(activeEditor);
      activeEditor.getTitle().equals("MyLabelProvider.java");
    }
  }

  public void test_selectExisting() throws Exception {
    prepareMyLabelProvider();
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'/>",
        "</Shell>");
    refresh();
    TableViewerInfo viewer = getObjectByName("viewer");
    final Property property = viewer.getPropertyByTitle("labelProvider");
    // open dialog and animate it
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        // set filter
        {
          context.useShell("Open type");
          Text filterText = context.findFirstWidget(Text.class);
          filterText.setText("MyLabelProvider");
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
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.labelProvider>",
        "      <t:MyLabelProvider/>",
        "    </TableViewer.labelProvider>",
        "  </TableViewer>",
        "</Shell>");
    // new state
    assertTrue(property.isModified());
    assertEquals("MyLabelProvider", getPropertyText(property));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepareMyLabelProvider() throws Exception {
    setFileContentSrc(
        "test/MyLabelProvider.java",
        getJavaSource(
            "public class MyLabelProvider extends LabelProvider implements ITableLabelProvider {",
            "  public Image getColumnImage(Object element, int columnIndex) {",
            "    return null;",
            "  }",
            "  public String getColumnText(Object element, int columnIndex) {",
            "    return element.toString();",
            "  }",
            "}"));
    waitForAutoBuild();
  }
}