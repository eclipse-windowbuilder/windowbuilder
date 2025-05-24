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
 */
public class ConverterCurrencyToSymbolString extends Converter<Currency, String> {

	public ConverterCurrencyToSymbolString() {
		super(Currency.class, String.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IConverter
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public String convert(Currency fromObject) {
		if (fromObject == null) {
			return "???";
		}
		return fromObject.getSymbol();
	}
}