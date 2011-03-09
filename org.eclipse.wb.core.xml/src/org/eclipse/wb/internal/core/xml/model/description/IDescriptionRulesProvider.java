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

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;

/**
 * Provider for contributing {@link Rule}s for loading {@link ComponentDescription}.
 * 
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public interface IDescriptionRulesProvider {
  void addRules(Digester digester, EditorContext context, Class<?> componentClass);
}
