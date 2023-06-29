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

import org.eclipse.wb.core.controls.CSpinner;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.MigLayout.model.MigDimensionInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

/**
 * {@link Composite} for editing grow/shrink behavior.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public final class DimensionResizeComposite extends Composite {
	private final int m_defaultWeight;
	private final String m_propertyName;
	private final Listener m_listener;
	private MigDimensionInfo m_dimension;
	// UI
	private final Button m_defaultWeightButton;
	private final Button m_customWeightButton;
	private final CSpinner m_weightSpinner;
	private final Button m_defaultPriorityButton;
	private final Button m_customPriorityButton;
	private final CSpinner m_prioritySpinner;
	// listener
	private boolean m_updatingDimension;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DimensionResizeComposite(Composite parent,
			int style,
			String defaultWeightLabel,
			String customWeightLabel,
			final int defaultWeight,
			final int customWeight,
			String propertyName,
			Listener listener) {
		super(parent, style);
		m_defaultWeight = defaultWeight;
		m_propertyName = StringUtils.capitalize(propertyName);
		m_listener = listener;
		GridLayoutFactory.create(this).noMargins().columns(2).spacingH(15);
		// weight
		{
			Composite weightComposite = new Composite(this, SWT.NONE);
			GridDataFactory.create(weightComposite).grabH().fill();
			GridLayoutFactory.create(weightComposite).noMargins().columns(2);
			// no weight
			{
				m_defaultWeightButton = new Button(weightComposite, SWT.RADIO);
				GridDataFactory.create(m_defaultWeightButton).spanH(2);
				m_defaultWeightButton.setText(defaultWeightLabel);
				m_defaultWeightButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						m_weightSpinner.setEnabled(false);
						m_weightSpinner.setSelection(defaultWeight);
						toDimension();
					}
				});
			}
			// weight
			{
				m_customWeightButton = new Button(weightComposite, SWT.RADIO);
				m_customWeightButton.setText(customWeightLabel);
				m_customWeightButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						m_weightSpinner.setEnabled(true);
						m_weightSpinner.setSelection(customWeight);
						toDimension();
					}
				});
				//
				m_weightSpinner = new CSpinner(weightComposite, SWT.BORDER);
				GridDataFactory.create(m_weightSpinner).hintHC(10).grabH().fill();
				m_weightSpinner.setRange(0, Integer.MAX_VALUE);
				m_weightSpinner.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						toDimension();
					}
				});
			}
		}
		// priority
		{
			Composite priorityComposite = new Composite(this, SWT.NONE);
			GridDataFactory.create(priorityComposite).grabH().fill();
			GridLayoutFactory.create(priorityComposite).noMargins().columns(2);
			// default priority
			{
				m_defaultPriorityButton = new Button(priorityComposite, SWT.RADIO);
				GridDataFactory.create(m_defaultPriorityButton).spanH(2);
				m_defaultPriorityButton.setText(ModelMessages.DimensionResizeComposite_defaultPriority);
				m_defaultPriorityButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						m_prioritySpinner.setEnabled(false);
						m_prioritySpinner.setSelection(100);
						toDimension();
					}
				});
			}
			// priority
			{
				m_customPriorityButton = new Button(priorityComposite, SWT.RADIO);
				m_customPriorityButton.setText(ModelMessages.DimensionResizeComposite_customPriority);
				m_customPriorityButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						m_prioritySpinner.setEnabled(true);
						m_prioritySpinner.setSelection(100);
						toDimension();
					}
				});
				//
				m_prioritySpinner = new CSpinner(priorityComposite, SWT.BORDER);
				GridDataFactory.create(m_prioritySpinner).hintHC(10).grabH().fill();
				m_prioritySpinner.setRange(0, Integer.MAX_VALUE);
				m_prioritySpinner.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						toDimension();
					}
				});
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Updates this field from {@link MigDimensionInfo}.
	 */
	public void fromDimension(MigDimensionInfo dimension) {
		if (!m_updatingDimension) {
			m_dimension = dimension;
			try {
				// weight
				{
					String methodName = MessageFormat.format("get{0}", m_propertyName);
					Float weight = (Float) ReflectionUtils.invokeMethod2(m_dimension, methodName);
					if (weight == null || weight.intValue() == m_defaultWeight) {
						m_defaultWeightButton.setSelection(true);
						m_customWeightButton.setSelection(false);
						m_weightSpinner.setEnabled(false);
						m_weightSpinner.setSelection(0);
					} else {
						m_defaultWeightButton.setSelection(false);
						m_customWeightButton.setSelection(true);
						m_weightSpinner.setEnabled(true);
						m_weightSpinner.setSelection(weight.intValue());
					}
				}
				// priority
				{
					String methodName = MessageFormat.format("get{0}Priority", m_propertyName);
					int priority = (Integer) ReflectionUtils.invokeMethod2(m_dimension, methodName);
					if (priority == 100) {
						m_defaultPriorityButton.setSelection(true);
						m_customPriorityButton.setSelection(false);
						m_prioritySpinner.setEnabled(false);
					} else {
						m_defaultPriorityButton.setSelection(false);
						m_customPriorityButton.setSelection(true);
						m_prioritySpinner.setEnabled(true);
					}
					m_prioritySpinner.setSelection(priority);
				}
			} catch (Throwable e) {
			}
		}
	}

	/**
	 * Uses values from UI widgets to update {@link MigDimensionInfo}.
	 */
	private void toDimension() {
		m_updatingDimension = true;
		try {
			// weight
			{
				Float weight;
				if (m_defaultWeightButton.getSelection()) {
					weight = null;
				} else {
					weight = Float.valueOf(m_weightSpinner.getSelection());
				}
				String methodName = MessageFormat.format("set{0}", m_propertyName);
				ReflectionUtils.invokeMethod2(m_dimension, methodName, Float.class, weight);
			}
			// priority
			{
				int priority;
				if (m_defaultPriorityButton.getSelection()) {
					priority = 100;
				} else {
					priority = m_prioritySpinner.getSelection();
				}
				String methodName = MessageFormat.format("set{0}Priority", m_propertyName);
				ReflectionUtils.invokeMethod2(m_dimension, methodName, int.class, priority);
			}
			notifyModified();
		} catch (Throwable e) {
		} finally {
			m_updatingDimension = false;
		}
	}

	/**
	 * Notifies {@link #m_listener} that this field was updated, with given valid state.
	 */
	private void notifyModified() {
		Event event = new Event();
		m_listener.handleEvent(event);
	}
}
