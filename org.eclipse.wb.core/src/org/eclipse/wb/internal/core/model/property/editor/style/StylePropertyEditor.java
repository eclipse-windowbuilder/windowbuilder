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
package org.eclipse.wb.internal.core.model.property.editor.style;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.EmptyProperty;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.IValueSourcePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.impl.BooleanStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.impl.BooleanUsingEqualsStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.impl.EnumerationStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.impl.MacroStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.impl.SelectionStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.impl.SelectionUsingEqualsStylePropertyImpl;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * The {@link PropertyEditor} for configure SWT styles.
 * 
 * @author lobas_av
 * @coverage core.model.property.editor
 */
public class StylePropertyEditor extends TextDisplayPropertyEditor
    implements
      IConfigurablePropertyObject,
      IComplexPropertyEditor,
      IValueSourcePropertyEditor,
      IClipboardSourceProvider {
  private static final String STYLE_TITLE = "Style";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_title = "Style";
  private String m_className;
  private Class<?> m_class;
  private final List<SubStylePropertyImpl> m_macroProperties = Lists.newArrayList();
  private final List<SubStylePropertyImpl> m_otherProperties = Lists.newArrayList();
  private SubStylePropertyImpl[] m_properties;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    return "[" + getSource(property, false, ", ") + "]";
  }

  public String getPropertyTitle() {
    return m_title;
  }

  private int getPriority() {
    return STYLE_TITLE.equals(m_title) ? 1 : 2;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IValueSourcePropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getValueSource(Object value) throws Exception {
    Property property = getPropertyForValue(value);
    return getSource(property);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardSourceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getClipboardSource(GenericProperty property) throws Exception {
    return getSource(property);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the current style value.
   */
  long getStyle(Property property) throws Exception {
    Number value = (Number) property.getValue();
    return value != null ? value.longValue() : 0;
  }

  /**
   * Sets the new value of given {@link SubStyleProperty}.
   */
  void setStyleValue(Property property, long newValue) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    String source = getSource(getPropertyForValue(newValue));
    genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
  }

  /**
   * @return the {@link Property} that has given value.
   */
  private static Property getPropertyForValue(final Object value) {
    return new EmptyProperty() {
      @Override
      public Object getValue() throws Exception {
        return value;
      }
    };
  }

  /**
   * @return the source that represents updated value of style property.
   */
  private String getSource(Property property) throws Exception {
    return getSource(property, true, " | ");
  }

  private String getSource(Property mainProperty, boolean addClassAndDefault, String separator)
      throws Exception {
    StringBuffer source = new StringBuffer();
    long macroFlag = 0;
    // handle macro properties
    for (SubStylePropertyImpl property : m_macroProperties) {
      String sFlag = property.getFlagValue(mainProperty);
      if (sFlag != null) {
        // add class prefix
        if (addClassAndDefault) {
          source.append(m_className);
          source.append('.');
        }
        // add flag
        source.append(sFlag);
        macroFlag = property.getFlag(sFlag);
        break;
      }
    }
    // handle other (set, select) properties
    for (SubStylePropertyImpl property : m_otherProperties) {
      String sFlag = property.getFlagValue(mainProperty);
      if (sFlag != null) {
        // skip current flag if it part of macro flag
        if (macroFlag != 0 && (macroFlag & property.getFlag(sFlag)) != 0) {
          continue;
        }
        // add separator if need
        if (source.length() != 0) {
          source.append(separator);
        }
        // add class prefix
        if (addClassAndDefault) {
          source.append(m_className);
          source.append('.');
        }
        // add flag
        source.append(sFlag);
      }
    }
    // use null (default), if no other flags
    if (addClassAndDefault && source.length() == 0) {
      return null;
    }
    return source.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // As string
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link String} presentation of this {@link StylePropertyEditor}, for tests.
   */
  public String getAsString() {
    StringBuilder builder = new StringBuilder();
    builder.append(m_className);
    builder.append("\n");
    for (SubStylePropertyImpl property : m_properties) {
      builder.append("\t");
      property.getAsString(builder);
      builder.append("\n");
    }
    return builder.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditorState state, Map<String, Object> parameters) throws Exception {
    // prepare class
    m_className = (String) parameters.get("class");
    m_class = state.getEditorLoader().loadClass(m_className);
    // prepare title
    if (parameters.containsKey("title")) {
      m_title = (String) parameters.get("title");
    }
    // prepare sub properties
    List<SubStylePropertyImpl> properties = Lists.newArrayList();
    configureSet(properties, state, parameters);
    configureSetUsingEquals(properties, state, parameters);
    configureMacro(properties, state, parameters);
    configureSelections(properties, state, parameters);
    configureSelectionUsingEquals(properties, state, parameters);
    configureEnums(properties, state, parameters);
    m_properties = properties.toArray(new SubStylePropertyImpl[properties.size()]);
  }

  private void configureSet(List<SubStylePropertyImpl> properties,
      EditorState state,
      Map<String, Object> parameters) throws Exception {
    if (parameters.containsKey("set")) {
      String[] setters = StringUtils.split((String) parameters.get("set"));
      // loop of all set's
      for (int i = 0; i < setters.length; i++) {
        // prepare flag name
        String[] names = StringUtils.split(setters[i], ':');
        String flagName = names[0];
        // prepare flag value
        Field field = getField(state, m_class, flagName);
        if (field == null) {
          continue;
        }
        long flag = field.getLong(null);
        // add property
        SubStylePropertyImpl property;
        if (names.length == 2) {
          property = new BooleanStylePropertyImpl(this, names[1], flagName, flag);
        } else {
          property = new BooleanStylePropertyImpl(this, flagName.toLowerCase(), flagName, flag);
        }
        properties.add(property);
        m_otherProperties.add(property);
      }
    }
  }

  private void configureSetUsingEquals(List<SubStylePropertyImpl> properties,
      EditorState state,
      Map<String, Object> parameters) throws Exception {
    if (parameters.containsKey("setUsingEqual")) {
      String[] setters = StringUtils.split((String) parameters.get("setUsingEqual"));
      // loop of all set's
      for (int i = 0; i < setters.length; i++) {
        // prepare flag name
        String[] names = StringUtils.split(setters[i], ':');
        String flagName = names[0];
        // prepare flag value
        Field field = getField(state, m_class, flagName);
        if (field == null) {
          continue;
        }
        long flag = field.getLong(null);
        // add property
        SubStylePropertyImpl property;
        if (names.length == 2) {
          property =
              new BooleanUsingEqualsStylePropertyImpl(this, names[1], flagName, flag, m_className);
        } else {
          property =
              new BooleanUsingEqualsStylePropertyImpl(this,
                  flagName.toLowerCase(),
                  flagName,
                  flag,
                  m_className);
        }
        properties.add(property);
        m_otherProperties.add(property);
      }
    }
  }

  private void configureMacro(List<SubStylePropertyImpl> properties,
      EditorState state,
      Map<String, Object> parameters) throws Exception {
    int macroIndex = 0;
    while (true) {
      // prepare "macro" key
      String key = "macro" + Integer.toString(macroIndex++);
      if (!parameters.containsKey(key)) {
        break;
      }
      // prepare all part's
      String[] values = StringUtils.split((String) parameters.get(key));
      // title
      String title = values[0];
      // prepare flag string values
      int flagCount = 0;
      String[] flagValues = new String[values.length - 1];
      for (int i = 0; i < flagValues.length; i++) {
        String flag = values[i + 1];
        if (getField(state, m_class, flag) == null) {
          continue;
        }
        flagValues[flagCount++] = flag;
      }
      // flag values
      long[] flags = new long[flagCount];
      String[] sFlags = new String[flagCount + 1];
      sFlags[flagCount] = "";
      for (int i = 0; i < flagCount; i++) {
        String flag = flagValues[i];
        flags[i] = m_class.getField(flag).getLong(null);
        sFlags[i] = flag;
      }
      // add property
      SubStylePropertyImpl property = new MacroStylePropertyImpl(this, title, flags, sFlags);
      properties.add(property);
      m_macroProperties.add(property);
    }
  }

  private void configureSelections(List<SubStylePropertyImpl> properties,
      EditorState state,
      Map<String, Object> parameters) throws Exception {
    int selectIndex = 0;
    while (true) {
      // prepare "select" key
      String key = "select" + Integer.toString(selectIndex++);
      if (!parameters.containsKey(key)) {
        break;
      }
      // prepare all part's
      String[] values = StringUtils.split((String) parameters.get(key));
      // title
      String title = values[0];
      // default value
      String defaultString = values[1];
      long defaultFlag;
      if (StringUtils.isNumeric(defaultString)) {
        defaultFlag = Long.parseLong(defaultString);
      } else {
        defaultFlag = m_class.getField(defaultString).getLong(null);
      }
      // prepare flag string values
      int flagCount = 0;
      String[] flagValues = new String[values.length - 2];
      for (int i = 0; i < flagValues.length; i++) {
        String flag = values[i + 2];
        if (!StringUtils.isNumeric(flag) && getField(state, m_class, flag) == null) {
          continue;
        }
        flagValues[flagCount++] = flag;
      }
      // flag values
      long[] flags = new long[flagCount];
      String[] sFlags = new String[flagCount];
      for (int i = 0; i < flagCount; i++) {
        String flag = flagValues[i];
        if (StringUtils.isNumeric(flag)) {
          flags[i] = Long.parseLong(flag);
        } else {
          flags[i] = m_class.getField(flag).getLong(null);
        }
        sFlags[i] = flag;
      }
      // add property
      SubStylePropertyImpl property =
          new SelectionStylePropertyImpl(this, title, flags, sFlags, defaultFlag);
      properties.add(property);
      m_otherProperties.add(property);
    }
  }

  private void configureSelectionUsingEquals(List<SubStylePropertyImpl> properties,
      EditorState state,
      Map<String, Object> parameters) throws Exception {
    int selectIndex = 0;
    while (true) {
      // prepare "select" key
      String key = "selectUsingEqual" + Integer.toString(selectIndex++);
      if (!parameters.containsKey(key)) {
        break;
      }
      // prepare all part's
      String[] values = StringUtils.split((String) parameters.get(key));
      // title
      String title = values[0];
      //
      // Default value is used in SelectionStylePropertyImpl, but is not used in SelectionUsingEqualsStylePropertyImpl
      //
      // default value
      //String defaultString = values[1];
      //long defaultFlag;
      //if (StringUtils.isNumeric(defaultString)) {
      //	defaultFlag = Long.parseLong(defaultString);
      //} else {
      //	defaultFlag = m_class.getField(defaultString).getLong(null);
      //}
      //
      // prepare flag string values
      int flagCount = 0;
      String[] flagValues = new String[values.length - 1];
      for (int i = 0; i < flagValues.length; i++) {
        String flag = values[i + 1];
        if (!StringUtils.isNumeric(flag) && getField(state, m_class, flag) == null) {
          continue;
        }
        flagValues[flagCount++] = flag;
      }
      // flag values
      long[] flags = new long[flagCount];
      String[] sFlags = new String[flagCount];
      for (int i = 0; i < flagCount; i++) {
        String flag = flagValues[i];
        if (StringUtils.isNumeric(flag)) {
          flags[i] = Long.parseLong(flag);
        } else {
          flags[i] = m_class.getField(flag).getLong(null);
        }
        sFlags[i] = flag;
      }
      // add property
      SubStylePropertyImpl property =
          new SelectionUsingEqualsStylePropertyImpl(this, title, flags, sFlags, m_className);
      properties.add(property);
      m_otherProperties.add(property);
    }
  }

  private void configureEnums(List<SubStylePropertyImpl> properties,
      EditorState state,
      Map<String, Object> parameters) throws Exception {
    int selectIndex = 0;
    while (true) {
      // prepare "enum" key
      String key = "enum" + Integer.toString(selectIndex++);
      if (!parameters.containsKey(key)) {
        break;
      }
      // prepare all part's
      String[] values = StringUtils.split((String) parameters.get(key));
      // title
      String title = values[0];
      // clear mask
      String mask16 = values[1]; // 0x[value]
      int clearMask = Integer.parseInt(mask16.substring(2), 16);
      // prepare flag string values
      int flagCount = 0;
      String[] flagValues = new String[values.length - 2];
      for (int i = 0; i < flagValues.length; i++) {
        String flag = values[i + 2];
        if (!StringUtils.isNumeric(flag) && getField(state, m_class, flag) == null) {
          continue;
        }
        flagValues[flagCount++] = flag;
      }
      // flag values
      long[] flags = new long[flagCount];
      String[] sFlags = new String[flagCount];
      for (int i = 0; i < flagCount; i++) {
        String flag = flagValues[i];
        if (StringUtils.isNumeric(flag)) {
          flags[i] = Long.parseLong(flag);
        } else {
          flags[i] = m_class.getField(flag).getLong(null);
        }
        sFlags[i] = flag;
      }
      // add property
      SubStylePropertyImpl property =
          new EnumerationStylePropertyImpl(this, title, flags, sFlags, clearMask);
      properties.add(property);
      m_otherProperties.add(property);
    }
  }

  private static Field getField(EditorState state, Class<?> baseClass, String name) {
    try {
      return baseClass.getField(name);
    } catch (NoSuchFieldException e) {
      state.addWarning(new EditorWarning("StylePropertyEditor: can not find field "
          + baseClass.getName()
          + "."
          + name, e));
      return null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  public Property[] getProperties(Property mainProperty) throws Exception {
    GenericProperty genericProperty = (GenericProperty) mainProperty;
    JavaInfo javaInfo = genericProperty.getJavaInfo();
    Property[] properties = (Property[]) javaInfo.getArbitraryValue(this);
    if (properties == null) {
      int length = m_properties.length;
      properties = new Property[length];
      for (int i = 0; i < length; i++) {
        properties[i] = new SubStyleProperty(mainProperty, m_properties[i]);
      }
      javaInfo.putArbitraryValue(this, properties);
    }
    return properties;
  }

  /**
   * Analyzes given {@link Property}'s and moves ones with {@link StylePropertyEditor} from
   * "Constructor" and "Factory" on top level, to simplify access for user.
   */
  public static void addStyleProperties(List<Property> properties) throws Exception {
    // find style properties
    for (ListIterator<Property> I = properties.listIterator(); I.hasNext();) {
      Property property = I.next();
      // check "constructor" property
      if (property instanceof ComplexProperty
          && ("Constructor".equals(property.getTitle()) || "Factory".equals(property.getTitle()))) {
        ComplexProperty complexProperty = (ComplexProperty) property;
        Property[] subProperties = complexProperty.getProperties();
        // loop of all sub properties
        for (Property subProperty : subProperties) {
          PropertyEditor editor = subProperty.getEditor();
          // check style property
          if (editor instanceof StylePropertyEditor) {
            GenericPropertyImpl constructorStyleProperty = (GenericPropertyImpl) subProperty;
            StylePropertyEditor styleEditor = (StylePropertyEditor) editor;
            String propertyTitle = styleEditor.getPropertyTitle();
            // create "style" property
            GenericPropertyImpl styleProperty =
                new GenericPropertyImpl(constructorStyleProperty, propertyTitle);
            styleProperty.setCategory(PropertyCategory.system(
                complexProperty.getCategory(),
                styleEditor.getPriority()));
            I.add(styleProperty);
          }
        }
      }
    }
  }

  /**
   * Contributes actions into {@link Property} context menu.
   */
  public void contributeActions(Property mainProperty,
      IMenuManager manager,
      String implementTitle,
      boolean isCascade) throws Exception {
    // prepare "implement" menu
    IMenuManager implementMenuManager = new MenuManager(implementTitle);
    if (isCascade) {
      // add all "boolean" properties
      for (SubStylePropertyImpl property : m_properties) {
        if (property instanceof BooleanStylePropertyImpl) {
          property.contributeActions(mainProperty, implementMenuManager);
        }
      }
      //
      implementMenuManager.add(new Separator());
      // add other properties
      for (SubStylePropertyImpl property : m_properties) {
        if (!(property instanceof BooleanStylePropertyImpl)) {
          IMenuManager subMenu = new MenuManager(property.getTitle());
          property.contributeActions(mainProperty, subMenu);
          implementMenuManager.add(subMenu);
        }
      }
    } else {
      for (SubStylePropertyImpl property : m_properties) {
        property.contributeActions(mainProperty, implementMenuManager);
      }
    }
    // add "implement" menu
    manager.appendToGroup(IContextMenuConstants.GROUP_LAYOUT, implementMenuManager);
  }

  public static void configureContributeActions(final JavaInfo hostInfo) {
    hostInfo.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == hostInfo) {
          Property property = hostInfo.getPropertyByTitle(STYLE_TITLE);
          if (property != null && property.getEditor() instanceof StylePropertyEditor) {
            IPreferenceStore preferences = hostInfo.getDescription().getToolkit().getPreferences();
            boolean isCascade =
                preferences.getBoolean(IPreferenceConstants.P_STYLE_PROPERTY_CASCADE_POPUP);
            //
            StylePropertyEditor editor = (StylePropertyEditor) property.getEditor();
            editor.contributeActions(property, manager, "Style", isCascade);
          }
        }
      }
    });
  }
}