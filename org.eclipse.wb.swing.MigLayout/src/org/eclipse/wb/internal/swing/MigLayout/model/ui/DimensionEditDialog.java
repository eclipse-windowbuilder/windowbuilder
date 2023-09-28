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

import org.eclipse.wb.core.controls.Separator;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.swing.MigLayout.Activator;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigDimensionInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The dialog for editing {@link MigDimensionInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
abstract class DimensionEditDialog<T extends MigDimensionInfo, A extends Enum<?>>
extends
ResizableDialog {
	private final MigLayoutInfo m_layout;
	private final List<T> m_dimensions;
	private final boolean m_horizontal;
	private final String m_dimensionName;
	private final List<AlignmentDescription<A>> m_alignments;
	////////////////////////////////////////////////////////////////////////////
	//
	// Current state
	//
	////////////////////////////////////////////////////////////////////////////
	private T m_dimension;
	private String m_dimensionString;
	private int m_currentIndex;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DimensionEditDialog(Shell parentShell,
			MigLayoutInfo layout,
			List<T> dimensions,
			T dimension,
			String dimensionName,
			List<AlignmentDescription<A>> alignments) {
		super(parentShell, Activator.getDefault());
		setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		m_layout = layout;
		m_dimensions = dimensions;
		m_horizontal = dimension instanceof MigColumnInfo;
		m_dimensionName = dimensionName;
		m_alignments = alignments;
		setEditDimension(dimension);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI fields
	//
	////////////////////////////////////////////////////////////////////////////
	private Text m_indexText;
	private Button m_prevButton;
	private Button m_nextButton;
	// specification
	private DimensionSpecificationComposite m_specificationComposite;
	// alignment
	private List<Button> m_alignmentButtons;
	// size
	private DimensionUnitValueField m_minField;
	private DimensionUnitValueField m_prefField;
	private DimensionUnitValueField m_maxField;
	// resize
	private DimensionResizeComposite m_growComposite;
	private DimensionResizeComposite m_shrinkComposite;

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void create() {
		super.create();
		showDimension();
		m_specificationComposite.setFocus();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		createHeaderComposite(container);
		createAlignmentComposite(container);
		createSizeComposite(container);
		createResizeComposites(container);
		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(MessageFormat.format(ModelMessages.DimensionEditDialog_title, m_dimensionName));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Buttons
	//
	////////////////////////////////////////////////////////////////////////////
	private static final int APPLY_ID = IDialogConstants.CLIENT_ID + 1;

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		if (m_layout != null) {
			createButton(parent, APPLY_ID, ModelMessages.DimensionEditDialog_applyButton, false);
		}
	}

	@Override
	protected void buttonPressed(int buttonId) {
		try {
			if (buttonId == IDialogConstants.OK_ID || buttonId == APPLY_ID) {
				applyChanges();
				m_dimensionString = m_dimension.getString(false);
			}
			if (buttonId == IDialogConstants.CANCEL_ID) {
				m_dimension.setString(m_dimensionString);
			}
		} catch (Throwable e) {
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Enables/disable OK/Apply buttons.
	 */
	private void updateButtons(boolean valid) {
		getButton(IDialogConstants.OK_ID).setEnabled(valid);
		getButton(APPLY_ID).setEnabled(valid);
	}

	/**
	 * Saves current {@link DimensionInfo} changes into source and refreshes GUI.
	 */
	private void applyChanges() throws Exception {
		ExecutionUtils.run(m_layout, new RunnableEx() {
			@Override
			public void run() throws Exception {
				m_layout.writeDimensions();
			}
		});
	}

	/**
	 * Sets the {@link DimensionInfo} to edit.
	 */
	private void setEditDimension(final T dimension) {
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				// apply changes
				if (m_dimension != null) {
					applyChanges();
				}
				// remember new dimension
				m_dimension = dimension;
				m_dimensionString = m_dimension.getString(false);
				m_currentIndex = m_dimensions.indexOf(m_dimension);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the alignment of {@link DimensionInfo}.
	 */
	protected abstract A getAlignment(T dimension);

	/**
	 * Sets alignment of {@link DimensionInfo}.
	 */
	protected abstract void setAlignment(T dimension, A alignment);

	////////////////////////////////////////////////////////////////////////////
	//
	// Composites
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Create the header displaying {@link Composite}.
	 */
	private void createHeaderComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.create(composite).grabH().fill();
		GridLayoutFactory.create(composite).noMargins().columns(4);
		// index
		{
			new Label(composite, SWT.NONE).setText(MessageFormat.format(
					ModelMessages.DimensionEditDialog_index,
					m_dimensionName));
			// index
			{
				m_indexText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
				GridDataFactory.create(m_indexText).grabH().fillH().indentHC(3);
			}
			// prev button
			{
				m_prevButton = new Button(composite, SWT.NONE);
				m_prevButton.setToolTipText(MessageFormat.format(
						ModelMessages.DimensionEditDialog_previousButton,
						m_dimensionName));
				m_prevButton.setImage(m_horizontal
						? Activator.getImage("navigation/left.gif")
								: Activator.getImage("navigation/up.gif"));
				m_prevButton.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						setEditDimension(m_dimensions.get(m_currentIndex - 1));
						showDimension();
					}
				});
			}
			// next button
			{
				m_nextButton = new Button(composite, SWT.NONE);
				m_nextButton.setToolTipText(MessageFormat.format(
						ModelMessages.DimensionEditDialog_nextButton,
						m_dimensionName));
				m_nextButton.setImage(m_horizontal
						? Activator.getImage("navigation/right.gif")
								: Activator.getImage("navigation/down.gif"));
				m_nextButton.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						setEditDimension(m_dimensions.get(m_currentIndex + 1));
						showDimension();
					}
				});
			}
		}
		// specification
		{
			new Label(composite, SWT.NONE).setText(ModelMessages.DimensionEditDialog_specification);
			{
				m_specificationComposite = new DimensionSpecificationComposite(composite);
				GridDataFactory.create(m_specificationComposite).spanH(3).grabH().fillH().indentHC(3);
				m_specificationComposite.addListener(SWT.Modify, new Listener() {
					@Override
					public void handleEvent(Event e) {
						updateButtons(e.doit);
						showDimension();
					}
				});
			}
		}
	}

	/**
	 * Creates the alignment editing {@link Composite}.
	 */
	private void createAlignmentComposite(Composite parent) {
		createSeparator(parent, ModelMessages.DimensionEditDialog_defaultAlignment);
		Composite composite = new Composite(parent, SWT.NONE);
		//
		GridDataFactory.create(composite).grabH().fill().indentHC(2);
		GridLayoutFactory.create(composite).noMargins().columns(4);
		//
		m_alignmentButtons = new ArrayList<>();
		for (final AlignmentDescription<A> description : m_alignments) {
			// create radio button
			Button button = new Button(composite, SWT.RADIO);
			//GridDataFactory.create(button).grabH().fillH();
			button.setText(description.getTitle());
			// add listener
			m_alignmentButtons.add(button);
			button.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					setAlignment(m_dimension, description.getAlignment());
					showDimension();
				}
			});
		}
	}

	/**
	 * Creates the size editing {@link Composite}.
	 */
	private void createSizeComposite(Composite parent) {
		createSeparator(parent, ModelMessages.DimensionEditDialog_size);
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.create(composite).grabH().fill().indentHC(2);
		GridLayoutFactory.create(composite).noMargins().columns(2);
		// minimum
		{
			Listener listener = new Listener() {
				@Override
				public void handleEvent(Event e) {
					updateButtons(e.doit);
					showDimension();
				}
			};
			m_minField =
					new DimensionUnitValueField(composite,
							ModelMessages.DimensionEditDialog_minimumSize,
							"minimumSize",
							listener);
			m_prefField =
					new DimensionUnitValueField(composite,
							ModelMessages.DimensionEditDialog_preferredSize,
							"preferredSize",
							listener);
			m_maxField =
					new DimensionUnitValueField(composite,
							ModelMessages.DimensionEditDialog_maximumSize,
							"maximumSize",
							listener);
		}
	}

	/**
	 * Creates the grow/shrink editing {@link Composite}.
	 */
	private void createResizeComposites(Composite parent) {
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				showDimension();
			}
		};
		{
			createSeparator(parent, ModelMessages.DimensionEditDialog_growTitle);
			m_growComposite =
					new DimensionResizeComposite(parent,
							SWT.NONE,
							"no grow",
							"grow",
							0,
							100,
							"grow",
							listener);
			GridDataFactory.create(m_growComposite).grabH().fill().indentHC(2);
		}
		{
			createSeparator(parent, ModelMessages.DimensionEditDialog_shrinkTitle);
			m_shrinkComposite =
					new DimensionResizeComposite(parent,
							SWT.NONE,
							"default shrink",
							"shrink",
							100,
							100,
							"shrink",
							listener);
			GridDataFactory.create(m_shrinkComposite).grabH().fill().indentHC(2);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Shows the {@link DimensionInfo} in UI controls.
	 */
	private void showDimension() {
		// index
		{
			m_indexText.setText(Integer.toString(m_currentIndex));
			m_prevButton.setEnabled(m_currentIndex != 0);
			m_nextButton.setEnabled(m_currentIndex < m_dimensions.size() - 1);
		}
		// specification
		m_specificationComposite.fromDimension(m_dimension);
		// alignment
		{
			A alignment = getAlignment(m_dimension);
			for (int i = 0; i < m_alignments.size(); i++) {
				AlignmentDescription<A> description = m_alignments.get(i);
				Button button = m_alignmentButtons.get(i);
				button.setSelection(alignment == description.getAlignment());
			}
		}
		// size
		{
			m_minField.fromDimension(m_dimension);
			m_prefField.fromDimension(m_dimension);
			m_maxField.fromDimension(m_dimension);
		}
		// resize
		m_growComposite.fromDimension(m_dimension);
		m_shrinkComposite.fromDimension(m_dimension);
	}

	/**
	 * Creates separator with given text.
	 */
	private static void createSeparator(Composite parent, String text) {
		Separator separator = new Separator(parent, SWT.NONE);
		GridDataFactory.create(separator).grabH().fillH();
		separator.setText(text);
		separator.setForeground(separator.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
	}
}
