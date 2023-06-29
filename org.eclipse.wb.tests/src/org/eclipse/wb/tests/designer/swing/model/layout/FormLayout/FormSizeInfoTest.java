/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import org.eclipse.wb.internal.swing.FormLayout.model.FormSizeConstantInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormSizeInfo;
import org.eclipse.wb.internal.swing.laf.LafSupport;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.DblValue;
import org.eclipse.wb.tests.designer.Expectations.IntValue;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.ConstantSize.Unit;
import com.jgoodies.forms.layout.Size;
import com.jgoodies.forms.layout.Sizes;

/**
 * Test for {@link FormSizeInfo}.
 *
 * @author scheglov_ke
 */
public class FormSizeInfoTest extends AbstractFormLayoutTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		LafSupport.applySelectedLAF(LafSupport.getDefaultLAF());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FormSizeConstantInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link FormSizeConstantInfo}.
	 */
	public void DISABLE_test_FormSizeConstantInfo() throws Exception {
		FormSizeConstantInfo size = new FormSizeConstantInfo(25, ConstantSize.PIXEL);
		assertEquals(25.0, size.getValue(), 0.001);
		assertEquals(25, size.getAsPixels());
		assertSame(ConstantSize.PIXEL, size.getUnit());
		// source
		{
			assertEquals("25px", size.getSource(true, true));
			assertEquals(
					"com.jgoodies.forms.layout.Sizes.constant(\"25px\", true)",
					size.getSource(false, true));
			assertEquals(
					"com.jgoodies.forms.layout.Sizes.constant(\"25px\", false)",
					size.getSource(false, false));
		}
		// value
		{
			Size sizeValue = size.getSize(true);
			assertEquals(Sizes.constant("25px", true), sizeValue);
		}
		// modify value
		{
			// double
			size.setValue(20.0);
			assertEquals("20px", size.getSource(true, true));
			// String
			{
				size.setValueString("bad string - ignored");
				assertEquals("20px", size.getSource(true, true));
				//
				size.setValueString("18");
				assertEquals("18px", size.getSource(true, true));
			}
			// pixels
			size.setAsPixels(40);
			assertEquals("40px", size.getSource(true, true));
		}
		// modify unit - keep in mind, that it can not be absolutely precise
		{
			// no change - pixels
			size.setUnit(ConstantSize.PIXEL);
			assertEquals(40, size.getValue(), 0.001);
			// to millimeters
			size.setUnit(ConstantSize.MILLIMETER);
			assertEquals(
					Expectations.get(10.7, new DblValue[]{
							new DblValue("kosta-home", 8.5),
							new DblValue("scheglov-win", 10.7)}),
					size.getValue(),
					0.001);
		}
	}

	/**
	 * Test for {@link FormSizeConstantInfo} conversion special cases.
	 */
	public void test_FormSizeConstantInfo_convertSpecial() throws Exception {
		{
			FormSizeConstantInfo size = new FormSizeConstantInfo(1.0, ConstantSize.CENTIMETER);
			size.setUnit(ConstantSize.MILLIMETER);
			assertEquals("10mm", size.getSource(true, true));
		}
		{
			FormSizeConstantInfo size = new FormSizeConstantInfo(23.0, ConstantSize.MILLIMETER);
			size.setUnit(ConstantSize.CENTIMETER);
			assertEquals("2.3cm", size.getSource(true, true));
		}
	}

	/**
	 * Test for {@link FormSizeConstantInfo#convertFromPixels(int, Unit)}
	 */
	public void DISABLE_test_FormSizeConstantInfo_convertFromPixels() throws Exception {
		{
			double expected = 50.0;
			check_convertFromPixels(50, ConstantSize.PIXEL, expected);
		}
		{
			double expected =
					Expectations.get(39.0, new DblValue[]{
							new DblValue("kosta-home", 31.0),
							new DblValue("scheglov-win", 38.0)});
			check_convertFromPixels(50, ConstantSize.POINT, expected);
		}
		{
			double expected =
					Expectations.get(34.0, new DblValue[]{
							new DblValue("kosta-home", 26.0),
							new DblValue("scheglov-win", 34.0)});
			check_convertFromPixels(50, ConstantSize.DIALOG_UNITS_X, expected);
		}
		{
			double expected =
					Expectations.get(34.0, new DblValue[]{
							new DblValue("kosta-home", 32.0),
							new DblValue("scheglov-win", 34.0)});
			check_convertFromPixels(50, ConstantSize.DIALOG_UNITS_Y, expected);
		}
		{
			double expected =
					Expectations.get(13.4, new DblValue[]{
							new DblValue("kosta-home", 10.7),
							new DblValue("scheglov-win", 13.4)});
			check_convertFromPixels(50, ConstantSize.MILLIMETER, expected);
		}
		{
			double expected =
					Expectations.get(1.3, new DblValue[]{
							new DblValue("kosta-home", 1.1),
							new DblValue("scheglov-win", 1.3)});
			check_convertFromPixels(50, ConstantSize.CENTIMETER, expected);
		}
		{
			double expected =
					Expectations.get(0.5, new DblValue[]{
							new DblValue("kosta-home", 0.4),
							new DblValue("scheglov-win", 0.5)});
			check_convertFromPixels(50, ConstantSize.INCH, expected);
		}
	}

	private void check_convertFromPixels(int pixels, Unit unit, double expected) throws Exception {
		assertEquals(expected, FormSizeConstantInfo.convertFromPixels(pixels, unit), 0.001);
	}

	/**
	 * Test for {@link FormSizeConstantInfo#convertToPixels(double, Unit)}
	 */
	public void DISABLE_test_FormSizeConstantInfo_convertToPixels() throws Exception {
		{
			int expected = 10;
			check_convertToPixels(10.0, ConstantSize.PIXEL, expected);
		}
		{
			int expected =
					Expectations.get(13, new IntValue[]{
							new IntValue("kosta-home", 16),
							new IntValue("scheglov-win", 13)});
			check_convertToPixels(10.0, ConstantSize.POINT, expected);
		}
		{
			int expected =
					Expectations.get(15, new IntValue[]{
							new IntValue("kosta-home", 20),
							new IntValue("scheglov-win", 15)});
			check_convertToPixels(10.0, ConstantSize.DIALOG_UNITS_X, expected);
		}
		{
			int expected =
					Expectations.get(15, new IntValue[]{
							new IntValue("kosta-home", 16),
							new IntValue("scheglov-win", 15)});
			check_convertToPixels(10.0, ConstantSize.DIALOG_UNITS_Y, expected);
		}
		{
			int expected =
					Expectations.get(38, new IntValue[]{
							new IntValue("kosta-home", 47),
							new IntValue("scheglov-win", 38)});
			check_convertToPixels(10.0, ConstantSize.MILLIMETER, expected);
		}
		{
			int expected =
					Expectations.get(378, new IntValue[]{
							new IntValue("kosta-home", 472),
							new IntValue("scheglov-win", 378)});
			check_convertToPixels(10.0, ConstantSize.CENTIMETER, expected);
		}
		{
			int expected =
					Expectations.get(960, new IntValue[]{
							new IntValue("kosta-home", 1200),
							new IntValue("scheglov-win", 960)});
			check_convertToPixels(10.0, ConstantSize.INCH, expected);
		}
	}

	private void check_convertToPixels(double value, Unit unit, int expected) throws Exception {
		assertEquals(expected, FormSizeConstantInfo.convertToPixels(value, unit));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FormSizeInfo
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_FormSize_constant() throws Exception {
		FormSizeInfo size = new FormSizeInfo(Sizes.constant("25px", true), true);
		assertTrue(size.isString());
		assertEquals("25px", size.getSource());
		assertEquals(Sizes.constant("25px", true), size.getSize());
		assertNull(size.getComponentSize());
		// display
		assertEquals("25px", size.getDisplayString());
		assertEquals("25px", size.toString());
		// lower/upper
		assertFalse(size.hasLowerSize());
		assertFalse(size.hasUpperSize());
		assertNull(size.getLowerSize());
		assertNull(size.getUpperSize());
		// constant
		FormSizeConstantInfo constantSize = size.getConstantSize();
		assertNotNull(constantSize);
		assertEquals(25.0, constantSize.getValue(), 0.001);
		assertSame(ConstantSize.PIXEL, constantSize.getUnit());
	}

	public void test_FormSize_constantSet() throws Exception {
		FormSizeInfo size = new FormSizeInfo(Sizes.DEFAULT, true);
		assertEquals("default", size.getSource());
		assertNotNull(size.getComponentSize());
		assertNull(size.getConstantSize());
		// set constant
		size.setConstantSize(new FormSizeConstantInfo(25, ConstantSize.PIXEL));
		assertNull(size.getComponentSize());
		assertEquals("25px", size.getConstantSize().getSource(true, true));
	}

	public void test_FormSize_component_DEFAULT() throws Exception {
		FormSizeInfo size = new FormSizeInfo(Sizes.DEFAULT, true);
		assertTrue(size.isString());
		assertEquals("default", size.getSource());
		assertSame(Sizes.DEFAULT, size.getComponentSize());
		assertEquals(Sizes.DEFAULT, size.getSize());
		assertNull(size.getConstantSize());
		// lower/upper
		assertFalse(size.hasLowerSize());
		assertFalse(size.hasUpperSize());
		assertNull(size.getLowerSize());
		assertNull(size.getUpperSize());
	}

	public void test_FormSize_component_PREFERRED() throws Exception {
		FormSizeInfo size = new FormSizeInfo(Sizes.PREFERRED, true);
		assertTrue(size.isString());
		assertEquals("pref", size.getSource());
		assertSame(Sizes.PREFERRED, size.getComponentSize());
		assertEquals(Sizes.PREFERRED, size.getSize());
		assertNull(size.getConstantSize());
		// lower/upper
		assertFalse(size.hasLowerSize());
		assertFalse(size.hasUpperSize());
		assertNull(size.getLowerSize());
		assertNull(size.getUpperSize());
	}

	public void test_FormSize_component_MINIMUM() throws Exception {
		FormSizeInfo size = new FormSizeInfo(Sizes.MINIMUM, true);
		assertTrue(size.isString());
		assertEquals("min", size.getSource());
		assertSame(Sizes.MINIMUM, size.getComponentSize());
		assertEquals(Sizes.MINIMUM, size.getSize());
		assertNull(size.getConstantSize());
		// lower/upper
		assertFalse(size.hasLowerSize());
		assertFalse(size.hasUpperSize());
		assertNull(size.getLowerSize());
		assertNull(size.getUpperSize());
	}

	public void test_FormSize_setComponentSize() throws Exception {
		FormSizeInfo size = new FormSizeInfo(Sizes.DEFAULT, true);
		size.setComponentSize(Sizes.MINIMUM);
		assertTrue(size.isString());
		assertEquals("min", size.getSource());
	}

	public void test_FormSize_boundedLower() throws Exception {
		FormSizeInfo size = new FormSizeInfo(ColumnSpec.decode("max(4cm;default)").getSize(), true);
		assertTrue(size.isString());
		assertEquals("max(4cm;default)", size.getSource());
		assertSame(Sizes.DEFAULT, size.getComponentSize());
		assertEquals(Sizes.bounded(Sizes.DEFAULT, Sizes.constant("4cm", true), null), size.getSize());
		assertNull(size.getConstantSize());
		assertNull(size.getUpperSize());
		//
		FormSizeConstantInfo lowerSize = size.getLowerSize();
		assertNotNull(lowerSize);
		assertEquals(4.0, lowerSize.getValue(), 0.001);
		assertSame(ConstantSize.CENTIMETER, lowerSize.getUnit());
	}

	public void test_FormSize_boundedUpper() throws Exception {
		FormSizeInfo size = new FormSizeInfo(ColumnSpec.decode("min(3cm;default)").getSize(), true);
		assertTrue(size.isString());
		assertEquals("min(3cm;default)", size.getSource());
		assertSame(Sizes.DEFAULT, size.getComponentSize());
		assertEquals(Sizes.bounded(Sizes.DEFAULT, null, Sizes.constant("3cm", true)), size.getSize());
		assertNull(size.getConstantSize());
		// lower
		assertFalse(size.hasLowerSize());
		assertNull(size.getLowerSize());
		// upper
		assertTrue(size.hasUpperSize());
		FormSizeConstantInfo upperSize = size.getUpperSize();
		assertNotNull(upperSize);
		assertEquals(3.0, upperSize.getValue(), 0.001);
		assertSame(ConstantSize.CENTIMETER, upperSize.getUnit());
	}

	public void test_FormSize_boundedLowerSet() throws Exception {
		FormSizeInfo size = new FormSizeInfo(Sizes.DEFAULT, true);
		assertTrue(size.isString());
		assertEquals("default", size.getSource());
		// no bounds
		assertFalse(size.hasLowerSize());
		assertFalse(size.hasUpperSize());
		// set lower
		size.setLowerSize(new FormSizeConstantInfo(5.0, ConstantSize.CM));
		assertTrue(size.hasLowerSize());
		assertTrue(size.isString());
		assertEquals("max(5cm;default)", size.getSource());
		// remove "hasLower"
		size.setLowerSize(false);
		assertEquals("default", size.getSource());
	}

	public void test_FormSize_boundedUpperSet() throws Exception {
		FormSizeInfo size = new FormSizeInfo(Sizes.DEFAULT, true);
		assertTrue(size.isString());
		assertEquals("default", size.getSource());
		// no bounds
		assertFalse(size.hasLowerSize());
		assertFalse(size.hasUpperSize());
		// set upper
		size.setUpperSize(new FormSizeConstantInfo(5.0, ConstantSize.CM));
		assertTrue(size.hasUpperSize());
		assertTrue(size.isString());
		assertEquals("min(5cm;default)", size.getSource());
		// remove "hasUpper"
		size.setUpperSize(false);
		assertEquals("default", size.getSource());
	}

	public void test_FormSize_boundedLowerUpper() throws Exception {
		Size expectedSize =
				Sizes.bounded(Sizes.DEFAULT, Sizes.constant("3cm", true), Sizes.constant("40mm", true));
		FormSizeInfo size = new FormSizeInfo(expectedSize, true);
		assertFalse(size.isString());
		assertEquals(
				"com.jgoodies.forms.layout.Sizes.bounded(com.jgoodies.forms.layout.Sizes.DEFAULT, com.jgoodies.forms.layout.Sizes.constant(\"3cm\", true), com.jgoodies.forms.layout.Sizes.constant(\"40mm\", true))",
				size.getSource());
		assertEquals(expectedSize, size.getSize());
		assertNull(size.getConstantSize());
		// lower
		{
			FormSizeConstantInfo lowerSize = size.getLowerSize();
			assertNotNull(lowerSize);
			assertEquals(3.0, lowerSize.getValue(), 0.001);
			assertSame(ConstantSize.CENTIMETER, lowerSize.getUnit());
		}
		// upper
		{
			FormSizeConstantInfo upperSize = size.getUpperSize();
			assertNotNull(upperSize);
			assertEquals(40.0, upperSize.getValue(), 0.001);
			assertSame(ConstantSize.MILLIMETER, upperSize.getUnit());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// bounded source/display tests
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_bounded_getDisplayString() throws Exception {
		check_bounded_getDisplayString(Sizes.DEFAULT, "DEFAULT", "default");
		check_bounded_getDisplayString(Sizes.PREFERRED, "PREFERRED", "preferred");
		check_bounded_getDisplayString(Sizes.MINIMUM, "MINIMUM", "minimum");
	}

	private void check_bounded_getDisplayString(Size componentSize, String source, String display)
			throws Exception {
		Size expectedSize =
				Sizes.bounded(componentSize, Sizes.constant("3cm", true), Sizes.constant("40mm", true));
		FormSizeInfo size = new FormSizeInfo(expectedSize, true);
		assertEquals(expectedSize, size.getSize());
		{
			assertFalse(size.isString());
			assertEquals(
					"com.jgoodies.forms.layout.Sizes.bounded(com.jgoodies.forms.layout.Sizes."
							+ source
							+ ", com.jgoodies.forms.layout.Sizes.constant(\"3cm\", true), com.jgoodies.forms.layout.Sizes.constant(\"40mm\", true))",
							size.getSource());
		}
		assertEquals("3cm<" + display + "<40mm", size.getDisplayString());
	}
}
