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
package org.eclipse.wb.internal.swing.MigLayout.model;

import org.eclipse.wb.internal.core.utils.check.Assert;

import net.miginfocom.layout.AC;
import net.miginfocom.layout.BoundSize;
import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.DimConstraint;
import net.miginfocom.layout.IDEUtil;
import net.miginfocom.layout.UnitValue;
import net.miginfocom.swing.MigLayout;
import net.miginfocom.swing.SwingContainerWrapper;

import org.apache.commons.lang3.StringUtils;

import java.awt.Container;
import java.beans.Beans;
import java.util.List;
import java.util.Locale;

/**
 * Description for {@link DimConstraint} in {@link MigLayout}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.model
 */
public abstract class MigDimensionInfo {
	protected final MigLayoutInfo m_layout;
	private final boolean m_horizontal;
	protected DimConstraint m_constraint;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MigDimensionInfo(MigLayoutInfo layout, boolean horizontal) {
		m_layout = layout;
		m_horizontal = horizontal;
		m_constraint = createDefaultConstraint();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public abstract void delete() throws Exception;

	public abstract List<? extends MigDimensionInfo> getSiblings();

	/**
	 * @return the index of this {@link MigDimensionInfo} in all dimensions of {@link MigLayoutInfo}.
	 */
	public final int getIndex() {
		return getSiblings().indexOf(this);
	}

	/**
	 * @return the tooltip text to show for user.
	 */
	public final String getTooltip() {
		return getString(true);
	}

	/**
	 * @param withBraces
	 *          is <code>true</code>, if surrounding <code>[]</code> should be used.
	 *
	 * @return the constraints of this {@link MigDimensionInfo} as string.
	 */
	public final String getString(boolean withBraces) {
		AC ac = new AC();
		ac.setConstraints(new DimConstraint[]{m_constraint});
		String constraintString = IDEUtil.getConstraintString(ac, false, m_horizontal);
		if (!withBraces) {
			constraintString = StringUtils.strip(constraintString, "[]");
		}
		return constraintString;
	}

	/**
	 * Sets the constraints of this {@link MigDimensionInfo} as string.
	 */
	public final void setString(String s) {
		m_constraint = parseDimConstraint(m_horizontal, s);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Gaps
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the gap before this dimension.
	 */
	public final BoundSize getGapBefore() {
		return m_constraint.getGapBefore();
	}

	/**
	 * Sets the gap before this dimension.
	 */
	public final void setGapBefore(String boundSizeString) {
		BoundSize gap = parseBoundSize(boundSizeString);
		// set "before" gap for this dimension
		m_constraint.setGapBefore(gap);
		// if has previous dimension, set "after" gap
		{
			List<? extends MigDimensionInfo> siblings = getSiblings();
			int index = siblings.indexOf(this);
			if (index != 0) {
				siblings.get(index - 1).m_constraint.setGapAfter(gap);
			}
		}
	}

	/**
	 * @return the gap after this dimension.
	 */
	public final BoundSize getGapAfter() {
		return m_constraint.getGapAfter();
	}

	/**
	 * Sets the gap before this dimension.
	 */
	public final void setGapAfter(String boundSizeString) {
		BoundSize gap = parseBoundSize(boundSizeString);
		// set "after" gap for this dimension
		m_constraint.setGapAfter(gap);
		// if has next dimension, set "before" gap
		{
			List<? extends MigDimensionInfo> siblings = getSiblings();
			int index = siblings.indexOf(this);
			if (index != siblings.size() - 1) {
				siblings.get(index + 1).m_constraint.setGapBefore(gap);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Grow
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link DimConstraint#getGrowPriority()} property.
	 */
	public final int getGrowPriority() {
		return m_constraint.getGrowPriority();
	}

	/**
	 * Sets the {@link DimConstraint#getGrowPriority()} property.
	 */
	public final void setGrowPriority(int priority) {
		m_constraint.setGrowPriority(priority);
	}

	/**
	 * @return the grow weight, may be <code>null</code> if no grow required.
	 */
	public final Float getGrow() {
		return m_constraint.getGrow();
	}

	/**
	 * Sets the grow weight, may be <code>null</code> if no grow required.
	 */
	public final void setGrow(Float weight) {
		m_constraint.setGrow(weight);
	}

	/**
	 * @return <code>true</code> if this {@link MigDimensionInfo} has not <code>null</code> grow
	 *         weight.
	 */
	public final boolean hasGrow() {
		Float grow = m_constraint.getGrow();
		return grow != null && grow.floatValue() > 0.001f;
	}

	/**
	 * Flips "grow" property between <code>true</code> to <code>false</code>.
	 */
	public final void flipGrow() {
		if (hasGrow()) {
			m_constraint.setGrow(null);
		} else {
			m_constraint.setGrow(100.0f);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Shrink
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link DimConstraint#getShrinkPriority()} property.
	 */
	public final int getShrinkPriority() {
		return m_constraint.getShrinkPriority();
	}

	/**
	 * Sets the {@link DimConstraint#getGrowPriority()} property.
	 */
	public final void setShrinkPriority(int priority) {
		m_constraint.setShrinkPriority(priority);
	}

	/**
	 * @return the shrink weight, may be <code>null</code> if no shrink required.
	 */
	public final Float getShrink() {
		return m_constraint.getShrink();
	}

	/**
	 * Sets the shrink weight, may be <code>null</code> if no shrink required.
	 */
	public final void setShrink(Float weight) {
		m_constraint.setShrink(weight);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Updates {@link #m_constraint} with corresponding {@link DimConstraint}.
	 */
	final void updateConstraint() throws Exception {
		m_constraint = fetchConstraint();
	}

	/**
	 * @return the corresponding {@link DimConstraint} for this {@link MigDimensionInfo}.
	 */
	protected abstract DimConstraint fetchConstraint();

	/**
	 * @return the default {@link DimConstraint} for this type of {@link MigDimensionInfo}.
	 */
	protected abstract DimConstraint createDefaultConstraint();

	////////////////////////////////////////////////////////////////////////////
	//
	// Size
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return minimum size of this {@link MigDimensionInfo}.
	 */
	public UnitValue getMinimumSize() {
		BoundSize boundSize = m_constraint.getSize();
		return boundSize.getMin();
	}

	/**
	 * Sets only minimum size of this dimension {@link BoundSize}.
	 */
	public void setMinimumSize(String s) {
		UnitValue size = parseSimpleSize(s);
		BoundSize boundSize = m_constraint.getSize();
		m_constraint.setSize(new BoundSize(size, boundSize.getPreferred(), boundSize.getMax(), null));
	}

	/**
	 * @return preferred size of this {@link MigDimensionInfo}.
	 */
	public UnitValue getPreferredSize() {
		BoundSize boundSize = m_constraint.getSize();
		return boundSize.getPreferred();
	}

	/**
	 * Sets only preferred size of this dimension {@link BoundSize}.
	 */
	public void setPreferredSize(String s) {
		UnitValue size = parseSimpleSize(s);
		BoundSize boundSize = m_constraint.getSize();
		m_constraint.setSize(new BoundSize(boundSize.getMin(), size, boundSize.getMax(), null));
	}

	/**
	 * @return maximum size of this {@link MigDimensionInfo}.
	 */
	public UnitValue getMaximumSize() {
		BoundSize boundSize = m_constraint.getSize();
		return boundSize.getMax();
	}

	/**
	 * Sets only maximum size of this dimension {@link BoundSize}.
	 */
	public void setMaximumSize(String s) {
		UnitValue size = parseSimpleSize(s);
		BoundSize boundSize = m_constraint.getSize();
		m_constraint.setSize(new BoundSize(boundSize.getMin(), boundSize.getPreferred(), size, null));
	}

	/**
	 * Sets {@link BoundSize} of dimension as single string.
	 */
	public void setSize(String sizeString) {
		BoundSize size = parseBoundSize(sizeString);
		if (size == null) {
			size = new BoundSize(null, null, null, null);
		}
		m_constraint.setSize(size);
	}

	/**
	 * @return the {@link UnitValue} corresponding to the single size, such as <code>"1cm"</code>.
	 */
	private UnitValue parseSimpleSize(String s) {
		BoundSize boundSize = parseBoundSize(s);
		return boundSize != null ? boundSize.getPreferred() : null;
	}

	/**
	 * @return the {@link BoundSize} of dimension parsed from string.
	 */
	private BoundSize parseBoundSize(final String sizeString) {
		if (sizeString == null) {
			return null;
		}
		boolean old_designTime = Beans.isDesignTime();
		try {
			Beans.setDesignTime(true);
			return ConstraintParser.parseBoundSize(sizeString, false, m_horizontal);
		} finally {
			Beans.setDesignTime(old_designTime);
		}
	}

	/**
	 * @return the string presentation of given {@link UnitValue}.
	 */
	public final String getString(UnitValue value) {
		if (value != null) {
			return getString(new BoundSize(null, value, null, null));
		} else {
			return null;
		}
	}

	/**
	 * @return the string presentation of given {@link BoundSize}.
	 */
	public final String getString(BoundSize boundSize) {
		if (boundSize != null) {
			AC ac = new AC();
			DimConstraint dimConstraint = new DimConstraint();
			dimConstraint.setSize(boundSize);
			ac.setConstraints(new DimConstraint[]{dimConstraint});
			String constraintString = IDEUtil.getConstraintString(ac, false, m_horizontal);
			return StringUtils.strip(constraintString, "[]");
		} else {
			return null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parsing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Parses {@link DimConstraint} from {@link String}.
	 */
	protected static DimConstraint parseDimConstraint(boolean horizontal, String s) {
		// prepare constraints
		AC ac;
		{
			boolean old_designTime = Beans.isDesignTime();
			try {
				Beans.setDesignTime(true);
				if (horizontal) {
					ac = ConstraintParser.parseColumnConstraints(s);
				} else {
					ac = ConstraintParser.parseRowConstraints(s);
				}
			} finally {
				Beans.setDesignTime(old_designTime);
			}
		}
		// extract single constraint
		Assert.equals(1, ac.getCount(), s);
		return ac.getConstraints()[0];
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Unit conversion
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the size string that uses given unit and has required size in pixels.
	 */
	public String toUnitString(int pixels, String unit) {
		return String.format(Locale.ENGLISH, "%.2f%s", toUnit(pixels, unit), unit);
	}

	/**
	 * @return the value in units corresponding to the value in pixels.
	 */
	public float toUnit(int pixels, String unit) {
		float pixelsInOne = getPixelsInOneUnit(unit);
		return pixels / pixelsInOne;
	}

	/**
	 * @return the size of single unit in pixels.
	 */
	public float getPixelsInOneUnit(String unit) {
		UnitValue unitValue = ConstraintParser.parseUnitValue("1" + unit, m_horizontal);
		Container container = m_layout.getContainer().getContainer();
		SwingContainerWrapper parentWrapper = new SwingContainerWrapper(container);
		int refValue = m_horizontal ? container.getBounds().width : container.getBounds().height;
		return unitValue.getPixelsExact(refValue, parentWrapper, null);
	}
}
