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
package org.eclipse.wb.internal.swing.preferences.laf;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.laf.LafSupport;
import org.eclipse.wb.internal.swing.laf.command.AddCategoryCommand;
import org.eclipse.wb.internal.swing.laf.command.Command;
import org.eclipse.wb.internal.swing.laf.command.MoveCategoryCommand;
import org.eclipse.wb.internal.swing.laf.command.MoveCommand;
import org.eclipse.wb.internal.swing.laf.command.RemoveCategoryCommand;
import org.eclipse.wb.internal.swing.laf.command.RemoveCommand;
import org.eclipse.wb.internal.swing.laf.command.RenameCategoryCommand;
import org.eclipse.wb.internal.swing.laf.command.SetVisibleCommand;
import org.eclipse.wb.internal.swing.laf.model.CategoryInfo;
import org.eclipse.wb.internal.swing.laf.model.LafEntryInfo;
import org.eclipse.wb.internal.swing.laf.model.LafInfo;
import org.eclipse.wb.internal.swing.laf.model.SeparatorLafInfo;
import org.eclipse.wb.internal.swing.laf.ui.AddCustomLookAndFeelDialog;
import org.eclipse.wb.internal.swing.laf.ui.EditCustomLookAndFeelDialog;
import org.eclipse.wb.internal.swing.preferences.Messages;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import swingintegration.example.EmbeddedSwingComposite;

/**
 * The {@link PreferencePage} for managing Swing Look-n-Feel.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage swing.preferences.laf
 */
public class LafPreferencePage extends PreferencePage
    implements
      IWorkbenchPreferencePage,
      IPreferenceConstants {
  // constants
  private static final Image CATEGORY_IMAGE = Activator.getImage("info/laf/container.gif");
  private static final Image LAF_ITEM_IMAGE = Activator.getImage("info/laf/laf.png");
  // variables
  private LafInfo m_defaultLAF;
  private final LookAndFeel m_currentLookAndFeel;
  // ui variables
  private CheckboxTreeViewer m_lafTree;
  private Group m_previewGroup;
  private Button m_deleteButton;
  private Button m_applyInMainButton;
  private Button m_moveUpButton;
  private Button m_moveDownButton;
  private Button m_editButton;
  private Button m_setDefaultButton;
  // updating preview
  private boolean m_updatingPreview;
  private final Timer m_previewTimer = new Timer();
  private TimerTask m_previewTimerTask;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LafPreferencePage() {
    m_currentLookAndFeel = UIManager.getLookAndFeel();
    m_defaultLAF = LafSupport.getDefaultLAF();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(container).margins(0);
    {
      m_applyInMainButton = new Button(container, SWT.CHECK);
      m_applyInMainButton.setText(Messages.LafPreferencePage_applyInMain);
      m_applyInMainButton.setSelection(getPreferenceStore().getBoolean(P_APPLY_IN_MAIN));
    }
    {
      Group lafGroup = new Group(container, SWT.NONE);
      GridDataFactory.create(lafGroup).grab().fill();
      GridLayoutFactory.create(lafGroup).columns(2);
      lafGroup.setText(Messages.LafPreferencePage_available);
      // LAF tree
      {
        m_lafTree =
            new CheckboxTreeViewer(lafGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL);
        final Tree tree = m_lafTree.getTree();
        GridDataFactory.create(tree).grab().fill();
        m_lafTree.setContentProvider(new LAFItemsContentProvider());
        m_lafTree.setLabelProvider(new LAFItemsLabelProvider());
        m_lafTree.addSelectionChangedListener(new ISelectionChangedListener() {
          public void selectionChanged(SelectionChangedEvent event) {
            handleLAFSelectionChanged();
          }
        });
        m_lafTree.addDoubleClickListener(new IDoubleClickListener() {
          public void doubleClick(DoubleClickEvent event) {
            handleSetDefaultLAF();
          }
        });
        m_lafTree.setInput(new Object[0]);
        refreshViewer();
        m_lafTree.addCheckStateListener(new ICheckStateListener() {
          public void checkStateChanged(CheckStateChangedEvent event) {
            handleChangeVisibility(event);
          }
        });
        configureDND();
      }
      // buttons
      {
        createButtons(lafGroup);
      }
    }
    {
      m_previewGroup = new Group(container, SWT.NONE);
      GridDataFactory.create(m_previewGroup).grabH().fill();
      m_previewGroup.setText(Messages.LafPreferencePage_preview);
      m_previewGroup.setLayout(new FillLayout());
    }
    // return back LAF
    container.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        // cancel preview updating
        cancelPreviewUpdate();
        m_previewTimer.cancel();
        restoreLookAndFeel();
      }
    });
    return container;
  }

  /**
   * Creates the buttons on own composite for managing look-and-feels.
   */
  private void createButtons(Composite parent) {
    Composite buttonsComposite = new Composite(parent, SWT.NONE);
    GridDataFactory.create(buttonsComposite).grabV().fill();
    GridLayoutFactory.create(buttonsComposite).noMargins();
    //
    createButton(buttonsComposite, Messages.LafPreferencePage_addButton, new Listener() {
      public void handleEvent(Event event) {
        handleAddUserDefinedLAF();
      }
    });
    createButton(buttonsComposite, Messages.LafPreferencePage_addCategoryButton, new Listener() {
      public void handleEvent(Event event) {
        handleAddCategory();
      }
    });
    //
    createButtonSeparator(buttonsComposite);
    m_setDefaultButton =
        createButton(buttonsComposite, Messages.LafPreferencePage_setDefaultButton, new Listener() {
          public void handleEvent(Event event) {
            handleSetDefaultLAF();
          }
        });
    createButtonSeparator(buttonsComposite);
    m_editButton =
        createButton(buttonsComposite, Messages.LafPreferencePage_editButton, new Listener() {
          public void handleEvent(Event event) {
            handleEdit();
          }
        });
    m_deleteButton =
        createButton(buttonsComposite, Messages.LafPreferencePage_removeButton, new Listener() {
          public void handleEvent(Event event) {
            handleDelete();
          }
        });
    //
    createButtonSeparator(buttonsComposite);
    m_moveUpButton =
        createButton(buttonsComposite, Messages.LafPreferencePage_upButton, new Listener() {
          public void handleEvent(Event event) {
            handleMove(-1);
          }
        });
    m_moveDownButton =
        createButton(buttonsComposite, Messages.LafPreferencePage_downButton, new Listener() {
          public void handleEvent(Event event) {
            handleMove(+2);
          }
        });
    //
    createButtonSeparator(buttonsComposite);
    createButton(buttonsComposite, Messages.LafPreferencePage_collapseAllButton, new Listener() {
      public void handleEvent(Event event) {
        m_lafTree.collapseAll();
      }
    });
    createButton(buttonsComposite, Messages.LafPreferencePage_expandAllButton, new Listener() {
      public void handleEvent(Event event) {
        m_lafTree.expandAll();
      }
    });
    // update buttons
    updateButtons();
  }

  /**
   * Creates {@link Button} with given text and {@link SWT#Selection} listener.
   */
  private static Button createButton(Composite parent, String text, Listener selectionListener) {
    Button button = new Button(parent, SWT.NONE);
    GridDataFactory.create(button).grabH().fillH();
    button.setText(text);
    button.addListener(SWT.Selection, selectionListener);
    return button;
  }

  /**
   * Creates separator between buttons on vertical buttons bar.
   */
  private static void createButtonSeparator(Composite parent) {
    Label separator = new Label(parent, SWT.NONE);
    GridDataFactory.create(separator).hintV(7);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //	IWorkbenchPreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench) {
  }

  @Override
  public IPreferenceStore getPreferenceStore() {
    return ToolkitProvider.DESCRIPTION.getPreferences();
  }

  @Override
  public boolean performOk() {
    getPreferenceStore().setValue(P_APPLY_IN_MAIN, m_applyInMainButton.getSelection());
    getPreferenceStore().setValue(P_DEFAULT_LAF, m_defaultLAF.getID());
    // write all executed commands
    {
      for (Command command : m_commands) {
        LafSupport.commands_add(command);
      }
      LafSupport.commands_write();
      if (!m_commands.isEmpty()) {
        fireLookAndFeelsChanged();
      }
    }
    return super.performOk();
  }

  @Override
  protected void performDefaults() {
    m_applyInMainButton.setSelection(getPreferenceStore().getDefaultBoolean(P_APPLY_IN_MAIN));
    m_defaultLAF = LafSupport.getSettingsDefaultLAF();
    if (MessageDialog.openConfirm(
        getShell(),
        Messages.LafPreferencePage_removeConfirmTitle,
        Messages.LafPreferencePage_removeConfirmMessage)) {
      LafSupport.resetToDefaults();
      refreshViewer();
      fireLookAndFeelsChanged();
    }
    super.performDefaults();
  }

  /**
   * Runs the firing of listeners asynchronously, needed for dispose listeners to be fired first and
   * restore
   */
  private void fireLookAndFeelsChanged() {
    restoreLookAndFeel();
    LafSupport.fireLookAndFeelsChanged();
  }

  @Override
  public boolean performCancel() {
    LafSupport.reloadLAFList();
    return super.performCancel();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action handling
  //
  ////////////////////////////////////////////////////////////////////////////
  private void handleAddCategory() {
    InputDialog inputDialog =
        new InputDialog(getShell(),
            Messages.LafPreferencePage_addCategoryTitle,
            Messages.LafPreferencePage_addCategoryMessage,
            "",
            null);
    if (inputDialog.open() == Window.OK) {
      commands_add(new AddCategoryCommand("category_" + System.currentTimeMillis(),
          inputDialog.getValue()));
    }
  }

  /**
   * Changes the visibility state.
   */
  private void handleChangeVisibility(CheckStateChangedEvent event) {
    LafEntryInfo checkedEntry = (LafEntryInfo) event.getElement();
    commands_add(new SetVisibleCommand(checkedEntry, event.getChecked()));
  }

  private void handleMove(int moveDelta) {
    m_lafTree.getTree().setRedraw(false);
    try {
      for (Object entry : getSelectedEntries()) {
        if (entry instanceof CategoryInfo) {
          CategoryInfo category = (CategoryInfo) entry;
          List<CategoryInfo> categories = LafSupport.getLAFCategoriesList();
          int index = categories.indexOf(entry);
          int targetIndex = index + moveDelta;
          CategoryInfo nextCategory =
              targetIndex < categories.size() ? (CategoryInfo) categories.get(targetIndex) : null;
          commands_add(new MoveCategoryCommand(category, nextCategory));
        } else if (entry instanceof LafInfo) {
          LafInfo lafInfo = (LafInfo) entry;
          CategoryInfo category = lafInfo.getCategory();
          List<LafInfo> lafs = category.getLAFList();
          int index = lafs.indexOf(lafInfo);
          if (index + moveDelta < lafs.size()) {
            commands_add(new MoveCommand(lafInfo, category, lafs.get(index + moveDelta)));
          } else {
            commands_add(new MoveCommand(lafInfo, category, null));
          }
        }
      }
      refreshViewer();
      updateButtons();
    } finally {
      m_lafTree.getTree().setRedraw(true);
    }
  }

  /**
   * Called by pressing "default" button or double-clicking in LAF list. Marks selected LAF as
   * default.
   */
  private void handleSetDefaultLAF() {
    final LafInfo selectedLAF = getSelectedLAF();
    if (selectedLAF == null || selectedLAF instanceof SeparatorLafInfo) {
      return;
    }
    m_defaultLAF = selectedLAF;
    refreshViewer();
  }

  /**
   * Handles the selection changing in LAF table.
   * 
   * @param event
   */
  protected void handleLAFSelectionChanged() {
    // update buttons
    updateButtons();
    // update preview
    updatePreview();
  }

  /**
   * Deletes (or permanently hide) LAF and store LAF list into persistence.
   */
  @SuppressWarnings("unchecked")
  private void handleDelete() {
    List<Object> selection = getSelectedEntries();
    if (selection.contains(m_defaultLAF)) {
      MessageDialog.openWarning(
          getShell(),
          Messages.LafPreferencePage_deleteWarningTitle,
          Messages.LafPreferencePage_deleteWarningMessage);
      // filter out default LAF
      selection = (List<Object>) CollectionUtils.select(selection, new Predicate() {
        public boolean evaluate(Object object) {
          return object != m_defaultLAF;
        }
      });
    }
    if (!selection.isEmpty()) {
      if (MessageDialog.openConfirm(
          getShell(),
          Messages.LafPreferencePage_deleteConfirmTitle,
          MessageFormat.format(Messages.LafPreferencePage_deleteConfirmMessage, selection.size()))) {
        for (Object entry : selection) {
          if (entry instanceof CategoryInfo) {
            commands_add(new RemoveCategoryCommand((CategoryInfo) entry));
          } else if (entry instanceof LafInfo) {
            commands_add(new RemoveCommand((LafInfo) entry));
          }
        }
        refreshViewer();
      }
    }
  }

  /**
   * Adds user-defined LAF.
   */
  private void handleAddUserDefinedLAF() {
    CategoryInfo targetCategory = LafSupport.getCategory(LafSupport.ROOT_ID);
    {
      List<Object> selectedEntries = getSelectedEntries();
      if (!CollectionUtils.isEmpty(selectedEntries)) {
        Object entry = selectedEntries.get(0);
        if (entry instanceof CategoryInfo) {
          targetCategory = (CategoryInfo) entry;
        } else if (entry instanceof LafInfo) {
          targetCategory = ((LafInfo) entry).getCategory();
        }
      }
    }
    AddCustomLookAndFeelDialog dialog =
        new AddCustomLookAndFeelDialog(DesignerPlugin.getShell(), targetCategory);
    if (dialog.open() == Window.OK) {
      List<Command> commands = dialog.getCommands();
      for (Command command : commands) {
        commands_add(command);
      }
      refreshViewer();
    }
  }

  /**
   * Performs editing either of category or look-and-feel entry.
   */
  private void handleEdit() {
    Object entry = getSelectedEntries().get(0);
    if (entry instanceof CategoryInfo) {
      CategoryInfo category = (CategoryInfo) entry;
      InputDialog inputDialog =
          new InputDialog(getShell(),
              Messages.LafPreferencePage_editCategoryTitle,
              Messages.LafPreferencePage_editCategoryMessage,
              category.getName(),
              null);
      // execute dialog
      if (inputDialog.open() == Window.OK) {
        commands_add(new RenameCategoryCommand(category, inputDialog.getValue()));
      }
    } else if (entry instanceof LafInfo) {
      LafInfo lafInfo = (LafInfo) entry;
      EditCustomLookAndFeelDialog dialog =
          new EditCustomLookAndFeelDialog(DesignerPlugin.getShell(), lafInfo);
      // execute dialog
      if (dialog.open() == Window.OK) {
        List<Command> commands = dialog.getCommands();
        for (Command command : commands) {
          commands_add(command);
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update UI
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Simply refreshes the LAF list viewer.
   */
  private void refreshViewer() {
    m_lafTree.refresh();
    updateVisibilityStates();
  }

  /**
   * Updates buttons basing on selection in LAF list viewer.
   */
  private void updateButtons() {
    List<Object> selection = getSelectedEntries();
    m_setDefaultButton.setEnabled(selection.size() == 1 && selection.get(0) instanceof LafInfo);
    m_editButton.setEnabled(selection.size() == 1);
    m_deleteButton.setEnabled(!selection.isEmpty());
    // move up/down
    List<CategoryInfo> categories = LafSupport.getLAFCategoriesList();
    {
      boolean upEnabled = !selection.isEmpty();
      boolean downEnabled = !selection.isEmpty();
      for (Object element : selection) {
        if (element instanceof CategoryInfo) {
          upEnabled &= categories.indexOf(element) != 0;
          downEnabled &= categories.indexOf(element) != categories.size() - 1;
        } else if (element instanceof LafInfo) {
          LafInfo lafInfo = (LafInfo) element;
          List<LafInfo> lafList = lafInfo.getCategory().getLAFList();
          upEnabled &= lafList.indexOf(lafInfo) != 0;
          downEnabled &= lafList.indexOf(lafInfo) != lafList.size() - 1;
        }
      }
      m_moveUpButton.setEnabled(upEnabled);
      m_moveDownButton.setEnabled(downEnabled);
    }
  }

  /**
   * Updates the LAF list checking items which has "visible" property set.
   */
  private void updateVisibilityStates() {
    List<Object> visibleElements = Lists.newArrayList();
    for (CategoryInfo category : LafSupport.getLAFCategoriesList()) {
      if (category.isVisible()) {
        visibleElements.add(category);
      }
      for (LafInfo lafInfo : category.getLAFList()) {
        if (lafInfo.isVisible()) {
          visibleElements.add(lafInfo);
        }
      }
    }
    // update viewer
    m_lafTree.setCheckedElements(visibleElements.toArray());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Swing LAF Preview
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Restores the {@link LookAndFeel} used before preview changed it.
   */
  private void restoreLookAndFeel() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        UIManager.put("ClassLoader", m_currentLookAndFeel.getClass().getClassLoader());
        UIManager.setLookAndFeel(m_currentLookAndFeel);
      }
    });
  }

  /**
   * Schedules Swing LAF preview updating.
   */
  private void updatePreview() {
    cancelPreviewUpdate();
    m_previewTimerTask = new TimerTask() {
      @Override
      public void run() {
        DesignerPlugin.getStandardDisplay().syncExec(new Runnable() {
          public void run() {
            updatePreview0();
          }
        });
      }
    };
    m_previewTimer.schedule(m_previewTimerTask, 200);
  }

  /**
   * Updates the Swing preview part basing on selected LAF.
   */
  private void updatePreview0() {
    if (m_updatingPreview) {
      return;
    }
    m_updatingPreview = true;
    try {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          try {
            m_previewGroup.getParent().setRedraw(false);
            for (Control control : m_previewGroup.getChildren()) {
              control.dispose();
            }
            LafInfo selectedLAF = getSelectedLAF();
            if (selectedLAF == null) {
              // nothing selected
              return;
            }
            LookAndFeel lookAndFeel = selectedLAF.getLookAndFeelInstance();
            m_previewGroup.getParent().layout(true);
            UIManager.put("ClassLoader", lookAndFeel.getClass().getClassLoader());
            UIManager.setLookAndFeel(lookAndFeel);
            createPreviewArea(m_previewGroup);
            m_previewGroup.getParent().layout(true);
          } finally {
            m_previewGroup.getParent().setRedraw(true);
          }
        }
      });
    } finally {
      m_updatingPreview = false;
    }
  }

  /**
   * Cancels any pending preview updating task.
   */
  private void cancelPreviewUpdate() {
    if (m_previewTimerTask != null) {
      m_previewTimerTask.cancel();
    }
  }

  /**
   * Creates {@link EmbeddedSwingComposite} with some Swing components to show it using different
   * LAFs.
   */
  private void createPreviewArea(Group previewGroup) {
    try {
      LookAndFeel currentLookAndFeel = UIManager.getLookAndFeel();
      EmbeddedSwingComposite awtComposite = new EmbeddedSwingComposite(previewGroup, SWT.NONE) {
        @Override
        protected JComponent createSwingComponent() {
          // create the JRootPane
          JRootPane rootPane = new JRootPane();
          {
            JMenuBar menuBar = new JMenuBar();
            rootPane.setJMenuBar(menuBar);
            {
              JMenu mnFile = new JMenu(Messages.LafPreferencePage_previewFile);
              menuBar.add(mnFile);
              {
                JMenuItem mntmNew = new JMenuItem(Messages.LafPreferencePage_previewNew);
                mnFile.add(mntmNew);
              }
              {
                JMenuItem mntmExit = new JMenuItem(Messages.LafPreferencePage_previewExit);
                mnFile.add(mntmExit);
              }
            }
            {
              JMenu mnView = new JMenu(Messages.LafPreferencePage_previewView);
              menuBar.add(mnView);
              {
                JMenuItem mntmCommon = new JMenuItem(Messages.LafPreferencePage_previewCommon);
                mnView.add(mntmCommon);
              }
            }
          }
          GridBagLayout gridBagLayout = new GridBagLayout();
          gridBagLayout.columnWidths = new int[]{0, 0, 0};
          gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
          gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0E-4};
          gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0E-4};
          rootPane.getContentPane().setLayout(gridBagLayout);
          {
            JLabel lblLabel = new JLabel(Messages.LafPreferencePage_previewLabel);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 5, 5);
            gbc.gridx = 0;
            gbc.gridy = 0;
            rootPane.getContentPane().add(lblLabel, gbc);
          }
          {
            JButton btnPushButton = new JButton(Messages.LafPreferencePage_previewButton);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 5, 0);
            gbc.gridx = 1;
            gbc.gridy = 0;
            rootPane.getContentPane().add(btnPushButton, gbc);
          }
          {
            JComboBox comboBox = new JComboBox();
            comboBox.setModel(new DefaultComboBoxModel(new String[]{
                Messages.LafPreferencePage_previewCombo,
                "ComboBox Item 1",
                "ComboBox Item 2"}));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 1;
            rootPane.getContentPane().add(comboBox, gbc);
          }
          {
            JRadioButton rdbtnRadioButton =
                new JRadioButton(Messages.LafPreferencePage_previewRadio);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 5, 0);
            gbc.gridx = 1;
            gbc.gridy = 1;
            rootPane.getContentPane().add(rdbtnRadioButton, gbc);
          }
          {
            JCheckBox chckbxCheckbox = new JCheckBox(Messages.LafPreferencePage_previewCheck);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 5);
            gbc.gridx = 0;
            gbc.gridy = 2;
            rootPane.getContentPane().add(chckbxCheckbox, gbc);
          }
          {
            JTextField textField = new JTextField();
            textField.setText(Messages.LafPreferencePage_previewTextField);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 1;
            gbc.gridy = 2;
            rootPane.getContentPane().add(textField, gbc);
          }
          return rootPane;
        }
      };
      awtComposite.populate();
      // restore current laf
      UIManager.put("ClassLoader", currentLookAndFeel.getClass().getClassLoader());
      UIManager.setLookAndFeel(currentLookAndFeel);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<Command> m_commands = Lists.newArrayList();

  /**
   * Executes new {@link Command} and updates {@link #m_viewer} accordingly.
   */
  private void commands_add(Command command) {
    command.execute();
    command.addToCommandList(m_commands);
    refreshViewer();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DND
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<Object> m_dragEntries;
  private boolean m_dragCategory;

  /**
   * Configures DND for LAF list viewer.
   */
  private void configureDND() {
    Transfer[] transfers = new Transfer[]{LookAndFeelTransfer.INSTANCE};
    m_lafTree.addDragSupport(DND.DROP_MOVE, transfers, new DragSourceListener() {
      public void dragStart(DragSourceEvent event) {
        m_dragEntries = getSelectedEntries();
        m_dragCategory = m_dragEntries.get(0) instanceof CategoryInfo;
        // check that we drag only categories or only entries
        for (Object element : m_dragEntries) {
          if (m_dragCategory != element instanceof CategoryInfo) {
            event.doit = false;
          }
        }
      }

      public void dragSetData(DragSourceEvent event) {
      }

      public void dragFinished(DragSourceEvent event) {
      }
    });
    ViewerDropAdapter dropAdapter = new ViewerDropAdapter(m_lafTree) {
      @Override
      protected int determineLocation(DropTargetEvent event) {
        if (event.item instanceof Item) {
          Item item = (Item) event.item;
          Point coordinates = m_lafTree.getControl().toControl(event.x, event.y);
          Rectangle bounds = getBounds(item);
          // when dragging LAF entry relation with category can be only ON
          if (!m_dragCategory && determineTarget(event) instanceof CategoryInfo) {
            return LOCATION_ON;
          }
          // in all other cases, drop before/after
          return coordinates.y < bounds.y + bounds.height / 2 ? LOCATION_BEFORE : LOCATION_AFTER;
        }
        return LOCATION_NONE;
      }

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferType) {
        // category can be dragged only relative other category
        if (m_dragCategory) {
          return target instanceof CategoryInfo;
        }
        // all other cases are valid
        return true;
      }

      @Override
      public boolean performDrop(Object data) {
        Object target = getCurrentTarget();
        int location = getCurrentLocation();
        if (m_dragCategory) {
          Assert.instanceOf(CategoryInfo.class, target);
          Assert.isTrue(location == LOCATION_BEFORE || location == LOCATION_AFTER);
          // prepare next category
          CategoryInfo nextCategory;
          {
            List<CategoryInfo> categories = LafSupport.getLAFCategoriesList();
            int index = categories.indexOf(target);
            if (location == LOCATION_BEFORE) {
              nextCategory = categories.get(index);
            } else {
              nextCategory = GenericsUtils.getNextOrNull(categories, index);
            }
          }
          // add commands
          for (Object element : m_dragEntries) {
            CategoryInfo category = (CategoryInfo) element;
            commands_add(new MoveCategoryCommand(category, nextCategory));
          }
        } else if (target instanceof CategoryInfo) {
          Assert.isTrue(location == LOCATION_ON);
          CategoryInfo targetCategory = (CategoryInfo) target;
          for (Object entry : m_dragEntries) {
            commands_add(new MoveCommand((LafInfo) entry, targetCategory, null));
          }
        } else {
          LafInfo targetLAF = (LafInfo) target;
          CategoryInfo targetCategory = targetLAF.getCategory();
          // prepare next LAF
          LafInfo nextLAF;
          {
            List<LafInfo> entries = targetCategory.getLAFList();
            int index = entries.indexOf(targetLAF);
            if (location == LOCATION_BEFORE) {
              nextLAF = entries.get(index);
            } else {
              nextLAF = GenericsUtils.getNextOrNull(entries, index);
            }
          }
          // add commands
          for (Object entry : m_dragEntries) {
            commands_add(new MoveCommand((LafInfo) entry, targetCategory, nextLAF));
          }
        }
        // refresh viewer to show result of applying commands
        refreshViewer();
        return true;
      }
    };
    dropAdapter.setScrollExpandEnabled(false);
    m_lafTree.addDropSupport(DND.DROP_MOVE, transfers, dropAdapter);
  }

  /**
   * Implementation of {@link ByteArrayTransfer} for LAF entries.
   * 
   * @author mitin_aa
   */
  private static final class LookAndFeelTransfer extends ByteArrayTransfer {
    public static LookAndFeelTransfer INSTANCE = new LookAndFeelTransfer();
    private static final String TYPE_NAME = "__WBP_LookAndFeel_Tranfser";
    private static final int TYPE_ID = registerType(TYPE_NAME);

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    private LookAndFeelTransfer() {
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Transfer
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected int[] getTypeIds() {
      return new int[]{TYPE_ID};
    }

    @Override
    protected String[] getTypeNames() {
      return new String[]{TYPE_NAME};
    }

    @Override
    protected void javaToNative(Object object, TransferData transferData) {
    }

    @Override
    protected Object nativeToJava(TransferData transferData) {
      return ArrayUtils.EMPTY_BYTE_ARRAY;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils/Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the currently selected {@link LafInfo} from LAF table or <code>null</code> if nothing
   *         or separator selected.
   */
  private LafInfo getSelectedLAF() {
    IStructuredSelection selection = (IStructuredSelection) m_lafTree.getSelection();
    if (selection == null) {
      return null;
    }
    Object firstElement = selection.getFirstElement();
    return firstElement instanceof LafInfo && !(firstElement instanceof SeparatorLafInfo)
        ? (LafInfo) firstElement
        : null;
  }

  /**
   * @return the {@link List} of anything selectes in LAF list viewer.
   */
  @SuppressWarnings("unchecked")
  private List<Object> getSelectedEntries() {
    IStructuredSelection selection = (IStructuredSelection) m_lafTree.getSelection();
    return selection.toList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Content/LabelProvider for LAF list
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class LAFItemsLabelProvider extends LabelProvider {
    ////////////////////////////////////////////////////////////////////////////
    //
    // LabelProvider
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getText(Object element) {
      LafEntryInfo laf = (LafEntryInfo) element;
      String name = laf.getName();
      boolean isDefault =
          m_defaultLAF != null
              && laf.getID().equals(m_defaultLAF.getID())
              && name.equals(m_defaultLAF.getName());
      return isDefault ? name + " [default]" : name;
    }

    @Override
    public Image getImage(Object element) {
      if (element instanceof CategoryInfo) {
        return CATEGORY_IMAGE;
      }
      return LAF_ITEM_IMAGE;
    }
  }
  /**
   * Content provider for LAF list.
   * 
   * @author mitin_aa
   */
  private static class LAFItemsContentProvider implements ITreeContentProvider {
    @SuppressWarnings("unchecked")
    public Object[] getElements(Object inputElement) {
      List<Object> resultList = Lists.newArrayList();
      for (CategoryInfo category : LafSupport.getLAFCategoriesList()) {
        if (LafSupport.isRootCategory(category)) {
          resultList.addAll(category.getLAFList());
        } else {
          resultList.add(category);
        }
      }
      // filter out SeparatorLAFInfo
      resultList = (List<Object>) CollectionUtils.select(resultList, new Predicate() {
        public boolean evaluate(Object object) {
          return !(object instanceof SeparatorLafInfo);
        }
      });
      return resultList.toArray(new Object[resultList.size()]);
    }

    public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof CategoryInfo) {
        CategoryInfo category = (CategoryInfo) parentElement;
        return LafSupport.isRootCategory(category)
            ? ArrayUtils.EMPTY_OBJECT_ARRAY
            : category.getLAFList().toArray();
      }
      return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }

    public Object getParent(Object element) {
      if (element instanceof LafInfo) {
        return ((LafInfo) element).getCategory();
      }
      return null;
    }

    public boolean hasChildren(Object element) {
      return getChildren(element).length != 0;
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }
}
