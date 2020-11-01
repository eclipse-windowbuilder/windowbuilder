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
package org.eclipse.wb.internal.swing.FormLayout.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.grid.GridAlignmentHelper;
import org.eclipse.wb.internal.core.model.util.grid.GridAlignmentHelper.IAlignmentProcessor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.swing.FormLayout.Activator;
import org.eclipse.wb.internal.swing.FormLayout.model.ui.ColumnsDialog;
import org.eclipse.wb.internal.swing.FormLayout.model.ui.RowsDialog;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutAssistantSupport;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.util.DefaultUnitConverter;

import java.awt.Container;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Model for JGoodies FormLayout.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public final class FormLayoutInfo extends LayoutInfo implements IPreferenceConstants {
  private final List<FormColumnInfo> m_columns = new ArrayList<>();
  private final List<FormRowInfo> m_rows = new ArrayList<>();
  private final List<List<FormColumnInfo>> m_columnGroups = new ArrayList<>();
  private final List<List<FormRowInfo>> m_rowGroups = new ArrayList<>();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    // create initial columns/rows
    if (m_columns.isEmpty() && m_rows.isEmpty()) {
      FormLayout layout = (FormLayout) getObject();
      // create columns
      {
        int columnCount = layout.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
          ColumnSpec spec = layout.getColumnSpec(column);
          m_columns.add(new FormColumnInfo(spec));
        }
      }
      // create rows
      {
        int rowCount = layout.getRowCount();
        for (int row = 1; row <= rowCount; row++) {
          RowSpec spec = layout.getRowSpec(row);
          m_rows.add(new FormRowInfo(spec));
        }
      }
    }
    // events
    addBroadcastListener(new ObjectInfoTreeComplete() {
      public void invoke() throws Exception {
        FormLayout layout = (FormLayout) getObject();
        fillDimensionGroups(layout.getColumnGroups(), m_columns, m_columnGroups);
        fillDimensionGroups(layout.getRowGroups(), m_rows, m_rowGroups);
      }
    });
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        addContextMenuActions(object, manager);
      }
    });
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (isManagedObject(javaInfo)) {
          ComponentInfo component = (ComponentInfo) javaInfo;
          CellConstraintsSupport support = getConstraints(component);
          properties.add(support.getCellProperty());
        }
      }
    });
    // alignment
    new SelectionActionsSupport(this);
    // assistant
    new LayoutAssistantSupport(this) {
      @Override
      protected AbstractAssistantPage createConstraintsPage(Composite parent,
          List<ObjectInfo> objects) {
        return new CellConstraintsAssistantPage(parent, FormLayoutInfo.this, objects);
      }
    };
  }

  @Override
  protected void removeComponentConstraints(ContainerInfo container, ComponentInfo component)
      throws Exception {
    m_constraints.remove(component);
    super.removeComponentConstraints(container, component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link FormLayoutInfo} actions into context menu.
   */
  private void addContextMenuActions(ObjectInfo object, IMenuManager manager) throws Exception {
    if (object == getContainer()) {
      manager.appendToGroup(
          IContextMenuConstants.GROUP_TOP,
          new EditDimensionsAction(ModelMessages.FormLayoutInfo_editColumns, true));
      manager.appendToGroup(
          IContextMenuConstants.GROUP_TOP,
          new EditDimensionsAction(ModelMessages.FormLayoutInfo_editRows, false));
    }
    if (object instanceof ComponentInfo && object.getParent() == getContainer()) {
      ComponentInfo component = (ComponentInfo) object;
      CellConstraintsSupport support = getConstraints(component);
      support.addContextMenu(manager);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu: EditDimensionsAction
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class EditDimensionsAction extends Action {
    private final boolean m_horizontal;

    public EditDimensionsAction(String text, boolean horizontal) {
      super(text);
      m_horizontal = horizontal;
    }

    @Override
    public void run() {
      Shell shell = DesignerPlugin.getShell();
      if (m_horizontal) {
        new ColumnsDialog(shell, FormLayoutInfo.this).open();
      } else {
        new RowsDialog(shell, FormLayoutInfo.this).open();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    properties.add(new DimensionsProperty(this, true));
    properties.add(new DimensionsProperty(this, false));
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CellConstraintsSupport access
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<ComponentInfo, CellConstraintsSupport> m_constraints = new HashMap<>();

  /**
   * @return the {@link CellConstraintsSupport} for given {@link ComponentInfo}.
   */
  public static CellConstraintsSupport getConstraints(ComponentInfo component) {
    ContainerInfo container = (ContainerInfo) component.getParent();
    Assert.isTrue(container.getChildrenComponents().contains(component));
    Assert.instanceOf(FormLayoutInfo.class, container.getLayout());
    //
    FormLayoutInfo layout = (FormLayoutInfo) container.getLayout();
    CellConstraintsSupport support = layout.m_constraints.get(component);
    if (support == null) {
      support = new CellConstraintsSupport(layout, component);
      layout.m_constraints.put(component, support);
    }
    return support;
  }

  @Override
  public void onSet() throws Exception {
    super.onSet();
    FormLayoutConverter.convert(getContainer(), this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access columns/rows
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} of {@link FormColumnInfo}.
   */
  public List<FormColumnInfo> getColumns() {
    return m_columns;
  }

  /**
   * @return the {@link List} of {@link FormRowInfo}.
   */
  public List<FormRowInfo> getRows() {
    return m_rows;
  }

  /**
   * Sets the new {@link List} of {@link FormColumnInfo}.
   */
  public void setColumns(List<FormColumnInfo> columns) throws Exception {
    m_columns.clear();
    m_columns.addAll(columns);
    writeDimensions();
  }

  /**
   * Sets the new {@link List} of {@link FormRowInfo}.
   */
  public void setRows(List<FormRowInfo> rows) throws Exception {
    m_rows.clear();
    m_rows.addAll(rows);
    writeDimensions();
  }

  /**
   * @return the {@link Dimension} with minimal count of columns/rows required for this
   *         {@link FormLayoutInfo}.
   */
  public Dimension getMinimumSize() {
    final Dimension size = new Dimension(0, 0);
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        visitComponents(new FormComponentVisitor() {
          public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
            size.width = Math.max(size.width, cell.x + cell.width - 1);
            size.height = Math.max(size.height, cell.y + cell.height - 1);
          }
        });
      }
    });
    return size;
  }

  /**
   * @return the count of components that begin in each column.
   */
  public int[] getColumnComponentsCounts() throws Exception {
    final int[] counts = new int[m_columns.size()];
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        visitComponents(new FormComponentVisitor() {
          public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
            counts[cell.x - 1]++;
          }
        });
      }
    });
    return counts;
  }

  /**
   * @return the count of components that begin in each row.
   */
  public int[] getRowComponentsCounts() throws Exception {
    final int[] counts = new int[m_rows.size()];
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        visitComponents(new FormComponentVisitor() {
          public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
            counts[cell.y - 1]++;
          }
        });
      }
    });
    return counts;
  }

  /**
   * If there are components that span multiple columns/rows, and no other "real" components in
   * these columns/rows, then removes these excess columns/rows.
   */
  public void normalizeSpanning() throws Exception {
    boolean columnRowDeleted = true;
    while (columnRowDeleted) {
      columnRowDeleted = false;
      // prepare filled columns/rows
      final boolean[] filledColumns = new boolean[m_columns.size()];
      final boolean[] filledRows = new boolean[m_rows.size()];
      visitComponents(new FormComponentVisitor() {
        public void visit(ComponentInfo bean, CellConstraintsSupport constraints) throws Exception {
          filledColumns[constraints.x - 1] = true;
          filledRows[constraints.y - 1] = true;
        }
      });
      // remove empty columns
      for (int column = filledColumns.length - 1; column >= 0; column--) {
        if (!filledColumns[column] && !m_columns.get(column).isGap()) {
          deleteColumn(column);
          columnRowDeleted = true;
          break;
        }
      }
      // remove empty rows
      for (int row = filledRows.length - 1; row >= 0; row--) {
        if (!filledRows[row] && !m_rows.get(row).isGap()) {
          deleteRow(row);
          columnRowDeleted = true;
          break;
        }
      }
    }
    // write dimensions
    writeDimensions();
  }

  /**
   * @return <code>true</code> if dimensions of this layout can be changed. We can change them only
   *         if we created layout using constructor.
   */
  public boolean canChangeDimensions() {
    return getCreationSupport() instanceof ConstructorCreationSupport;
  }

  /**
   * Writes columns/rows to the source.
   */
  public void writeDimensions() throws Exception {
    // write constructor
    {
      ConstructorCreationSupport creationSupport =
          (ConstructorCreationSupport) getCreationSupport();
      ClassInstanceCreation creation = creationSupport.getCreation();
      //
      String[] columnsSource =
          getDimenstionsSource(m_columns, "com.jgoodies.forms.layout.ColumnSpec");
      String[] rowsSource = getDimenstionsSource(m_rows, "com.jgoodies.forms.layout.RowSpec");
      columnsSource[columnsSource.length - 1] += ",";
      //
      getEditor().replaceCreationArguments(
          creation,
          ImmutableList.copyOf(CodeUtils.join(columnsSource, rowsSource)));
    }
    // write groups
    writeDimensionsGroups("setColumnGroups", m_columns, m_columnGroups);
    writeDimensionsGroups("setRowGroups", m_rows, m_rowGroups);
  }

  /**
   * @return the array of source lines for given {@link List} of {@link FormDimensionInfo}.
   */
  private static String[] getDimenstionsSource(List<? extends FormDimensionInfo> dimensions,
      String typeSource) throws Exception {
    if (dimensions.isEmpty()) {
      return new String[]{"\tnew " + typeSource + "[] {}"};
    } else {
      String[] lines = new String[1 + dimensions.size()];
      lines[0] = "\tnew " + typeSource + "[] {";
      for (int i = 0; i < dimensions.size(); i++) {
        FormDimensionInfo dimension = dimensions.get(i);
        lines[1 + i] = "\t\t" + dimension.getSource() + ",";
      }
      //
      lines[lines.length - 1] += "}";
      return lines;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Groups
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the index of group to which belongs given {@link FormDimensionInfo} or <code>-1</code>
   *         if it does not belong to any group.
   */
  public int getDimensionGroupIndex(FormDimensionInfo dimension) {
    if (dimension instanceof FormColumnInfo) {
      FormColumnInfo column = (FormColumnInfo) dimension;
      return m_columnGroups.indexOf(getColumnGroup(column));
    } else {
      FormRowInfo row = (FormRowInfo) dimension;
      return m_rowGroups.indexOf(getRowGroup(row));
    }
  }

  /**
   * @return the {@link List} of {@link FormColumnInfo} that belong to same group or
   *         <code>null</code> if given {@link FormColumnInfo} does not belong to any group.
   */
  public List<FormColumnInfo> getColumnGroup(FormColumnInfo column) {
    return getDimensionGroup(column, m_columnGroups);
  }

  /**
   * @return the {@link List} of {@link FormRowInfo} that belong to same group or <code>null</code>
   *         if given {@link FormRowInfo} does not belong to any group.
   */
  public List<FormRowInfo> getRowGroup(FormRowInfo row) {
    return getDimensionGroup(row, m_rowGroups);
  }

  /**
   * @return the {@link List} of {@link FormDimensionInfo} that belong to same group or
   *         <code>null</code> if given {@link FormDimensionInfo} does not belong to any group.
   */
  private static <T extends FormDimensionInfo> List<T> getDimensionGroup(T dimension,
      List<List<T>> groups) {
    for (List<T> group : groups) {
      if (group.contains(dimension)) {
        return group;
      }
    }
    // no group
    return null;
  }

  /**
   * Converts "int[][]" groups into {@link FormDimensionInfo}'s groups.
   */
  private static <T extends FormDimensionInfo> void fillDimensionGroups(int[][] intGroups,
      List<T> dimensions,
      List<List<T>> groups) {
    for (int groupIndex = 0; groupIndex < intGroups.length; groupIndex++) {
      int[] intGroup = intGroups[groupIndex];
      // create group
      List<T> group = new ArrayList<>();
      groups.add(group);
      // fill group
      for (int groupElementIndex = 0; groupElementIndex < intGroup.length; groupElementIndex++) {
        int dimensionIndex = intGroup[groupElementIndex];
        T dimension = dimensions.get(dimensionIndex - 1);
        group.add(dimension);
      }
    }
  }

  /**
   * Adds invocation of "set*Groups(new int[][] {...})" for given {@link FormDimensionInfo}'s and
   * groups.
   */
  private <T extends FormDimensionInfo> void writeDimensionsGroups(String methodName,
      List<T> dimensions,
      List<List<T>> groups) throws Exception {
    // clean up groups
    for (Iterator<List<T>> I = groups.iterator(); I.hasNext();) {
      List<T> group = I.next();
      // remove all "dangling" dimensions
      for (Iterator<T> J = group.iterator(); J.hasNext();) {
        T dimensionInGroup = J.next();
        if (!dimensions.contains(dimensionInGroup)) {
          J.remove();
        }
      }
      // remove empty group or group with single dimension
      if (group.size() < 2) {
        I.remove();
      }
    }
    // prepare "set*Groups" invocation
    String methodSignature = methodName + "(int[][])";
    MethodInvocation invocation = getMethodInvocation(methodSignature);
    // update "set*Groups"
    if (!groups.isEmpty()) {
      String groupsSource = "new int[][]{";
      for (ListIterator<List<T>> I = groups.listIterator(); I.hasNext();) {
        List<T> group = I.next();
        // open array
        if (I.previousIndex() != 0) {
          groupsSource += ", ";
        }
        groupsSource += "new int[]{";
        // add separate dimensions
        for (ListIterator<T> J = group.listIterator(); J.hasNext();) {
          FormDimensionInfo dimensionInGroup = J.next();
          if (J.previousIndex() != 0) {
            groupsSource += ", ";
          }
          groupsSource += 1 + dimensions.indexOf(dimensionInGroup);
        }
        // close array
        groupsSource += "}";
      }
      groupsSource += "}";
      //
      if (invocation != null) {
        Expression groupsExpression = (Expression) invocation.arguments().get(0);
        getEditor().replaceExpression(groupsExpression, groupsSource);
      } else {
        addMethodInvocation(methodSignature, groupsSource);
      }
    } else if (invocation != null) {
      getEditor().removeEnclosingStatement(invocation);
    }
  }

  /**
   * Un-groups given {@link FormColumnInfo}'s.
   */
  public void unGroupColumns(List<FormColumnInfo> columns) throws Exception {
    unGroupDimensions(columns, m_columnGroups);
    writeDimensionsGroups("setColumnGroups", m_columns, m_columnGroups);
  }

  /**
   * Un-groups given {@link FormRowInfo}'s.
   */
  public void unGroupRows(List<FormRowInfo> rows) throws Exception {
    unGroupDimensions(rows, m_rowGroups);
    writeDimensionsGroups("setRowGroups", m_rows, m_rowGroups);
  }

  /**
   * Un-groups given {@link FormDimensionInfo}'s.
   */
  private static <T extends FormDimensionInfo> void unGroupDimensions(List<T> dimensions,
      List<List<T>> groups) throws Exception {
    for (List<T> group : groups) {
      group.removeAll(dimensions);
    }
  }

  /**
   * Groups given {@link FormColumnInfo}'s.
   */
  public void groupColumns(List<FormColumnInfo> columns) throws Exception {
    groupDimensions(columns, m_columnGroups);
    writeDimensionsGroups("setColumnGroups", m_columns, m_columnGroups);
  }

  /**
   * Groups given {@link FormRowInfo}'s.
   */
  public void groupRows(List<FormRowInfo> rows) throws Exception {
    groupDimensions(rows, m_rowGroups);
    writeDimensionsGroups("setRowGroups", m_rows, m_rowGroups);
  }

  /**
   * Groups given {@link FormDimensionInfo}'s.
   */
  private static <T extends FormDimensionInfo> void groupDimensions(List<T> dimensions,
      List<List<T>> groups) {
    // remove gaps
    for (Iterator<T> I = dimensions.iterator(); I.hasNext();) {
      T dimension = I.next();
      if (dimension.isGap()) {
        I.remove();
      }
    }
    // check that at least two dimensions remain
    if (dimensions.size() < 2) {
      return;
    }
    // check there is one and only one target group
    List<T> targetGroup = null;
    for (List<T> group : groups) {
      for (T dimension : dimensions) {
        if (group.contains(dimension)) {
          if (targetGroup == null) {
            targetGroup = group;
          } else if (targetGroup != group) {
            // more than one group selected, ignore
            return;
          }
        }
      }
    }
    // if there are no existing group, create new one
    if (targetGroup == null) {
      targetGroup = new ArrayList<>();
      groups.add(targetGroup);
    }
    // add all dimensions into group
    for (T dimension : dimensions) {
      if (!targetGroup.contains(dimension)) {
        targetGroup.add(dimension);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link FormColumnInfo} and possible gap into target index.
   */
  public void insertColumn(int targetIndex) throws Exception {
    boolean targetLast = targetIndex == m_columns.size();
    boolean targetGap = !targetLast && m_columns.get(targetIndex).isGap();
    //
    moveComponentsForInsert(1 + targetIndex, true, -1, false);
    moveComponentsForInsert(1 + targetIndex, true, -1, false);
    if (targetGap) {
      m_columns.add(targetIndex, new FormColumnInfo(FormSpecs.RELATED_GAP_COLSPEC));
      m_columns.add(targetIndex + 1, new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC));
    } else if (targetLast) {
      if (!m_columns.isEmpty() && !m_columns.get(targetIndex - 1).isGap()) {
        m_columns.add(targetIndex++, new FormColumnInfo(FormSpecs.RELATED_GAP_COLSPEC));
      }
      m_columns.add(targetIndex, new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC));
    } else {
      m_columns.add(targetIndex, new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC));
      m_columns.add(targetIndex + 1, new FormColumnInfo(FormSpecs.RELATED_GAP_COLSPEC));
    }
    writeDimensions();
  }

  /**
   * Deletes the {@link FormColumnInfo} with given index.
   */
  public void deleteColumn(int index) throws Exception {
    // prepare context information
    boolean isFirst = index == 0;
    boolean isLast = index == m_columns.size() - 1;
    boolean isGap = m_columns.get(index).isGap();
    boolean isPrevGap = !isFirst && m_columns.get(index - 1).isGap();
    boolean isNextGap = !isLast && m_columns.get(index + 1).isGap();
    // do delete
    if (isGap) {
      deleteSingleColumn(index); // gap
    } else {
      deleteSingleColumn(index); // column
      if (isPrevGap) {
        deleteSingleColumn(index - 1); // gap
      } else if (isNextGap) {
        deleteSingleColumn(index); // gap
      }
    }
    writeDimensions();
  }

  /**
   * Deletes single {@link FormColumnInfo} with given index.
   */
  private void deleteSingleColumn(final int index) throws Exception {
    visitComponents(new FormComponentVisitor() {
      public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
        if (cell.x == 1 + index) {
          component.delete();
        } else if (cell.x > 1 + index) {
          cell.x--;
        } else if (cell.x + cell.width > 1 + index) {
          cell.width--;
        }
        cell.write();
      }
    });
    m_columns.remove(index);
  }

  /**
   * Deletes the {@link ComponentInfo}'s that located in {@link FormColumnInfo} with given index.
   */
  public void deleteColumnContents(final int index) throws Exception {
    visitComponents(new FormComponentVisitor() {
      public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
        if (cell.x == 1 + index) {
          component.delete();
        }
      }
    });
  }

  /**
   * Splits the {@link FormColumnInfo} with given index, i.e. adds duplicate of this
   * {@link FormColumnInfo}.
   */
  public void splitColumn(final int index) throws Exception {
    FormColumnInfo column = m_columns.get(index);
    m_columns.add(index + 1, new FormColumnInfo(FormSpecs.RELATED_GAP_COLSPEC));
    m_columns.add(index + 2, column.copy());
    writeDimensions();
    // update constraints
    visitComponents(new FormComponentVisitor() {
      public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
        if (cell.x > 1 + index) {
          cell.x += 2;
        } else if (cell.x + cell.width > 1 + index) {
          cell.width += 2;
        }
        cell.write();
      }
    });
  }

  /**
   * Moves {@link FormColumnInfo} at given index (and possible its gap) into target index.
   */
  public void command_MOVE_COLUMN(int index, int targetIndex) throws Exception {
    // prepare context information for old position
    boolean isFirst = index == 0;
    boolean isLast = index == m_columns.size() - 1;
    boolean isGap = m_columns.get(index).isGap();
    boolean isPrevGap = !isFirst && m_columns.get(index - 1).isGap();
    boolean isNextGap = !isLast && m_columns.get(index + 1).isGap();
    // prepare context information for new position
    boolean targetFirst = targetIndex == 0;
    boolean targetLast = targetIndex == m_columns.size();
    boolean targetGap = !targetLast && m_columns.get(targetIndex).isGap();
    //
    if (index < targetIndex) {
      if (isGap) {
        moveSingleColumn(index, targetIndex); // gap
      } else if (isPrevGap) {
        moveSingleColumn(index - 1, targetIndex); // gap
        moveSingleColumn(index - 1, targetIndex); // column
      } else if (isFirst && isNextGap) {
        if (targetLast) {
          moveSingleColumn(1, targetIndex); // gap
          moveSingleColumn(0, targetIndex); // column
        } else {
          if (targetGap) {
            targetIndex++;
          }
          moveSingleColumn(0, targetIndex); // column
          moveSingleColumn(0, targetIndex); // gap
        }
      } else {
        moveSingleColumn(index, targetIndex); // column
      }
    } else {
      if (isGap) {
        moveSingleColumn(index, targetIndex); // gap
      } else if (isPrevGap && targetFirst && !targetGap) {
        moveSingleColumn(index, 0); // column
        moveSingleColumn(index, 1); // gap
      } else if (isPrevGap) {
        moveSingleColumn(index, targetIndex); // column
        moveSingleColumn(index, targetIndex); // gap
      } else {
        moveSingleColumn(index, targetIndex); // column
      }
    }
    //
    writeDimensions();
  }

  /**
   * Moves single {@link FormColumnInfo}.
   */
  private void moveSingleColumn(final int index, final int targetIndex) throws Exception {
    FormColumnInfo column = m_columns.remove(index);
    if (index < targetIndex) {
      // add column
      m_columns.add(targetIndex - 1, column);
      // change constraints
      visitComponents(new FormComponentVisitor() {
        public void visit(ComponentInfo bean, CellConstraintsSupport constraints) throws Exception {
          int x = constraints.x;
          int w = constraints.width;
          if (x < 1 + index) {
            // if component contains source and doesn't contain target, decrease size
            if (x + w - 1 >= 1 + index && x + w - 1 < 1 + targetIndex) {
              constraints.width = w - 1;
            }
          } else if (x == 1 + index) {
            constraints.x = targetIndex;
            constraints.width = 1;
          } else if (x > 1 + index && x < 1 + targetIndex) {
            constraints.x = x - 1;
            // if component contains target, increase size
            if (x + w - 1 >= 1 + targetIndex) {
              constraints.width = w + 1;
            }
          } else if (x >= 1 + targetIndex) {
          }
          constraints.write();
        }
      });
    } else {
      // add column
      m_columns.add(targetIndex, column);
      // change constraints
      visitComponents(new FormComponentVisitor() {
        public void visit(ComponentInfo bean, CellConstraintsSupport constraints) throws Exception {
          int x = constraints.x;
          int w = constraints.width;
          if (x < 1 + targetIndex) {
            // if component doesn't contains source and contains target, increase size
            if (x + w - 1 >= 1 + targetIndex && x + w - 1 < 1 + index) {
              constraints.width = w + 1;
            }
          } else if (x < 1 + index) {
            constraints.x = x + 1;
            // if component contains source, decrease size
            if (x + w - 1 >= 1 + index) {
              constraints.width = w - 1;
            }
          } else if (x == 1 + index) {
            constraints.x = 1 + targetIndex;
            constraints.width = 1;
          } else if (x > 1 + index) {
          }
          constraints.write();
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link FormRowInfo} and possible gap into target index.
   */
  public void insertRow(int targetIndex) throws Exception {
    boolean targetLast = targetIndex == m_rows.size();
    boolean targetGap = !targetLast && m_rows.get(targetIndex).isGap();
    //
    moveComponentsForInsert(-1, false, 1 + targetIndex, true);
    moveComponentsForInsert(-1, false, 1 + targetIndex, true);
    if (targetGap) {
      m_rows.add(targetIndex, new FormRowInfo(FormSpecs.RELATED_GAP_ROWSPEC));
      m_rows.add(targetIndex + 1, new FormRowInfo(FormSpecs.DEFAULT_ROWSPEC));
    } else if (targetLast) {
      if (!m_rows.isEmpty() && !m_rows.get(targetIndex - 1).isGap()) {
        m_rows.add(targetIndex++, new FormRowInfo(FormSpecs.RELATED_GAP_ROWSPEC));
      }
      m_rows.add(targetIndex, new FormRowInfo(FormSpecs.DEFAULT_ROWSPEC));
    } else {
      m_rows.add(targetIndex, new FormRowInfo(FormSpecs.DEFAULT_ROWSPEC));
      m_rows.add(targetIndex + 1, new FormRowInfo(FormSpecs.RELATED_GAP_ROWSPEC));
    }
    writeDimensions();
  }

  /**
   * Deletes the {@link FormRowInfo} with given index.
   */
  public void deleteRow(int index) throws Exception {
    // prepare context information
    boolean isFirst = index == 0;
    boolean isLast = index == m_rows.size() - 1;
    boolean isGap = m_rows.get(index).isGap();
    boolean isPrevGap = !isFirst && m_rows.get(index - 1).isGap();
    boolean isNextGap = !isLast && m_rows.get(index + 1).isGap();
    // do delete
    if (isGap) {
      deleteSingleRow(index); // gap
    } else {
      deleteSingleRow(index); // row
      if (isPrevGap) {
        deleteSingleRow(index - 1); // gap
      } else if (isNextGap) {
        deleteSingleRow(index); // gap
      }
    }
    writeDimensions();
  }

  /**
   * Deletes single {@link FormRowInfo} with given index.
   */
  private void deleteSingleRow(final int index) throws Exception {
    visitComponents(new FormComponentVisitor() {
      public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
        if (cell.y == 1 + index) {
          component.delete();
        } else if (cell.y > 1 + index) {
          cell.y--;
        } else if (cell.y + cell.height > 1 + index) {
          cell.height--;
        }
        cell.write();
      }
    });
    m_rows.remove(index);
  }

  /**
   * Deletes the {@link ComponentInfo}'s that located in {@link FormRowInfo} with given index.
   */
  public void deleteRowContents(final int index) throws Exception {
    visitComponents(new FormComponentVisitor() {
      public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
        if (cell.y == 1 + index) {
          component.delete();
        }
      }
    });
  }

  /**
   * Splits the {@link FormRowInfo} with given index, i.e. adds duplicate of this
   * {@link FormRowInfo}.
   */
  public void splitRow(final int index) throws Exception {
    FormRowInfo row = m_rows.get(index);
    m_rows.add(index + 1, new FormRowInfo(FormSpecs.RELATED_GAP_ROWSPEC));
    m_rows.add(index + 2, row.copy());
    writeDimensions();
    // update constraints
    visitComponents(new FormComponentVisitor() {
      public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
        if (cell.y > 1 + index) {
          cell.y += 2;
        } else if (cell.y + cell.height > 1 + index) {
          cell.height += 2;
        }
        cell.write();
      }
    });
  }

  /**
   * Moves {@link FormRowInfo} at given index (and possible its gap) into target index.
   */
  public void command_MOVE_ROW(int index, int targetIndex) throws Exception {
    // prepare context information for old position
    boolean isFirst = index == 0;
    boolean isLast = index == m_rows.size() - 1;
    boolean isGap = m_rows.get(index).isGap();
    boolean isPrevGap = !isFirst && m_rows.get(index - 1).isGap();
    boolean isNextGap = !isLast && m_rows.get(index + 1).isGap();
    // prepare context information for new position
    boolean targetFirst = targetIndex == 0;
    boolean targetLast = targetIndex == m_rows.size();
    boolean targetGap = !targetLast && m_rows.get(targetIndex).isGap();
    //
    if (index < targetIndex) {
      if (isGap) {
        moveSingleRow(index, targetIndex); // gap
      } else if (isPrevGap) {
        moveSingleRow(index - 1, targetIndex); // gap
        moveSingleRow(index - 1, targetIndex); // row
      } else if (isFirst && isNextGap) {
        if (targetLast) {
          moveSingleRow(1, targetIndex); // gap
          moveSingleRow(0, targetIndex); // row
        } else {
          if (targetGap) {
            targetIndex++;
          }
          moveSingleRow(0, targetIndex); // row
          moveSingleRow(0, targetIndex); // gap
        }
      } else {
        moveSingleRow(index, targetIndex); // row
      }
    } else {
      if (isGap) {
        moveSingleRow(index, targetIndex); // gap
      } else if (isPrevGap && targetFirst && !targetGap) {
        moveSingleRow(index, 0); // row
        moveSingleRow(index, 1); // gap
      } else if (isPrevGap) {
        moveSingleRow(index, targetIndex); // row
        moveSingleRow(index, targetIndex); // gap
      } else {
        moveSingleRow(index, targetIndex); // row
      }
    }
    //
    writeDimensions();
  }

  /**
   * Moves single {@link FormRowInfo}.
   */
  private void moveSingleRow(final int index, final int targetIndex) throws Exception {
    FormRowInfo row = m_rows.remove(index);
    if (index < targetIndex) {
      // add row
      m_rows.add(targetIndex - 1, row);
      // change constraints
      visitComponents(new FormComponentVisitor() {
        public void visit(ComponentInfo bean, CellConstraintsSupport constraints) throws Exception {
          int y = constraints.y;
          int h = constraints.height;
          if (y < 1 + index) {
            // if component contains source and doesn't contain target, decrease size
            if (y + h - 1 >= 1 + index && y + h - 1 < 1 + targetIndex) {
              constraints.height = h - 1;
            }
          } else if (y == 1 + index) {
            constraints.y = targetIndex;
            constraints.height = 1;
          } else if (y > 1 + index && y < 1 + targetIndex) {
            constraints.y = y - 1;
            // if component contains target, increase size
            if (y + h - 1 >= 1 + targetIndex) {
              constraints.height = h + 1;
            }
          } else if (y >= 1 + targetIndex) {
          }
          constraints.write();
        }
      });
    } else {
      // add row
      m_rows.add(targetIndex, row);
      // change constraints
      visitComponents(new FormComponentVisitor() {
        public void visit(ComponentInfo bean, CellConstraintsSupport constraints) throws Exception {
          int y = constraints.y;
          int h = constraints.height;
          if (y < 1 + targetIndex) {
            // if component doesn't contains source and contains target, increase size
            if (y + h - 1 >= 1 + targetIndex && y + h - 1 < 1 + index) {
              constraints.height = h + 1;
            }
          } else if (y < 1 + index) {
            constraints.y = y + 1;
            // if component contains source, decrease size
            if (y + h - 1 >= 1 + index) {
              constraints.height = h - 1;
            }
          } else if (y == 1 + index) {
            constraints.y = 1 + targetIndex;
            constraints.height = 1;
          } else if (y > 1 + index) {
          }
          constraints.write();
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link ComponentInfo} in given cell.
   *
   * @param newComponent
   *          the new {@link ComponentInfo} to create.
   * @param column
   *          the column in {@link FormLayout} terms (1 based).
   * @param row
   *          the row in {@link FormLayout} terms (1 based).
   */
  public void command_CREATE(ComponentInfo newComponent,
      int column,
      boolean insertColumn,
      int row,
      boolean insertRow) throws Exception {
    Point cell = prepareCell(column, insertColumn, row, insertRow);
    // do add
    ComponentInfo nextComponent = getReference(column, row, null);
    add(newComponent, "\"" + cell.x + ", " + cell.y + "\"", nextComponent);
    {
      CellConstraintsSupport constraints = getConstraints(newComponent);
      constraints.x = cell.x;
      constraints.y = cell.y;
    }
    doAutomaticAlignment(newComponent);
  }

  /**
   * Moves existing {@link ComponentInfo} into new cell.
   */
  public void command_MOVE(ComponentInfo component,
      int column,
      boolean insertColumn,
      int row,
      boolean insertRow) throws Exception {
    Point cell = prepareCell(column, insertColumn, row, insertRow);
    // move in components
    {
      ComponentInfo nextComponent = getReference(column, row, component);
      move(component, null, nextComponent);
    }
    // move in grid
    {
      CellConstraintsSupport constraints = getConstraints(component);
      constraints.x = cell.x;
      constraints.y = cell.y;
      constraints.width = 1;
      constraints.height = 1;
      constraints.write();
    }
  }

  /**
   * Adds {@link ComponentInfo} from other parent into cell.
   */
  public void command_ADD(ComponentInfo component,
      int column,
      boolean insertColumn,
      int row,
      boolean insertRow) throws Exception {
    Point cell = prepareCell(column, insertColumn, row, insertRow);
    // move in components
    {
      ComponentInfo nextComponent = getReference(column, row, component);
      move(component, "\"" + cell.x + ", " + cell.y + "\"", nextComponent);
    }
    // move in grid
    {
      CellConstraintsSupport constraints = getConstraints(component);
      constraints.x = cell.x;
      constraints.y = cell.y;
      constraints.write();
    }
  }

  /**
   * @return the {@link ComponentInfo} that should be used as reference of adding into given cell.
   *
   * @param exclude
   *          the {@link ComponentInfo} that should not be checked, for example because we move it
   *          now.
   */
  private ComponentInfo getReference(int column, int row, ComponentInfo exclude) throws Exception {
    for (ComponentInfo component : getContainer().getChildrenComponents()) {
      if (component != exclude) {
        CellConstraintsSupport constraints = getConstraints(component);
        if (constraints.y > row || constraints.y == row && constraints.x >= column) {
          return component;
        }
      }
    }
    // no reference
    return null;
  }

  /**
   * Prepares cell with given column/row - inserts columns/rows if necessary.
   */
  private Point prepareCell(int column, boolean insertColumn, int row, boolean insertRow)
      throws Exception {
    boolean writeDimensions = false;
    if (insertColumn || insertRow) {
      // move existing components
      {
        moveComponentsForInsert(column, insertColumn, row, insertRow);
        moveComponentsForInsert(column, insertColumn, row, insertRow);
      }
      // insert gap and empty column/row
      if (insertColumn) {
        m_columns.add(column - 1, new FormColumnInfo(FormSpecs.RELATED_GAP_COLSPEC));
        m_columns.add(column - 1 + 1, new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC));
        column += 1;
      }
      if (insertRow) {
        m_rows.add(row - 1, new FormRowInfo(FormSpecs.RELATED_GAP_ROWSPEC));
        m_rows.add(row - 1 + 1, new FormRowInfo(FormSpecs.DEFAULT_ROWSPEC));
        row += 1;
      }
      // write dimensions
      writeDimensions = true;
    }
    // add columns
    {
      int addColumns = column - m_columns.size();
      if (addColumns > 0) {
        Assert.isTrue(
            addColumns % 2 == 0,
            MessageFormat.format(ModelMessages.FormLayoutInfo_evenDiffNumColumns, addColumns));
        for (int i = 0; i < addColumns / 2; i++) {
          m_columns.add(new FormColumnInfo(FormSpecs.RELATED_GAP_COLSPEC));
          m_columns.add(new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC));
        }
        writeDimensions = true;
      }
    }
    // add rows
    {
      int addRows = row - m_rows.size();
      if (addRows > 0) {
        Assert.isTrue(
            addRows % 2 == 0,
            MessageFormat.format(ModelMessages.FormLayoutInfo_evenDiffNumRows, addRows));
        for (int i = 0; i < addRows / 2; i++) {
          m_rows.add(new FormRowInfo(FormSpecs.RELATED_GAP_ROWSPEC));
          m_rows.add(new FormRowInfo(FormSpecs.DEFAULT_ROWSPEC));
        }
        writeDimensions = true;
      }
    }
    // write dimensions
    if (writeDimensions) {
      writeDimensions();
    }
    // return new cell
    return new Point(column, row);
  }

  /**
   * Moves/resizes components constraints for inserting single column/row.
   */
  private void moveComponentsForInsert(final int column,
      final boolean insertColumn,
      final int row,
      final boolean insertRow) throws Exception {
    visitComponents(new FormComponentVisitor() {
      public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
        if (insertColumn) {
          if (cell.x >= column) {
            cell.x++;
          } else if (cell.x + cell.width > column) {
            cell.width++;
          }
        }
        if (insertRow) {
          if (cell.y >= row) {
            cell.y++;
          } else if (cell.y + cell.height > row) {
            cell.height++;
          }
        }
        cell.write();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Visitor for {@link ComponentInfo} and their {@link CellConstraintsSupport}.
   */
  private interface FormComponentVisitor {
    void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception;
  }

  /**
   * Visits all {@link ComponentInfo} of this {@link ContainerInfo}.
   */
  private void visitComponents(FormComponentVisitor visitor) throws Exception {
    for (ComponentInfo component : getContainer().getChildrenComponents()) {
      CellConstraintsSupport cell = getConstraints(component);
      visitor.visit(component, cell);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Automatic alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Performs automatic alignment, such as grab/fill for {@link JTextField} or {@link JTable}, right
   * alignment for {@link JLabel}.
   */
  private void doAutomaticAlignment(ComponentInfo component) throws Exception {
    final IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
    GridAlignmentHelper.doAutomaticAlignment(component, new IAlignmentProcessor<ComponentInfo>() {
      public boolean grabEnabled() {
        return preferences.getBoolean(P_ENABLE_GRAB);
      }

      public boolean rightEnabled() {
        return preferences.getBoolean(P_ENABLE_RIGHT_ALIGNMENT);
      }

      public ComponentInfo getComponentAtLeft(ComponentInfo component) {
        CellConstraintsSupport constraints = getConstraints(component);
        int x = constraints.x - 1;
        if (x > 0 && m_columns.get(x - 1).isGap()) {
          x--;
        }
        return getComponentAt(x, constraints.y);
      }

      public ComponentInfo getComponentAtRight(ComponentInfo component) {
        CellConstraintsSupport constraints = getConstraints(component);
        int x = constraints.x + 1;
        if (x < m_columns.size() && m_columns.get(x - 1).isGap()) {
          x++;
        }
        return getComponentAt(x, constraints.y);
      }

      public void setGrabFill(ComponentInfo component, boolean horizontal) throws Exception {
        boolean canChangeDimensions = canChangeDimensions();
        CellConstraintsSupport constraints = getConstraints(component);
        if (horizontal) {
          if (canChangeDimensions) {
            getColumns().get(constraints.x - 1).setWeight(FormSpec.DEFAULT_GROW);
          }
          constraints.setAlignH(CellConstraints.FILL);
        } else {
          if (canChangeDimensions) {
            getRows().get(constraints.y - 1).setWeight(FormSpec.DEFAULT_GROW);
          }
          constraints.setAlignV(CellConstraints.FILL);
        }
        if (canChangeDimensions) {
          writeDimensions();
        }
        constraints.write();
      }

      public void setRightAlignment(ComponentInfo component) throws Exception {
        CellConstraintsSupport constraints = getConstraints(component);
        constraints.setAlignH(CellConstraints.RIGHT);
        constraints.write();
      }
    });
  }

  /**
   * @return the {@link ComponentInfo} with given top-left cell, may be <code>null</code>.
   */
  private ComponentInfo getComponentAt(int x, int y) {
    for (ComponentInfo component : getContainer().getChildrenComponents()) {
      CellConstraintsSupport constraints = getConstraints(component);
      if (constraints.x == x && constraints.y == y) {
        return component;
      }
    }
    // no such component
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final int DEFAULT_SIZE = 5;
  private int m_defaultColumnSize;
  private int m_gapColumnSize;
  private int m_defaultRowSize;
  private int m_gapRowSize;

  @Override
  public void refresh_dispose() throws Exception {
    m_gridInfo = null;
    super.refresh_dispose();
  }

  @Override
  protected void refresh_afterCreate2() throws Exception {
    super.refresh_afterCreate2();
    FormLayout layout = (FormLayout) getObject();
    Container container = getContainer().getContainer();
    // prepare origins
    int[] columnOrigins;
    int[] rowOrigins;
    {
      com.jgoodies.forms.layout.FormLayout.LayoutInfo layoutInfo = layout.getLayoutInfo(container);
      columnOrigins = layoutInfo.columnOrigins;
      rowOrigins = layoutInfo.rowOrigins;
    }
    // initialize default sizes in pixels
    {
      DefaultUnitConverter converter = DefaultUnitConverter.getInstance();
      //
      m_defaultColumnSize = converter.millimeterAsPixel(DEFAULT_SIZE, container);
      m_defaultRowSize = converter.millimeterAsPixel(DEFAULT_SIZE, container);
      m_gapColumnSize = new FormSizeInfo(FormSpecs.RELATED_GAP_COLSPEC.getSize(),
          true).getConstantSize().getAsPixels();
      m_gapRowSize = new FormSizeInfo(FormSpecs.RELATED_GAP_ROWSPEC.getSize(),
          false).getConstantSize().getAsPixels();
    }
    // set constant size for empty columns/rows
    {
      int columnCount = layout.getColumnCount();
      int rowCount = layout.getRowCount();
      // update columns
      for (int column = 1; column <= columnCount; column++) {
        ColumnSpec spec = layout.getColumnSpec(column);
        if (columnOrigins[column] - columnOrigins[column - 1] == 0) {
          spec = ColumnSpec.decode(DEFAULT_SIZE + "mm");
          layout.setColumnSpec(column, spec);
        }
      }
      // update rows
      for (int row = 1; row <= rowCount; row++) {
        RowSpec spec = layout.getRowSpec(row);
        if (rowOrigins[row] - rowOrigins[row - 1] == 0) {
          spec = RowSpec.decode(DEFAULT_SIZE + "mm");
          layout.setRowSpec(row, spec);
        }
      }
      // may be column/row specs were updated, force layout
      container.doLayout();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGridInfo support
  //
  ////////////////////////////////////////////////////////////////////////////
  private IGridInfo m_gridInfo;

  /**
   * @return the {@link IGridInfo} that describes this layout.
   */
  public IGridInfo getGridInfo() {
    if (m_gridInfo == null) {
      ExecutionUtils.runRethrow(new RunnableEx() {
        public void run() throws Exception {
          createGridInfo();
        }
      });
    }
    return m_gridInfo;
  }

  /**
   * Initializes {@link #m_gridInfo}.
   */
  private void createGridInfo() throws Exception {
    // prepare intervals
    final Interval[] columnIntervals;
    final Interval[] rowIntervals;
    {
      // prepare origins
      int[] columnOrigins;
      int[] rowOrigins;
      {
        FormLayout layout = (FormLayout) getObject();
        Container container = getContainer().getContainer();
        com.jgoodies.forms.layout.FormLayout.LayoutInfo layoutInfo =
            layout.getLayoutInfo(container);
        //
        columnOrigins = layoutInfo.columnOrigins;
        rowOrigins = layoutInfo.rowOrigins;
      }
      // convert origins into intervals
      columnIntervals = getIntervalsForOrigins(columnOrigins);
      rowIntervals = getIntervalsForOrigins(rowOrigins);
    }
    // prepare cells
    final Map<ComponentInfo, Rectangle> componentToCells = new HashMap<>();
    final Map<Point, ComponentInfo> occupiedCells = new HashMap<>();
    visitComponents(new FormComponentVisitor() {
      public void visit(ComponentInfo component, CellConstraintsSupport support) throws Exception {
        Rectangle cells =
            new Rectangle(support.x - 1, support.y - 1, support.width, support.height);
        // fill map: ComponentInfo -> cells Rectangle
        componentToCells.put(component, cells);
        // fill occupied cells map
        for (int x = cells.x; x < cells.right(); x++) {
          for (int y = cells.y; y < cells.bottom(); y++) {
            occupiedCells.put(new Point(x, y), component);
          }
        }
      }
    });
    // create IGridInfo instance
    m_gridInfo = new IGridInfo() {
      ////////////////////////////////////////////////////////////////////////////
      //
      // Dimensions
      //
      ////////////////////////////////////////////////////////////////////////////
      public int getColumnCount() {
        return columnIntervals.length;
      }

      public int getRowCount() {
        return rowIntervals.length;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Intervals
      //
      ////////////////////////////////////////////////////////////////////////////
      public Interval[] getColumnIntervals() {
        return columnIntervals;
      }

      public Interval[] getRowIntervals() {
        return rowIntervals;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Cells
      //
      ////////////////////////////////////////////////////////////////////////////
      public Rectangle getComponentCells(IAbstractComponentInfo component) {
        Assert.instanceOf(ComponentInfo.class, component);
        return componentToCells.get(component);
      }

      public Rectangle getCellsRectangle(Rectangle cells) {
        int x = columnIntervals[cells.x].begin;
        int y = rowIntervals[cells.y].begin;
        int w = columnIntervals[cells.right() - 1].end() - x;
        int h = rowIntervals[cells.bottom() - 1].end() - y;
        return new Rectangle(x, y, w + 1, h + 1);
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Feedback
      //
      ////////////////////////////////////////////////////////////////////////////
      public boolean isRTL() {
        return false;
      }

      public Insets getInsets() {
        return getContainer().getInsets();
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Virtual columns
      //
      ////////////////////////////////////////////////////////////////////////////
      public boolean hasVirtualColumns() {
        return true;
      }

      public int getVirtualColumnSize() {
        return m_defaultColumnSize;
      }

      public int getVirtualColumnGap() {
        return m_gapColumnSize;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Virtual rows
      //
      ////////////////////////////////////////////////////////////////////////////
      public boolean hasVirtualRows() {
        return true;
      }

      public int getVirtualRowSize() {
        return m_defaultRowSize;
      }

      public int getVirtualRowGap() {
        return m_gapRowSize;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Checks
      //
      ////////////////////////////////////////////////////////////////////////////
      public ComponentInfo getOccupied(int column, int row) {
        return occupiedCells.get(new Point(column, row));
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Manage general layout data.
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final BiMap<GeneralLayoutData.HorizontalAlignment, CellConstraints.Alignment> m_horizontalAlignmentMap =
      ImmutableBiMap.of(
          GeneralLayoutData.HorizontalAlignment.LEFT,
          CellConstraints.LEFT,
          GeneralLayoutData.HorizontalAlignment.CENTER,
          CellConstraints.CENTER,
          GeneralLayoutData.HorizontalAlignment.RIGHT,
          CellConstraints.RIGHT,
          GeneralLayoutData.HorizontalAlignment.FILL,
          CellConstraints.FILL,
          GeneralLayoutData.HorizontalAlignment.NONE,
          CellConstraints.DEFAULT);
  public static final BiMap<GeneralLayoutData.VerticalAlignment, CellConstraints.Alignment> m_verticalAlignmentMap =
      ImmutableBiMap.of(
          GeneralLayoutData.VerticalAlignment.TOP,
          CellConstraints.TOP,
          GeneralLayoutData.VerticalAlignment.CENTER,
          CellConstraints.CENTER,
          GeneralLayoutData.VerticalAlignment.BOTTOM,
          CellConstraints.BOTTOM,
          GeneralLayoutData.VerticalAlignment.FILL,
          CellConstraints.FILL,
          GeneralLayoutData.VerticalAlignment.NONE,
          CellConstraints.DEFAULT);

  @Override
  protected void storeLayoutData(ComponentInfo component) throws Exception {
    CellConstraintsSupport gridData = getConstraints(component);
    if (gridData != null) {
      GeneralLayoutData generalLayoutData = new GeneralLayoutData();
      int x = gridData.x - 1;
      int y = gridData.y - 1;
      int width = gridData.width;
      int height = gridData.height;
      int dx = 0;
      int dy = 0;
      // correct position
      for (int i = 0; i < x; i++) {
        if (m_columns.get(i).isGap()) {
          dx--;
        }
      }
      for (int j = 0; j < y; j++) {
        if (m_rows.get(j).isGap()) {
          dy--;
        }
      }
      // correct size
      for (int i = 1; i < width; i++) {
        if (m_columns.get(x + i).isGap()) {
          width--;
        }
      }
      for (int j = 1; j < height; j++) {
        if (m_rows.get(y + j).isGap()) {
          height--;
        }
      }
      // cell
      generalLayoutData.gridX = x + dx;
      generalLayoutData.gridY = y + dy;
      generalLayoutData.spanX = width;
      generalLayoutData.spanY = height;
      // grab
      generalLayoutData.horizontalGrab = m_columns.get(gridData.x - 1).hasGrow();
      generalLayoutData.verticalGrab = m_rows.get(gridData.y - 1).hasGrow();
      // alignments
      generalLayoutData.horizontalAlignment =
          GeneralLayoutData.getGeneralValue(m_horizontalAlignmentMap, gridData.alignH);
      generalLayoutData.verticalAlignment =
          GeneralLayoutData.getGeneralValue(m_verticalAlignmentMap, gridData.alignV);
      generalLayoutData.putToInfo(component);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array of {@link Interval}'s for given array of origins.
   */
  private static Interval[] getIntervalsForOrigins(int origins[]) {
    Assert.isTrue(origins.length != 0);
    Interval[] intervals = new Interval[origins.length - 1];
    for (int i = 0; i < intervals.length; i++) {
      int begin = origins[i];
      int end = origins[i + 1];
      intervals[i] = new Interval(begin, end - begin);
    }
    return intervals;
  }
}
