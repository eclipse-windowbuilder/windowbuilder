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
package org.eclipse.wb.internal.core.databinding.wizards.autobindings;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang.ArrayUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Container for {@link AbstractDescriptor}'s.
 *
 * @author lobas_av
 * @coverage bindings.wizard.auto
 */
public final class DescriptorContainer {
  private final List<AbstractDescriptor> m_descriptors = Lists.newArrayList();
  private final List<AbstractDescriptor> m_defaults = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link List} with all descriptors.
   */
  public List<AbstractDescriptor> getDescriptors() {
    return m_descriptors;
  }

  /**
   * @return default {@link AbstractDescriptor} for given object.
   */
  public AbstractDescriptor getDefaultDescriptor(Object propertyDescriptor, boolean ensure) {
    if (m_defaults.size() == 1) {
      return m_defaults.get(0);
    }
    //
    for (AbstractDescriptor descriptor : m_defaults) {
      if (descriptor.isDefault(propertyDescriptor)) {
        return descriptor;
      }
    }
    //
    if (ensure) {
      if (!m_defaults.isEmpty()) {
        return m_defaults.get(0);
      }
      if (!m_descriptors.isEmpty()) {
        return m_descriptors.get(0);
      }
    }
    //
    return null;
  }

  private void addDescriptor(AbstractDescriptor descriptor) {
    m_descriptors.add(descriptor);
    if (descriptor.isDefault()) {
      m_defaults.add(descriptor);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parse descriptors XML file.
   *
   * @return {@link Map} with all descriptors.
   */
  public static Map<String, DescriptorContainer> parseDescriptors(InputStream stream,
      final ClassLoader classLoader,
      final IImageLoader imageLoader) throws Exception {
    final Map<String, DescriptorContainer> containers = Maps.newHashMap();
    //
    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
    parser.parse(stream, new DefaultHandler() {
      private DescriptorContainer m_container;
      private AbstractDescriptor m_descriptor;
      private Class<?> m_descriptorClass;

      //
      @Override
      public void startElement(String uri, String localName, String name, Attributes attributes)
          throws SAXException {
        try {
          if ("descriptors".equals(name)) {
            // create container
            m_container = new DescriptorContainer();
            containers.put(attributes.getValue("name"), m_container);
            m_descriptorClass = classLoader.loadClass(attributes.getValue("class"));
          } else if ("descriptor".equals(name)) {
            // create descriptor
            m_descriptor = (AbstractDescriptor) m_descriptorClass.newInstance();
          } else if (m_descriptor != null) {
            // fill attributes
            if (attributes.getLength() == 0) {
              // method without parameters
              ReflectionUtils.invokeMethod(m_descriptor, name + "()", ArrayUtils.EMPTY_OBJECT_ARRAY);
            } else {
              if (name.endsWith("Image")) {
                // special support for images
                try {
                  ReflectionUtils.invokeMethod(
                      m_descriptor,
                      name + "(org.eclipse.swt.graphics.Image)",
                      new Object[]{imageLoader.getImage(attributes.getValue("value"))});
                } catch (Throwable e) {
                  DesignerPlugin.log(
                      "DescriptorContainer: error load image " + attributes.getValue("value"),
                      e);
                }
              } else {
                // method with single String parameter
                ReflectionUtils.invokeMethod(
                    m_descriptor,
                    name + "(java.lang.String)",
                    new Object[]{attributes.getValue("value")});
              }
            }
          }
        } catch (Exception e) {
          throw new SAXException("startElement", e);
        }
      }

      @Override
      public void endElement(String uri, String localName, String name) throws SAXException {
        // clear state
        if ("descriptors".equals(name)) {
          m_container = null;
        } else if ("descriptor".equals(name)) {
          m_container.addDescriptor(m_descriptor);
          m_descriptor = null;
        }
      }
    });
    //
    return containers;
  }
}