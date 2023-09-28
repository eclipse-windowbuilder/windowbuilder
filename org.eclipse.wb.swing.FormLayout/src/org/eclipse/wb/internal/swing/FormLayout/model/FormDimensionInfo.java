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
package org.eclipse.wb.internal.swing.FormLayout.model;

import org.eclipse.wb.internal.core.utils.check.Assert;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.FormSpec.DefaultAlignment;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Description for {@link FormSpec}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public abstract class FormDimensionInfo {
	private final boolean m_horizontal;
	private FormSizeInfo m_size;
	private DefaultAlignment m_alignment;
	private double m_weight;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormDimensionInfo(FormSpec spec) throws Exception {
		m_horizontal = spec instanceof ColumnSpec;
		setFormSpec(spec);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return getDisplayString();
	}

	/**
	 * Same as {@link #equals(Object)}, but we can not override real {@link #equals(Object)} because
	 * this causes problems with collections. We just need to compare two {@link FormDimensionInfo}.
	 */
	public boolean equals2(Object obj) {
		FormDimensionInfo dimension = (FormDimensionInfo) obj;
		return dimension.m_alignment == m_alignment
				&& dimension.m_size.getSize().equals(m_size.getSize())
				&& dimension.m_weight == m_weight;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Size
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link FormSizeInfo} for this {@link FormDimensionInfo}.
	 */
	public final FormSizeInfo getSize() {
		return m_size;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link DefaultAlignment} for this {@link FormDimensionInfo}.
	 */
	public final DefaultAlignment getAlignment() {
		return m_alignment;
	}

	/**
	 * Sets the {@link DefaultAlignment}.
	 */
	public final void setAlignment(DefaultAlignment alignment) {
		m_alignment = alignment;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Weight
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the resize weight.
	 */
	public final double getWeight() {
		return m_weight;
	}

	/**
	 * Sets the resize weight.
	 */
	public final void setWeight(double weight) {
		m_weight = weight;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this {@link FormDimensionInfo} is gap.
	 */
	public final boolean isGap() {
		Field field = getTemplateField();
		return field != null && field.getName().indexOf("GAP") != -1;
	}

	/**
	 * @return <code>true</code> if this {@link FormDimensionInfo} has grow.
	 */
	public final boolean hasGrow() {
		return m_weight != 0;
	}

	/**
	 * Converts this {@link FormDimensionInfo} (with constant size in pixels) into nearest gap
	 * template with size difference not more than given delta.
	 */
	public void convertToNearestGap(int maxDelta) throws Exception {
		// prepare size of this dimension in pixels
		int thisPixels;
		{
			Assert.isNotNull(m_size.getConstantSize());
			Assert.isTrue(m_size.getConstantSize().getUnit() == ConstantSize.PIXEL);
			thisPixels = (int) m_size.getConstantSize().getValue();
		}
		//
		int minDelta = Integer.MAX_VALUE;
		Field minField = null;
		//
		Field[] fields = FormDimensionUtils.getTemplateFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			// check for type
			if (m_horizontal && !ColumnSpec.class.isAssignableFrom(field.getType())) {
				continue;
			}
			if (!m_horizontal && !RowSpec.class.isAssignableFrom(field.getType())) {
				continue;
			}
			// check for constant size
			int sizeInPixels;
			{
				FormSpec formSpec = (FormSpec) field.get(null);
				if (!(formSpec.getSize() instanceof ConstantSize)) {
					continue;
				}
				if (formSpec.getResizeWeight() != FormSpec.NO_GROW) {
					continue;
				}
				ConstantSize constantSize = (ConstantSize) formSpec.getSize();
				sizeInPixels = constantSize.getPixelSize(FormSizeConstantInfo.m_toolkitComponent);
			}
			// check that size in range
			int delta = Math.abs(sizeInPixels - thisPixels);
			if (delta <= maxDelta && delta < minDelta) {
				minDelta = delta;
				minField = field;
			}
		}
		// set found gap
		if (minField != null) {
			setFormSpec((FormSpec) minField.get(null));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Copy
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the copy of this {@link FormDimensionInfo}.
	 */
	public abstract FormDimensionInfo copy() throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Templates
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the array of all {@link FormDimensionTemplate}'s.
	 */
	public abstract FormDimensionTemplate[] getTemplates();

	/**
	 * @return the array of similar/dissimilar {@link FormDimensionTemplate}'s.
	 */
	public final FormDimensionTemplate[] getTemplates(boolean similar) {
		boolean isComponent = !isGap();
		boolean needComponent = similar ? isComponent : !isComponent;
		List<FormDimensionTemplate> selectedTemplates = new ArrayList<>();
		//
		FormDimensionTemplate[] templates = getTemplates();
		for (int i = 0; i < templates.length; i++) {
			FormDimensionTemplate template = templates[i];
			if (template.isComponent() == needComponent) {
				selectedTemplates.add(template);
			}
		}
		//
		return selectedTemplates.toArray(new FormDimensionTemplate[selectedTemplates.size()]);
	}

	/**
	 * @return the {@link FormDimensionTemplate} if this {@link FormDimensionInfo} is template or
	 *         <code>null</code> if it is not a template.
	 */
	public final FormDimensionTemplate getTemplate() {
		FormDimensionTemplate[] templates = getTemplates();
		for (int i = 0; i < templates.length; i++) {
			FormDimensionTemplate template = templates[i];
			if (isTemplate(template)) {
				return template;
			}
		}
		// not a template
		return null;
	}

	/**
	 * @return <code>true</code> if this {@link FormDimensionInfo} is same as given template.
	 */
	public final boolean isTemplate(FormDimensionTemplate template) {
		Field field = getTemplateField();
		return field != null && field.getName().equals(template.getFieldName());
	}

	/**
	 * Uses given template as value for {@link FormDimensionInfo}.
	 */
	public final void setTemplate(FormDimensionTemplate template) throws Exception {
		Field field = FormSpecs.class.getField(template.getFieldName());
		setFormSpec((FormSpec) field.get(null));
	}

	/**
	 * Assigns value from given {@link FormDimensionInfo}.
	 */
	public final void assign(FormDimensionInfo dimension) throws Exception {
		setFormSpec(dimension.getFormSpec());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Text
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the tooltip text.
	 */
	public final String getToolTip() {
		// check for template
		{
			FormDimensionTemplate template = getTemplate();
			if (template != null) {
				return template.getTitle();
			}
		}
		// not template, use encoded string
		return getDisplayString();
	}

	/**
	 * @return the encoded textual presentation of {@link FormDimensionInfo} for displaying to user.
	 */
	public String getDisplayString() {
		String encoded = m_size.getDisplayString();
		// add alignment
		DefaultAlignment defaultAlignment = m_horizontal ? ColumnSpec.DEFAULT : RowSpec.DEFAULT;
		if (m_alignment != defaultAlignment) {
			encoded = m_alignment + ":" + encoded;
		}
		// add grow
		if (m_weight != 0.0) {
			if (m_weight == 1.0) {
				encoded += ":grow";
			} else {
				encoded += ":grow(" + FORMAT.format(m_weight) + ")";
			}
		}
		// return final encoded description
		return encoded;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Source generation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the source for this {@link FormDimensionInfo}.
	 */
	public final String getSource() {
		// check for template
		{
			Field field = getTemplateField();
			if (field != null) {
				return "com.jgoodies.forms.layout.FormSpecs." + field.getName();
			}
		}
		// new instance
		String sourceType =
				m_horizontal ? "com.jgoodies.forms.layout.ColumnSpec" : "com.jgoodies.forms.layout.RowSpec";
		if (m_size.isString()) {
			String encoded = m_size.getSource();
			// add alignment
			DefaultAlignment defaultAlignment = m_horizontal ? ColumnSpec.DEFAULT : RowSpec.DEFAULT;
			if (m_alignment != defaultAlignment) {
				encoded = m_alignment + ":" + encoded;
			}
			// add grow
			if (m_weight != 0.0) {
				if (m_weight == 1.0) {
					encoded += ":grow";
				} else {
					encoded += ":grow(" + FORMAT.format(m_weight) + ")";
				}
			}
			// finalize
			return sourceType + ".decode(\"" + encoded + "\")";
		} else {
			String source = "new " + sourceType + "(";
			// add alignment
			source += getAlignmentSource(m_horizontal, m_alignment);
			// add size
			source += ", ";
			source += m_size.getSource();
			// add grow
			source += ", ";
			source += FORMAT.format(m_weight);
			// finalize
			return source + ")";
		}
	}

	/**
	 * @return the source for {@link #m_alignment}.
	 */
	private static String getAlignmentSource(boolean horizontal, DefaultAlignment alignment) {
		if (horizontal) {
			String source;
			if (alignment == ColumnSpec.LEFT) {
				source = "LEFT";
			} else if (alignment == ColumnSpec.CENTER) {
				source = "CENTER";
			} else if (alignment == ColumnSpec.RIGHT) {
				source = "RIGHT";
			} else {
				Assert.isTrue(alignment == ColumnSpec.FILL);
				source = "FILL";
			}
			return "com.jgoodies.forms.layout.ColumnSpec." + source;
		} else {
			String source;
			if (alignment == RowSpec.TOP) {
				source = "TOP";
			} else if (alignment == RowSpec.CENTER) {
				source = "CENTER";
			} else if (alignment == RowSpec.BOTTOM) {
				source = "BOTTOM";
			} else {
				Assert.isTrue(alignment == RowSpec.FILL);
				source = "FILL";
			}
			return "com.jgoodies.forms.layout.RowSpec." + source;
		}
	}

	/**
	 * @return the {@link Field} from {@link FormSpecs} that has same value.
	 */
	private Field getTemplateField() {
		FormSpec formSpec = getFormSpec();
		return FormDimensionUtils.getFormFactoryTemplate(formSpec);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FormSpec creation
	//
	////////////////////////////////////////////////////////////////////////////
	private static final DecimalFormat FORMAT = new DecimalFormat("#.##",
			new DecimalFormatSymbols(Locale.ENGLISH));

	/**
	 * @return the {@link FormSpec} instance according to the current alignment/size/weight.
	 */
	public final FormSpec getFormSpec() {
		if (m_horizontal) {
			return new ColumnSpec(m_alignment, m_size.getSize(), m_weight);
		} else {
			return new RowSpec(m_alignment, m_size.getSize(), m_weight);
		}
	}

	/**
	 * Updates {@link FormDimensionInfo} model using {@link FormSpec}.
	 */
	private void setFormSpec(FormSpec spec) throws Exception {
		m_size = new FormSizeInfo(spec.getSize(), m_horizontal);
		m_alignment = spec.getDefaultAlignment();
		m_weight = spec.getResizeWeight();
	}
}
