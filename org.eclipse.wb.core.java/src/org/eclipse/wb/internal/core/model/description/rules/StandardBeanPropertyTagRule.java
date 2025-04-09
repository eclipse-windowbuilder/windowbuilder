/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.core.model.description.rules;

import org.eclipse.wb.core.databinding.xsd.component.Component.PropertyTag;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;

import org.apache.commons.lang3.function.FailableBiConsumer;

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
