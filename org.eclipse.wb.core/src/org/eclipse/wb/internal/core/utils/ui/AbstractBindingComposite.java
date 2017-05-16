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
package org.eclipse.wb.internal.core.utils.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.binding.IDataEditor;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.CheckButtonEditor;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.ComboSelectionEditor;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.ComboTextEditor;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.TextSingleEditor;
import org.eclipse.wb.internal.core.utils.binding.providers.BooleanPreferenceProvider;
import org.eclipse.wb.internal.core.utils.binding.providers.IntegerPreferenceProvider;
import org.eclipse.wb.internal.core.utils.binding.providers.StringPreferenceProvider;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import java.util.List;
import java.util.Map;

/**
 * {@link Composite} with {@link DataBindManager} for convenient binding {@link Control}'s on
 * preferences.
 *
 * @author scheglov_ke
 * @coverage core.preferences.ui
 */
public abstract class AbstractBindingComposite extends Composite {
  protected final DataBindManager m_bindManager;
  protected final IPreferenceStore m_preferences;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractBindingComposite(Composite parent,
      DataBindManager bindManager,
      IPreferenceStore preferences) {
    super(parent, SWT.NONE);
    m_bindManager = bindManager;
    m_preferences = preferences;
    GridLayoutFactory.create(this);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Listener
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<Runnable> m_updateListeners = Lists.newArrayList();
  /**
   * Adds new {@link Runnable} for update of bounds preferences.
   */
  public final void addUpdateListener(Runnable listener) {
    if (!m_updateListeners.contains(listener)) {
      m_updateListeners.add(listener);
    }
  }
  /**
   * Removes {@link Runnable} for update of bounds preferences.
   */
  public final void removeUpdateListener(Runnable listener) {
    m_updateListeners.remove(listener);
  }
  /**
   * Sends notification that one of the bound preferences was changes.
   */
  private void notifyUpdateListeners() {
    {
      String message = validate0();
      if (m_validationListener != null) {
        m_validationListener.update(message);
      }
      if (message != null) {
        return;
      }
    }
    for (Runnable listener : Lists.newArrayList(m_updateListeners)) {
      listener.run();
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  private IValidationListener m_validationListener;
  /**
   * Listener for validness status.
   */
  public static interface IValidationListener {
    /**
     * @param message
     *          the <code>null</code> if OK, or error message to show.
     */
    void update(String message);
  }
  /**
   * Sets the single {@link IValidationListener}.
   */
  public void setValidationListener(IValidationListener validationListener) {
    m_validationListener = validationListener;
  }
  /**
   * @return the <code>null</code> if OK, or error message to show.
   */
  private String validate0() {
    try {
      return validate();
    } catch (Throwable e) {
      return e.getMessage();
    }
  }
  /**
   * @return the <code>null</code> if OK, or error message to show.
   */
  protected String validate() throws Exception {
    return null;
  }
  /**
   * Forces validation right now, for example to show error directly after opening UI.
   */
  public void updateValidate() {
    notifyUpdateListeners();
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notifies this {@link AbstractBindingComposite} that user commits changes, for example clicks
   * "OK" button.
   *
   * @return <code>true</code> if commit was successful
   */
  public boolean performOk() {
    return true;
  }
  /**
   * Notifies this {@link AbstractBindingComposite} that user wants to rollback to the default
   * state.
   */
  public void performDefaults() {
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Binding
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, IDataEditor> m_booleanEditors = Maps.newTreeMap();
  private final Map<String, IDataEditor> m_stringEditors = Maps.newTreeMap();
  private final Map<String, IDataEditor> m_integerEditors = Maps.newTreeMap();
  /**
   * Shared {@link Listener} used for listening various events in bounds {@link Control}'s.
   */
  private final Listener m_controlModifyListener = new Listener() {
    public void handleEvent(Event event) {
      notifyUpdateListeners();
    }
  };
  /**
   * Binds <code>boolean</code> value using given check {@link Button}.
   *
   * @return the {@link CheckButtonEditor} used for binding.
   */
  protected final CheckButtonEditor bindBoolean(Button button, String key) {
    Assert.isTrue((button.getStyle() & SWT.CHECK) != 0, "Check button expected.");
    // register editor
    CheckButtonEditor editor = new CheckButtonEditor(button);
    m_booleanEditors.put(key, editor);
    // do bind
    m_bindManager.bind(editor, new BooleanPreferenceProvider(m_preferences, key), true);
    button.addListener(SWT.Selection, m_controlModifyListener);
    return editor;
  }
  /**
   * Binds <code>String</code> value using given {@link Text}.
   */
  protected final void bindString(Text text, String key) {
    Assert.isTrue((text.getStyle() & SWT.MULTI) == 0, "Single line Text expected.");
    // register editor
    IDataEditor editor = new TextSingleEditor(text);
    m_stringEditors.put(key, editor);
    // do bind
    m_bindManager.bind(editor, new StringPreferenceProvider(m_preferences, key), true);
    text.addListener(SWT.Modify, m_controlModifyListener);
  }
  /**
   * Binds <code>int</code> value using text of given {@link Text}.
   */
  protected final void bindInteger(Text text, String key) {
    Assert.isTrue((text.getStyle() & SWT.MULTI) == 0, "Single line Text expected.");
    // register editor
    IDataEditor editor = new TextSingleEditor(text);
    m_integerEditors.put(key, editor);
    // do bind
    m_bindManager.bind(editor, new IntegerPreferenceProvider(m_preferences, key), true);
    text.addListener(SWT.Modify, m_controlModifyListener);
  }
  /**
   * Binds <code>integer</code> value of selection in given {@link Combo}.
   */
  protected final void bindSelection(Combo combo, String key) {
    // register editor
    IDataEditor editor = new ComboSelectionEditor(combo);
    m_integerEditors.put(key, editor);
    // do bind
    m_bindManager.bind(editor, new IntegerPreferenceProvider(m_preferences, key), true);
    combo.addListener(SWT.Selection, m_controlModifyListener);
  }
  /**
   * Binds <code>String</code> value using given {@link Combo}.
   */
  protected final void bindString(Combo combo, String key) {
    // register editor
    IDataEditor editor = new ComboTextEditor(combo);
    m_stringEditors.put(key, editor);
    // do bind
    m_bindManager.bind(editor, new StringPreferenceProvider(m_preferences, key), true);
    combo.addListener(SWT.Modify, m_controlModifyListener);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Binding access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the <code>boolean</code> value for given key, selected currently in this
   *         {@link AbstractBindingComposite}.
   */
  public final boolean getBoolean(String key) {
    IDataEditor editor = m_booleanEditors.get(key);
    Assert.isNotNull(editor, "Can not find boolean editor for: " + key);
    return ((Boolean) editor.getValue()).booleanValue();
  }
  /**
   * @return the <code>String</code> value for given key, selected currently in this
   *         {@link AbstractBindingComposite}.
   */
  public final String getString(String key) {
    IDataEditor editor = m_stringEditors.get(key);
    Assert.isNotNull(editor, "Can not find String editor for: " + key);
    return (String) editor.getValue();
  }
  /**
   * @return the <code>int</code> value for given key, selected currently in this
   *         {@link AbstractBindingComposite}.
   */
  public final int getInteger(String key) {
    IDataEditor editor = m_integerEditors.get(key);
    Assert.isNotNull(editor, "Can not find integer editor for: " + key);
    //
    Object value = editor.getValue();
    if (value instanceof Integer) {
      return ((Integer) value).intValue();
    } else {
      return Integer.parseInt(value.toString());
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Binding controls
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates check {@link Button} for editing boolean preference with given key.
   *
   * @param text
   *          the text for {@link Button}
   * @param key
   *          the preference key
   */
  protected final Button checkButton(Composite parent, String text, String key) {
    return checkButton(parent, 1, text, key);
  }
  /**
   * Creates check {@link Button} for editing boolean preference with given key.
   *
   * @param parent
   *          the parent for {@link Button} widget
   * @param horizontalSpan
   *          the span for {@link GridData}
   * @param text
   *          the text for {@link Button}
   * @param key
   *          the preference key
   */
  protected final Button checkButton(Composite parent, int horizontalSpan, String text, String key) {
    Button button = new Button(parent, SWT.CHECK);
    GridDataFactory.create(button).spanH(horizontalSpan);
    button.setText(text);
    bindBoolean(button, key);
    return button;
  }
  /**
   * Creates {@link Text} for editing string value.
   */
  protected final Control[] stringField(Composite parent,
      int horizontalSpan,
      String label,
      String key) {
    Label labelWidget = new Label(parent, SWT.NONE);
    labelWidget.setText(label);
    //
    Text text = new Text(parent, SWT.BORDER);
    GridDataFactory.create(text).spanH(horizontalSpan - 1).fillH();
    bindString(text, key);
    //
    return new Control[]{labelWidget, text};
  }
  /**
   * Creates {@link Text} for editing integer value.
   */
  protected final Text integerField(Composite parent, int horizontalSpan, String label, String key) {
    new Label(parent, SWT.NONE).setText(label);
    Text text = new Text(parent, SWT.BORDER);
    GridDataFactory.create(text).spanH(horizontalSpan - 1).fillH();
    bindInteger(text, key);
    return text;
  }
}