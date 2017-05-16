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
package org.eclipse.wb.internal.core.nls.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * <code>TableTextCellEditor</code> is a copy of TextCellEditor, with the following changes:
 *
 * <ul>
 * <li>modify events are sent out as the text is changed, and not only after editing is done</li>
 *
 * <li>the <code>Control</code> from <code>getControl(Composite)</code> does not notify registered
 * FocusListeners. This is a workaround for bug 58777.</li>
 *
 * <li>the user can go to the next/previous row with up and down keys</li>
 * </ul>
 */
public class TableTextCellEditor extends CellEditor {
  private static final int DEFAULT_STYLE = SWT.SINGLE;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Initial fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final TableViewer m_viewer;
  private final int m_column;
  ////////////////////////////////////////////////////////////////////////////
  //
  // UI fields
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The editor's value on activation. This value is reset to the cell when the editor is left via
   * ESC key.
   */
  private String m_originalValue;
  private Text m_text;
  ////////////////////////////////////////////////////////////////////////////
  //
  // State fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean isSelection = false;
  private boolean isDeleteable = false;
  private boolean isSelectable = false;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableTextCellEditor(TableViewer viewer, int column) {
    super(viewer.getTable(), DEFAULT_STYLE);
    m_viewer = viewer;
    m_column = column;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CellEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void activate() {
    super.activate();
    m_originalValue = m_text.getText();
  }

  @Override
  protected Control createControl(Composite parent) {
    //workaround for bug 58777: don't accept focus listeners on the text control
    Composite result = new Composite(parent, SWT.NONE) {
      @Override
      public void addListener(int eventType, final Listener listener) {
        if (eventType != SWT.FocusIn && eventType != SWT.FocusOut) {
          m_text.addListener(eventType, listener);
        }
      }
    };
    result.setFont(parent.getFont());
    result.setBackground(parent.getBackground());
    result.setLayout(new FillLayout());
    //
    m_text = new Text(result, getStyle());
    m_text.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        handleDefaultSelection(e);
      }
    });
    m_text.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.keyCode) {
          case SWT.ARROW_DOWN :
            e.doit = false;
            int nextRow = m_viewer.getTable().getSelectionIndex() + 1;
            if (nextRow >= m_viewer.getTable().getItemCount()) {
              break;
            }
            editRow(nextRow);
            break;
          case SWT.ARROW_UP :
            e.doit = false;
            int prevRow = m_viewer.getTable().getSelectionIndex() - 1;
            if (prevRow < 0) {
              break;
            }
            editRow(prevRow);
            break;
        }
      }

      private void editRow(int row) {
        m_viewer.getTable().setSelection(row);
        IStructuredSelection newSelection = (IStructuredSelection) m_viewer.getSelection();
        if (newSelection.size() == 1) {
          m_viewer.editElement(newSelection.getFirstElement(), m_column);
        }
      }
    });
    m_text.addKeyListener(new KeyAdapter() {
      // hook key pressed - see PR 14201
      @Override
      public void keyPressed(KeyEvent e) {
        keyReleaseOccured(e);
        // as a result of processing the above call, clients may have
        // disposed this cell editor
        if (getControl() == null || getControl().isDisposed()) {
          return;
        }
        checkSelection(); // see explaination below
        checkDeleteable();
        checkSelectable();
      }
    });
    m_text.addTraverseListener(new TraverseListener() {
      public void keyTraversed(TraverseEvent e) {
        if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) {
          e.doit = false;
        }
      }
    });
    // We really want a selection listener but it is not supported so we
    // use a key listener and a mouse listener to know when selection changes
    // may have occured
    m_text.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        checkSelection();
        checkDeleteable();
        checkSelectable();
      }
    });
    m_text.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        TableTextCellEditor.this.focusLost();
      }
    });
    m_text.setFont(parent.getFont());
    m_text.setBackground(parent.getBackground());
    m_text.setText("");//$NON-NLS-1$
    //
    return result;
  }

  @Override
  protected void fireCancelEditor() {
    /* bug 58540: change signature refactoring interaction: validate as you type [refactoring] */
    m_text.setText(m_originalValue);
    super.fireApplyEditorValue();
  }

  /**
   * The <code>TextCellEditor</code> implementation of this <code>CellEditor</code> framework method
   * returns the text string.
   *
   * @return the text string
   */
  @Override
  protected Object doGetValue() {
    return m_text.getText();
  }

  @Override
  protected void doSetFocus() {
    if (m_text != null) {
      m_text.selectAll();
      m_text.setFocus();
      checkSelection();
      checkDeleteable();
      checkSelectable();
    }
  }

  /**
   * The <code>TextCellEditor2</code> implementation of this <code>CellEditor</code> framework
   * method accepts a text string (type <code>String</code>).
   *
   * @param value
   *          a text string (type <code>String</code>)
   */
  @Override
  protected void doSetValue(Object value) {
    Assert.isTrue(m_text != null && value instanceof String);
    m_text.setText((String) value);
  }

  @Override
  public LayoutData getLayoutData() {
    return new LayoutData();
  }

  protected void handleDefaultSelection(SelectionEvent event) {
    // same with enter-key handling code in keyReleaseOccured(e);
    fireApplyEditorValue();
    deactivate();
  }

  @Override
  protected void keyReleaseOccured(KeyEvent keyEvent) {
    if (keyEvent.character == '\r') { // Return key
      // Enter is handled in handleDefaultSelection.
      // Do not apply the editor value in response to an Enter key event
      // since this can be received from the IME when the intent is -not-
      // to apply the value.
      // See bug 39074 [CellEditors] [DBCS] canna input mode fires bogus event from Text Control
      //
      // An exception is made for Ctrl+Enter for multi-line texts, since
      // a default selection event is not sent in this case.
      if (m_text != null && !m_text.isDisposed() && (m_text.getStyle() & SWT.MULTI) != 0) {
        if ((keyEvent.stateMask & SWT.CTRL) != 0) {
          super.keyReleaseOccured(keyEvent);
        }
      }
      return;
    }
    super.keyReleaseOccured(keyEvent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Text getText() {
    return m_text;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard operations: check
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void checkSelection() {
    boolean oldIsSelection = isSelection;
    isSelection = m_text.getSelectionCount() > 0;
    if (oldIsSelection != isSelection) {
      fireEnablementChanged(COPY);
      fireEnablementChanged(CUT);
    }
  }

  protected void checkDeleteable() {
    boolean oldIsDeleteable = isDeleteable;
    isDeleteable = isDeleteEnabled();
    if (oldIsDeleteable != isDeleteable) {
      fireEnablementChanged(DELETE);
    }
  }

  protected void checkSelectable() {
    boolean oldIsSelectable = isSelectable;
    isSelectable = isSelectAllEnabled();
    if (oldIsSelectable != isSelectable) {
      fireEnablementChanged(SELECT_ALL);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard operations: enable
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isCopyEnabled() {
    if (m_text == null || m_text.isDisposed()) {
      return false;
    }
    return m_text.getSelectionCount() > 0;
  }

  @Override
  public boolean isCutEnabled() {
    if (m_text == null || m_text.isDisposed()) {
      return false;
    }
    return m_text.getSelectionCount() > 0;
  }

  @Override
  public boolean isDeleteEnabled() {
    if (m_text == null || m_text.isDisposed()) {
      return false;
    }
    return m_text.getSelectionCount() > 0 || m_text.getCaretPosition() < m_text.getCharCount();
  }

  @Override
  public boolean isPasteEnabled() {
    if (m_text == null || m_text.isDisposed()) {
      return false;
    }
    return true;
  }

  @Override
  public boolean isSelectAllEnabled() {
    if (m_text == null || m_text.isDisposed()) {
      return false;
    }
    return m_text.getCharCount() > 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard operations: perform
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performCopy() {
    m_text.copy();
  }

  @Override
  public void performCut() {
    m_text.cut();
    checkSelection();
    checkDeleteable();
    checkSelectable();
  }

  @Override
  public void performDelete() {
    if (m_text.getSelectionCount() > 0) {
      // remove the contents of the current selection
      m_text.insert(""); //$NON-NLS-1$
    } else {
      // remove the next character
      int pos = m_text.getCaretPosition();
      if (pos < m_text.getCharCount()) {
        m_text.setSelection(pos, pos + 1);
        m_text.insert(""); //$NON-NLS-1$
      }
    }
    checkSelection();
    checkDeleteable();
    checkSelectable();
  }

  @Override
  public void performPaste() {
    m_text.paste();
    checkSelection();
    checkDeleteable();
    checkSelectable();
  }

  @Override
  public void performSelectAll() {
    m_text.selectAll();
    checkSelection();
    checkDeleteable();
  }
}
