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
package org.eclipse.wb.internal.swing.MigLayout.model.ui;

import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo.Alignment;
import org.eclipse.wb.internal.swing.MigLayout.model.ModelMessages;

import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.List;

/**
 * The dialog for editing {@link MigRowInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public final class RowEditDialog extends DimensionEditDialog<MigRowInfo, Alignment> {
	private static final List<AlignmentDescription<Alignment>> ALIGNMENTS = new ArrayList<>();
	static {
		ALIGNMENTS.add(new AlignmentDescription<>(Alignment.DEFAULT,
				ModelMessages.RowEditDialog_alignmentDefault));
		ALIGNMENTS.add(new AlignmentDescription<>(Alignment.TOP,
				ModelMessages.RowEditDialog_alignmentTop));
		ALIGNMENTS.add(new AlignmentDescription<>(Alignment.CENTER,
				ModelMessages.RowEditDialog_alignmentCenter));
		ALIGNMENTS.add(new AlignmentDescription<>(Alignment.BOTTOM,
				ModelMessages.RowEditDialog_alignmentBottom));
		ALIGNMENTS.add(new AlignmentDescription<>(Alignment.FILL,
				ModelMessages.RowEditDialog_alignmentFill));
		ALIGNMENTS.add(new AlignmentDescription<>(Alignment.BASELINE,
				ModelMessages.RowEditDialog_alignmentBaseline));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RowEditDialog(Shell parentShell, MigLayoutInfo layout, MigRowInfo row) {
		super(parentShell, layout, layout.getRows(), row, ModelMessages.RowEditDialog_title, ALIGNMENTS);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Alignment getAlignment(MigRowInfo dimension) {
		return dimension.getAlignment(false);
	}

	@Override
	protected void setAlignment(MigRowInfo dimension, Alignment alignment) {
		dimension.setAlignment(alignment);
	}
}
