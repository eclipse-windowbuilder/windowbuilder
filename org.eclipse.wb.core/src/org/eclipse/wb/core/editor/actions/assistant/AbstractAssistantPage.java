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
package org.eclipse.wb.core.editor.actions.assistant;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.controls.CSpinner;
import org.eclipse.wb.core.controls.CSpinnerDeferredNotifier;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Abstract implementation of {@link ILayoutAssistantPage} with convenient function to create
 * property editors.
 *
 * @author lobas_av
 * @coverage core.editor.action.assistant
 */
public abstract class AbstractAssistantPage extends Composite implements ILayoutAssistantPage {
  private static final Object NO_VALUE = new Object();
  private final List<ObjectInfo> m_selection;
  private final List<PropertyInfo> m_properties = Lists.newArrayList();
  private boolean m_saving;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public AbstractAssistantPage(Composite parent, Object selection) {
    super(parent, SWT.NONE);
    // prepare selection
    if (selection instanceof List<?>) {
      m_selection = (List<ObjectInfo>) selection;
    } else {
      m_selection = Collections.singletonList((ObjectInfo) selection);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutAssistantPage
  //
  ////////////////////////////////////////////////////////////////////////////
  public final boolean isPageValid() {
    // validate of all objects
    for (ObjectInfo object : m_selection) {
      ObjectInfo parent = object.getParent();
      if (!parent.getChildren().contains(object)) {
        return false;
      }
    }
    return true;
  }

  public final void updatePage() {
    if (!m_saving) {
      // update all properties GUI bindings
      for (PropertyInfo property : m_properties) {
        property.showValue();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Abstract adapter for multiple properties with same title.
   */
  protected abstract class PropertyInfo {
    private final String m_property;
    private final String m_subProperty;
    private final String m_innerProperty;
    private List<Property> m_propertyList;
    private Object m_currentValue = Property.UNKNOWN_VALUE;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public PropertyInfo(String property) {
      // parse property name
      String[] parts;
      // sub property name
      {
        parts = StringUtils.split(property, '.');
        m_subProperty = parts.length == 2 ? parts[1] : null;
      }
      // inner property name
      {
        parts = StringUtils.split(parts[0], ':');
        m_innerProperty = parts.length == 2 ? parts[1] : null;
      }
      // property name
      m_property = parts[0];
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Shows current value in GUI.
     */
    public abstract void showValue();

    ////////////////////////////////////////////////////////////////////////////
    //
    // Save
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Uses {@link #setValue(Object)} to perform implementation specific save.
     */
    protected abstract void doSaveValue();

    /**
     * Subclasses use this method to perform save operation.
     */
    protected final void saveValue() {
      try {
        m_saving = true;
        doSaveValue();
      } finally {
        m_saving = false;
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Value
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return common value of properties.
     */
    protected final Object getValue() {
      prepareProperties();
      m_currentValue = ExecutionUtils.runObjectLog(new RunnableObjectEx<Object>() {
        public Object runObject() throws Exception {
          Object commonValue = NO_VALUE;
          for (Property property : m_propertyList) {
            Object value = property.getValue();
            if (commonValue == NO_VALUE) {
              commonValue = value;
            } else if (!ObjectUtils.equals(commonValue, value)) {
              return Property.UNKNOWN_VALUE;
            }
          }
          return commonValue;
        }
      }, null);
      return m_currentValue;
    }

    /**
     * Sets given value in all properties.
     */
    protected final void setValue(final Object value) {
      prepareProperties();
      if (m_currentValue != Property.UNKNOWN_VALUE && ObjectUtils.equals(m_currentValue, value)) {
        return;
      }
      m_currentValue = value;
      ObjectInfo firstObject = getEditObject();
      ExecutionUtils.run(firstObject, new RunnableEx() {
        public void run() throws Exception {
          for (Property property : m_propertyList) {
            property.setValue(value);
          }
        }
      });
    }

    /**
     * Initializes list of properties.
     */
    private void prepareProperties() {
      if (m_propertyList == null) {
        m_propertyList = Lists.newArrayList();
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            // prepare properties
            for (ObjectInfo object : m_selection) {
              Property property = object.getPropertyByTitle(m_property);
              if (property == null) {
                property = getReflectionProperty(object, m_property);
              }
              if (property == null) {
                property = getCustomProperty(object, m_property);
              }
              if (property != null) {
                m_propertyList.add(property);
              }
            }
            // check inner properties
            if (m_innerProperty != null) {
              int size = m_propertyList.size();
              for (int i = 0; i < size; i++) {
                Property property = m_propertyList.get(i);
                IComplexPropertyEditor editor = (IComplexPropertyEditor) property.getEditor();
                for (Property innerProperty : editor.getProperties(property)) {
                  if (m_innerProperty.equals(innerProperty.getTitle())) {
                    m_propertyList.set(i, innerProperty);
                  }
                }
              }
            }
            // check sub properties
            if (m_subProperty != null) {
              int size = m_propertyList.size();
              for (int i = 0; i < size; i++) {
                Property property = m_propertyList.get(i);
                m_propertyList.set(i, new SubFieldProperty(m_subProperty, property));
              }
            }
          }
        });
      }
    }
  }

  /**
   * @return {@link Property} association with given <code>object</code>.<code>propertyName</code>
   *         or <code>null</code>.
   */
  protected Property getCustomProperty(Object object, String propertyName) throws Exception {
    return null;
  }

  /**
   * @return {@link ObjectInfo} that need for work <code>startEdit()/endEdit()/commitEdit()</code>
   *         during modify properties.
   */
  protected ObjectInfo getEditObject() {
    return m_selection.get(0);
  }

  private static Property getReflectionProperty(Object object, String propertyName) {
    propertyName = StringUtils.capitalize(propertyName);
    Class<?> objectClass = object.getClass();
    String getName = "get" + propertyName;
    String setName = "set" + propertyName;
    Method getMethod = null;
    Method setMethod = null;
    // find getter and setter
    for (Method method : objectClass.getMethods()) {
      if (getMethod == null
          && getName.equals(method.getName())
          && method.getParameterTypes().length == 0) {
        getMethod = method;
        if (setMethod != null) {
          break;
        }
      } else if (setMethod == null
          && setName.equals(method.getName())
          && method.getParameterTypes().length == 1) {
        setMethod = method;
        if (getMethod != null) {
          break;
        }
      }
    }
    // create property
    if (getMethod != null && setMethod != null) {
      return new ReflectionProperty(getMethod, setMethod, object);
    }
    return null;
  }

  /**
   * Add property to list.
   */
  protected boolean add(PropertyInfo propertyInfo) {
    return m_properties.add(propertyInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static abstract class InternalProperty extends Property {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public InternalProperty() {
      super(null);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getTitle() {
      return null;
    }

    @Override
    public boolean isModified() throws Exception {
      return false;
    }
  }
  private static final class ReflectionProperty extends InternalProperty {
    private final Method m_getMethod;
    private final Method m_setMethod;
    private final Object m_thisObject;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ReflectionProperty(Method getMethod, Method setMethod, Object thisObject) {
      m_getMethod = getMethod;
      m_setMethod = setMethod;
      m_thisObject = thisObject;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Object getValue() throws Exception {
      return m_getMethod.invoke(m_thisObject);
    }

    @Override
    public void setValue(Object value) throws Exception {
      m_setMethod.invoke(m_thisObject, value);
    }
  }
  private static final class SubFieldProperty extends InternalProperty {
    private final String m_field;
    private final Property m_property;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SubFieldProperty(String field, Property property) {
      m_field = field;
      m_property = property;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Object getValue() throws Exception {
      Object mainValue = m_property.getValue();
      return ReflectionUtils.getFieldObject(mainValue, m_field);
    }

    @Override
    public void setValue(Object value) throws Exception {
      Object mainValue = m_property.getValue();
      ReflectionUtils.setField(mainValue, m_field, value);
      m_property.setValue(mainValue);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // String
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adapter for <code>String</code> property.
   */
  private final class StringPropertyInfo extends PropertyInfo {
    private final Text m_text;
    private boolean m_enableListener;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public StringPropertyInfo(String property, Text text) {
      super(property);
      m_text = text;
      m_enableListener = true;
      m_text.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          if (m_enableListener) {
            saveValue();
          }
        }
      });
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // PropertyInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void showValue() {
      try {
        m_enableListener = false;
        Object value = getValue();
        if (value instanceof String) {
          m_text.setText((String) value);
        } else {
          m_text.setText("");
        }
      } finally {
        m_enableListener = true;
      }
    }

    @Override
    protected void doSaveValue() {
      setValue(m_text.getText());
    }
  }

  /**
   * Creates editor for single <code>String</code> property.
   */
  protected final Text addStringProperty(Composite parent, String property, String title) {
    new Label(parent, SWT.NONE).setText(title);
    //
    Text text = new Text(parent, SWT.BORDER);
    GridDataFactory.create(text).hintHC(10).fillH();
    //
    StringPropertyInfo propertyInfo = new StringPropertyInfo(property, text);
    add(propertyInfo);
    //
    return text;
  }

  protected final Text addStringProperty(Composite parent, String property) {
    return addStringProperty(parent, property, property);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Boolean
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adapter for <code>boolean</code> property.
   */
  private final class BooleanPropertyInfo extends PropertyInfo {
    private final Button m_button;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public BooleanPropertyInfo(String property, Button button) {
      super(property);
      m_button = button;
      m_button.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          saveValue();
        }
      });
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // PropertyInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void showValue() {
      Object value = getValue();
      if (value instanceof Boolean) {
        Boolean boolValue = (Boolean) value;
        m_button.setSelection(boolValue.booleanValue());
      } else {
        m_button.setSelection(false);
      }
    }

    @Override
    protected void doSaveValue() {
      setValue(m_button.getSelection());
    }
  }

  /**
   * Creates editor for single <code>boolean</code> property.
   */
  protected final Button addBooleanProperty(Composite parent, String property, String title) {
    Button button = new Button(parent, SWT.CHECK);
    button.setText(title);
    //
    BooleanPropertyInfo propertyInfo = new BooleanPropertyInfo(property, button);
    add(propertyInfo);
    //
    return button;
  }

  protected final Button addBooleanProperty(Composite parent, String property) {
    return addBooleanProperty(parent, property, property);
  }

  /**
   * Creates {@link Group} with <code>boolean</code> properties.
   */
  protected final Group addBooleanProperties(Composite parent,
      String title,
      String[][] propertyAndTitleArray) {
    Group group = new Group(parent, SWT.NONE);
    GridLayoutFactory.create(group);
    group.setText(title);
    //
    for (int i = 0; i < propertyAndTitleArray.length; i++) {
      String[] propertyAndTitle = propertyAndTitleArray[i];
      Button button = addBooleanProperty(group, propertyAndTitle[0], propertyAndTitle[1]);
      GridDataFactory.create(button).fillH();
    }
    //
    return group;
  }

  /**
   * Creates {@link Group} with <code>boolean</code> properties.
   */
  protected final Group addBooleanProperties(Composite parent, String title, String[] propertyArray) {
    Group group = new Group(parent, SWT.NONE);
    GridLayoutFactory.create(group);
    group.setText(title);
    //
    for (int i = 0; i < propertyArray.length; i++) {
      String property = propertyArray[i];
      Button button = addBooleanProperty(group, property);
      GridDataFactory.create(button).fillH();
    }
    //
    return group;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Choice
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adapter for choosing single value.
   */
  private final class ChoicePropertyInfo extends PropertyInfo {
    private final List<Button> m_buttons = Lists.newArrayList();
    private final Listener m_listener = new Listener() {
      public void handleEvent(Event event) {
        saveValue();
      }
    };

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ChoicePropertyInfo(String property) {
      super(property);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public void addButton(Button button, Object value) {
      button.setData(value);
      button.addListener(SWT.Selection, m_listener);
      m_buttons.add(button);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // PropertyInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void showValue() {
      Object value = getValue();
      for (Button button : m_buttons) {
        Object buttonValue = button.getData();
        button.setSelection(ObjectUtils.equals(value, buttonValue));
      }
    }

    @Override
    protected void doSaveValue() {
      for (Button button : m_buttons) {
        if (button.getSelection()) {
          Object buttonValue = button.getData();
          setValue(buttonValue);
        }
      }
    }
  }

  /**
   * Creates {@link Group} for choosing one of the given values for property.
   */
  protected final Group addChoiceProperty(Composite parent,
      String property,
      String title,
      Object[][] titleAndValueArray) {
    Group group = new Group(parent, SWT.NONE);
    group.setText(title);
    GridLayoutFactory.create(group);
    //
    ChoicePropertyInfo propertyInfo = new ChoicePropertyInfo(property);
    add(propertyInfo);
    //
    for (int i = 0; i < titleAndValueArray.length; i++) {
      Button trueButton = new Button(group, SWT.RADIO);
      GridDataFactory.create(trueButton).fillH();
      //
      Object[] titleAndValue = titleAndValueArray[i];
      String buttonTitle = (String) titleAndValue[0];
      Object value = titleAndValue[1];
      //
      trueButton.setText(buttonTitle);
      propertyInfo.addButton(trueButton, value);
    }
    //
    return group;
  }

  /**
   * Creates {@link Group} for choosing one of the enum values for property.
   */
  protected final Group addEnumProperty(Composite parent,
      String property,
      String title,
      String enumClassName) {
    Object[][] titleAndValueArray;
    ClassLoader classLoader = GlobalState.getClassLoader();
    try {
      Class<?> enumClass = classLoader.loadClass(enumClassName);
      if (enumClass.isEnum()) {
        Enum<?>[] constants = (Enum<?>[]) enumClass.getEnumConstants();
        titleAndValueArray = new Object[constants.length][2];
        for (int i = 0; i < constants.length; i++) {
          Enum<?> constantValue = constants[i];
          titleAndValueArray[i][0] = constantValue.name();
          titleAndValueArray[i][1] = constantValue;
        }
      } else {
        titleAndValueArray = new Object[][]{{}};
      }
      return addChoiceProperty(parent, property, title, titleAndValueArray);
    } catch (ClassNotFoundException e) {
      DesignerPlugin.log(e);
      return new Group(parent, SWT.NONE);
    }
  }

  /**
   * Creates {@link Group} for choosing one of the class static fields values for property.
   */
  protected final Group addStaticFieldsProperty(Composite parent,
      String property,
      String title,
      String className,
      String fieldNames[]) {
    Object[][] titleAndValueArray = new Object[fieldNames.length][2];
    ClassLoader classLoader = GlobalState.getClassLoader();
    try {
      Class<?> clazz = classLoader.loadClass(className);
      for (int i = 0; i < fieldNames.length; i++) {
        titleAndValueArray[i][0] = fieldNames[i];
        titleAndValueArray[i][1] = ReflectionUtils.getFieldObject(clazz, fieldNames[i]);
      }
      return addChoiceProperty(parent, property, title, titleAndValueArray);
    } catch (ClassNotFoundException e) {
      DesignerPlugin.log(e);
      return new Group(parent, SWT.NONE);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Integer
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adapter for <code>int</code> property.
   */
  private final class IntegerPropertyInfo extends PropertyInfo {
    private final CSpinner m_spinner;
    private final Listener m_listener = new Listener() {
      public void handleEvent(Event event) {
        if (event.doit) {
          saveValue();
        }
      }
    };

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public IntegerPropertyInfo(String property, CSpinner spinner) {
      super(property);
      m_spinner = spinner;
      new CSpinnerDeferredNotifier(m_spinner, 500, m_listener);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // PropertyInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void showValue() {
      Object value = getValue();
      if (value instanceof Integer) {
        Integer intValue = (Integer) value;
        m_spinner.setSelection(intValue.intValue());
      } else {
        m_spinner.setSelection(m_spinner.getMinimum());
      }
    }

    @Override
    protected void doSaveValue() {
      setValue(m_spinner.getSelection());
    }
  }

  /**
   * Create editor for single <code>int</code> property with given min/max values.
   */
  protected final void addIntegerProperty(Composite parent,
      String property,
      String title,
      int minValue,
      int maxValue) {
    new Label(parent, SWT.NONE).setText(title);
    //
    CSpinner spinner = new CSpinner(parent, SWT.BORDER);
    GridDataFactory.create(spinner).hintHC(10);
    spinner.setMinimum(minValue);
    spinner.setMaximum(maxValue);
    //
    IntegerPropertyInfo propertyInfo = new IntegerPropertyInfo(property, spinner);
    add(propertyInfo);
  }

  /**
   * Create editor for single <code>int</code> property with valid interval
   * <code>[minValue, Integer.MAX_VALUE]</code>.
   */
  protected final void addIntegerProperty(Composite parent,
      String property,
      String title,
      int minValue) {
    addIntegerProperty(parent, property, title, minValue, Integer.MAX_VALUE);
  }

  /**
   * Create editor for single <code>int</code> property with valid interval
   * <code>[0, Integer.MAX_VALUE]</code>.
   */
  protected final void addIntegerProperty(Composite parent, String property, String title) {
    addIntegerProperty(parent, property, title, 0);
  }

  protected final void addIntegerProperty(Composite parent, String property) {
    addIntegerProperty(parent, property, property);
  }

  /**
   * Creates <code>int</code> properties.
   */
  protected final void addIntegerProperties(Composite parent, String[][] propertyAndTitleArray) {
    for (int i = 0; i < propertyAndTitleArray.length; i++) {
      String[] propertyAndTitle = propertyAndTitleArray[i];
      addIntegerProperty(parent, propertyAndTitle[0], propertyAndTitle[1]);
    }
  }

  /**
   * Creates {@link Group} for multiple <code>int</code> properties.
   */
  protected final Group addIntegerProperties(Composite parent,
      String title,
      String[][] propertyAndTitleArray) {
    Group group = new Group(parent, SWT.NONE);
    group.setText(title);
    GridLayoutFactory.create(group).columns(2);
    //
    for (int i = 0; i < propertyAndTitleArray.length; i++) {
      String[] propertyAndTitle = propertyAndTitleArray[i];
      addIntegerProperty(group, propertyAndTitle[0], propertyAndTitle[1]);
    }
    return group;
  }

  /**
   * Creates {@link Group} for multiple <code>int</code> properties.
   */
  protected final Group addIntegerProperties(Composite parent,
      String title,
      String[][] propertyAndTitleArray,
      int[] minumunValues) {
    Group group = new Group(parent, SWT.NONE);
    group.setText(title);
    GridLayoutFactory.create(group).columns(2);
    //
    for (int i = 0; i < propertyAndTitleArray.length; i++) {
      String[] propertyAndTitle = propertyAndTitleArray[i];
      addIntegerProperty(group, propertyAndTitle[0], propertyAndTitle[1], minumunValues[i]);
    }
    return group;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Double
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adapter for <code>double</code> property.
   */
  private final class DoublePropertyInfo extends PropertyInfo {
    private final CSpinner m_spinner;
    private final double m_multiplier;
    private final Listener m_listener = new Listener() {
      public void handleEvent(Event event) {
        if (event.doit) {
          saveValue();
        }
      }
    };

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public DoublePropertyInfo(String property, CSpinner spinner, double multiplier) {
      super(property);
      m_spinner = spinner;
      m_multiplier = multiplier;
      new CSpinnerDeferredNotifier(m_spinner, 500, m_listener);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // PropertyInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void showValue() {
      Object value = getValue();
      if (value instanceof Number) {
        Number numberValue = (Number) value;
        m_spinner.setSelection((int) (numberValue.doubleValue() * m_multiplier));
      } else {
        m_spinner.setSelection(m_spinner.getMinimum());
      }
    }

    @Override
    protected void doSaveValue() {
      setValue(m_spinner.getSelection() / m_multiplier);
    }
  }

  /**
   * Create editor for single <code>double</code> property with given min/max values.
   */
  protected final void addDoubleProperty(Composite parent,
      String property,
      String title,
      double minValue,
      double maxValue,
      int digits) {
    int multiplier = (int) Math.pow(10, digits);
    new Label(parent, SWT.NONE).setText(title);
    //
    CSpinner spinner = new CSpinner(parent, SWT.BORDER);
    GridDataFactory.create(spinner).hintHC(10);
    spinner.setMinimum((int) (minValue * multiplier));
    spinner.setMaximum((int) (maxValue * multiplier));
    spinner.setDigits(digits);
    //
    DoublePropertyInfo propertyInfo = new DoublePropertyInfo(property, spinner, multiplier);
    add(propertyInfo);
  }

  /**
   * Create editor for single <code>double</code> property with valid interval
   * <code>[minValue, Double.MAX_VALUE]</code>.
   */
  protected final void addDoubleProperty(Composite parent,
      String property,
      String title,
      double minValue) {
    addDoubleProperty(parent, property, title, minValue, Double.MAX_VALUE, 1);
  }

  /**
   * Create editor for single <code>double</code> property with valid interval
   * <code>[0, Double.MAX_VALUE]</code>.
   */
  protected final void addDoubleProperty(Composite parent, String property, String title) {
    addDoubleProperty(parent, property, title, 0);
  }

  protected final void addDoubleProperty(Composite parent, String property) {
    addDoubleProperty(parent, property, property, 0);
  }

  /**
   * Creates <code>double</code> properties.
   */
  protected final void addDoubleProperties(Composite parent, String[][] propertyAndTitleArray) {
    for (int i = 0; i < propertyAndTitleArray.length; i++) {
      String[] propertyAndTitle = propertyAndTitleArray[i];
      addDoubleProperty(parent, propertyAndTitle[0], propertyAndTitle[1]);
    }
  }

  /**
   * Creates {@link Group} for multiple <code>double</code> properties.
   */
  protected final Group addDoubleProperties(Composite parent,
      String title,
      String[][] propertyAndTitleArray) {
    Group group = new Group(parent, SWT.NONE);
    group.setText(title);
    GridLayoutFactory.create(group).columns(2);
    //
    for (int i = 0; i < propertyAndTitleArray.length; i++) {
      String[] propertyAndTitle = propertyAndTitleArray[i];
      addDoubleProperty(group, propertyAndTitle[0], propertyAndTitle[1]);
    }
    return group;
  }

  /**
   * Creates {@link Group} for multiple <code>double</code> properties.
   */
  protected final Group addDoubleProperties(Composite parent,
      String title,
      String[][] propertyAndTitleArray,
      double[] minumunValues) {
    Group group = new Group(parent, SWT.NONE);
    group.setText(title);
    GridLayoutFactory.create(group).columns(2);
    //
    for (int i = 0; i < propertyAndTitleArray.length; i++) {
      String[] propertyAndTitle = propertyAndTitleArray[i];
      addDoubleProperty(group, propertyAndTitle[0], propertyAndTitle[1], minumunValues[i]);
    }
    return group;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Filler
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final Label addFiller(Composite parent) {
    return new Label(parent, SWT.NONE);
  }
}