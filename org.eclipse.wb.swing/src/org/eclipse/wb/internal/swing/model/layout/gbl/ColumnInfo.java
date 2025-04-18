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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import java.awt.GridBagLayout;
import java.util.EnumSet;
import java.util.List;

/**
 * Model for column in {@link AbstractGridBagLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class ColumnInfo extends DimensionInfo {
	public enum Alignment {
		UNKNOWN, LEFT, CENTER, RIGHT, FILL
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnInfo(AbstractGridBagLayoutInfo layout, DimensionOperations<?> operations) {
		super(layout, operations);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the alignment of this {@link ColumnInfo}. {@link ColumnInfo} has alignment only if all
	 *         {@link ComponentInfo}'s have same alignment. In other case {@link Alignment#UNKNOWN}
	 *         will be returned.
	 */
	public Alignment getAlignment() {
		final EnumSet<Alignment> alignments = EnumSet.noneOf(Alignment.class);
		visit(new IComponentVisitor() {
			@Override
			public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
					throws Exception {
				alignments.add(constraints.getHorizontalAlignment());
			}
		});
		return alignments.size() == 1 ? alignments.iterator().next() : Alignment.UNKNOWN;
	}

	/**
	 * Sets alignment for all {@link ComponentInfo}'s in this {@link ColumnInfo}.
	 */
	public void setAlignment(final ColumnInfo.Alignment alignment) throws Exception {
		visit(new IComponentVisitor() {
			@Override
			public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
					throws Exception {
				constraints.setHorizontalAlignment(alignment);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected int initialize_getMinimumSize(int index) {
		GridBagLayout layoutObject = m_layout.getLayoutManager();
		int[] widths = layoutObject.columnWidths;
		if (widths != null && widths.length > index) {
			return widths[index];
		} else {
			return 0;
		}
	}

	@Override
	protected double initialize_getWeight(int index) {
		GridBagLayout layoutObject = m_layout.getLayoutManager();
		double[] weights = layoutObject.columnWeights;
		if (weights != null && weights.length > index) {
			return weights[index];
		} else {
			return 0;
		}
	}

	@Override
	protected List<? extends DimensionInfo> getDimensions() {
		return m_layout.getColumns();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isDimensionComponent(AbstractGridBagConstraintsInfo constraints) {
		return constraints.x == getIndex();
	}
}
