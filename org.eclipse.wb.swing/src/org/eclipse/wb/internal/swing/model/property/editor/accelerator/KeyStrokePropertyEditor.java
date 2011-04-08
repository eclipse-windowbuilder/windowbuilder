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
package org.eclipse.wb.internal.swing.model.property.editor.accelerator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.ModelMessages;

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

import org.apache.commons.lang.StringUtils;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import javax.swing.KeyStroke;

/**
 * Implementation of {@link PropertyEditor} for {@link KeyStroke}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class KeyStrokePropertyEditor extends TextDialogPropertyEditor {
  private static final int CTRL_MASK = InputEvent.CTRL_MASK | InputEvent.CTRL_DOWN_MASK;
  private static final int ALT_MASK = InputEvent.ALT_MASK | InputEvent.ALT_DOWN_MASK;
  private static final int SHIFT_MASK = InputEvent.SHIFT_MASK | InputEvent.SHIFT_DOWN_MASK;
  private static final int META_MASK = InputEvent.META_MASK | InputEvent.META_DOWN_MASK;
  private static final int ALT_GRAPH_MASK = InputEvent.ALT_GRAPH_MASK
      | InputEvent.ALT_GRAPH_DOWN_MASK;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new KeyStrokePropertyEditor();

  private KeyStrokePropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof KeyStroke) {
      return getText((KeyStroke) value);
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
    // set initial KeyStroke
    {
      Object value = property.getValue();
      if (value instanceof KeyStroke) {
        KeyStroke keyStroke = (KeyStroke) value;
        int modifiers = keyStroke.getModifiers();
        int keyCode = keyStroke.getKeyCode();
        boolean onKeyRelease = keyStroke.isOnKeyRelease();
        dialog.setKeyStroke(KeyStroke.getKeyStroke(keyCode, modifiers, onKeyRelease));
      } else {
        dialog.setKeyStroke(KeyStroke.getKeyStroke(0, 0));
      }
    }
    // open dialog
    if (dialog.open() == Window.OK) {
      KeyStroke keyStroke = dialog.getKeyStroke();
      GenericProperty genericProperty = (GenericProperty) property;
      // prepare source
      String source = getKeyStrokeSource(keyStroke);
      // update source
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
    private KeyStroke m_keyStroke;

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
     * Sets the {@link KeyStroke} to edit.
     */
    public void setKeyStroke(KeyStroke keyStroke) {
      m_keyStroke = keyStroke;
    }

    /**
     * @return the {@link KeyStroke} result.
     */
    public KeyStroke getKeyStroke() {
      return m_keyStroke;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    private Text m_keyStrokeText;
    private List m_keyCodeList;
    private final Map<Integer, Button> m_modifierToButton = Maps.newHashMap();

    @Override
    protected Control createDialogArea(Composite parent) {
      Composite area = new Composite(parent, SWT.NONE);
      GridDataFactory.create(area).grab().fill();
      GridLayoutFactory.create(area);
      // combination
      {
        {
          Label label = new Label(area, SWT.NONE);
          label.setText(ModelMessages.KeyStrokePropertyEditor_pressCombination);
        }
        {
          m_keyStrokeText = new Text(area, SWT.BORDER | SWT.READ_ONLY);
          GridDataFactory.create(m_keyStrokeText).grabH().fillH();
          m_keyStrokeText.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
              int unmodifiedAccelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(event);
              int keyCode = unmodifiedAccelerator & 0xFFFF;
              if (keyCode != 0) {
                int modifiers = 0;
                if ((unmodifiedAccelerator & SWT.CTRL) != 0) {
                  modifiers |= CTRL_MASK;
                }
                if ((unmodifiedAccelerator & SWT.ALT) != 0) {
                  modifiers |= ALT_MASK;
                }
                if ((unmodifiedAccelerator & SWT.SHIFT) != 0) {
                  modifiers |= SHIFT_MASK;
                }
                // set new KeyStroke
                m_keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
                displayKeyStroke();
              }
            }
          });
        }
      }
      // properties
      {
        Group propertiesGroup = new Group(area, SWT.NONE);
        GridDataFactory.create(propertiesGroup).grab().fill();
        GridLayoutFactory.create(propertiesGroup);
        propertiesGroup.setText(ModelMessages.KeyStrokePropertyEditor_keyStrokeProperties);
        // modifiers
        {
          Group modifiersGroup = new Group(propertiesGroup, SWT.NONE);
          GridDataFactory.create(modifiersGroup).grabH().fillH();
          GridLayoutFactory.create(modifiersGroup).columns(5);
          modifiersGroup.setText(ModelMessages.KeyStrokePropertyEditor_modifiers);
          addModifierButton(modifiersGroup, CTRL_MASK, "&Ctrl");
          addModifierButton(modifiersGroup, ALT_MASK, "&Alt");
          addModifierButton(modifiersGroup, SHIFT_MASK, "&Shift");
          addModifierButton(modifiersGroup, META_MASK, "&Meta");
          addModifierButton(modifiersGroup, ALT_GRAPH_MASK, "Alt &Gr");
        }
        // key code
        {
          Group keyGroup = new Group(propertiesGroup, SWT.NONE);
          GridDataFactory.create(keyGroup).grab().fill();
          GridLayoutFactory.create(keyGroup);
          keyGroup.setText(ModelMessages.KeyStrokePropertyEditor_keyCode);
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
            public void handleEvent(Event event) {
              int modifiers = m_keyStroke.getModifiers();
              boolean onKeyRelease = m_keyStroke.isOnKeyRelease();
              // prepare keyCode
              int keyCode;
              {
                String name = m_keyCodeList.getSelection()[0];
                keyCode = getKeyCode(name);
              }
              // update existing KeyStroke
              m_keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers, onKeyRelease);
              displayKeyStroke();
            }
          });
        }
      }
      //
      displayKeyStroke();
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
        public void handleEvent(Event event) {
          int modifiers = m_keyStroke.getModifiers();
          int keyCode = m_keyStroke.getKeyCode();
          boolean onKeyRelease = m_keyStroke.isOnKeyRelease();
          // update modifiers
          if (button.getSelection()) {
            modifiers |= modifier;
          } else {
            modifiers &= ~modifier;
          }
          // update existing KeyStroke
          m_keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers, onKeyRelease);
          displayKeyStroke();
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
    private void displayKeyStroke() {
      m_keyStrokeText.setText(KeyStrokePropertyEditor.getText(m_keyStroke));
      // update modifiers buttons
      for (Map.Entry<Integer, Button> entry : m_modifierToButton.entrySet()) {
        int modifier = entry.getKey();
        Button button = entry.getValue();
        if ((m_keyStroke.getModifiers() & modifier) != 0) {
          button.setSelection(true);
        } else {
          button.setSelection(false);
        }
      }
      // update key code list
      m_keyCodeList.setSelection(new String[]{getKeyName(m_keyStroke.getKeyCode())});
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // KeyStroke utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the textual presentation of given {@link KeyStroke}.
   */
  private static String getText(KeyStroke stroke) {
    try {
      // prepare modifiers
      String modifiersText = "";
      {
        int modifiers = stroke.getModifiers();
        if ((modifiers & CTRL_MASK) != 0) {
          modifiersText += "Ctrl+";
        }
        if ((modifiers & ALT_MASK) != 0) {
          modifiersText += "Alt+";
        }
        if ((modifiers & SHIFT_MASK) != 0) {
          modifiersText += "Shift+";
        }
        if ((modifiers & META_MASK) != 0) {
          modifiersText += "Meta+";
        }
        if ((modifiers & ALT_GRAPH_MASK) != 0) {
          modifiersText += "AltGr+";
        }
        // remove trailing '+'
        if (modifiersText.length() != 0) {
          modifiersText = StringUtils.substring(modifiersText, 0, -1);
        }
      }
      // add key
      int keyCode = stroke.getKeyCode();
      if (keyCode != KeyEvent.VK_UNDEFINED) {
        return modifiersText + "-" + getKeyName(keyCode);
      }
      // no key
      return modifiersText;
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
    return null;
  }

  /**
   * @return the source for given {@link KeyStroke}.
   */
  private static String getKeyStrokeSource(KeyStroke keyStroke) {
    // prepare modifiers source
    String modifiersSource = "";
    {
      int modifiers = keyStroke.getModifiers();
      if ((modifiers & CTRL_MASK) != 0) {
        modifiersSource += "java.awt.event.InputEvent.CTRL_MASK | ";
      }
      if ((modifiers & ALT_MASK) != 0) {
        modifiersSource += "java.awt.event.InputEvent.ALT_MASK | ";
      }
      if ((modifiers & SHIFT_MASK) != 0) {
        modifiersSource += "java.awt.event.InputEvent.SHIFT_MASK | ";
      }
      if ((modifiers & META_MASK) != 0) {
        modifiersSource += "java.awt.event.InputEvent.META_MASK | ";
      }
      if ((modifiers & ALT_GRAPH_MASK) != 0) {
        modifiersSource += "java.awt.event.InputEvent.ALT_GRAPH_MASK | ";
      }
      //
      if (modifiersSource.length() != 0) {
        modifiersSource = StringUtils.substring(modifiersSource, 0, -" | ".length());
      } else {
        modifiersSource = "0";
      }
    }
    // prepare key source
    String keyCodeSource;
    {
      String keyName = getKeyName(keyStroke.getKeyCode());
      if (keyName == null) {
        return null;
      }
      keyCodeSource = "java.awt.event.KeyEvent.VK_" + keyName;
    }
    // prepare source
    return "javax.swing.KeyStroke.getKeyStroke(" + keyCodeSource + ", " + modifiersSource + ")";
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
  private static String getKeyName(int keyCode) {
    prepareKeyMaps();
    return m_keyCodeToName.get(keyCode);
  }

  /**
   * @return the code of key with given name.
   */
  private static int getKeyCode(String keyName) {
    prepareKeyMaps();
    Integer value = m_keyNameToCode.get(keyName);
    if (value != null) {
      return value.intValue();
    }
    return KeyEvent.VK_UNDEFINED;
  }

  /**
   * Prepares {@link Map}'s for key code/name conversion.
   */
  private static synchronized void prepareKeyMaps() {
    if (m_keyCodeToName == null) {
      m_keyFields = Lists.newArrayList();
      m_keyCodeToName = Maps.newTreeMap();
      m_keyNameToCode = Maps.newTreeMap();
      // add fields
      try {
        int expected_modifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
          Field field = fields[i];
          if (field.getModifiers() == expected_modifiers
              && field.getType() == Integer.TYPE
              && field.getName().startsWith("VK_")) {
            String name = field.getName().substring(3);
            Integer value = (Integer) field.get(null);
            m_keyFields.add(name);
            m_keyCodeToName.put(value, name);
            m_keyNameToCode.put(name, value);
          }
        }
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
    }
  }
}
