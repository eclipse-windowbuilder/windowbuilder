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
package org.eclipse.wb.internal.swing.customize;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;

import java.awt.Component;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Objects;

/**
 * {@link Action} for performing customization.
 *
 * @author lobas_av
 * @coverage swing.customize
 */
class CustomizerAction extends Action {
	private final JavaInfo m_javaInfo;
	private final Class<Customizer> m_customizerClass;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CustomizerAction(JavaInfo javaInfo, Class<Customizer> customizerClass) {
		m_javaInfo = javaInfo;
		m_customizerClass = customizerClass;
		setImageDescriptor(Activator.getImageDescriptor("actions/customize.gif"));
		setText(ModelMessages.CustomizerAction_title);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IAction
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void run() {
		performCustomize();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private void performCustomize() {
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				ExecutionUtils.runDesignTime(new RunnableEx() {
					@Override
					public void run() throws Exception {
						performCustomize0();
					}
				});
			}
		});
	}

	private void performCustomize0() throws Exception {
		Customizer customizer = m_customizerClass.newInstance();
		// prepare properties information
		final JavaInfoState javaInfoState = JavaInfoState.getState(m_javaInfo);
		boolean explicit = isExplicitPropertyChange(m_javaInfo);
		PropertyChangeListener propertyChangeListener = null;
		if (explicit) {
			propertyChangeListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					javaInfoState.changedProperties.add(event.getPropertyName());
					javaInfoState.changedPropertyValues.put(event.getPropertyName(), event.getNewValue());
				}
			};
			customizer.addPropertyChangeListener(propertyChangeListener);
		}
		//
		try {
			// open customizer dialog
			customizer.setObject(javaInfoState.object);
			AwtComponentDialog dialog =
					new AwtComponentDialog(DesignerPlugin.getDefault(),
							(Component) customizer,
							ModelMessages.CustomizerAction_dialogTitle,
							MessageFormat.format(
									ModelMessages.CustomizerAction_dialogMessage,
									m_customizerClass.getName()));
			int dialogResult = dialog.open();
			// handle update properties
			if (dialogResult == Window.OK) {
				RunnableEx runnable = null;
				if (explicit) {
					// update changed properties
					runnable = new RunnableEx() {
						@Override
						public void run() throws Exception {
							int size = javaInfoState.properties.size();
							for (int i = 0; i < size; i++) {
								Property property = javaInfoState.properties.get(i);
								if (javaInfoState.changedProperties.contains(property.getTitle())) {
									Object newValue = javaInfoState.changedPropertyValues.get(property.getTitle());
									Object oldValue = javaInfoState.oldValues.get(i);
									if (!Objects.equals(newValue, oldValue)) {
										property.setValue(newValue);
									}
								}
							}
						}
					};
				} else {
					// update all properties
					runnable = new RunnableEx() {
						@Override
						public void run() throws Exception {
							int size = javaInfoState.properties.size();
							for (int i = 0; i < size; i++) {
								Object newValue = javaInfoState.getters.get(i).invoke(javaInfoState.object);
								Object oldValue = javaInfoState.oldValues.get(i);
								if (!Objects.equals(newValue, oldValue)) {
									javaInfoState.properties.get(i).setValue(newValue);
								}
							}
						}
					};
				}
				// run update
				ExecutionUtils.run(m_javaInfo, runnable);
			}
			// rollback property changes
			if (dialogResult == Window.CANCEL) {
				ExecutionUtils.run(m_javaInfo, new RunnableEx() {
					@Override
					public void run() throws Exception {
						int size = javaInfoState.properties.size();
						for (int i = 0; i < size; i++) {
							Object newValue = javaInfoState.getters.get(i).invoke(javaInfoState.object);
							Object oldValue = javaInfoState.oldValues.get(i);
							if (!Objects.equals(newValue, oldValue)) {
								javaInfoState.setters.get(i).invoke(javaInfoState.object, oldValue);
							}
						}
					}
				});
			}
		} finally {
			if (propertyChangeListener != null) {
				customizer.removePropertyChangeListener(propertyChangeListener);
			}
		}
	}

	/**
	 * @return <code>true</code> if {@link BeanInfo} for given info object contains flag
	 *         "EXPLICIT_PROPERTY_CHANGE".
	 */
	private static boolean isExplicitPropertyChange(JavaInfo javaInfo) throws Exception {
		BeanDescriptor beanDescriptor = javaInfo.getDescription().getBeanDescriptor();
		return Boolean.TRUE.equals(beanDescriptor.getValue("EXPLICIT_PROPERTY_CHANGE"));
	}
}