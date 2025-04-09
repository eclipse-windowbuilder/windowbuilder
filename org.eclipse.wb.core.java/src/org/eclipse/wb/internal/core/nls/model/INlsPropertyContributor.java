/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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
