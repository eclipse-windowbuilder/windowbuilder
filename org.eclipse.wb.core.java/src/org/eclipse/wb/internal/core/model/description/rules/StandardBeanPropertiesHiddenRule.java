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

import org.eclipse.wb.core.databinding.xsd.component.Component.PropertiesHidden;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;

import org.apache.commons.lang3.function.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that sets {@link PropertyCategory#HIDDEN} for
 * standard bean properties.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class StandardBeanPropertiesHiddenRule extends StandardBeanPropertiesFlaggedRule {
	@Override
	protected void configure(GenericPropertyDescription propertyDescription) {
		propertyDescription.setCategory(PropertyCategory.HIDDEN);
	}

	@Override
	protected String getNames(Object properties) {
		return ((PropertiesHidden) properties).getNames();
	}
}
