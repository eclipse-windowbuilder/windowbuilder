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

public class SelectionUpdateValueStrategy extends UpdateValueStrategy<Integer, Boolean> {
	public SelectionUpdateValueStrategy() {
		setConverter(new Converter<>(Integer.class, Boolean.class) {
			@Override
			public Boolean convert(Integer fromObject) {
				return fromObject != -1;
			}
		});
	}
}