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
package org.eclipse.wb.internal.rcp.databinding.xwt.parser;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.utils.xml.DocumentAttribute;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.DocumentModelVisitor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.PropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.xwt.Messages;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.AttributeDocumentEditor;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.BindingInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.ElementDocumentEditor;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.ObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.beans.BeansObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.widgets.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.widgets.WidgetsObserveTypeContainer;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 *
 * @author lobas_av
 *
 */
public final class DatabindingParser {
  private static final String BINDING = "{binding ";
  private final XmlObjectInfo m_xmlObjectRoot;
  private final DatabindingsProvider m_provider;
  private final BeansObserveTypeContainer m_beanContainer;
  private final WidgetsObserveTypeContainer m_widgetContainer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void parse(DatabindingsProvider provider) throws Exception {
    new DatabindingParser(provider.getXmlObjectRoot(), provider);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private DatabindingParser(XmlObjectInfo xmlObjectRoot, DatabindingsProvider provider)
      throws Exception {
    m_xmlObjectRoot = xmlObjectRoot;
    m_provider = provider;
    m_beanContainer = (BeansObserveTypeContainer) m_provider.getContainer(ObserveType.BEANS);
    m_widgetContainer = (WidgetsObserveTypeContainer) m_provider.getContainer(ObserveType.WIDGETS);
    //
    for (ObserveTypeContainer container : provider.getContainers()) {
      container.createObservables(m_xmlObjectRoot);
    }
    m_xmlObjectRoot.addBroadcastListener(new ObjectInfoTreeComplete() {
      public void invoke() throws Exception {
        m_xmlObjectRoot.removeBroadcastListener(this);
        parse();
      }
    });
  }

  private void parse() throws Exception {
    m_xmlObjectRoot.getElement().accept(new DocumentModelVisitor() {
      @Override
      public boolean visit(DocumentElement element) {
        if (element.getTag().equalsIgnoreCase("binding")) {
          DocumentElement propertyElement = element.getParent();
          if (propertyElement.getTag().equalsIgnoreCase("MultiBinding")) {
            // XXX
          } else {
            try {
              DocumentElement objectElement = propertyElement.getParent();
              String property =
                  StringUtils.substringAfter(propertyElement.getTag(), ".").toLowerCase();
              //
              BindingInfo binding = parseBinding(objectElement, property, getAttributes(element));
              if (binding != null) {
                binding.getConverter().parse(
                    m_provider,
                    element.getChild("Binding.converter", true));
                binding.getValidator().parse(
                    m_provider,
                    element.getChild("Binding.validationRules", true),
                    element.getChild("Binding.validationRule", true));
                binding.setDocumentEditor(new ElementDocumentEditor(binding, element));
              }
            } catch (Throwable e) {
              e.printStackTrace(); // XXX
            }
          }
        } else {
          for (DocumentAttribute attribute : element.getDocumentAttributes()) {
            String value = attribute.getValue();
            if (value.toLowerCase().startsWith(BINDING) && value.endsWith("}")) {
              try {
                Map<String, String> attributes =
                    getAttributes(value.substring(BINDING.length(), value.length() - 1));
                BindingInfo binding =
                    parseBinding(element, attribute.getName().toLowerCase(), attributes);
                if (binding != null) {
                  binding.getConverter().parse(
                      m_provider,
                      element.getRoot(),
                      attributes.get("converter"));
                  binding.getValidator().parse(
                      m_provider,
                      element.getRoot(),
                      attributes.get("validationrules"),
                      attributes.get("validationrule"));
                  binding.setDocumentEditor(new AttributeDocumentEditor(binding, attribute));
                }
              } catch (Throwable e) {
                e.printStackTrace(); // XXX
              }
            }
          }
        }
        return true;
      }
    });
    //
    for (BindingInfo binding : m_provider.getBindings0()) {
      binding.postParse();
    }
  }

  private BindingInfo parseBinding(DocumentElement element,
      String property,
      Map<String, String> attributes) throws Exception {
    WidgetBindableInfo target = m_widgetContainer.resolve(element);
    if (target == null) {
      m_provider.addWarning(
          MessageFormat.format(Messages.DatabindingParser_widgetNotFound, element),
          new Throwable());
      return null;
    }
    //
    WidgetPropertyBindableInfo targetProperty = target.resolvePropertyByText(property);
    if (targetProperty == null) {
      m_provider.addWarning(
          MessageFormat.format(Messages.DatabindingParser_widgetPropertyNotFound, element, property),
          new Throwable());
      return null;
    }
    //
    String path = attributes.get("path");
    String elementName = attributes.get("elementname");
    //
    if (elementName != null) {
      WidgetBindableInfo model = m_widgetContainer.resolve(elementName);
      if (model == null) {
        m_provider.addWarning(
            MessageFormat.format(Messages.DatabindingParser_widgetNotFound, elementName),
            new Throwable());
        return null;
      }
      //
      if (path.toLowerCase().startsWith("singleselection.(") && path.endsWith(")")) {
        if (property.equalsIgnoreCase("input")) {
          // XXX
        }
        // XXX
        return null;
      }
      //
      WidgetPropertyBindableInfo modelProperty = model.resolvePropertyByText(path.toLowerCase());
      if (modelProperty == null) {
        m_provider.addWarning(
            MessageFormat.format(Messages.DatabindingParser_widgetPropertyNotFound, element, path),
            new Throwable());
        return null;
      }
      //
      return createBinding(target, targetProperty, model, modelProperty, attributes);
    }
    //
    BeanBindableInfo model = null;
    String source = attributes.get("source");
    //
    if (source != null) {
      String sourceReference = StringUtils.substringBetween(source, "{StaticResource", "}").trim();
      model = (BeanBindableInfo) m_beanContainer.resolve(sourceReference);
    } else {
      model = m_beanContainer.getDataContext();
    }
    //
    if (model == null) {
      m_provider.addWarning(Messages.DatabindingParser_beanNotFound, new Throwable());
      return null;
    }
    //
    PropertyBindableInfo modelProperty =
        model.resolvePropertyReference("\"" + path.toLowerCase() + "\"");
    if (modelProperty == null) {
      m_provider.addWarning(
          MessageFormat.format(Messages.DatabindingParser_beanPropertyNotFound, path),
          new Throwable());
      return null;
    }
    //
    if (property.equalsIgnoreCase("input")) {
      // XXX
      return null;
    }
    //
    return createBinding(target, targetProperty, model, modelProperty, attributes);
  }

  private BindingInfo createBinding(BindableInfo target,
      BindableInfo targetProperty,
      BindableInfo model,
      BindableInfo modelProperty,
      Map<String, String> attributes) throws Exception {
    BindingInfo binding = new BindingInfo(target, targetProperty, model, modelProperty);
    binding.setMode(attributes.get("mode"));
    binding.setTrigger(attributes.get("updatesourcetrigger"));
    m_provider.getBindings0().add(binding);
    return binding;
  }

  private static Map<String, String> getAttributes(DocumentElement element) {
    Map<String, String> attributes = Maps.newHashMap();
    for (DocumentAttribute attribute : element.getDocumentAttributes()) {
      attributes.put(attribute.getName().toLowerCase(), attribute.getValue());
    }
    return attributes;
  }

  private static Map<String, String> getAttributes(String value) {
    Map<String, String> attributes = Maps.newHashMap();
    for (String pair : splitAttributePairs(value, " \t,")) {
      String[] values = StringUtils.split(pair, '=');
      if (values.length == 2) {
        attributes.put(values[0].toLowerCase(), values[1]);
      }
    }
    return attributes;
  }

  private static List<String> splitAttributePairs(String str, String separatorChars) {
    // StringUtils.split(str, separatorChars)
    List<String> attributePairs = Lists.newArrayList();
    int lastSeparatorPosition = 0;
    int innerCounter = 0;
    for (int i = 0; i < str.length(); i++) {
      char curChar = str.charAt(i);
      if (curChar == '{') {
        innerCounter++;
      } else if (curChar == '}') {
        --innerCounter;
      } else if (innerCounter == 0) {
        if (separatorChars.indexOf(curChar) >= 0) {
          String substring = str.substring(lastSeparatorPosition, i).trim();
          if (!StringUtils.isEmpty(substring)) {
            attributePairs.add(substring);
          }
          lastSeparatorPosition = i + 1;
        }
      }
    }
    if (lastSeparatorPosition < str.length()) {
      String substring = str.substring(lastSeparatorPosition).trim();
      if (!StringUtils.isEmpty(substring)) {
        attributePairs.add(substring);
      }
    }
    return attributePairs;
  }
}