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
package org.eclipse.wb.internal.core.xml.model.description.rules;

import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;

import org.apache.commons.digester3.Rule;
import org.xml.sax.Attributes;

/**
 * {@link Rule} that sets {@link ExpressionAccessor#NO_DEFAULT_VALUE_TAG} for
 * {@link GenericPropertyDescription} properties.
 *
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class PropertiesNoDefaultValueRule extends PropertiesFlagRule {
	@Override
	protected void configure(GenericPropertyDescription propertyDescription, Attributes attributes) {
		propertyDescription.putTag(ExpressionAccessor.NO_DEFAULT_VALUE_TAG, "true");
	}
}
