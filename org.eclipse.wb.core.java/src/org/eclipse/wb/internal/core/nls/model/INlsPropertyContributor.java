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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import java.util.List;

/**
 * {@link PropertyEditor} may implement this interface to contribute additional {@link Property}s.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public interface INlsPropertyContributor {
  void contributeNlsProperties(Property property, List<Property> properties) throws Exception;
}
