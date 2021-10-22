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
package org.eclipse.wb.internal.core.xml.model.description;

import org.eclipse.wb.internal.core.xml.model.EditorContext;

/**
 * Implementations of this interface can process {@link ComponentDescription}'s after loading them.
 *
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public interface IDescriptionProcessor {
  /**
   * Processes given {@link ComponentDescription}.
   */
  void process(EditorContext context, ComponentDescription componentDescription) throws Exception;
}
