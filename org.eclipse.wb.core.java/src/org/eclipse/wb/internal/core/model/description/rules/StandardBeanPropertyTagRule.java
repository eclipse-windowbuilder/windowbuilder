/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.description.rules;

import org.eclipse.wb.core.databinding.xsd.component.Component.PropertyTag;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that adds some tag for standard bean property,
 * created by {@link StandardBeanPropertiesRule}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class StandardBeanPropertyTagRule
		implements FailableBiConsumer<ComponentDescription, PropertyTag, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(ComponentDescription componentDescription, PropertyTag propertyTag) throws Exception {
		String propertyName = propertyTag.getName();
		// check all properties
		for (GenericPropertyDescription propertyDescription : componentDescription.getProperties()) {
			String id = propertyDescription.getId();
			if (StandardBeanPropertiesFlaggedRule.matchPropertyId(id, propertyName)) {
				String tag = propertyTag.getTag();
				String value = propertyTag.getValue();
				propertyDescription.putTag(tag, value);
			}
		}
	}
}
