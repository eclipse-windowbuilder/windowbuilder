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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.PropertiesClipboardCommand;
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
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutAssistantSupport;
import org.eclipse.wb.internal.swing.model.layout.LayoutClipboardCommand;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.actions.SelectionActionsSupport;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import java.awt.Container;
import java.awt.GridBagLayout;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Model for abstraction of {@link GridBagLayout}.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage swing.model.layout
 */
public abstract class AbstractGridBagLayoutInfo extends LayoutInfo implements IPreferenceConstants {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractGridBagLayoutInfo(AstEditor editor,
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
    // add listeners
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (isManagedObject(javaInfo)) {
          ComponentInfo component = (ComponentInfo) javaInfo;
          getConstraints(component).addConstraintsProperties(properties);
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
        List<AbstractGridBagConstraintsInfo> constraints =
            Lists.transform(objects, new Function<ObjectInfo, AbstractGridBagConstraintsInfo>() {
              public AbstractGridBagConstraintsInfo apply(ObjectInfo from) {
                return getConstraints((ComponentInfo) from);
              }
            });
        return new GridBagConstraintsAssistantPage(parent, constraints);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GridBagConstraintsInfo access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link AbstractGridBagConstraintsInfo} for given {@link ComponentInfo}.
   */
  public abstract AbstractGridBagConstraintsInfo getConstraints(ComponentInfo component);

  public abstract Object getConstraintsObject(java.awt.Component component) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Visits all {@link ComponentInfo} of this {@link ContainerInfo}.
   */
  void visitComponents(IComponentVisitor visitor) throws Exception {
    visitComponents(visitor, IComponentPredicate.TRUE);
  }

  /**
   * Visits all {@link ComponentInfo} of this {@link ContainerInfo}.
   */
  void visitComponents(IComponentVisitor visitor, IComponentPredicate predicate) throws Exception {
    List<ComponentInfo> components = ImmutableList.copyOf(getComponents());
    for (ComponentInfo component : components) {
      AbstractGridBagConstraintsInfo constraints = getConstraints(component);
      if (predicate.apply(component, constraints)) {
        visitor.visit(component, constraints);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Set
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void onSet() throws Exception {
    super.onSet();
    GridBagLayoutConverter.convert(getContainer(), this);
  }

  @Override
  protected void removeComponentConstraints(ContainerInfo container, ComponentInfo component)
      throws Exception {
    getConstraints(component).delete();
    super.removeComponentConstraints(container, component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final Set<Integer> m_refreshFilledColumns = Sets.newTreeSet();
  protected final Set<Integer> m_refreshFilledRows = Sets.newTreeSet();

  @Override
  public void refresh_dispose() throws Exception {
    m_gridInfo = null;
    super.refresh_dispose();
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    // force layout() to calculate "real" location/size in GridBagConstraints."temp*" fields
    {
      ContainerInfo container = getContainer();
      // do layout, use some reasonable size
      {
        Container containerObject = container.getContainer();
        java.awt.Dimension containerSizeOriginal = containerObject.getSize();
        try {
          containerObject.setSize(450, 300);
          containerObject.doLayout();
        } finally {
          containerObject.setSize(containerSizeOriginal);
        }
      }
      // fetch fields
      for (ComponentInfo component : getComponents()) {
        getConstraints(component).getCurrentObjectFields(false);
      }
    }
    // initialize dimensions
    int[][] dimensions = getLayoutDimensions();
    int gridWidth = dimensions[0].length;
    int gridHeight = dimensions[1].length;
    if (!m_dimensionsInitialized) {
      m_dimensionsInitialized = true;
      // columns
      for (int i = 0; i < gridWidth; i++) {
        ColumnInfo column = new ColumnInfo(this, m_columnOperations);
        m_columns.add(column);
        column.initialize();
      }
      if (!m_columns.isEmpty() && m_columns.getLast().isFiller()) {
        m_columns.removeLast();
      }
      // rows
      for (int i = 0; i < gridHeight; i++) {
        RowInfo row = new RowInfo(this, m_rowOperations);
        m_rows.add(row);
        row.initialize();
      }
      if (!m_rows.isEmpty() && m_rows.getLast().isFiller()) {
        m_rows.removeLast();
      }
    }
    // cut columns/rows that does not exist in layout
    {
      boolean removedAnyDimension = false;
      removedAnyDimension |= m_columnOperations.cutToGrid(gridWidth);
      removedAnyDimension |= m_rowOperations.cutToGrid(gridHeight);
      if (removedAnyDimension) {
        getEditor().commitChanges();
      }
    }
    // set constant size for empty columns/rows
    {
      // prepare columns/rows with components
      beforeRefreshFilled();
      visitComponents(new IComponentVisitor() {
        public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
            throws Exception {
          if (constraints.width == 1) {
            m_refreshFilledColumns.add(constraints.x);
          }
          if (constraints.height == 1) {
            m_refreshFilledRows.add(constraints.y);
          }
        }
      });
      // we changed minimum size for columns/rows, force layout
      if (afterRefreshFilled()) {
        getContainer().getContainer().doLayout();
        ((AbstractComponentInfo) getRoot()).getTopBoundsSupport().apply();
      }
    }
  }

  /**
   * Do before fetching filled columns/rows numbers in {@link #refresh_afterCreate()}.
   */
  protected void beforeRefreshFilled() throws Exception {
    m_refreshFilledColumns.clear();
    m_refreshFilledRows.clear();
  }

  /**
   * Do after fetching filled columns/rows numbers in {@link #refresh_afterCreate()}.
   */
  protected boolean afterRefreshFilled() throws Exception {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimensions
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_dimensionsInitialized;
  private final LinkedList<ColumnInfo> m_columns = Lists.newLinkedList();
  private final LinkedList<RowInfo> m_rows = Lists.newLinkedList();
  private final DimensionOperations<ColumnInfo> m_columnOperations =
      new DimensionOperationsColumn(this);
  private final DimensionOperations<RowInfo> m_rowOperations = new DimensionOperationsRow(this);

  /**
   * @return Dimensions
   */
  protected abstract int[][] getLayoutDimensions() throws Exception;

  /**
   * @return Origin
   */
  protected abstract java.awt.Point getLayoutOrigin() throws Exception;

  /**
   * @return the {@link ColumnInfo}'s of this {@link AbstractGridBagLayoutInfo}.
   */
  public LinkedList<ColumnInfo> getColumns() {
    return m_columns;
  }

  /**
   * @return the {@link RowInfo}'s of this {@link AbstractGridBagLayoutInfo}.
   */
  public LinkedList<RowInfo> getRows() {
    return m_rows;
  }

  /**
   * @return the {@link DimensionOperations} for operations with {@link ColumnInfo}'s.
   */
  public DimensionOperations<ColumnInfo> getColumnOperations() {
    return m_columnOperations;
  }

  /**
   * @return the {@link DimensionOperations} for operations with {@link RowInfo}'s.
   */
  public DimensionOperations<RowInfo> getRowOperations() {
    return m_rowOperations;
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
   *          the column, 0 based.
   * @param row
   *          the row, 0 based.
   */
  public void command_CREATE(ComponentInfo newComponent,
      int column,
      boolean insertColumn,
      int row,
      boolean insertRow) throws Exception {
    prepareCell(column, insertColumn, row, insertRow);
    // do add
    ComponentInfo nextComponent = getReference(column, row, null);
    add(newComponent, null, nextComponent);
    // set location
    AbstractGridBagConstraintsInfo constraints = getConstraints(newComponent);
    constraints.setY(row);
    constraints.setX(column);
    // automatic alignment
    doAutomaticAlignment(newComponent);
    // ensure gaps
    ensureGapInsets();
  }

  /**
   * Creates new {@link ComponentInfo} into first empty cell at the end of grid.
   */
  public void command_CREATE_last(ComponentInfo newComponent) throws Exception {
    Point emptyCell = getFirstEmptyCellAtTheEnd();
    if (emptyCell == null) {
      IGridInfo gridInfo = getGridInfo();
      int rowCount = gridInfo.getRowCount();
      emptyCell = new Point(0, rowCount);
    }
    command_CREATE(newComponent, emptyCell.x, false, emptyCell.y, false);
  }

  /**
   * Moves given {@link ComponentInfo} in new cell.
   * 
   * @param component
   *          the new {@link ComponentInfo} to move.
   * @param column
   *          the column, 0 based.
   * @param row
   *          the row, 0 based.
   */
  public void command_MOVE(ComponentInfo component,
      int column,
      boolean insertColumn,
      int row,
      boolean insertRow) throws Exception {
    prepareCell(column, insertColumn, row, insertRow);
    // do move
    ComponentInfo nextComponent = getReference(column, row, component);
    move(component, null, nextComponent);
    // set location
    AbstractGridBagConstraintsInfo constraints = getConstraints(component);
    constraints.setY(row);
    constraints.setX(column);
    // ensure gaps
    ensureGapInsets();
  }

  /**
   * Sets the location/size of {@link ComponentInfo} inside of existing cells, without
   * inserting/appending new dimensions.
   */
  public void command_setCells(ComponentInfo component, Rectangle cells) throws Exception {
    AbstractGridBagConstraintsInfo constraints = getConstraints(component);
    // may be move
    if (constraints.x != cells.x || constraints.y != cells.y) {
      ComponentInfo nextComponent = getReference(cells.x, cells.y, component);
      move(component, null, nextComponent);
    }
    // set location/size
    constraints.setX(cells.x);
    constraints.setY(cells.y);
    constraints.setWidth(cells.width);
    constraints.setHeight(cells.height);
    // ensure gaps
    ensureGapInsets();
  }

  private Point getFirstEmptyCellAtTheEnd() {
    IGridInfo gridInfo = getGridInfo();
    int columnCount = gridInfo.getColumnCount();
    int rowCount = gridInfo.getRowCount();
    int emptyRow = -1;
    int emptyColumn = -1;
    for (int row = rowCount - 1; row >= 0; row--) {
      for (int column = columnCount - 1; column >= 0; column--) {
        if (gridInfo.getOccupied(column, row) != null) {
          break;
        }
        emptyRow = row;
        emptyColumn = column;
      }
    }
    if (emptyColumn == -1) {
      return null;
    }
    return new Point(emptyColumn, emptyRow);
  }

  /**
   * @return the {@link ComponentInfo} that should be used as reference of adding into given cell.
   * 
   * @param exclude
   *          the {@link ComponentInfo} that should not be checked, for example because we move it
   *          now.
   */
  private ComponentInfo getReference(int column, int row, ComponentInfo exclude) throws Exception {
    for (ComponentInfo component : getComponents()) {
      if (component != exclude) {
        AbstractGridBagConstraintsInfo constraints = getConstraints(component);
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
  void prepareCell(int column, boolean insertColumn, int row, boolean insertRow) throws Exception {
    // as first step: materialize locations
    visitComponents(new IComponentVisitor() {
      public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
          throws Exception {
        constraints.materializeLocation();
      }
    });
    // prepare dimensions
    m_columnOperations.prepare(column, insertColumn);
    m_rowOperations.prepare(row, insertRow);
  }

  /**
   * Ensures that in the middle of the grid have gap insets on the right/bottom.
   */
  void ensureGapInsets() throws Exception {
    // may be changing insets disabled
    {
      IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
      if (!preferences.getBoolean(IPreferenceConstants.P_CHANGE_INSETS_FOR_GAPS)) {
        return;
      }
    }
    // perform changes
    visitComponents(new IComponentVisitor() {
      public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
          throws Exception {
        if (constraints.x + constraints.width < m_columns.size()) {
          ensureInsets(constraints, "right", true);
        } else {
          removeInsets(constraints, "right", true);
        }
        if (constraints.y + constraints.height < m_rows.size()) {
          ensureInsets(constraints, "bottom", false);
        } else {
          removeInsets(constraints, "bottom", false);
        }
      }

      /**
       * Ensures that given {@link AbstractGridBagConstraintsInfo} has at least <code>GAP</code> as
       * insets on given side.
       */
      private void ensureInsets(AbstractGridBagConstraintsInfo constraints,
          String side,
          boolean horizontal) throws Exception {
        int value = constraints.getInsets(side);
        if (value < getGap(horizontal)) {
          constraints.setInsets(side, getGap(horizontal));
        }
      }

      /**
       * Removes <code>GAP</code> insets of given side of {@link AbstractGridBagConstraintsInfo}.
       */
      private void removeInsets(AbstractGridBagConstraintsInfo constraints,
          String side,
          boolean horizontal) throws Exception {
        int value = constraints.getInsets(side);
        if (value == getGap(horizontal)) {
          constraints.setInsets(side, 0);
        }
      }
    });
  }

  /**
   * @return the gap between dimensions.
   */
  private static int getGap(boolean horizontal) {
    IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
    String key = horizontal ? IPreferenceConstants.P_GAP_COLUMN : IPreferenceConstants.P_GAP_ROW;
    return preferences.getInt(key);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Automatic alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_autoAlignmentEnabled = true;

  /**
   * Performs automatic alignment, such as grab/fill for {@link JTextField} or {@link JTable}, right
   * alignment for {@link JLabel}.
   */
  private void doAutomaticAlignment(ComponentInfo component) throws Exception {
    if (!m_autoAlignmentEnabled) {
      return;
    }
    GridAlignmentHelper.doAutomaticAlignment(component, new IAlignmentProcessor<ComponentInfo>() {
      public boolean grabEnabled() {
        return getDescription().getToolkit().getPreferences().getBoolean(P_ENABLE_GRAB);
      }

      public boolean rightEnabled() {
        return getDescription().getToolkit().getPreferences().getBoolean(P_ENABLE_RIGHT_ALIGNMENT);
      }

      public ComponentInfo getComponentAtLeft(ComponentInfo component) {
        AbstractGridBagConstraintsInfo constraints = getConstraints(component);
        return getComponentAt(constraints.x - 1, constraints.y);
      }

      public ComponentInfo getComponentAtRight(ComponentInfo component) {
        AbstractGridBagConstraintsInfo constraints = getConstraints(component);
        return getComponentAt(constraints.x + 1, constraints.y);
      }

      public void setGrabFill(ComponentInfo component, boolean horizontal) throws Exception {
        AbstractGridBagConstraintsInfo constraints = getConstraints(component);
        if (horizontal) {
          getColumns().get(constraints.x).setWeight(1.0);
          constraints.setHorizontalAlignment(ColumnInfo.Alignment.FILL);
        } else {
          getRows().get(constraints.y).setWeight(1.0);
          constraints.setVerticalAlignment(RowInfo.Alignment.FILL);
        }
      }

      public void setRightAlignment(ComponentInfo component) throws Exception {
        AbstractGridBagConstraintsInfo constraints = getConstraints(component);
        constraints.setHorizontalAlignment(ColumnInfo.Alignment.RIGHT);
      }
    });
  }

  /**
   * @return the {@link ComponentInfo} with given top-left cell, may be <code>null</code>.
   */
  private ComponentInfo getComponentAt(int x, int y) {
    for (ComponentInfo component : getComponents()) {
      AbstractGridBagConstraintsInfo constraints = getConstraints(component);
      if (constraints.x == x && constraints.y == y) {
        return component;
      }
    }
    // no such component
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addContainerCommands(List<ClipboardCommand> commands)
      throws Exception {
    commands.add(getColumnOperations().getClipboardCommand());
    commands.add(getRowOperations().getClipboardCommand());
    super.clipboardCopy_addContainerCommands(commands);
  }

  @Override
  protected void clipboardCopy_addComponentCommands(ComponentInfo component,
      List<ClipboardCommand> commands) throws Exception {
    // prepare information
    final Rectangle cells = getGridInfo().getComponentCells(component);
    final PropertiesClipboardCommand propertiesCommand;
    {
      AbstractGridBagConstraintsInfo constraints = getConstraints(component);
      propertiesCommand = new PropertiesClipboardCommand(constraints);
    }
    // add command
    commands.add(new LayoutClipboardCommand<AbstractGridBagLayoutInfo>(component) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(AbstractGridBagLayoutInfo layout, ComponentInfo component)
          throws Exception {
        layout.m_autoAlignmentEnabled = false;
        try {
          layout.command_CREATE(component, cells.x, false, cells.y, false);
          layout.command_setCells(component, cells);
          {
            AbstractGridBagConstraintsInfo constraints = layout.getConstraints(component);
            propertiesCommand.execute(constraints);
          }
        } finally {
          layout.m_autoAlignmentEnabled = true;
        }
      }
    });
    super.clipboardCopy_addComponentCommands(component, commands);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Image} for {@link AbstractGridBagLayoutInfo}.
   */
  public static Image getImage(String name) {
    return Activator.getImage("info/layout/gridBagLayout/" + name);
  }

  /**
   * @return the {@link ImageDescriptor} for {@link AbstractGridBagLayoutInfo}.
   */
  public static ImageDescriptor getImageDescriptor(String name) {
    return Activator.getImageDescriptor("info/layout/gridBagLayout/" + name);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGridInfo support
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static final int EMPTY_DIM = 25;
  protected static final int EMPTY_GAP = 5;
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
    // prepare cells
    final Map<ComponentInfo, Rectangle> componentToCells = Maps.newHashMap();
    final Map<Point, ComponentInfo> occupiedCells = Maps.newHashMap();
    visitComponents(new IComponentVisitor() {
      public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
          throws Exception {
        Rectangle cells =
            new Rectangle(constraints.x, constraints.y, constraints.width, constraints.height);
        // fill map: ComponentInfo -> cells Rectangle
        componentToCells.put(component, cells);
        // fill map: occupied cells
        for (int x = cells.x; x < cells.right(); x++) {
          for (int y = cells.y; y < cells.bottom(); y++) {
            occupiedCells.put(new Point(x, y), component);
          }
        }
      }
    });
    // prepare intervals
    Interval[] columnIntervals;
    Interval[] rowIntervals;
    {
      int[][] layoutDimensions = getLayoutDimensions();
      java.awt.Point layoutOrigin = getLayoutOrigin();
      // convert origins into intervals
      columnIntervals = getIntervalsForLengths(layoutOrigin.x, layoutDimensions[0]);
      rowIntervals = getIntervalsForLengths(layoutOrigin.y, layoutDimensions[1]);
      // update intervals with insets (gaps)
      {
        final int[] leftGaps = new int[columnIntervals.length];
        final int[] rightGaps = new int[columnIntervals.length];
        final int[] topGaps = new int[rowIntervals.length];
        final int[] bottomGaps = new int[rowIntervals.length];
        // deduce gaps from insets
        visitComponents(new IComponentVisitor() {
          public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
              throws Exception {
            updateGaps(leftGaps, constraints.x, constraints.insets.left);
            updateGaps(rightGaps, constraints.x + constraints.width - 1, constraints.insets.right);
            updateGaps(topGaps, constraints.y, constraints.insets.top);
            updateGaps(
                bottomGaps,
                constraints.y + constraints.height - 1,
                constraints.insets.bottom);
          }

          private void updateGaps(int[] gaps, int index, int newGap) {
            newGap = Math.max(newGap, 0);
            if (gaps[index] == 0) {
              gaps[index] = newGap;
            } else {
              gaps[index] = Math.min(gaps[index], newGap);
            }
          }
        });
        // force gap after empty column/row
        for (int column = 0; column < rightGaps.length; column++) {
          if (!m_refreshFilledColumns.contains(column)) {
            rightGaps[column] = EMPTY_GAP;
          }
        }
        for (int row = 0; row < bottomGaps.length; row++) {
          if (!m_refreshFilledRows.contains(row)) {
            bottomGaps[row] = EMPTY_GAP;
          }
        }
        // update intervals
        updateIntervals(columnIntervals, leftGaps, rightGaps);
        updateIntervals(rowIntervals, topGaps, bottomGaps);
      }
      // if last interval has weight 1.0E-4, it should be considered as synthetic and removed
      columnIntervals = checkColumnIntervals(columnIntervals);
      rowIntervals = checkRowIntervals(rowIntervals);
    }
    // create IGridInfo instance
    final Interval[] m_columnIntervals = columnIntervals;
    final Interval[] m_rowIntervals = rowIntervals;
    m_gridInfo = new IGridInfo() {
      ////////////////////////////////////////////////////////////////////////////
      //
      // Dimensions
      //
      ////////////////////////////////////////////////////////////////////////////
      public int getColumnCount() {
        return m_columnIntervals.length;
      }

      public int getRowCount() {
        return m_rowIntervals.length;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Intervals
      //
      ////////////////////////////////////////////////////////////////////////////
      public Interval[] getColumnIntervals() {
        return m_columnIntervals;
      }

      public Interval[] getRowIntervals() {
        return m_rowIntervals;
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
        int x = m_columnIntervals[cells.x].begin;
        int y = m_rowIntervals[cells.y].begin;
        int w = m_columnIntervals[cells.right() - 1].end() - x;
        int h = m_rowIntervals[cells.bottom() - 1].end() - y;
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
        return EMPTY_DIM;
      }

      public int getVirtualColumnGap() {
        return EMPTY_GAP;
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
        return EMPTY_DIM;
      }

      public int getVirtualRowGap() {
        return EMPTY_GAP;
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

  protected Interval[] checkColumnIntervals(Interval[] columnIntervals) {
    return columnIntervals;
  }

  protected Interval[] checkRowIntervals(Interval[] rowIntervals) {
    return rowIntervals;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Manage general layout data. 
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final BiMap<GeneralLayoutData.HorizontalAlignment, ColumnInfo.Alignment> m_horizontalAlignmentMap =
      ImmutableBiMap.of(
          GeneralLayoutData.HorizontalAlignment.LEFT,
          ColumnInfo.Alignment.LEFT,
          GeneralLayoutData.HorizontalAlignment.CENTER,
          ColumnInfo.Alignment.CENTER,
          GeneralLayoutData.HorizontalAlignment.RIGHT,
          ColumnInfo.Alignment.RIGHT,
          GeneralLayoutData.HorizontalAlignment.FILL,
          ColumnInfo.Alignment.FILL,
          GeneralLayoutData.HorizontalAlignment.NONE,
          ColumnInfo.Alignment.UNKNOWN);
  public static final BiMap<GeneralLayoutData.VerticalAlignment, RowInfo.Alignment> m_verticalAlignmentMap =
      ImmutableBiMap.of(
          GeneralLayoutData.VerticalAlignment.TOP,
          RowInfo.Alignment.TOP,
          GeneralLayoutData.VerticalAlignment.CENTER,
          RowInfo.Alignment.CENTER,
          GeneralLayoutData.VerticalAlignment.BOTTOM,
          RowInfo.Alignment.BOTTOM,
          GeneralLayoutData.VerticalAlignment.FILL,
          RowInfo.Alignment.FILL,
          GeneralLayoutData.VerticalAlignment.NONE,
          RowInfo.Alignment.UNKNOWN);

  @Override
  protected void storeLayoutData(ComponentInfo component) throws Exception {
    AbstractGridBagConstraintsInfo gridData = getConstraints(component);
    if (gridData != null) {
      GeneralLayoutData generalLayoutData = new GeneralLayoutData();
      generalLayoutData.gridX = gridData.x;
      generalLayoutData.gridY = gridData.y;
      generalLayoutData.spanX = gridData.width;
      generalLayoutData.spanY = gridData.height;
      generalLayoutData.horizontalGrab = null;
      generalLayoutData.verticalGrab = null;
      // alignments
      generalLayoutData.horizontalAlignment =
          GeneralLayoutData.getGeneralValue(
              m_horizontalAlignmentMap,
              gridData.getHorizontalAlignment());
      generalLayoutData.verticalAlignment =
          GeneralLayoutData.getGeneralValue(m_verticalAlignmentMap, gridData.getVerticalAlignment());
      generalLayoutData.putToInfo(component);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array of {@link Interval}'s for given array of column/row lengths.
   */
  private static Interval[] getIntervalsForLengths(int begin, int lengths[]) {
    Interval[] intervals = new Interval[lengths.length];
    for (int i = 0; i < lengths.length; i++) {
      int length = lengths[i];
      intervals[i] = new Interval(begin, length);
      begin += length;
    }
    return intervals;
  }

  /**
   * Updates given {@link Interval}'s using begin/end gaps.
   */
  private static void updateIntervals(Interval[] intervals, int[] beginGaps, int[] endGaps) {
    for (int i = 0; i < intervals.length; i++) {
      Interval interval = intervals[i];
      interval.growLeading(beginGaps[i]);
      interval.growTrailing(-endGaps[i]);
    }
  }
}
