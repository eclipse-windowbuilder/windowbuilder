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
package org.eclipse.wb.internal.core.editor.actions.errors;

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for displaying error in {@link ErrorsDialog}.
 *
 * @author scheglov_ke
 * @coverage core.editor.action.error
 */
public interface IErrorPage {
  /**
   * Sets the root {@link ObjectInfo}.
   */
  void setRoot(ObjectInfo rootObject);

  /**
   * @return <code>true</code> if page has errors.
   */
  boolean hasErrors();

  /**
   * @return the title of this page.
   */
  String getTitle();

  /**
   * Creates {@link Control} on given parent.
   */
  Control create(Composite parent);
}
