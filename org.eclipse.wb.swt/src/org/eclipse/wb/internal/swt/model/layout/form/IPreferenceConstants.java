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

import org.eclipse.swt.layout.FormLayout;

/**
 * Preference constants for {@link FormLayout}.
 *
 * @author mitin_aa
 */
public interface IPreferenceConstants {
  // which FromLayout support to use.
  String PREF_FORMLAYOUT_MODE = "formLayout.mode";
  String VAL_FORMLAYOUT_MODE_AUTO = "auto";
  String VAL_FORMLAYOUT_MODE_CLASSIC = "classic";
  // preferences for classic mode
  String PREF_SNAP_SENS = "formLayout.snap.sensitivity";
  String PREF_V_WINDOW_MARGIN = "formLayout.snap.windowMargin.vertical";
  String PREF_V_PERCENT_OFFSET = "formLayout.snap.percentOffset.vertical";
  String PREF_V_WIDGET_OFFSET = "formLayout.snap.widgetOffset.vertical";
  String PREF_V_PERCENTS = "formLayout.snap.percents.vertical";
  String PREF_H_WINDOW_MARGIN = "formLayout.snap.windowMargin.horizontal";
  String PREF_H_PERCENT_OFFSET = "formLayout.snap.percentOffset.horizontal";
  String PREF_H_WIDGET_OFFSET = "formLayout.snap.widgetOffset.horizontal";
  String PREF_H_PERCENTS = "formLayout.snap.percents.horizontal";
  String PREF_KEEP_ATTACHMENTS_STYLE = "formLayout.keepAttachmentsStyle";
}
