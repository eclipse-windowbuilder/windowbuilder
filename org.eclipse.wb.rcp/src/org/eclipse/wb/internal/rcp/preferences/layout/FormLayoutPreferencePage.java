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
package org.eclipse.wb.internal.rcp.preferences.layout;

import com.google.common.collect.Sets;

import org.eclipse.wb.core.controls.jface.preference.ComboFieldEditor;
import org.eclipse.wb.core.controls.jface.preference.FieldLayoutPreferencePage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutPreferences;
import org.eclipse.wb.internal.swt.model.layout.form.IPreferenceConstants;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import java.util.Iterator;
import java.util.Set;

/**
 * Main {@link PreferencePage} for RCP FormLayout Support.
 * 
 * @author mitin_aa
 * @coverage rcp.preferences.ui
 */
public final class FormLayoutPreferencePage extends FieldLayoutPreferencePage
    implements
      IWorkbenchPreferencePage,
      IPreferenceConstants {
  private static Image m_addImage = AbstractUIPlugin.imageDescriptorFromPlugin(
      "org.eclipse.ui",
      "/icons/full/obj16/add_obj.gif").createImage();
  private static Image m_removeImage = AbstractUIPlugin.imageDescriptorFromPlugin(
      "org.eclipse.ui",
      "/icons/full/obj16/delete_obj.gif").createImage();
  private Composite m_prefsAutomatic;
  private Composite m_prefsClassic;
  private StackLayout m_stackLayout;
  private ComboFieldEditor m_modeEditor;
  private Composite m_details;
  private PercentsGroup m_verticalPercentsGroup;
  private PercentsGroup m_horizontalPercentsGroup;

  /**
   * @return The {@link IPreferenceStore} of RCP Toolkit Support plugin
   */
  @Override
  public IPreferenceStore getPreferenceStore() {
    return Activator.getDefault().getPreferenceStore();
  }

  @Override
  protected void initialize() {
    super.initialize();
    String mode = getPreferenceStore().getString(PREF_FORMLAYOUT_MODE);
    propertyChange(new PropertyChangeEvent(m_modeEditor, PREF_FORMLAYOUT_MODE, null, mode));
  }

  @Override
  protected Control createPageContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(container);
    m_modeEditor =
        createComboFieldEditor(PREF_FORMLAYOUT_MODE, "FromLayout editing mode", container);
    {
      m_details = new Composite(container, SWT.NONE);
      GridDataFactory.create(m_details).grab().fill();
      m_stackLayout = new StackLayout();
      m_details.setLayout(m_stackLayout);
      {
        m_prefsAutomatic = new Composite(m_details, SWT.NONE);
        GridLayoutFactory.create(m_prefsAutomatic);
        {
          Group snapPoints = new Group(m_prefsAutomatic, SWT.NONE);
          GridLayoutFactory.create(snapPoints).columns(2).noMargins().noSpacing();
          GridDataFactory.create(snapPoints).grab().fill();
          snapPoints.setText("Snap points");
          {
            m_verticalPercentsGroup =
                new PercentsGroup(snapPoints,
                    "Vertical",
                    PREF_V_WINDOW_MARGIN,
                    PREF_V_PERCENT_OFFSET,
                    PREF_V_WIDGET_OFFSET,
                    PREF_V_PERCENTS);
          }
          {
            m_horizontalPercentsGroup =
                new PercentsGroup(snapPoints,
                    "Horizontal",
                    PREF_H_WINDOW_MARGIN,
                    PREF_H_PERCENT_OFFSET,
                    PREF_H_WIDGET_OFFSET,
                    PREF_H_PERCENTS);
          }
        }
      }
      {
        m_prefsClassic = new Composite(m_details, SWT.NONE);
        GridLayoutFactory.create(m_prefsClassic);
        {
          Group snapPoints = new Group(m_prefsClassic, SWT.NONE);
          GridLayoutFactory.create(snapPoints).columns(2).noMargins().noSpacing();
          GridDataFactory.create(snapPoints).grab().fill();
          snapPoints.setText("Snap points");
          {
            m_verticalPercentsGroup =
                new PercentsGroup(snapPoints,
                    "Vertical",
                    PREF_V_WINDOW_MARGIN,
                    PREF_V_PERCENT_OFFSET,
                    PREF_V_WIDGET_OFFSET,
                    PREF_V_PERCENTS);
          }
          {
            m_horizontalPercentsGroup =
                new PercentsGroup(snapPoints,
                    "Horizontal",
                    PREF_H_WINDOW_MARGIN,
                    PREF_H_PERCENT_OFFSET,
                    PREF_H_WIDGET_OFFSET,
                    PREF_H_PERCENTS);
          }
        }
        {
          Composite composite = new Composite(m_prefsClassic, SWT.NONE);
          GridDataFactory.create(composite).grabH().fillH();
          IntegerFieldEditor sensitivity =
              new IntegerFieldEditor(PREF_SNAP_SENS, "Snap sensitivity", composite);
          addField(sensitivity);
        }
        {
          Composite composite = new Composite(m_prefsClassic, SWT.NONE);
          GridDataFactory.create(composite).grabH().fillH();
          BooleanFieldEditor keepAttachments =
              new BooleanFieldEditor(PREF_KEEP_ATTACHMENTS_STYLE,
                  "Keep attachments style in align operations",
                  composite);
          addField(keepAttachments);
        }
      }
    }
    return container;
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (m_modeEditor == event.getSource()) {
      if (VAL_FORMLAYOUT_MODE_CLASSIC.equals(event.getNewValue())) {
        m_stackLayout.topControl = m_prefsClassic;
      } else {
        m_stackLayout.topControl = m_prefsAutomatic;
      }
      m_details.layout(true);
      return;
    }
    super.propertyChange(event);
  }

  @Override
  protected void performDefaults() {
    String mode = getPreferenceStore().getDefaultString(PREF_FORMLAYOUT_MODE);
    propertyChange(new PropertyChangeEvent(m_modeEditor, PREF_FORMLAYOUT_MODE, null, mode));
    if (m_horizontalPercentsGroup != null) {
      m_horizontalPercentsGroup.loadDefaults();
    }
    if (m_verticalPercentsGroup != null) {
      m_verticalPercentsGroup.loadDefaults();
    }
    super.performDefaults();
  }

  @Override
  public boolean performOk() {
    if (m_horizontalPercentsGroup != null) {
      m_horizontalPercentsGroup.store();
    }
    if (m_verticalPercentsGroup != null) {
      m_verticalPercentsGroup.store();
    }
    return super.performOk();
  }

  /**
   * Helper method Create {@link #BooleanFieldEditor} within own composite on specified parent
   */
  private ComboFieldEditor createComboFieldEditor(final String key,
      final String text,
      final Composite parent) {
    final Composite composite = new Composite(parent, SWT.NONE);
    ComboFieldEditor editor =
        new ComboFieldEditor(key, text, new String[][]{
            {"Automatic placement", VAL_FORMLAYOUT_MODE_AUTO},
            {"Classic", VAL_FORMLAYOUT_MODE_CLASSIC}}, composite);
    addField(editor);
    return editor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Percents (classic)
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class PercentsGroup extends Group {
    private Set<Integer> m_percentsSet;
    private final ListViewer m_listViewer;
    private final String m_keyPercents;

    public PercentsGroup(Composite parent,
        String title,
        String keyMargin,
        String keyPercent,
        String keyWidget,
        String keyPercents) {
      super(parent, SWT.NONE);
      m_keyPercents = keyPercents;
      GridDataFactory.create(this).grab().fill();
      setText(title);
      {
        addField(new IntegerFieldEditor(keyMargin, "Window margin", this));
        addField(new IntegerFieldEditor(keyPercent, "Percent offset", this));
        addField(new IntegerFieldEditor(keyWidget, "Widget offset", this));
        {
          Group percentsGroup = new Group(this, SWT.NONE);
          GridLayoutFactory.create(percentsGroup).columns(2);
          GridDataFactory.create(percentsGroup).spanH(2).grab().fill();
          percentsGroup.setText("Default percentage points");
          {
            m_listViewer = new ListViewer(percentsGroup, SWT.FULL_SELECTION);
            GridDataFactory.create(m_listViewer.getControl()).grab().fill();
            m_listViewer.setContentProvider(new IStructuredContentProvider() {
              public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
              }

              public void dispose() {
              }

              public Object[] getElements(Object inputElement) {
                ensureLoadPreferences();
                return m_percentsSet.toArray();
              }
            });
            m_listViewer.setInput(new Object());
            // toolbar
            ToolBar toolBar = new ToolBar(percentsGroup, SWT.VERTICAL | SWT.FLAT);
            GridDataFactory.create(toolBar).alignVT();
            // tool buttons
            ToolItem itemAdd = new ToolItem(toolBar, SWT.NONE);
            itemAdd.setImage(m_addImage);
            itemAdd.addSelectionListener(new SelectionAdapter() {
              @Override
              public void widgetSelected(SelectionEvent e) {
                InputDialog dialog =
                    new InputDialog(getShell(),
                        "New Percentage Offset",
                        "Enter new percentage offset value",
                        null,
                        new IInputValidator() {
                          public String isValid(String newText) {
                            try {
                              int value = Integer.parseInt(newText);
                              if (value >= 0 && value <= 100) {
                                return null;
                              }
                            } catch (Throwable e) {
                              // ignore
                            }
                            return "Please enter a number in 0..100 range.";
                          }
                        });
                if (dialog.open() == Window.OK) {
                  m_percentsSet.add(Integer.parseInt(dialog.getValue()));
                  m_listViewer.refresh();
                }
              }
            });
            final ToolItem itemRemove = new ToolItem(toolBar, SWT.NONE);
            itemRemove.setImage(m_removeImage);
            itemRemove.addSelectionListener(new SelectionAdapter() {
              @Override
              public void widgetSelected(SelectionEvent e) {
                StructuredSelection selection = (StructuredSelection) m_listViewer.getSelection();
                if (selection != null && !selection.isEmpty()) {
                  Iterator<?> I = selection.iterator();
                  while (I.hasNext()) {
                    m_percentsSet.remove(I.next());
                  }
                  m_listViewer.refresh();
                }
              }
            });
            // selection listener
            m_listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
              public void selectionChanged(SelectionChangedEvent event) {
                boolean enabled = event != null && !event.getSelection().isEmpty();
                itemRemove.setEnabled(enabled);
              }
            });
          }
        }
      }
    }

    public void store() {
      StringBuffer buffer = new StringBuffer();
      for (Integer percent : m_percentsSet) {
        buffer.append(percent);
        buffer.append(" ");
      }
      buffer.delete(buffer.length() - 1, buffer.length());
      getPreferenceStore().setValue(m_keyPercents, buffer.toString());
    }

    public void loadDefaults() {
      String percents = getPreferenceStore().getDefaultString(m_keyPercents);
      m_percentsSet =
          (Set<Integer>) FormLayoutPreferences.fillPercents(percents, Sets.<Integer>newTreeSet());
      m_listViewer.refresh();
    }

    @Override
    protected void checkSubclass() {
    }

    private void ensureLoadPreferences() {
      if (m_percentsSet == null) {
        String percents = getPreferenceStore().getString(m_keyPercents);
        m_percentsSet =
            (Set<Integer>) FormLayoutPreferences.fillPercents(percents, Sets.<Integer>newTreeSet());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //	IWorkbenchPreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench) {
  }
}
