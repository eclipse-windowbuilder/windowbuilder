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
package org.eclipse.wb.internal.core.model.util.generic;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * This helper allows to create top-level {@link Property} that as wrapper for some
 * {@link ObjectInfo} method.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class ModelMethodPropertySupport extends ModelMethodPropertyAbstractSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Installation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Configures given {@link ObjectInfo} to create properties according parameters in description.
	 */
	public static void install(ObjectInfo object, String prefix) {
		new ModelMethodPropertySupport(object, prefix).install();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ModelMethodPropertySupport(ObjectInfo object, String prefix) {
		super(object, prefix);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ParameterProcessor createParameterProcessor() {
		return new ParameterProcessor() {
			@Override
			protected void processGetterSignature() {
				getterSignature += "()";
			}

			@Override
			protected void processSetterSignature() {
				setterSignature += "(java.lang.Object)";
			}

			@Override
			protected void configureProperty() {
				new PropertyProcessor() {
					@Override
					protected boolean isPropertyTarget(ObjectInfo target) {
						return target == object;
					}

					@Override
					protected Object getValue(ObjectInfo target) throws Exception {
						return getter.invoke(object);
					}

					@Override
					protected void setValue(ObjectInfo target, Object value) throws Exception {
						setter.invoke(object, value);
					}
				};
			}
		};
	}
}
