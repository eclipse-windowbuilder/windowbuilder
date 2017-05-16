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
package org.eclipse.wb.internal.core.model.variable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.variable.description.FieldUniqueVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.text.StrSubstitutor;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Helper for working with variable names and acronyms in {@link VariableSupport}.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class NamesManager {
  public static final String NAME_PARAMETER = "variable.name";
  public static final String ACRONYM_PARAMETER = "variable.acronym";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private NamesManager() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Auto-rename of "text" property change
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets new name for given {@link AbstractSimpleVariableSupport} according new value of "text"
   * property and mode/template.
   */
  public static void renameForText(AbstractSimpleVariableSupport variableSupport,
      GenericProperty property,
      String text) throws Exception {
    JavaInfo javaInfo = variableSupport.getJavaInfo();
    IPreferenceStore preferences = javaInfo.getDescription().getToolkit().getPreferences();
    // check mode
    {
      int renameMode = preferences.getInt(IPreferenceConstants.P_VARIABLE_TEXT_MODE);
      // never rename
      if (renameMode == IPreferenceConstants.V_VARIABLE_TEXT_MODE_NEVER) {
        return;
      }
      // rename default
      if (renameMode == IPreferenceConstants.V_VARIABLE_TEXT_MODE_DEFAULT
          && !isDefaultName(javaInfo, variableSupport.getName())) {
        return;
      }
    }
    // prepare initial name
    String newName = getNameForText(javaInfo, text);
    if (newName == null) {
      return;
    }
    // decorate in variable type specific way
    newName = variableSupport.decorateTextName(newName);
    // generate unique name
    newName = javaInfo.getEditor().getUniqueVariableName(-1, newName, null);
    // do rename, if different name
    if (!variableSupport.getName().equals(newName)) {
      variableSupport.setName(newName);
    }
  }

  /**
   * @return <code>true</code> if given variable is is default one for component.
   */
  private static boolean isDefaultName(JavaInfo javaInfo, String name) {
    String baseName = StringUtils.substringBefore(name, "_").toLowerCase();
    return getName(javaInfo).equalsIgnoreCase(baseName);
  }

  /**
   * @return the basic name of variable (without prefix/suffix or uniqueness test) for given
   *         component and text, or <code>null</code> if no valid name can be generated.
   */
  private static String getNameForText(JavaInfo javaInfo, String text) {
    ComponentDescription description = javaInfo.getDescription();
    IPreferenceStore preferences = description.getToolkit().getPreferences();
    // prepare component class name for variable
    String classNameForVariable;
    {
      String qualifiedClassName = ReflectionUtils.getCanonicalName(description.getComponentClass());
      String shortClassName = CodeUtils.getShortClass(qualifiedClassName);
      classNameForVariable = StringUtilities.stripLeadingUppercaseChars(shortClassName, 1);
    }
    // prepare "text" part of template
    String textPart;
    {
      textPart = text.toLowerCase();
      textPart = WordUtils.capitalize(textPart);
      // remove HTML tags
      textPart = StringUtilities.stripHtml(textPart);
      // get first "wordsLimit" words to prevent too long identifiers
      {
        int wordsLimit = preferences.getInt(IPreferenceConstants.P_VARIABLE_TEXT_WORDS_LIMIT);
        if (wordsLimit > 0) {
          String[] strings = StringUtils.split(textPart);
          if (strings.length > wordsLimit) {
            textPart = "";
            for (int i = 0; i < wordsLimit; i++) {
              textPart += strings[i];
            }
          }
        }
      }
      // remove invalid characters
      textPart = StringUtilities.removeNonLatinCharacters(textPart);
      // is there any remaining symbol? :)
      if (textPart.length() == 0) {
        return null;
      }
    }
    // use template
    String name;
    {
      Map<String, String> valueMap = Maps.newTreeMap();
      {
        valueMap.put("class_name", classNameForVariable);
        valueMap.put("text", textPart);
        valueMap.put("default_name", getName(javaInfo));
        valueMap.put("class_acronym", getAcronym(javaInfo));
      }
      // use template
      String template = preferences.getString(IPreferenceConstants.P_VARIABLE_TEXT_TEMPLATE);
      name = StrSubstitutor.replace(template, valueMap);
    }
    // final modifications
    name = StringUtils.uncapitalize(name);
    return name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>null</code> if given template is valid, or some error message if no. For example
   *         invalid template may attempt to use unsupported variables (or in wrong case).
   */
  public static String validate(String template) {
    // prepare empty variables
    Map<String, String> valueMap = Maps.newTreeMap();
    {
      valueMap.put("class_name", "");
      valueMap.put("text", "");
      valueMap.put("default_name", "");
      valueMap.put("class_acronym", "");
    }
    // use template
    String evaluated = StrSubstitutor.replace(template, valueMap);
    if (evaluated.indexOf("$") != -1) {
      return evaluated;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the base variable name for given {@link JavaInfo}.
   */
  public static String getName(JavaInfo javaInfo) {
    ComponentDescription description = javaInfo.getDescription();
    // check type specific information
    {
      ComponentNameDescription nameDescription = getNameDescription(description);
      if (nameDescription != null) {
        return nameDescription.getName();
      }
    }
    // check component parameter
    {
      String name = JavaInfoUtils.getParameter(javaInfo, NAME_PARAMETER);
      if (!StringUtils.isEmpty(name)) {
        return name;
      }
    }
    // use default name
    {
      String qualifiedClassName = ReflectionUtils.getCanonicalName(description.getComponentClass());
      return getDefaultName(qualifiedClassName);
    }
  }

  /**
   * @return the default base variable name for given component class.
   */
  public static String getDefaultName(String qualifiedClassName) {
    String name = CodeUtils.getShortClass(qualifiedClassName);
    // check if class name has only upper case characters
    {
      boolean onlyUpper = true;
      for (int i = 0; i < name.length(); i++) {
        onlyUpper &= Character.isUpperCase(name.charAt(i));
      }
      if (onlyUpper) {
        return name.toLowerCase();
      }
    }
    // check if class name starts from lower case character
    if (Character.isLowerCase(name.charAt(0))) {
      return name + '_';
    }
    // remove all upper case characters from beginning except last one (most right from beginning)
    {
      int index = 0;
      {
        int maxIndex = name.length() - 1;
        while (index < maxIndex && Character.isUpperCase(name.charAt(index + 1))) {
          index++;
        }
      }
      name = name.substring(index);
    }
    //
    return StringUtils.uncapitalize(name);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Acronym
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the acronym for given {@link JavaInfo}.
   * @see #getDefaultAcronym(String) for examples of default acronyms
   */
  public static String getAcronym(JavaInfo javaInfo) {
    ComponentDescription description = javaInfo.getDescription();
    // check type specific information
    {
      ComponentNameDescription nameDescription = getNameDescription(description);
      if (nameDescription != null) {
        return nameDescription.getAcronym();
      }
    }
    // check component parameter
    {
      String acronym = JavaInfoUtils.getParameter(javaInfo, ACRONYM_PARAMETER);
      if (!StringUtils.isEmpty(acronym)) {
        return acronym;
      }
    }
    // use default acronym
    {
      String qualifiedClassName = ReflectionUtils.getCanonicalName(description.getComponentClass());
      return getDefaultAcronym(qualifiedClassName);
    }
  }

  /**
   * @return the default acronym of the given component class.
   *
   * <pre>
	 * org.eclipse.swt.widgets.Button    = btn
	 * javax.swing.JButton               = btn
	 * org.eclipse.swt.custom.StyledText = stldtxt
	 * </pre>
   */
  private static String getDefaultAcronym(String qualifiedClassName) {
    String shortClassName = CodeUtils.getShortClass(qualifiedClassName);
    String acronym = StringUtilities.stripLeadingUppercaseChars(shortClassName, 1).toLowerCase();
    // remove vowels
    acronym = StringUtils.replaceChars(acronym, "aeiouy", null);
    // remove sequential duplicate symbols
    acronym = StringUtilities.removeDuplicateCharacters(acronym);
    // check final result
    if (acronym.length() == 0) {
      return shortClassName;
    } else {
      return acronym;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Variable
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if {@link FieldUniqueVariableDescription} should be used instead of
   *         (and only of) {@link LocalUniqueVariableDescription}.
   */
  public static boolean shouldUseFieldInsteadOfLocal(ComponentDescription description) {
    // check type specific information
    {
      ComponentNameDescription nameDescription = getNameDescription(description);
      if (nameDescription != null) {
        return nameDescription.isAsField();
      }
    }
    // no specific information
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type specific names
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The id of {@link ComponentNameDescription} children in {@link IMemento}.
   */
  private static final String TYPE_DESCRIPTION_ID = "description";

  /**
   * @return the default {@link ComponentNameDescription} for given class.
   */
  public static ComponentNameDescription getDefaultNameDescription(String qualifiedClassName) {
    String name = getDefaultName(qualifiedClassName);
    String acronym = getDefaultAcronym(qualifiedClassName);
    return new ComponentNameDescription(qualifiedClassName, name, acronym);
  }

  /**
   * @return the type specific {@link ComponentNameDescription} for given
   *         {@link ComponentDescription}.
   */
  private static ComponentNameDescription getNameDescription(ComponentDescription description) {
    if (description == null || description.getComponentClass() == null) {
      return null;
    }
    String qualifiedClassName = ReflectionUtils.getCanonicalName(description.getComponentClass());
    return getNameDescription(description.getToolkit(), qualifiedClassName);
  }

  /**
   * @return the type specific {@link ComponentNameDescription} for given toolkit and class name.
   */
  public static ComponentNameDescription getNameDescription(ToolkitDescription toolkit,
      String componentClassName) {
    // check type specific descriptions
    List<ComponentNameDescription> nameDescriptions = getNameDescriptions(toolkit, false);
    for (ComponentNameDescription nameDescription : nameDescriptions) {
      if (nameDescription.getClassName().equals(componentClassName)) {
        return nameDescription;
      }
    }
    // no type specific name description
    return null;
  }

  /**
   * @return the {@link List} of component specific {@link ComponentNameDescription}'s.
   *
   * @param def
   *          is <code>true</code> if default descriptions should be returned
   */
  public static List<ComponentNameDescription> getNameDescriptions(ToolkitDescription toolkit,
      boolean def) {
    final List<ComponentNameDescription> descriptions = Lists.newArrayList();
    // prepare settings as String
    final String settingsString;
    {
      IPreferenceStore preferences = toolkit.getPreferences();
      if (def) {
        settingsString =
            preferences.getDefaultString(IPreferenceConstants.P_VARIABLE_TYPE_SPECIFIC);
      } else {
        settingsString = preferences.getString(IPreferenceConstants.P_VARIABLE_TYPE_SPECIFIC);
      }
      // check, may be no settings yet
      if (settingsString.length() == 0) {
        return descriptions;
      }
    }
    // read descriptions
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        XMLMemento rootMemento = XMLMemento.createReadRoot(new StringReader(settingsString));
        IMemento[] mementos = rootMemento.getChildren(TYPE_DESCRIPTION_ID);
        for (int i = 0; i < mementos.length; i++) {
          IMemento memento = mementos[i];
          // prepare description parameters
          String className = memento.getString("class");
          String name = memento.getString("name");
          String acronym = memento.getString("acronym");
          boolean asField = "true".equals(memento.getString("asField"));
          // add description
          descriptions.add(new ComponentNameDescription(className, name, acronym, asField));
        }
      }
    });
    return descriptions;
  }

  /**
   * Sets the {@link List} of default component specific {@link ComponentNameDescription}'s.
   * <p>
   * Should be used in toolkit providers.
   */
  public static void setDefaultNameDescriptions(ToolkitDescription toolkit,
      List<ComponentNameDescription> descriptions) {
    String settingsString = getDescriptionsAsString(descriptions);
    IPreferenceStore preferences = toolkit.getPreferences();
    preferences.setDefault(IPreferenceConstants.P_VARIABLE_TYPE_SPECIFIC, settingsString);
  }

  /**
   * Sets the {@link List} of component specific {@link ComponentNameDescription}'s.
   */
  public static void setNameDescriptions(ToolkitDescription toolkit,
      List<ComponentNameDescription> descriptions) {
    String settingsString = getDescriptionsAsString(descriptions);
    IPreferenceStore preferences = toolkit.getPreferences();
    preferences.setValue(IPreferenceConstants.P_VARIABLE_TYPE_SPECIFIC, settingsString);
  }

  /**
   * @return the {@link String} presentation of given {@link ComponentNameDescription}'s.
   */
  private static String getDescriptionsAsString(List<ComponentNameDescription> descriptions) {
    // fill memento
    final XMLMemento rootMemento = XMLMemento.createWriteRoot("descriptions");
    for (ComponentNameDescription nameDescription : descriptions) {
      IMemento memento = rootMemento.createChild(TYPE_DESCRIPTION_ID);
      memento.putString("class", nameDescription.getClassName());
      memento.putString("name", nameDescription.getName());
      memento.putString("acronym", nameDescription.getAcronym());
      memento.putString("asField", nameDescription.isAsField() ? "true" : "false");
    }
    // prepare as String
    return ExecutionUtils.runObject(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        StringWriter writer = new StringWriter();
        rootMemento.save(writer);
        return writer.toString();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ComponentNameDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Description for component specific name preferences.
   */
  public static final class ComponentNameDescription {
    private final String m_className;
    private String m_name;
    private String m_acronym;
    private boolean m_asField;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructors
    //
    ////////////////////////////////////////////////////////////////////////////
    public ComponentNameDescription(String className, String defaultName, String acronym) {
      this(className, defaultName, acronym, false);
    }

    public ComponentNameDescription(String className,
        String defaultName,
        String acronym,
        boolean asField) {
      m_className = className;
      m_name = defaultName;
      m_acronym = acronym;
      m_asField = asField;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the fully qualified class name of component.
     */
    public String getClassName() {
      return m_className;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Name
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the name of variable for this component.
     */
    public String getName() {
      return m_name;
    }

    /**
     * Sets the name of variable for this component.
     */
    public void setName(String name) {
      m_name = name;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Acronym
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the acronym for this component.
     */
    public String getAcronym() {
      return m_acronym;
    }

    /**
     * Sets the acronym for this component.
     */
    public void setAcronym(String acronym) {
      m_acronym = acronym;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Field
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return <code>true</code> if {@link FieldUniqueVariableDescription} should be used instead of
     *         (and only of) {@link LocalUniqueVariableDescription}.
     */
    public boolean isAsField() {
      return m_asField;
    }

    /**
     * Specifies if {@link FieldUniqueVariableDescription} should be used instead of (and only of)
     * {@link LocalUniqueVariableDescription}.
     */
    public void setAsField(boolean asField) {
      m_asField = asField;
    }
  }
}
