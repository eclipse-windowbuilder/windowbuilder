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

import org.eclipse.wb.core.databinding.xsd.component.Component.PropertiesPreferred;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper.FailableBiConsumer;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;

/**
 * The {@link FailableBiConsumer} that sets {@link PropertyCategory#PREFERRED}
 * for standard bean properties.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class StandardBeanPropertiesPreferredRule extends StandardBeanPropertiesFlaggedRule {
	@Override
	protected void configure(GenericPropertyDescription propertyDescription) {
		propertyDescription.setCategory(PropertyCategory.PREFERRED);
	}

	@Override
	protected String getNames(Object properties) {
		return ((PropertiesPreferred) properties).getNames();
	}
}
