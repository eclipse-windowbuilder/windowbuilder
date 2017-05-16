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
package org.eclipse.wb.internal.core.model.util.generic;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoAddProperties;
import org.eclipse.wb.internal.core.model.property.ITypedProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StringListPropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Helper to create top-level {@link Property} as wrapper for pair of {@link ObjectInfo} get/set
 * methods.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
abstract class ModelMethodPropertyAbstractSupport {
  protected final ObjectInfo object;
  private final String prefix;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ModelMethodPropertyAbstractSupport(ObjectInfo object, String prefix) {
    this.object = object;
    this.prefix = prefix;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Installation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures given {@link ObjectInfo} to create properties.
   */
  protected final void install() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        installEx();
      }
    });
  }

  private void installEx() throws Exception {
    Map<String, String> parameters = GlobalState.getParametersProvider().getParameters(object);
    for (String parameter : parameters.keySet()) {
      if (parameter.startsWith(prefix)) {
        createParameterProcessor().process(parameter);
      }
    }
  }

  protected abstract ParameterProcessor createParameterProcessor();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameter
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract class ParameterProcessor {
    // parameter parts
    protected String getterSignature;
    protected String setterSignature;
    protected Class<?> type;
    protected PropertyEditor propertyEditor;
    protected String title;
    protected PropertyCategory category = PropertyCategory.NORMAL;
    // prepared property features
    protected Method getter;
    protected Method setter;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public void process(String parameter) throws Exception {
      for (String part : StringUtils.split(parameter)) {
        processParameterPart(part);
      }
      // validate
      {
        String message = validate();
        if (message != null) {
          message += "\n" + parameter;
          EditorWarning warning = new EditorWarning(message, null);
          GlobalState.getOtherHelper().addWarning(warning);
          return;
        }
      }
      // OK, configure property
      configureProperty();
    }

    /**
     * Performs operations required to create {@link Property} with given parameters.
     */
    protected abstract void configureProperty();

    ////////////////////////////////////////////////////////////////////////////
    //
    // Parameters cycle
    //
    ////////////////////////////////////////////////////////////////////////////
    protected void processParameterPart(String part) throws Exception {
      if (part.startsWith("getter=")) {
        getterSignature = StringUtils.removeStart(part, "getter=");
      }
      if (part.startsWith("setter=")) {
        setterSignature = StringUtils.removeStart(part, "setter=");
      }
      if (part.startsWith("type=")) {
        String typeName = StringUtils.removeStart(part, "type=");
        type = ReflectionUtils.getClassByName(GlobalState.getClassLoader(), typeName);
        if (propertyEditor == null) {
          propertyEditor = GlobalState.getDescriptionHelper().getEditorForType(type);
        }
      }
      if (part.startsWith("editor=")) {
        String desc = StringUtils.removeStart(part, "editor=");
        parseEditor(desc);
      }
      if (part.startsWith("title=")) {
        title = StringUtils.removeStart(part, "title=");
      }
      if (part.startsWith("category=")) {
        String categoryText = StringUtils.removeStart(part, "category=");
        category = PropertyCategory.get(categoryText, category);
      }
    }

    /**
     * Attempts to parse {@link PropertyEditor} description.
     */
    private void parseEditor(String desc) {
      if (desc.startsWith("strings(")) {
        desc = StringUtils.substringBetween(desc, "strings(", ")");
        String[] strings = StringUtils.split(desc, ',');
        StringListPropertyEditor stringsPropertyEditor = new StringListPropertyEditor();
        stringsPropertyEditor.configure(strings);
        propertyEditor = stringsPropertyEditor;
      }
    }

    protected String validate() throws Exception {
      if (getterSignature == null
          || setterSignature == null
          || propertyEditor == null
          || title == null) {
        return "No 'getter' or 'setter' or 'type' or 'editor' or 'title' attributes: ";
      }
      // prepare getter
      processGetterSignature();
      getter = ReflectionUtils.getMethodBySignature(object.getClass(), getterSignature);
      if (getter == null) {
        return "Invalid '" + prefix + "' getter: " + getterSignature;
      }
      // prepare setter
      processSetterSignature();
      setter = ReflectionUtils.getMethodBySignature(object.getClass(), setterSignature);
      if (setter == null) {
        return "Invalid '" + prefix + "' setter: " + setterSignature;
      }
      // OK
      return null;
    }

    protected abstract void processGetterSignature();

    protected abstract void processSetterSignature();

    ////////////////////////////////////////////////////////////////////////////
    //
    // PropertyProcessor
    //
    ////////////////////////////////////////////////////////////////////////////
    protected abstract class PropertyProcessor {
      ////////////////////////////////////////////////////////////////////////////
      //
      // Constructor
      //
      ////////////////////////////////////////////////////////////////////////////
      protected PropertyProcessor() {
        object.addBroadcastListener(new ObjectInfoAddProperties() {
          public void invoke(ObjectInfo target, List<Property> properties) throws Exception {
            if (isPropertyTarget(target)) {
              Property property = createProperty(target);
              properties.add(property);
            }
          }
        });
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Property
      //
      ////////////////////////////////////////////////////////////////////////////
      private final Map<ObjectInfo, Property> m_properties =
          new WeakHashMap<ObjectInfo, Property>();

      private Property createProperty(ObjectInfo target) throws Exception {
        Property property = m_properties.get(target);
        if (property == null) {
          property = new ModelProperty(target);
          property.setCategory(category);
          m_properties.put(target, property);
        }
        return property;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Implementation
      //
      ////////////////////////////////////////////////////////////////////////////
      protected abstract boolean isPropertyTarget(ObjectInfo target);

      protected abstract Object getValue(ObjectInfo target) throws Exception;

      protected abstract void setValue(ObjectInfo target, Object value) throws Exception;

      ////////////////////////////////////////////////////////////////////////////
      //
      // ModelProperty
      //
      ////////////////////////////////////////////////////////////////////////////
      private class ModelProperty extends Property implements ITypedProperty {
        private final ObjectInfo target;

        ////////////////////////////////////////////////////////////////////////////
        //
        // Constructor
        //
        ////////////////////////////////////////////////////////////////////////////
        public ModelProperty(ObjectInfo target) {
          super(propertyEditor);
          this.target = target;
        }

        ////////////////////////////////////////////////////////////////////////////
        //
        // ITypedProperty
        //
        ////////////////////////////////////////////////////////////////////////////
        public Class<?> getType() {
          return type;
        }

        ////////////////////////////////////////////////////////////////////////////
        //
        // Property
        //
        ////////////////////////////////////////////////////////////////////////////
        @Override
        public String getTitle() {
          return title;
        }

        @Override
        public boolean isModified() throws Exception {
          return getValue() != UNKNOWN_VALUE;
        }

        @Override
        public Object getValue() throws Exception {
          return PropertyProcessor.this.getValue(target);
        }

        @Override
        public void setValue(Object value) throws Exception {
          PropertyProcessor.this.setValue(target, value);
        }
      }
    }
  }
}
