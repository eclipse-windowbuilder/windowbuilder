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
package org.eclipse.wb.internal.core.databinding.model.presentation;

/**
 * Decorator facade.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public interface IObservePresentationDecorator {
  /**
   * Enable or disable decorate this presentation.
   */
  void setBindingDecorator(int corner) throws Exception;
}