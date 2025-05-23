/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.swing.SwingImages;

import org.eclipse.jface.resource.ImageDescriptor;

import net.miginfocom.layout.AC;
import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.DimConstraint;
import net.miginfocom.layout.IDEUtil;
import net.miginfocom.layout.UnitValue;
import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.List;

/**
 * Description for row {@link DimConstraint} in {@link MigLayout}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.model
 */
public final class MigRowInfo extends MigDimensionInfo {
	public enum Alignment {
		UNKNOWN(SwingImages.ALIGNMENT_V_SMALL_UNKNOWN, SwingImages.ALIGNMENT_V_MENU_UNKNOWN),
		DEFAULT(SwingImages.ALIGNMENT_V_SMALL_DEFAULT, SwingImages.ALIGNMENT_V_MENU_DEFAULT),
		TOP(SwingImages.ALIGNMENT_V_SMALL_TOP, SwingImages.ALIGNMENT_V_MENU_TOP),
		CENTER(SwingImages.ALIGNMENT_V_SMALL_CENTER, SwingImages.ALIGNMENT_V_MENU_CENTER),
		BOTTOM(SwingImages.ALIGNMENT_V_SMALL_BOTTOM, SwingImages.ALIGNMENT_V_MENU_BOTTOM),
		FILL(SwingImages.ALIGNMENT_V_SMALL_FILL, SwingImages.ALIGNMENT_V_MENU_FILL),
		BASELINE(SwingImages.ALIGNMENT_V_SMALL_BASELINE, SwingImages.ALIGNMENT_V_MENU_BASELINE);

		private final ImageDescriptor smallDescriptor;
		private final ImageDescriptor menuDescriptor;

		private Alignment(ImageDescriptor smallDescriptor, ImageDescriptor menuDescriptor) {
			this.smallDescriptor = smallDescriptor;
			this.menuDescriptor = menuDescriptor;
		}
		/**
		 * @return the small image (5x9) to display current alignment to user.
		 */
		public ImageDescriptor getSmallImageDescriptor() {
			return smallDescriptor;
		}

		/**
		 * @return the big image (16x16) to display for user in menu.
		 */
		public ImageDescriptor getMenuImageDescriptor() {
			return menuDescriptor;
		}

		/**
		 * @return the text to use for menu item or toolbar tooltip.
		 */
		public String getText() {
			return StringUtils.capitalize(name().toLowerCase());
		}
	}

	public static final Alignment[] ALIGNMENTS_TO_SET = new Alignment[]{
			Alignment.DEFAULT,
			Alignment.TOP,
			Alignment.CENTER,
			Alignment.BOTTOM,
			Alignment.FILL,
			Alignment.BASELINE};

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MigRowInfo(MigLayoutInfo layout) {
		super(layout, false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void delete() throws Exception {
		m_layout.deleteRow(getIndex());
	}

	@Override
	public List<? extends MigDimensionInfo> getSiblings() {
		return m_layout.getRows();
	}

	@Override
	protected DimConstraint fetchConstraint() {
		int index = getIndex();
		AC colSpecs = (AC) ReflectionUtils.getFieldObject(m_layout.getObject(), "rowSpecs");
		if (index >= 0 && index < colSpecs.getCount()) {
			return colSpecs.getConstaints()[index];
		} else {
			return createDefaultConstraint();
		}
	}

	@Override
	protected DimConstraint createDefaultConstraint() {
		return ConstraintParser.parseRowConstraints("[]").getConstaints()[0];
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the default {@link Alignment} for components in this {@link MigRowInfo}.
	 */
	public Alignment getAlignment(boolean resolveDefault) {
		return getAlignment(m_constraint, true, resolveDefault);
	}

	/**
	 * Sets default {@link Alignment} for components in this {@link MigRowInfo}.
	 */
	public void setAlignment(Alignment alignment) {
		setAlignment(m_constraint, alignment, true);
	}

	/**
	 * @return the {@link Alignment} enum converted from alignment of {@link DimConstraint}.
	 */
	static Alignment getAlignment(DimConstraint constraint, boolean forRow, boolean resolveDefault) {
		if (forRow && constraint.isFill()) {
			return Alignment.FILL;
		}
		if (!forRow && constraint.getGrow() != null) {
			return Alignment.FILL;
		}
		// convert to enum
		UnitValue alignment =
				resolveDefault ? constraint.getAlignOrDefault(false) : constraint.getAlign();
		if (alignment == null) {
			return Alignment.DEFAULT;
		} else if (alignment == IDEUtil.TOP) {
			return Alignment.TOP;
		} else if (alignment == IDEUtil.CENTER) {
			return Alignment.CENTER;
		} else if (alignment == IDEUtil.BOTTOM) {
			return Alignment.BOTTOM;
		} else if (alignment == IDEUtil.BASELINE_IDENTITY) {
			return Alignment.BASELINE;
		}
		// unknown
		return Alignment.UNKNOWN;
	}

	/**
	 * Updates {@link DimConstraint} with {@link Alignment} enum value.
	 */
	static void setAlignment(DimConstraint constraint, Alignment alignment, boolean forRow) {
		Assert.isNotNull(alignment);
		// set default alignment
		if (alignment == Alignment.DEFAULT) {
			constraint.setAlign(null);
			if (forRow) {
				constraint.setFill(false);
			} else {
				constraint.setGrow(null);
			}
			return;
		}
		// special support for FILL
		if (alignment == Alignment.FILL) {
			if (forRow) {
				constraint.setFill(true);
			} else {
				constraint.setGrow(100f);
			}
			constraint.setAlign(null);
			return;
		}
		// prepare alignment as String
		UnitValue alignmentValue;
		switch (alignment) {
		case TOP :
			alignmentValue = IDEUtil.TOP;
			break;
		case CENTER :
			alignmentValue = IDEUtil.CENTER;
			break;
		case BOTTOM :
			alignmentValue = IDEUtil.BOTTOM;
			break;
		case BASELINE :
			alignmentValue = IDEUtil.BASELINE_IDENTITY;
			break;
		default :
			throw new IllegalArgumentException(MessageFormat.format(
					ModelMessages.MigRowInfo_canNotSetAlignment,
					alignment));
		}
		// parse alignment, update constraint
		constraint.setAlign(alignmentValue);
		if (!forRow) {
			constraint.setGrow(null);
		} else {
			constraint.setFill(false);
		}
	}
}
