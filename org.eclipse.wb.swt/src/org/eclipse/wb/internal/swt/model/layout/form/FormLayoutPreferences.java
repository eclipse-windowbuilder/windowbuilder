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
package org.eclipse.wb.internal.swt.model.layout.form;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class maintaining FromLayout support preferences (both Classic & Auto).
 *
 * @author mitin_aa
 * @coverage swt.model.layout.form
 */
public final class FormLayoutPreferences<C extends IControlInfo> {
  private static final String PREFERENCE_CHANGE_LISTENER_KEY =
      "FormLayout.preferenceChangeListener";
  private final IFormLayoutInfo<C> m_layout;
  private final ObjectInfo m_layoutModel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormLayoutPreferences(IFormLayoutInfo<C> layout, ToolkitDescription toolkit) {
    m_layout = layout;
    m_preferences = toolkit.getPreferences();
    m_layoutModel = layout.getUnderlyingModel();
    m_layoutModel.addBroadcastListener(new ObjectInfoTreeComplete() {
      public void invoke() throws Exception {
        addPropertyChangeListener();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPropertyChangeListener management
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link IPropertyChangeListener}, only one time and using root {@link ObjectInfo}.
   */
  public void addPropertyChangeListener() {
    ObjectInfo rooObject = m_layoutModel.getRoot();
    if (rooObject.getArbitraryValue(PREFERENCE_CHANGE_LISTENER_KEY) == null) {
      final IPropertyChangeListener listener = new PreferenceChangeListener();
      // add listener
      getPreferenceStore().addPropertyChangeListener(listener);
      rooObject.putArbitraryValue(PREFERENCE_CHANGE_LISTENER_KEY, listener);
      // schedule removing listener on dispose()
      rooObject.addBroadcastListener(new ObjectEventListener() {
        @Override
        public void dispose() throws Exception {
          getPreferenceStore().removePropertyChangeListener(listener);
          m_layoutModel.getRoot().putArbitraryValue(PREFERENCE_CHANGE_LISTENER_KEY, null);
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Settings
  //
  ////////////////////////////////////////////////////////////////////////////
  public IPreferenceStore getPreferenceStore() {
    return m_preferences;
  }

  public List<Integer> getVerticalPercents() {
    return new ArrayList<>(loadPercents(KEY_V_PERCENTS));
  }

  public List<Integer> getHorizontalPercents() {
    return new ArrayList<>(loadPercents(KEY_H_PERCENTS));
  }

  public int getSnapSensitivity() {
    return getPreferenceStore().getInt(IPreferenceConstants.PREF_SNAP_SENS);
  }

  public int getHorizontalContainerGap() {
    return getPreferenceStore().getInt(IPreferenceConstants.PREF_H_WINDOW_MARGIN);
  }

  public int getVerticalContainerGap() {
    return getPreferenceStore().getInt(IPreferenceConstants.PREF_V_WINDOW_MARGIN);
  }

  public int getHorizontalPercentsGap() {
    return getPreferenceStore().getInt(IPreferenceConstants.PREF_H_PERCENT_OFFSET);
  }

  public int getVerticalPercentsGap() {
    return getPreferenceStore().getInt(IPreferenceConstants.PREF_V_PERCENT_OFFSET);
  }

  public int getHorizontalComponentGap() {
    return getPreferenceStore().getInt(IPreferenceConstants.PREF_H_WIDGET_OFFSET);
  }

  public int getVerticalComponentGap() {
    return getPreferenceStore().getInt(IPreferenceConstants.PREF_V_WIDGET_OFFSET);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Manage current layout percent offsets
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final QualifiedName KEY_H_PERCENTS =
      new QualifiedName(Activator.PLUGIN_ID, IPreferenceConstants.PREF_H_PERCENTS);
  private static final QualifiedName KEY_V_PERCENTS =
      new QualifiedName(Activator.PLUGIN_ID, IPreferenceConstants.PREF_V_PERCENTS);
  private final IPreferenceStore m_preferences;

  /**
   * Stores given percent value into underlying resource.
   */
  public void addPercent(int percent, boolean isHorizontal) {
    QualifiedName keyPercents = isHorizontal ? KEY_H_PERCENTS : KEY_V_PERCENTS;
    Set<Integer> percents = loadPercents(keyPercents);
    percents.add(percent);
    savePercents(keyPercents, percents);
  }

  /**
   * Removes given percent value from underlying resource.
   */
  public void removePercent(int percent, boolean isHorizontal) {
    QualifiedName keyPercents = isHorizontal ? KEY_H_PERCENTS : KEY_V_PERCENTS;
    Set<Integer> percents = loadPercents(keyPercents);
    percents.remove(percent);
    savePercents(keyPercents, percents);
  }

  /**
   * Restores default percent values from settings.
   */
  public void defaultPercents(boolean isHorizontal) {
    QualifiedName keyPercents = isHorizontal ? KEY_H_PERCENTS : KEY_V_PERCENTS;
    setPersistentProperty(keyPercents, "");
  }

  /**
   * Use currently defined percents as default percents in preferences.
   */
  public void setAsDefaultPercents(boolean isHorizontal) {
    QualifiedName keyPercents = isHorizontal ? KEY_H_PERCENTS : KEY_V_PERCENTS;
    Set<Integer> percents = loadPercents(keyPercents);
    setPersistentProperty(keyPercents, "");
    getPreferenceStore().setValue(keyPercents.getLocalName(), getPercentsString(percents));
  }

  /**
   * Does save into persistence.
   */
  private void savePercents(final QualifiedName keyPercents, Set<Integer> percents) {
    setPersistentProperty(keyPercents, getPercentsString(percents));
  }

  /**
   * Returns space-separated string of percent values to be stored.
   */
  private String getPercentsString(Collection<Integer> percents) {
    final StringBuffer buffer = new StringBuffer();
    for (Integer percent : percents) {
      buffer.append(percent);
      buffer.append(" ");
    }
    buffer.delete(buffer.length() - 1, buffer.length());
    return buffer.toString();
  }

  /**
   * Loads the percent values into TreeSet of integers, these integers are sorted automatically in
   * TreeSet. First, tries to load percents from CU persistence then, if none found, loads from
   * preference store.
   */
  private Set<Integer> loadPercents(final QualifiedName keyPercents) {
    final Set<Integer> values = new TreeSet<>();
    // load persistence
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        fillPercents(getPersistentProperty(keyPercents), values);
      }
    });
    // if no per-file defined values found, load preferences
    if (values.isEmpty()) {
      fillPercents(getPreferenceStore().getString(keyPercents.getLocalName()), values);
    }
    return values;
  }

  /**
   * Stores the percent values into underlying resource of this CU.
   */
  private void setPersistentProperty(final QualifiedName key, final String value) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        IResource resource = m_layout.getUnderlyingResource();
        resource.setPersistentProperty(key, value);
      }
    });
  }

  /**
   * Load the percent values from underlying resource of this CU.
   */
  private String getPersistentProperty(QualifiedName key) throws Exception {
    IResource resource = m_layout.getUnderlyingResource();
    return resource.getPersistentProperty(key);
  }

  /**
   * Parses the whitespace-separated integers string and fills the input collection of integer.
   * Returns the filled collection. Doesn't do anything if input string is empty.
   */
  public static Collection<Integer> fillPercents(String percents,
      Collection<Integer> percentsCollection) {
    if (!StringUtils.isEmpty(percents)) {
      String[] splitted = StringUtils.split(percents);
      for (String percentString : splitted) {
        try {
          int percent = Integer.parseInt(percentString);
          percentsCollection.add(percent);
        } catch (Throwable e) {
          // ignore
        }
      }
    }
    return percentsCollection;
  }

  public boolean useClassic() {
    IPreferenceStore preferences = getPreferenceStore();
    String value = preferences.getString(IPreferenceConstants.PREF_FORMLAYOUT_MODE);
    return IPreferenceConstants.VAL_FORMLAYOUT_MODE_CLASSIC.equals(value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner classes
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class PreferenceChangeListener implements IPropertyChangeListener {
    public void propertyChange(final PropertyChangeEvent event) {
      if (IPreferenceConstants.PREF_FORMLAYOUT_MODE.equals(event.getProperty())) {
        ExecutionUtils.runAsync(new RunnableEx() {
          public void run() throws Exception {
            IDesignPageSite.Helper.getSite(m_layoutModel).reparse();
          }
        });
      }
    }
  }
  /**
   * Model for percents header edit part. Just Integer can't be used because equals int values are
   * equals Integer values.
   *
   * @author mitin_aa
   */
  public static final class PercentsInfo {
    public final int value;

    public PercentsInfo(Integer percent) {
      value = percent;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }
}
