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
package org.eclipse.wb.internal.core.nls.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;

/**
 * This interface helps in generating base keys for newly externalized properties.
 *
 * We should have separate class because good place for it is {@link AbstractSource}, but we can not
 * use it because when we create add new source, we don't have yet instance of
 * {@link AbstractSource}, we have only instance of {@link IEditableSource}.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public interface IKeyGeneratorStrategy {
  /**
   * @return the base key for given component and property. We use this method during externalizing
   *         property.
   */
  String generateBaseKey(JavaInfo component, GenericProperty property);
}
