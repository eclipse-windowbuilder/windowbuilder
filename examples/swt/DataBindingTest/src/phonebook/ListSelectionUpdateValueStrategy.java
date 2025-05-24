/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package phonebook;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.Converter;

import phonebook.model.Person;

public class ListSelectionUpdateValueStrategy extends UpdateValueStrategy<Person, Boolean> {
	public ListSelectionUpdateValueStrategy() {
		setConverter(new Converter<>(Person.class, Boolean.class) {
			@Override
			public Boolean convert(Person fromObject) {
				return fromObject != null;
			}
		});
	}
}