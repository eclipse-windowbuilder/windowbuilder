/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.swt.model.property.editor;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation of {@link PropertyEditor} for SWT accelerator.
 *
 * @author scheglov_ke
 * @coverage swt.property.editor
 */
public final class AcceleratorPropertyEditor extends TextDialogPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new AcceleratorPropertyEditor();

	private AcceleratorPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getText(Property property) throws Exception {
		Object value = property.getValue();
		if (value instanceof Integer) {
			return getText((Integer) value);
		}
		// unknown value
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void openDialog(Property property) throws Exception {
		KeyStrokeDialog dialog = new KeyStrokeDialog(property.getTitle());
		// set initial accelerator
		{
			Object value = property.getValue();
			if (value instanceof Integer) {
				int keyStroke = (Integer) value;
				dialog.setKeyStroke(keyStroke);
			}
		}
		// open dialog
		if (dialog.open() == Window.OK) {
			GenericProperty genericProperty = (GenericProperty) property;
			int accelerator = dialog.getAccelerator();
			// set source
			String source = getSource(accelerator);
			genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// KeyStrokeDialog
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class KeyStrokeDialog extends ResizableDialog {
		private final String m_title;
		private int m_accelerator;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public KeyStrokeDialog(String title) {
			super(DesignerPlugin.getShell(), Activator.getDefault());
			m_title = title;
			setShellStyle(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * Sets the accelerator to edit.
		 */
		public void setKeyStroke(int keyStroke) {
			m_accelerator = keyStroke;
		}

		/**
		 * @return the result accelerator.
		 */
		public int getAccelerator() {
			return m_accelerator;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// GUI
		//
		////////////////////////////////////////////////////////////////////////////
		private Text m_keyStrokeText;
		private List m_keyCodeList;
		private final Map<Integer, Button> m_modifierToButton = new HashMap<>();

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = new Composite(parent, SWT.NONE);
			GridDataFactory.create(area).grab().fill();
			GridLayoutFactory.create(area);
			// combination
			{
				{
					Label label = new Label(area, SWT.NONE);
					label.setText(ModelMessages.AcceleratorPropertyEditor_combinationLabel);
				}
				{
					m_keyStrokeText = new Text(area, SWT.BORDER | SWT.READ_ONLY);
					GridDataFactory.create(m_keyStrokeText).grabH().fillH();
					m_keyStrokeText.addListener(SWT.KeyDown, new Listener() {
						@Override
						public void handleEvent(Event event) {
							m_accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(event);
							displayAccelerator();
						}
					});
				}
			}
			// properties
			{
				Group propertiesGroup = new Group(area, SWT.NONE);
				GridDataFactory.create(propertiesGroup).grab().fill();
				GridLayoutFactory.create(propertiesGroup);
				propertiesGroup.setText(ModelMessages.AcceleratorPropertyEditor_keyStrokeLabel);
				// modifiers
				{
					Group modifiersGroup = new Group(propertiesGroup, SWT.NONE);
					GridDataFactory.create(modifiersGroup).grabH().fillH();
					GridLayoutFactory.create(modifiersGroup).columns(5);
					modifiersGroup.setText(ModelMessages.AcceleratorPropertyEditor_modifiers);
					addModifierButton(modifiersGroup, SWT.ALT, "&Alt");
					addModifierButton(modifiersGroup, SWT.CTRL, "&Ctrl");
					addModifierButton(modifiersGroup, SWT.SHIFT, "&Shift");
					addModifierButton(modifiersGroup, SWT.COMMAND, "&Command");
				}
				// key code
				{
					Group keyGroup = new Group(propertiesGroup, SWT.NONE);
					GridDataFactory.create(keyGroup).grab().fill();
					GridLayoutFactory.create(keyGroup);
					keyGroup.setText(ModelMessages.AcceleratorPropertyEditor_keyCode);
					//
					m_keyCodeList = new List(keyGroup, SWT.BORDER | SWT.V_SCROLL);
					GridDataFactory.create(m_keyCodeList).hintC(50, 15).grab().fill();
					// add items
					prepareKeyMaps();
					for (String name : m_keyFields) {
						m_keyCodeList.add(name);
					}
					// add listener
					m_keyCodeList.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event event) {
							// prepare keyCode
							int keyCode;
							{
								String name = m_keyCodeList.getSelection()[0];
								keyCode = getKeyCode(name);
							}
							// update accelerator
							int modifiers = m_accelerator & SWT.MODIFIER_MASK;
							m_accelerator = modifiers | keyCode;
							displayAccelerator();
						}
					});
				}
			}
			//
			displayAccelerator();
			return area;
		}

		/**
		 * Creates check {@link Button} for given modifier.
		 */
		private void addModifierButton(Composite parent, final int modifier, String title) {
			final Button button = new Button(parent, SWT.CHECK);
			button.setText(title);
			m_modifierToButton.put(modifier, button);
			// add listener
			button.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					// update modifiers
					if (button.getSelection()) {
						m_accelerator |= modifier;
					} else {
						m_accelerator &= ~modifier;
					}
					// show accelerator
					displayAccelerator();
				}
			});
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(m_title);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Internal
		//
		////////////////////////////////////////////////////////////////////////////
		private void displayAccelerator() {
			m_keyStrokeText.setText(AcceleratorPropertyEditor.getText(m_accelerator));
			// update modifiers buttons
			for (Map.Entry<Integer, Button> entry : m_modifierToButton.entrySet()) {
				int modifier = entry.getKey();
				Button button = entry.getValue();
				if ((m_accelerator & modifier) != 0) {
					button.setSelection(true);
				} else {
					button.setSelection(false);
				}
			}
			// update key code list
			m_keyCodeList.setSelection(new String[]{getKeyName(m_accelerator)});
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// KeyStroke utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the textual presentation of given accelerator.
	 */
	private static String getText(int accelerator) {
		KeyStroke keyStroke = SWTKeySupport.convertAcceleratorToKeyStroke(accelerator);
		return keyStroke.toString();
	}

	/**
	 * @return the source for given accelerator.
	 */
	private static String getSource(int accelerator) {
		String source = getText(accelerator);
		source = StringUtils.replace(source, "ALT+", "org.eclipse.swt.SWT.ALT | ");
		source = StringUtils.replace(source, "CTRL+", "org.eclipse.swt.SWT.CTRL | ");
		source = StringUtils.replace(source, "SHIFT+", "org.eclipse.swt.SWT.SHIFT | ");
		// check for character/keyCode
		int length = source.length();
		int index = StringUtils.lastIndexOf(source, ' ');
		if (index == length - 2) {
			source = source.substring(0, index) + " '" + source.substring(index + 1) + "'";
		} else if (index > 0) {
			source = source.substring(0, index) + " org.eclipse.swt.SWT." + source.substring(index + 1);
		} else {
			source = "org.eclipse.swt.SWT." + source;
		}
		// final resource
		return source;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Key codes
	//
	////////////////////////////////////////////////////////////////////////////
	private static java.util.List<String> m_keyFields;
	private static Map<Integer, String> m_keyCodeToName;
	private static Map<String, Integer> m_keyNameToCode;

	/**
	 * @return the name of key with given code.
	 */
	private static String getKeyName(int accelerator) {
		prepareKeyMaps();
		int keyCode = SWTKeySupport.convertAcceleratorToKeyStroke(accelerator).getNaturalKey();
		return m_keyCodeToName.get(keyCode);
	}

	/**
	 * @return the code of key with given name.
	 */
	private static int getKeyCode(String keyName) {
		prepareKeyMaps();
		return m_keyNameToCode.get(keyName);
	}

	/**
	 * Prepares {@link Map}'s for key code/name conversion.
	 */
	private static void prepareKeyMaps() {
		if (m_keyCodeToName == null) {
			m_keyFields = new ArrayList<>();
			m_keyCodeToName = new TreeMap<>();
			m_keyNameToCode = new TreeMap<>();
			// add fields
			ExecutionUtils.runLog(new RunnableEx() {
				@Override
				public void run() throws Exception {
					// add key codes from SWT
					for (Field field : SWT.class.getFields()) {
						String fieldName = field.getName();
						int fieldValue = field.getInt(null);
						if (hasBits(fieldValue, SWT.KEYCODE_BIT)) {
							m_keyFields.add(fieldName);
							m_keyCodeToName.put(fieldValue, fieldName);
							m_keyNameToCode.put(fieldName, fieldValue);
						}
					}
					// add numbers
					for (char c = '0'; c < '9'; c++) {
						String charName = Character.toString(c);
						int charValue = c;
						m_keyFields.add(charName);
						m_keyCodeToName.put(charValue, charName);
						m_keyNameToCode.put(charName, charValue);
					}
					// add characters
					for (char c = 'A'; c < 'Z'; c++) {
						String charName = Character.toString(c);
						int charValue = c;
						m_keyFields.add(charName);
						m_keyCodeToName.put(charValue, charName);
						m_keyNameToCode.put(charName, charValue);
					}
				}
			});
		}
	}

	/**
	 * @return <code>true</code> if given value has given bits set.
	 */
	private static boolean hasBits(int value, int bits) {
		return (value & bits) == bits;
	}
}
