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
package org.eclipse.wb.internal.core.databinding.model.reference;

/**
 * Encapsulate reference on any object. Reference may be change, but {@link #getReference()} any
 * time provide right value.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public interface IReferenceProvider {
  /**
   * @return string reference on hosted object.
   */
  String getReference() throws Exception;
}