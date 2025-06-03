/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import org.eclipse.wb.internal.swing.FormLayout.model.FormColumnInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormDimensionInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormDimensionTemplate;
import org.eclipse.wb.internal.swing.FormLayout.model.FormRowInfo;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.StrValue;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormSpec.DefaultAlignment;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Size;
import com.jgoodies.forms.layout.Sizes;

import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Test for {@link FormDimensionInfo}.
 *
 * @author scheglov_ke
 */
public class FormDimensionInfoTest extends AbstractFormLayoutTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_accessors() throws Exception {
		FormColumnInfo column = new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC);
		assertEquals("default", column.getDisplayString());
		// check initial state
		assertSame(Sizes.DEFAULT, column.getSize().getSize());
		assertSame(ColumnSpec.DEFAULT, column.getAlignment());
		assertEquals(0.0, column.getWeight(), 0.001);
		assertFalse(column.hasGrow());
		// modify
		column.setAlignment(ColumnSpec.LEFT);
		column.setWeight(0.2);
		assertEquals("left:default:grow(0.2)", column.getDisplayString());
		assertTrue(column.hasGrow());
	}

	@Test
	public void test_equals2() throws Exception {
		FormColumnInfo column = new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC);
		FormColumnInfo column2 = new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC);
		FormColumnInfo column3 = new FormColumnInfo(FormSpecs.PREF_COLSPEC);
		assertTrue(column.equals2(column2));
		assertFalse(column.equals2(column3));
	}

	@Test
	public void test_assign() throws Exception {
		FormColumnInfo target = new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC);
		FormColumnInfo source = new FormColumnInfo(FormSpecs.GROWING_BUTTON_COLSPEC);
		// check initial state
		assertEquals("default", target.getDisplayString());
		assertEquals("50dlu<preferred:grow", source.getDisplayString());
		// assign
		target.assign(source);
		assertEquals("50dlu<preferred:grow", target.getDisplayString());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Source
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link FormDimensionInfo#getSource()}.
	 */
	@Test
	public void test_FormDimensionInfo_sourceParseEncode() throws Exception {
		check_FormDimensionInfo_sourceParse("70dlu", "70dlu");
		check_FormDimensionInfo_sourceParse("70dlu:grow", "70dlu:grow");
		check_FormDimensionInfo_sourceParse("left:70dlu:grow", "left:70dlu:grow");
		check_FormDimensionInfo_sourceParse("right:70dlu", "right:70dlu");
		check_FormDimensionInfo_sourceParse("70dlu:none", "70dlu");
		check_FormDimensionInfo_sourceParse("70dlu:g(0.5)", "70dlu:grow(0.5)");
	}

	/**
	 * Checks that given description can be parsed and generates expected source/display.
	 */
	private void check_FormDimensionInfo_sourceParse(String toParse, String expected)
			throws Exception {
		FormColumnInfo column = new FormColumnInfo(ColumnSpec.decode(toParse));
		assertEquals(
				"com.jgoodies.forms.layout.ColumnSpec.decode(\"" + expected + "\")",
				column.getSource());
		assertEquals(expected, column.getDisplayString());
		assertEquals(column.getDisplayString(), column.toString());
	}

	@Test
	public void test_source_row() throws Exception {
		FormRowInfo row = new FormRowInfo(RowSpec.decode("4cm"));
		assertEquals("com.jgoodies.forms.layout.RowSpec.decode(\"4cm\")", row.getSource());
	}

	@Test
	public void test_source_columnTemplate() throws Exception {
		FormColumnInfo column = new FormColumnInfo(FormSpecs.GLUE_COLSPEC);
		assertEquals("com.jgoodies.forms.layout.FormSpecs.GLUE_COLSPEC", column.getSource());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// source: bounded
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_sourceBounded_column() throws Exception {
		check_getAlignmentSource(true, ColumnSpec.LEFT, "com.jgoodies.forms.layout.ColumnSpec.LEFT");
		check_getAlignmentSource(true, ColumnSpec.CENTER, "com.jgoodies.forms.layout.ColumnSpec.CENTER");
		check_getAlignmentSource(true, ColumnSpec.RIGHT, "com.jgoodies.forms.layout.ColumnSpec.RIGHT");
		check_getAlignmentSource(true, ColumnSpec.FILL, "com.jgoodies.forms.layout.ColumnSpec.FILL");
	}

	@Test
	public void test_sourceBounded_row() throws Exception {
		check_getAlignmentSource(false, RowSpec.TOP, "com.jgoodies.forms.layout.RowSpec.TOP");
		check_getAlignmentSource(false, RowSpec.CENTER, "com.jgoodies.forms.layout.RowSpec.CENTER");
		check_getAlignmentSource(false, RowSpec.BOTTOM, "com.jgoodies.forms.layout.RowSpec.BOTTOM");
		check_getAlignmentSource(false, RowSpec.FILL, "com.jgoodies.forms.layout.RowSpec.FILL");
	}

	public void check_getAlignmentSource(boolean horizontal,
			DefaultAlignment alignment,
			String alignmentName) throws Exception {
		Method method =
				FormDimensionInfo.class.getDeclaredMethod("getAlignmentSource", new Class[]{
						boolean.class,
						DefaultAlignment.class});
		method.setAccessible(true);
		assertEquals(
				alignmentName,
				method.invoke(null, new Object[]{Boolean.valueOf(horizontal), alignment}));
	}

	@Test
	public void test_sourceBounded() throws Exception {
		Size size =
				Sizes.bounded(Sizes.DEFAULT, Sizes.constant("3cm", true), Sizes.constant("40mm", true));
		FormColumnInfo column = new FormColumnInfo(new ColumnSpec(ColumnSpec.LEFT, size, 0.0));
		assertEquals(
				"new com.jgoodies.forms.layout.ColumnSpec(com.jgoodies.forms.layout.ColumnSpec.LEFT, com.jgoodies.forms.layout.Sizes.bounded(com.jgoodies.forms.layout.Sizes.DEFAULT, com.jgoodies.forms.layout.Sizes.constant(\"3cm\", true), com.jgoodies.forms.layout.Sizes.constant(\"40mm\", true)), 0)",
				column.getSource());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ToolTip
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_toolTip() throws Exception {
		assertEquals("default", new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC).getToolTip());
		assertEquals("minimum", new FormColumnInfo(FormSpecs.MIN_COLSPEC).getToolTip());
		assertEquals(
				"100dlu<preferred",
				new FormColumnInfo(ColumnSpec.decode("max(100dlu;pref)")).getToolTip());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Copy
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_copyColumn() throws Exception {
		FormColumnInfo column = new FormColumnInfo(ColumnSpec.decode("min(100dlu;default)"));
		assertEquals("default<100dlu", column.copy().getToolTip());
	}

	@Test
	public void test_copyRow() throws Exception {
		FormRowInfo row = new FormRowInfo(RowSpec.decode("min(100dlu;default)"));
		assertEquals("default<100dlu", row.copy().getToolTip());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Templates
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_columnTemplates() throws Exception {
		FormColumnInfo column = new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC);
		// similar templates - components because column is component
		{
			FormDimensionTemplate[] templates = column.getTemplates(true);
			assertEquals(5, templates.length);
			// check for DEFAULT template
			assertTrue(column.isTemplate(templates[0]));
			// check last template
			FormDimensionTemplate template = templates[4];
			assertEquals("GROWING_BUTTON_COLSPEC", template.getFieldName());
			assertTrue(template.isComponent());
			assertEquals("growing button", template.getTitle());
			assertNotNull(template.getIcon());
		}
		// dis-similar templates - gaps
		{
			FormDimensionTemplate[] templates = column.getTemplates(false);
			assertEquals(4, templates.length);
			// check last template
			FormDimensionTemplate template = templates[3];
			assertEquals("LABEL_COMPONENT_GAP_COLSPEC", template.getFieldName());
			assertFalse(template.isComponent());
		}
	}

	@Test
	public void test_columnTemplates2() throws Exception {
		FormColumnInfo column = new FormColumnInfo(FormSpecs.RELATED_GAP_COLSPEC);
		// similar templates - gaps, becuase column is gap
		{
			FormDimensionTemplate[] templates = column.getTemplates(true);
			assertEquals(4, templates.length);
			//
			FormDimensionTemplate template = templates[3];
			assertEquals("LABEL_COMPONENT_GAP_COLSPEC", template.getFieldName());
			assertFalse(template.isComponent());
		}
		// dis-similar templates
		assertEquals(5, column.getTemplates(false).length);
	}

	@Test
	public void test_columnTemplates_set() throws Exception {
		FormColumnInfo column = new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC);
		assertEquals("default", column.getToolTip());
		//
		column.setTemplate(column.getTemplates(true)[1]);
		assertEquals("preferred", column.getToolTip());
	}

	@Test
	public void test_rowTemplates() throws Exception {
		FormRowInfo row = new FormRowInfo(FormSpecs.DEFAULT_ROWSPEC);
		assertEquals(4, row.getTemplates(true).length);
		assertEquals(6, row.getTemplates(false).length);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Convert to GAP template
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_convertToNearestGap_columns() throws Exception {
		check_convertToNearestGap_column("4px", 5, "LABEL_COMPONENT_GAP_COLSPEC");
		check_convertToNearestGap_column("3px", 0, null);
		check_convertToNearestGap_column("5px", 1, "LABEL_COMPONENT_GAP_COLSPEC");
		{
			String desc =
					Expectations.get("8px", new StrValue[]{
							new StrValue("kosta-home", "10px"),
							new StrValue("scheglov-win", "8px")});
			check_convertToNearestGap_column(desc, 10, "RELATED_GAP_COLSPEC");
		}
		check_convertToNearestGap_column("12px", 10, "UNRELATED_GAP_COLSPEC");
		check_convertToNearestGap_column("20px", 10, "UNRELATED_GAP_COLSPEC");
		check_convertToNearestGap_column("20px", 5, null);
	}

	@Test
	public void test_convertToNearestGap_rows() throws Exception {
		check_convertToNearestGap_row("4px", 5, "LABEL_COMPONENT_GAP_ROWSPEC");
		check_convertToNearestGap_row("5px", 5, "LINE_GAP_ROWSPEC");
		check_convertToNearestGap_row("6px", 5, "RELATED_GAP_ROWSPEC");
	}

	private void check_convertToNearestGap_column(String desc,
			int maxDelta,
			String expectedTemplateField) throws Exception {
		FormColumnInfo dimension = new FormColumnInfo(ColumnSpec.decode(desc));
		check_convertToNearestGap(dimension, maxDelta, expectedTemplateField);
	}

	private void check_convertToNearestGap_row(String desc, int maxDelta, String expectedTemplateField)
			throws Exception {
		FormRowInfo dimension = new FormRowInfo(RowSpec.decode(desc));
		check_convertToNearestGap(dimension, maxDelta, expectedTemplateField);
	}

	private void check_convertToNearestGap(FormDimensionInfo dimension,
			int maxDelta,
			String expectedTemplateField) throws Exception {
		dimension.convertToNearestGap(maxDelta);
		// check
		FormDimensionTemplate template = dimension.getTemplate();
		if (expectedTemplateField == null) {
			assertNull(template);
		} else {
			assertNotNull(template);
			assertEquals(expectedTemplateField, template.getFieldName());
		}
	}
}
