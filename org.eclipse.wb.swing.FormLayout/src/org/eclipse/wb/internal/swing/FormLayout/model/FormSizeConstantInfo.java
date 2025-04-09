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
package org.eclipse.wb.internal.swing.FormLayout.model;

import org.eclipse.wb.internal.core.utils.check.Assert;

import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.ConstantSize.Unit;
import com.jgoodies.forms.layout.Size;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.util.DefaultUnitConverter;
import com.jgoodies.forms.util.UnitConverter;

import org.apache.commons.lang3.StringUtils;

import java.awt.Component;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.JFrame;

/**
 * Description for {@link ConstantSize}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public final class FormSizeConstantInfo {
	private double m_value;
	private Unit m_unit;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormSizeConstantInfo(double value, Unit unit) {
		m_value = value;
		m_unit = unit;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Value
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the size value.
	 */
	public double getValue() {
		return m_value;
	}

	/**
	 * @return the size value in pixels.
	 */
	public int getAsPixels() {
		return convertToPixels(m_value, m_unit);
	}

	/**
	 * Sets the size value.
	 */
	public void setValue(double value) {
		m_value = value;
	}

	/**
	 * Sets the size value as {@link String}.
	 */
	public void setValueString(String s) {
		try {
			m_value = FORMAT.parse(s).doubleValue();
		} catch (Throwable e) {
		}
	}

	/**
	 * Sets the size value as pixels (will convert into current {@link Unit}).
	 */
	public void setAsPixels(int pixels) throws Exception {
		m_value = convertFromPixels(pixels, m_unit);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Unit
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Unit}.
	 */
	public Unit getUnit() {
		return m_unit;
	}

	/**
	 * Sets the {@link Unit}.
	 */
	public void setUnit(Unit unit) throws Exception {
		m_value = convertValue(m_value, m_unit, unit);
		m_unit = unit;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	private static final DecimalFormat FORMAT =
			new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.ENGLISH));

	/**
	 * @param asString
	 *          is <code>true</code> if string presentation should be used, and <code>false</code> if
	 *          {@link Sizes} source should be used
	 *
	 * @param horizontal
	 *          is <code>true</code> if source for horizontal size should be generated
	 *
	 * @return the source of this {@link FormSizeConstantInfo}.
	 */
	public String getSource(boolean asString, boolean horizontal) {
		String encoded;
		{
			encoded = FORMAT.format(m_value) + m_unit.abbreviation();
			encoded = StringUtils.replace(encoded, "dluX", "dlu");
			encoded = StringUtils.replace(encoded, "dluY", "dlu");
		}
		// return encoded or source
		if (asString) {
			return encoded;
		} else {
			return "com.jgoodies.forms.layout.Sizes.constant(\"" + encoded + "\", " + horizontal + ")";
		}
	}

	/**
	 * @return the {@link Size} value.
	 */
	public Size getSize(boolean horizontal) {
		return Sizes.constant(getSource(true, horizontal), horizontal);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Units conversion
	//
	////////////////////////////////////////////////////////////////////////////
	static Component m_toolkitComponent = new JFrame();

	/**
	 * @return the value in <code>newUnit</code> converted from <code>oldUnit</code>.
	 */
	private static double convertValue(double value, Unit oldUnit, Unit newUnit) throws Exception {
		// special cases
		if (oldUnit == ConstantSize.CENTIMETER && newUnit == ConstantSize.MILLIMETER) {
			return value * 10.0;
		}
		if (oldUnit == ConstantSize.MILLIMETER && newUnit == ConstantSize.CENTIMETER) {
			return value / 10.0;
		}
		// generic case
		if (oldUnit != newUnit) {
			int pixels = convertToPixels(value, oldUnit);
			return convertFromPixels(pixels, newUnit);
		}
		return value;
	}

	/**
	 * @return the value in pixels for value in given {@link Unit}.
	 */
	public static int convertToPixels(double value, Unit unit) {
		UnitConverter converter = DefaultUnitConverter.getInstance();
		//
		int pixels = 0;
		if (unit == ConstantSize.PIXEL) {
			pixels = (int) value;
		} else if (unit == ConstantSize.POINT) {
			pixels = converter.pointAsPixel((int) value, m_toolkitComponent);
		} else if (unit == ConstantSize.DLUX) {
			pixels = converter.dialogUnitXAsPixel((int) value, m_toolkitComponent);
		} else if (unit == ConstantSize.DLUY) {
			pixels = converter.dialogUnitYAsPixel((int) value, m_toolkitComponent);
		} else if (unit == ConstantSize.MILLIMETER) {
			pixels = converter.millimeterAsPixel(value, m_toolkitComponent);
		} else if (unit == ConstantSize.CENTIMETER) {
			pixels = converter.centimeterAsPixel(value, m_toolkitComponent);
		} else if (unit == ConstantSize.INCH) {
			pixels = converter.inchAsPixel(value, m_toolkitComponent);
		}
		//
		return pixels;
	}

	/**
	 * @return the value in given {@link Unit} for value in pixels.
	 */
	public static double convertFromPixels(int pixels, Unit unit) throws Exception {
		if (unit == ConstantSize.PIXEL) {
			return pixels;
		} else if (unit == ConstantSize.POINT) {
			return convertFromPixelsInt(pixels, "pointAsPixel");
		} else if (unit == ConstantSize.DLUX) {
			return convertFromPixelsInt(pixels, "dialogUnitXAsPixel");
		} else if (unit == ConstantSize.DLUY) {
			return convertFromPixelsInt(pixels, "dialogUnitYAsPixel");
		} else if (unit == ConstantSize.MILLIMETER) {
			return convertFromPixelsDouble(pixels, "millimeterAsPixel");
		} else if (unit == ConstantSize.CENTIMETER) {
			return convertFromPixelsDouble(pixels, "centimeterAsPixel");
		} else {
			Assert.isTrue(unit == ConstantSize.INCH);
			return convertFromPixelsDouble(pixels, "inchAsPixel");
		}
	}

	/**
	 * @return the value in {@link Unit} corresponding to the given <code>methodName</code> and value
	 *         in pixels.
	 */
	private static int convertFromPixelsInt(int pixels, String methodName) throws Exception {
		UnitConverter converter = DefaultUnitConverter.getInstance();
		Method method =
				UnitConverter.class.getMethod(methodName, new Class[]{int.class, Component.class});
		int result = 0;
		while (true) {
			Integer newPixelsValue =
					(Integer) method.invoke(converter, new Object[]{result, m_toolkitComponent});
			int newPixels = newPixelsValue.intValue();
			if (newPixels > pixels) {
				return result;
			}
			result++;
		}
	}

	/**
	 * @return the value in {@link Unit} corresponding to the given <code>methodName</code> and value
	 *         in pixels.
	 */
	private static double convertFromPixelsDouble(int pixels, String methodName) throws Exception {
		UnitConverter converter = DefaultUnitConverter.getInstance();
		Method method =
				UnitConverter.class.getMethod(methodName, new Class[]{double.class, Component.class});
		double result = 0;
		while (true) {
			Integer newPixelsValue = (Integer) method.invoke(
					converter,
					new Object[]{Double.valueOf(result), m_toolkitComponent});
			int newPixels = newPixelsValue.intValue();
			if (newPixels > pixels) {
				return (int) (result * 10) / 10.0;
			}
			result += 0.05;
		}
	}
}
