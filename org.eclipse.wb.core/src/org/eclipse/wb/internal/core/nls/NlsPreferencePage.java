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
package org.eclipse.wb.internal.core.nls;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.preferences.bind.AbstractBindingPreferencesPage;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * {@link PreferencePage} with preferences for GWT builder.
 * 
 * @author scheglov_ke
 * @coverage core.nls
 */
public abstract class NlsPreferencePage extends AbstractBindingPreferencesPage
    implements
      IPreferenceConstants {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NlsPreferencePage(ToolkitDescription toolkit) {
    super(toolkit);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractBindingComposite createBindingComposite(Composite parent) {
    return new ContentsComposite(parent, m_bindManager, m_preferences);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  private class ContentsComposite extends AbstractBindingComposite {
    public ContentsComposite(Composite parent,
        DataBindManager bindManager,
        IPreferenceStore preferences) {
      super(parent, bindManager, preferences);
      GridLayoutFactory.create(this).columns(2).noMargins();
      checkButton(this, 2, Messages.NlsPreferencePage_autoExternalize, P_NLS_AUTO_EXTERNALIZE);
      checkButton(
          this,
          2,
          Messages.NlsPreferencePage_keyHasQualifiedClassName,
          P_NLS_KEY_QUALIFIED_TYPE_NAME);
      stringField(this, 2, Messages.NlsPreferencePage_keyInValuePrefix, P_NLS_KEY_AS_VALUE_PREFIX);
      {
        Control[] controls =
            stringField(
                this,
                2,
                Messages.NlsPreferencePage_alwaysVisibleLocales,
                P_NLS_ALWAYS_VISIBLE_LOCALES);
        Control labelWidget = controls[0];
        labelWidget.setToolTipText(Messages.NlsPreferencePage_alwaysVisibleLocalesHint);
      }
    }
  }
}
