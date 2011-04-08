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
package org.eclipse.wb.internal.swing.model.layout.gbl.ui;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.controls.CSpinner;
import org.eclipse.wb.core.controls.Separator;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.text.MessageFormat;
import java.util.List;

/**
 * The dialog for editing {@link DimensionInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout.ui
 */
abstract class DimensionEditDialog<T extends DimensionInfo, A extends Enum<?>>
    extends
      ResizableDialog {
  private final AbstractGridBagLayoutInfo m_layout;
  private final List<T> m_dimensions;
  private final boolean m_horizontal;
  private final String m_dimensionName;
  private final List<AlignmentDescription<A>> m_alignments;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Current state
  //
  ////////////////////////////////////////////////////////////////////////////
  private T m_dimension;
  private int m_currentIndex;
  private A m_currentAlignment;
  private int m_currentSize;
  private double m_currentWeight;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionEditDialog(Shell parentShell,
      AbstractGridBagLayoutInfo layout,
      List<T> dimensions,
      T dimension,
      String dimensionName,
      List<AlignmentDescription<A>> alignments) {
    super(parentShell, Activator.getDefault());
    setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    m_layout = layout;
    m_dimensions = dimensions;
    m_horizontal = dimension instanceof ColumnInfo;
    m_dimensionName = dimensionName;
    m_alignments = alignments;
    setEditDimension(dimension);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private Text m_indexText;
  private Button m_prevButton;
  private Button m_nextButton;
  // alignment
  private List<Button> m_alignmentButtons;
  // size
  private CSpinner m_sizeSpinner;
  // grow
  private Button m_noGrowButton;
  private Button m_growButton;
  private CSpinner m_growSpinner;

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    createHeaderComposite(container);
    createAlignmentComposite(container);
    createSizeComposite(container);
    createWeightComposite(container);
    //
    showDimension();
    return container;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(MessageFormat.format(ModelMessages.DimensionEditDialog_title, m_dimensionName));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final int APPLY_ID = IDialogConstants.CLIENT_ID + 1;

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    super.createButtonsForButtonBar(parent);
    if (m_layout != null) {
      createButton(parent, APPLY_ID, ModelMessages.DimensionEditDialog_applyButton, false);
    }
  }

  @Override
  protected void buttonPressed(int buttonId) {
    try {
      if (buttonId == IDialogConstants.OK_ID || buttonId == APPLY_ID) {
        applyChanges();
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
    super.buttonPressed(buttonId);
  }

  /**
   * Saves current {@link DimensionInfo} changes into source and refreshes GUI.
   */
  private void applyChanges() throws Exception {
    ExecutionUtils.run(m_layout, new RunnableEx() {
      public void run() throws Exception {
        setAlignment(m_dimension, m_currentAlignment);
        m_dimension.setSize(m_currentSize);
        m_dimension.setWeight(m_currentWeight);
      }
    });
  }

  /**
   * Sets the {@link DimensionInfo} to edit.
   */
  private void setEditDimension(final T dimension) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        // apply changes
        if (m_dimension != null) {
          applyChanges();
        }
        // remember new dimension
        m_dimension = dimension;
        m_currentIndex = m_dimensions.indexOf(m_dimension);
        m_currentAlignment = getAlignment(m_dimension);
        m_currentSize = m_dimension.getSize();
        m_currentWeight = m_dimension.getWeight();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the alignment of {@link DimensionInfo}.
   */
  protected abstract A getAlignment(T dimension);

  /**
   * Sets alignment of {@link DimensionInfo}.
   */
  protected abstract void setAlignment(T dimension, A alignment) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Composites
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create the header displaying {@link Composite}.
   */
  private void createHeaderComposite(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridDataFactory.create(composite).grabH().fill();
    GridLayoutFactory.create(composite).noMargins().columns(4);
    // index
    {
      new Label(composite, SWT.NONE).setText(m_dimensionName + ":");
      // index
      {
        m_indexText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
        GridDataFactory.create(m_indexText).grabH().fillH().indentHC(3);
      }
      // prev button
      {
        m_prevButton = new Button(composite, SWT.NONE);
        m_prevButton.setToolTipText(MessageFormat.format(
            ModelMessages.DimensionEditDialog_previousButton,
            m_dimensionName));
        m_prevButton.setImage(m_horizontal
            ? AbstractGridBagLayoutInfo.getImage("navigation/left.gif")
            : AbstractGridBagLayoutInfo.getImage("navigation/up.gif"));
        m_prevButton.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            setEditDimension(m_dimensions.get(m_currentIndex - 1));
            showDimension();
          }
        });
      }
      // next button
      {
        m_nextButton = new Button(composite, SWT.NONE);
        m_nextButton.setToolTipText(MessageFormat.format(
            ModelMessages.DimensionEditDialog_nextButton,
            m_dimensionName));
        m_nextButton.setImage(m_horizontal
            ? AbstractGridBagLayoutInfo.getImage("navigation/right.gif")
            : AbstractGridBagLayoutInfo.getImage("navigation/down.gif"));
        m_nextButton.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            setEditDimension(m_dimensions.get(m_currentIndex + 1));
            showDimension();
          }
        });
      }
    }
  }

  /**
   * Creates the alignment editing {@link Composite}.
   */
  private void createAlignmentComposite(Composite parent) {
    createSeparator(parent, ModelMessages.DimensionEditDialog_defaultAlignment);
    Composite composite = new Composite(parent, SWT.NONE);
    GridDataFactory.create(composite).grabH().fill().indentHC(2).hintHC(50);
    GridLayoutFactory.create(composite).noMargins().columns(1);
    //composite.setLayout(new RowLayout());
    //
    m_alignmentButtons = Lists.newArrayList();
    for (final AlignmentDescription<A> description : m_alignments) {
      // create radio button
      Button button = new Button(composite, SWT.RADIO);
      GridDataFactory.create(button).grabH().fillH();
      button.setText(description.getTitle());
      // add listener
      m_alignmentButtons.add(button);
      button.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          m_currentAlignment = description.getAlignment();
        }
      });
    }
  }

  /**
   * Creates the size editing {@link Composite}.
   */
  private void createSizeComposite(Composite parent) {
    createSeparator(parent, ModelMessages.DimensionEditDialog_size);
    Composite composite = new Composite(parent, SWT.NONE);
    GridDataFactory.create(composite).grabH().fill().indentHC(2);
    GridLayoutFactory.create(composite).noMargins().columns(2);
    // minimum
    {
      {
        Label label = new Label(composite, SWT.NONE);
        GridDataFactory.create(label).hintHC(10);
        label.setText(ModelMessages.DimensionEditDialog_minimum);
      }
      {
        m_sizeSpinner = new CSpinner(composite, SWT.BORDER);
        GridDataFactory.create(m_sizeSpinner).hintHC(15);
        m_sizeSpinner.setMinimum(0);
        m_sizeSpinner.setMaximum(Integer.MAX_VALUE);
        m_sizeSpinner.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            m_currentSize = m_sizeSpinner.getSelection();
          }
        });
      }
    }
  }

  /**
   * Creates the weight editing {@link Composite}.
   */
  private void createWeightComposite(Composite parent) {
    createSeparator(parent, ModelMessages.DimensionEditDialog_resizeBehavior);
    Composite composite = new Composite(parent, SWT.NONE);
    GridDataFactory.create(composite).grabH().fill().indentHC(2);
    GridLayoutFactory.create(composite).noMargins().columns(2);
    // none
    {
      m_noGrowButton = new Button(composite, SWT.RADIO);
      GridDataFactory.create(m_noGrowButton).spanH(2);
      m_noGrowButton.setText("&none");
      m_noGrowButton.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          m_currentWeight = 0.0;
          showDimension();
        }
      });
    }
    // grow
    {
      m_growButton = new Button(composite, SWT.RADIO);
      GridDataFactory.create(m_growButton).hintHC(10);
      m_growButton.setText("&grow");
      m_growButton.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          m_currentWeight = 1.0;
          showDimension();
        }
      });
      //
      m_growSpinner = new CSpinner(composite, SWT.BORDER);
      GridDataFactory.create(m_growSpinner).hintHC(15);
      m_growSpinner.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          m_currentWeight = m_growSpinner.getSelection();
          showDimension();
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shows the {@link DimensionInfo} in UI controls.
   */
  private void showDimension() {
    // header
    {
      m_indexText.setText("" + (1 + m_currentIndex));
      m_prevButton.setEnabled(m_currentIndex != 0);
      m_nextButton.setEnabled(m_currentIndex < m_dimensions.size() - 1);
    }
    // alignment
    for (int i = 0; i < m_alignments.size(); i++) {
      AlignmentDescription<A> description = m_alignments.get(i);
      Button button = m_alignmentButtons.get(i);
      button.setSelection(m_currentAlignment == description.getAlignment());
    }
    // size
    m_sizeSpinner.setSelection(m_currentSize);
    // grow
    if (m_currentWeight != 0.0) {
      m_noGrowButton.setSelection(false);
      m_growButton.setSelection(true);
      //
      m_growSpinner.setEnabled(true);
      int weight = Math.max((int) m_currentWeight, 1);
      if (m_growSpinner.getSelection() != weight) {
        m_growSpinner.setSelection(weight);
      }
    } else {
      m_noGrowButton.setSelection(true);
      m_growButton.setSelection(false);
      //
      m_growSpinner.setEnabled(false);
      m_growSpinner.setSelection(0);
    }
  }

  /**
   * Creates separator with given text.
   */
  private static void createSeparator(Composite parent, String text) {
    Separator separator = new Separator(parent, SWT.NONE);
    GridDataFactory.create(separator).grabH().fillH();
    separator.setText(text);
    separator.setForeground(separator.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
  }
}
