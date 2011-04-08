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
package org.eclipse.wb.internal.swing.model.property.editor.models.table;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.eclipse.wb.core.controls.CSpinner;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import swingintegration.example.EmbeddedSwingComposite2;

/**
 * Dialog for editing {@link TableModelDescription}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class TableModelDialog extends ResizableDialog {
  private static final BiMap<String, Class<?>> COLUMN_TYPES = HashBiMap.create();
  static {
    COLUMN_TYPES.put("Object", Object.class);
    COLUMN_TYPES.put("String", String.class);
    COLUMN_TYPES.put("Boolean", Boolean.class);
    COLUMN_TYPES.put("Integer", Integer.class);
    COLUMN_TYPES.put("Byte", Byte.class);
    COLUMN_TYPES.put("Short", Short.class);
    COLUMN_TYPES.put("Long", Long.class);
    COLUMN_TYPES.put("Float", Float.class);
    COLUMN_TYPES.put("Double", Double.class);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_title;
  private final TableModelDescription m_model;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableModelDialog(Shell parentShell, String title, TableModelDescription model) {
    super(parentShell, DesignerPlugin.getDefault());
    m_title = title;
    m_model = model;
    setShellStyle(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private Composite m_tableComposite;
  private TitledComposite m_columnsComposite;
  private TitledComposite m_rowsComposite;
  private TitledComposite m_columnPropertiesComposite;
  private EmbeddedSwingComposite2 m_swingComposite;
  private JTable m_table;
  // columns
  private CSpinner m_columnCountSpinner;
  private Button m_insertColumnButton;
  private Button m_deleteColumnButton;
  private Button m_moveColumnLeftButton;
  private Button m_moveColumnRightButton;
  // rows
  private CSpinner m_rowCountSpinner;
  private Button m_insertRowButton;
  private Button m_deleteRowButton;
  private Button m_moveRowUpButton;
  private Button m_moveRowDownButton;
  // column properties
  private Text m_columnPropertyNo;
  private Text m_columnPropertyTitle;
  private Combo m_columnPropertyType;
  private Button m_columnPropertyEditable;
  private Button m_columnPropertyResizable;
  private Label m_columnPropertyValuesLabel;
  private Text m_columnPropertyValues;
  private Button m_columnPropertyValuesEdit;
  private CSpinner m_columnPropertyPrefWidth;
  private CSpinner m_columnPropertyMinWidth;
  private CSpinner m_columnPropertyMaxWidth;

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    GridLayoutFactory.create(area).columns(2);
    // create GUI elements
    createJTableComposite(area);
    createColumnsComposite(area);
    createRowsComposite(area);
    createColumnPropertiesComposite(area);
    // set constraints
    GridDataFactory.create(m_tableComposite).spanV(2).hintC(70, 20).grab().fill();
    GridDataFactory.create(m_columnsComposite).fill();
    GridDataFactory.create(m_rowsComposite).fill();
    GridDataFactory.create(m_columnPropertiesComposite).grabH().fill();
    //
    m_swingComposite.populate();
    return area;
  }

  private void createJTableComposite(Composite parent) {
    m_tableComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(m_tableComposite).noMargins();
    new Label(m_tableComposite, SWT.NONE).setText(ModelMessages.TableModelDialog_itemsLabel);
    {
      m_swingComposite = new EmbeddedSwingComposite2(m_tableComposite, SWT.NONE) {
        @Override
        protected JComponent createSwingComponent() {
          TableModel model = m_model.createTableModel();
          m_table = new JTable(model);
          m_table.setCellSelectionEnabled(true);
          m_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
          updateTableModel();
          trackTableSelection();
          return new JScrollPane(m_table);
        }
      };
      GridDataFactory.create(m_swingComposite).grab().fill();
    }
    {
      Label label = new Label(m_tableComposite, SWT.WRAP);
      GridDataFactory.create(label).grabH().fill();
      label.setText(ModelMessages.TableModelDialog_hint);
    }
  }

  private void createColumnsComposite(Composite parent) {
    m_columnsComposite =
        new TitledComposite(parent, SWT.NONE, ModelMessages.TableModelDialog_columnsTitle);
    // content container
    Composite container = m_columnsComposite.getContent();
    GridLayoutFactory.create(container).columns(2).noMargins();
    // Count
    {
      new Label(container, SWT.NONE).setText(ModelMessages.TableModelDialog_columnsCount);
      m_columnCountSpinner = new CSpinner(container, SWT.BORDER);
      GridDataFactory.create(m_columnCountSpinner).grabH().fillH().hintHC(5);
      addJTableOperationSelectionListener(m_columnCountSpinner, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.setColumnCount(m_columnCountSpinner.getSelection());
        }
      });
    }
    // Insert
    {
      m_insertColumnButton = new Button(container, SWT.NONE);
      GridDataFactory.create(m_insertColumnButton).spanH(2).hintHC(13).alignHC();
      m_insertColumnButton.setText(ModelMessages.TableModelDialog_columnInsertButton);
      addJTableOperationSelectionListener(m_insertColumnButton, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.insertColumn(getInsertColumnIndex(column));
        }
      }, new TableOperationRunnable() {
        public void run(int row, int column) {
          setTableSelection(row, getInsertColumnIndex(column));
        }
      });
    }
    // Delete
    {
      m_deleteColumnButton = new Button(container, SWT.NONE);
      GridDataFactory.create(m_deleteColumnButton).spanH(2).hintHC(13).alignHC();
      m_deleteColumnButton.setText(ModelMessages.TableModelDialog_columnDeleteButton);
      addJTableOperationSelectionListener(m_deleteColumnButton, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.removeColumn(column);
        }
      }, new TableOperationRunnable() {
        public void run(int row, int column) {
          if (column < m_table.getColumnCount()) {
            setTableSelection(row, column);
          } else {
            setTableSelection(row, column - 1);
          }
        }
      });
    }
    // Move Left
    {
      m_moveColumnLeftButton = new Button(container, SWT.NONE);
      GridDataFactory.create(m_moveColumnLeftButton).spanH(2).hintHC(13).alignHC();
      m_moveColumnLeftButton.setText(ModelMessages.TableModelDialog_columnMoveLeftButton);
      addJTableOperationSelectionListener(m_moveColumnLeftButton, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.moveColumn(column, column - 1);
        }
      }, new TableOperationRunnable() {
        public void run(int row, int column) {
          setTableSelection(row, column - 1);
        }
      });
    }
    // Move Right
    {
      m_moveColumnRightButton = new Button(container, SWT.NONE);
      GridDataFactory.create(m_moveColumnRightButton).spanH(2).hintHC(13).alignHC();
      m_moveColumnRightButton.setText(ModelMessages.TableModelDialog_columnMoveRightButton);
      addJTableOperationSelectionListener(m_moveColumnRightButton, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.moveColumn(column, column + 1);
        }
      }, new TableOperationRunnable() {
        public void run(int row, int column) {
          setTableSelection(row, column + 1);
        }
      });
    }
  }

  private void createRowsComposite(Composite parent) {
    m_rowsComposite =
        new TitledComposite(parent, SWT.NONE, ModelMessages.TableModelDialog_rowsTitle);
    // content container
    Composite container = m_rowsComposite.getContent();
    GridLayoutFactory.create(container).columns(2).noMargins();
    // Count
    {
      new Label(container, SWT.NONE).setText(ModelMessages.TableModelDialog_rowsCount);
      m_rowCountSpinner = new CSpinner(container, SWT.BORDER);
      GridDataFactory.create(m_rowCountSpinner).grabH().fillH().hintHC(5);
      addJTableOperationSelectionListener(m_rowCountSpinner, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.setRowCount(m_rowCountSpinner.getSelection());
        }
      });
    }
    // Insert
    {
      m_insertRowButton = new Button(container, SWT.NONE);
      GridDataFactory.create(m_insertRowButton).spanH(2).hintHC(13).alignHC();
      m_insertRowButton.setText(ModelMessages.TableModelDialog_rowInsertButton);
      addJTableOperationSelectionListener(m_insertRowButton, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.insertRow(getInsertRowIndex(row));
        }
      }, new TableOperationRunnable() {
        public void run(int row, int column) {
          setTableSelection(getInsertRowIndex(row), column);
        }
      });
    }
    // Delete
    {
      m_deleteRowButton = new Button(container, SWT.NONE);
      GridDataFactory.create(m_deleteRowButton).spanH(2).hintHC(13).alignHC();
      m_deleteRowButton.setText(ModelMessages.TableModelDialog_rowDeleteButton);
      addJTableOperationSelectionListener(m_deleteRowButton, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.removeRow(row);
        }
      }, new TableOperationRunnable() {
        public void run(int row, int column) {
          if (row < m_table.getRowCount()) {
            setTableSelection(row, column);
          } else {
            setTableSelection(row - 1, column);
          }
        }
      });
    }
    // Move Up
    {
      m_moveRowUpButton = new Button(container, SWT.NONE);
      GridDataFactory.create(m_moveRowUpButton).spanH(2).hintHC(13).alignHC();
      m_moveRowUpButton.setText(ModelMessages.TableModelDialog_rowMoveUpButton);
      addJTableOperationSelectionListener(m_moveRowUpButton, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.moveRow(row, row - 1);
        }
      }, new TableOperationRunnable() {
        public void run(int row, int column) {
          setTableSelection(row - 1, column);
        }
      });
    }
    // Move Down
    {
      m_moveRowDownButton = new Button(container, SWT.NONE);
      GridDataFactory.create(m_moveRowDownButton).spanH(2).hintHC(13).alignHC();
      m_moveRowDownButton.setText(ModelMessages.TableModelDialog_rowMoveDownButton);
      addJTableOperationSelectionListener(m_moveRowDownButton, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.moveRow(row, row + 1);
        }
      }, new TableOperationRunnable() {
        public void run(int row, int column) {
          setTableSelection(row + 1, column);
        }
      });
    }
  }

  private void createColumnPropertiesComposite(Composite parent) {
    m_columnPropertiesComposite =
        new TitledComposite(parent, SWT.NONE, ModelMessages.TableModelDialog_columnProperties);
    // content container
    Composite container = m_columnPropertiesComposite.getContent();
    GridLayoutFactory.create(container).columns(8).noMargins();
    // header
    {
      Label label = new Label(container, SWT.NONE);
      GridDataFactory.create(label).spanH(8);
      label.setText(ModelMessages.TableModelDialog_columnPropertiesHint);
    }
    // first row
    {
      new Label(container, SWT.NONE).setText(ModelMessages.TableModelDialog_columnPropertiesNo);
      m_columnPropertyNo = new Text(container, SWT.BORDER | SWT.READ_ONLY);
      GridDataFactory.create(m_columnPropertyNo).hintHC(6).fillH();
    }
    {
      new Label(container, SWT.NONE).setText(ModelMessages.TableModelDialog_columnPropertiesTitle);
      m_columnPropertyTitle = new Text(container, SWT.BORDER);
      GridDataFactory.create(m_columnPropertyTitle).spanH(2).hintHC(40).fillH();
      addJTableOperationListener(m_columnPropertyTitle, SWT.Modify, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.getColumn(column).m_name = m_columnPropertyTitle.getText();
        }
      });
    }
    {
      new Label(container, SWT.NONE).setText(ModelMessages.TableModelDialog_columnPropertiesPrefWidth);
      m_columnPropertyPrefWidth = new CSpinner(container, SWT.BORDER);
      GridDataFactory.create(m_columnPropertyPrefWidth).hintHC(15);
      m_columnPropertyPrefWidth.setRange(0, Integer.MAX_VALUE);
      addJTableOperationSelectionListener(m_columnPropertyPrefWidth, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.getColumn(column).m_preferredWidth = m_columnPropertyPrefWidth.getSelection();
        }
      });
    }
    {
      ToolItem clearButton = createClearSpinnerButton(container);
      addJTableOperationSelectionListener(clearButton, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.getColumn(column).m_preferredWidth =
              TableColumnDescription.DEFAULT_PREFERRED_WIDTH;
        }
      });
    }
    // second row
    {
      new Label(container, SWT.NONE).setText(ModelMessages.TableModelDialog_columnPropertiesType);
      m_columnPropertyType = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
      GridDataFactory.create(m_columnPropertyType).hintHC(10).fillH();
      // items
      for (String typeTitle : COLUMN_TYPES.keySet()) {
        m_columnPropertyType.add(typeTitle);
      }
      m_columnPropertyType.setVisibleItemCount(COLUMN_TYPES.size());
      // listener
      addJTableOperationSelectionListener(m_columnPropertyType, new TableOperationRunnable() {
        public void run(int row, int column) {
          String typeTitle = m_columnPropertyType.getText();
          Class<?> type = COLUMN_TYPES.get(typeTitle);
          m_model.setColumnType(column, type);
        }
      });
    }
    {
      m_columnPropertyValuesLabel = new Label(container, SWT.NONE);
      m_columnPropertyValuesLabel.setText(ModelMessages.TableModelDialog_columnPropertiesValues);
      m_columnPropertyValuesLabel.setEnabled(false);
      m_columnPropertyValuesLabel.setData(UiUtils.KEY_IGNORE_HIERARCHY_ENABLED, true);
      //
      m_columnPropertyValues = new Text(container, SWT.BORDER);
      GridDataFactory.create(m_columnPropertyValues).hintHC(30);
      m_columnPropertyValues.setEnabled(false);
      m_columnPropertyValues.setData(UiUtils.KEY_IGNORE_HIERARCHY_ENABLED, true);
    }
    {
      m_columnPropertyValuesEdit = new Button(container, SWT.NONE);
      GridDataFactory.create(m_columnPropertyValuesEdit).hintHC(10);
      m_columnPropertyValuesEdit.setText(ModelMessages.TableModelDialog_columnPropertiesValuesEdit);
      m_columnPropertyValuesEdit.setEnabled(false);
      m_columnPropertyValuesEdit.setData(UiUtils.KEY_IGNORE_HIERARCHY_ENABLED, true);
    }
    {
      new Label(container, SWT.NONE).setText(ModelMessages.TableModelDialog_columnPropertiesMinWidth);
      m_columnPropertyMinWidth = new CSpinner(container, SWT.BORDER);
      GridDataFactory.create(m_columnPropertyMinWidth).hintHC(15);
      m_columnPropertyMinWidth.setRange(0, Integer.MAX_VALUE);
      addJTableOperationSelectionListener(m_columnPropertyMinWidth, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.getColumn(column).m_minWidth = m_columnPropertyMinWidth.getSelection();
        }
      });
    }
    {
      ToolItem clearButton = createClearSpinnerButton(container);
      addJTableOperationSelectionListener(clearButton, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.getColumn(column).m_minWidth = TableColumnDescription.DEFAULT_MIN_WIDTH;
        }
      });
    }
    // third row
    {
      Composite booleanContainer = new Composite(container, SWT.NONE);
      GridDataFactory.create(booleanContainer).spanH(5).fillH();
      GridLayoutFactory.create(booleanContainer).columns(2).noMargins();
      {
        m_columnPropertyEditable = new Button(booleanContainer, SWT.CHECK);
        m_columnPropertyEditable.setText(ModelMessages.TableModelDialog_columnPropertiesEditable);
        addJTableOperationSelectionListener(m_columnPropertyEditable, new TableOperationRunnable() {
          public void run(int row, int column) {
            m_model.getColumn(column).m_editable = m_columnPropertyEditable.getSelection();
          }
        });
      }
      {
        m_columnPropertyResizable = new Button(booleanContainer, SWT.CHECK);
        m_columnPropertyResizable.setText(ModelMessages.TableModelDialog_columnPropertiesResizable);
        addJTableOperationSelectionListener(
            m_columnPropertyResizable,
            new TableOperationRunnable() {
              public void run(int row, int column) {
                m_model.getColumn(column).m_resizable = m_columnPropertyResizable.getSelection();
              }
            });
      }
    }
    {
      new Label(container, SWT.NONE).setText(ModelMessages.TableModelDialog_columnPropertiesMaxWidth);
      m_columnPropertyMaxWidth = new CSpinner(container, SWT.BORDER);
      GridDataFactory.create(m_columnPropertyMaxWidth).hintHC(15);
      m_columnPropertyMaxWidth.setRange(0, Integer.MAX_VALUE);
      addJTableOperationSelectionListener(m_columnPropertyMaxWidth, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.getColumn(column).m_maxWidth = m_columnPropertyMaxWidth.getSelection();
        }
      });
    }
    {
      ToolItem clearButton = createClearSpinnerButton(container);
      addJTableOperationSelectionListener(clearButton, new TableOperationRunnable() {
        public void run(int row, int column) {
          m_model.getColumn(column).m_maxWidth = TableColumnDescription.DEFAULT_MAX_WIDTH;
        }
      });
    }
  }

  private ToolItem createClearSpinnerButton(Composite container) {
    ToolBar toolBar = new ToolBar(container, SWT.FLAT);
    ToolItem toolItem = new ToolItem(toolBar, SWT.NONE);
    toolItem.setImage(DesignerPlugin.getImage("clear.gif"));
    toolItem.setToolTipText(ModelMessages.TableModelDialog_resetValue);
    return toolItem;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  private static int getInsertColumnIndex(int column) {
    return column != -1 ? column : 0;
  }

  private static int getInsertRowIndex(int row) {
    return row != -1 ? row : 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection in JTable
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_tableSelectedColumn;
  private int m_tableSelectedRow;
  private boolean m_processTableSelectionEvent = true;

  private void trackTableSelection() {
    m_tableSelectedRow = -1;
    m_tableSelectedColumn = -1;
    ListSelectionListener listener = new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (m_processTableSelectionEvent) {
          m_tableSelectedColumn = m_table.getSelectedColumn();
          m_tableSelectedRow = m_table.getSelectedRow();
          updateControls();
        }
      }
    };
    m_table.getSelectionModel().addListSelectionListener(listener);
    m_table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
    updateControls();
    // XXX
    final PropertyChangeListener columnWidthListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (m_processTableSelectionEvent) {
          TableColumn column = (TableColumn) evt.getSource();
          int index = column.getModelIndex();
          m_model.getColumn(index).m_preferredWidth = (Integer) evt.getNewValue();
          updateControls();
        }
      }
    };
    m_table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
      public void columnAdded(TableColumnModelEvent e) {
        TableColumnModel columnModel = (TableColumnModel) e.getSource();
        int columnIndex = e.getToIndex();
        columnModel.getColumn(columnIndex).addPropertyChangeListener(columnWidthListener);
      }

      public void columnSelectionChanged(ListSelectionEvent e) {
      }

      public void columnRemoved(TableColumnModelEvent e) {
      }

      public void columnMoved(TableColumnModelEvent e) {
      }

      public void columnMarginChanged(ChangeEvent e) {
      }
    });
  }

  private void setTableSelection(int row, int column) {
    m_table.changeSelection(row, column, false, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Updates
  //
  ////////////////////////////////////////////////////////////////////////////
  private void updateControls() {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        updateControlsInDisplay();
      }
    });
  }

  private void updateControlsInDisplay() {
    int row = m_tableSelectedRow;
    int column = m_tableSelectedColumn;
    int rowCount = m_table.getRowCount();
    int columnCount = m_table.getColumnCount();
    //
    m_columnCountSpinner.setSelection(m_model.getColumnCount());
    m_rowCountSpinner.setSelection(m_model.getRowCount());
    // column
    m_deleteColumnButton.setEnabled(column != -1);
    m_moveColumnLeftButton.setEnabled(column > 0 && column < columnCount);
    m_moveColumnRightButton.setEnabled(column >= 0 && column < columnCount - 1);
    // row
    m_deleteRowButton.setEnabled(row != -1);
    m_moveRowUpButton.setEnabled(row > 0 && row < rowCount);
    m_moveRowDownButton.setEnabled(row >= 0 && row < rowCount - 1);
    // column properties
    UiUtils.changeControlEnable(m_columnPropertiesComposite, column != -1);
    if (column != -1) {
      TableColumnDescription columnDescription = m_model.getColumn(column);
      m_columnPropertyNo.setText("" + column);
      if (!m_columnPropertyTitle.getText().equals(columnDescription.m_name)) {
        m_columnPropertyTitle.setText(columnDescription.m_name);
      }
      m_columnPropertyType.setText(COLUMN_TYPES.inverse().get(columnDescription.m_class));
      m_columnPropertyPrefWidth.setSelection(columnDescription.m_preferredWidth);
      m_columnPropertyMinWidth.setSelection(columnDescription.m_minWidth);
      m_columnPropertyMaxWidth.setSelection(columnDescription.m_maxWidth);
      m_columnPropertyEditable.setSelection(columnDescription.m_editable);
      m_columnPropertyResizable.setSelection(columnDescription.m_resizable);
    }
  }

  private interface TableOperationRunnable {
    void run(int row, int column);
  }

  private final TableOperationRunnable DEFAULT_TABLE_UPDATE = new TableOperationRunnable() {
    public void run(int row, int column) {
      setTableSelection(row, column);
      updateControls();
    }
  };

  private void addJTableOperationListener(Widget eventTarget,
      int eventType,
      TableOperationRunnable operation) {
    addJTableOperationSelectionListener(eventTarget, eventType, operation, DEFAULT_TABLE_UPDATE);
  }

  private void addJTableOperationSelectionListener(Widget eventTarget,
      TableOperationRunnable operation) {
    addJTableOperationSelectionListener(eventTarget, operation, DEFAULT_TABLE_UPDATE);
  }

  private void addJTableOperationSelectionListener(Widget eventTarget,
      final TableOperationRunnable operation,
      final TableOperationRunnable tableUpdateRunnable) {
    addJTableOperationSelectionListener(eventTarget, SWT.Selection, operation, tableUpdateRunnable);
  }

  private void addJTableOperationSelectionListener(Widget eventTarget,
      int eventType,
      final TableOperationRunnable operation,
      final TableOperationRunnable tableUpdateRunnable) {
    eventTarget.addListener(eventType, new Listener() {
      public void handleEvent(Event event) {
        final int row = m_table.getSelectedRow();
        final int column = m_table.getSelectedColumn();
        operation.run(row, column);
        // update JTable and controls
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            updateTableModel();
            tableUpdateRunnable.run(row, column);
            updateControls();
          }
        });
      }
    });
  }

  private void updateTableModel() {
    m_processTableSelectionEvent = false;
    try {
      m_table.tableChanged(null);
      m_model.applyModel(m_table);
    } finally {
      m_processTableSelectionEvent = true;
    }
  }
}
