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
package org.eclipse.wb.internal.core.databinding.model;

import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;

/**
 * Decorator provider.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public interface IObserveDecoration {
  /**
   * @return {@link IObserveDecorator} for visual decorate this object.
   */
  IObserveDecorator getDecorator();
}