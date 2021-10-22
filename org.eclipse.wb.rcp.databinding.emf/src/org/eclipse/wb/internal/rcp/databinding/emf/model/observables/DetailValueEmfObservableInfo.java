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
package org.eclipse.wb.internal.rcp.databinding.emf.model.observables;

import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

/**
 * Model for observable object <code>EMFObservables.observeDetailValue(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public final class DetailValueEmfObservableInfo extends DetailEmfObservableInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DetailValueEmfObservableInfo(ObservableInfo masterObservable,
      PropertiesSupport propertiesSupport) {
    super(masterObservable, propertiesSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPresentationPrefix() {
    return "Value";
  }
}