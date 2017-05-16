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
package org.eclipse.wb.internal.core.model.description.resource;

/**
 * {@link ResourceInfo} for some component {@link Class}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ClassResourceInfo {
  public final Class<?> clazz;
  public final ResourceInfo resource;

  public ClassResourceInfo(Class<?> clazz, ResourceInfo resource) {
    this.clazz = clazz;
    this.resource = resource;
  }
}