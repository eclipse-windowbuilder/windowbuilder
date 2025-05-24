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
package convert;

import java.util.Currency;

import org.eclipse.core.databinding.conversion.Converter;

/**
 * @author lobas_av
 *
 */
public class ConverterStringToCurrency extends Converter<String, Currency> {

	public ConverterStringToCurrency() {
		super(String.class, Currency.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IConverter
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public Currency convert(String fromObject) {
		try {
			return Currency.getInstance(fromObject.toUpperCase());
		} catch (Throwable e) {
			return null;
		}
	}
}