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
package org.eclipse.wb.internal.swing.databinding.model.decorate;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.swing.databinding.Activator;

import org.apache.commons.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public final class DecorationUtils {
  private static final Map<String, BeanDecorationInfo> DECORATIONS = Maps.newHashMap();
  private static boolean m_loadDecorations;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link BeanDecorationInfo} for given {@code beanClass}.
   */
  public static BeanDecorationInfo getDecorationInfo(Class<?> beanClass) throws Exception {
    if (java.awt.Component.class.isAssignableFrom(beanClass)) {
      loadDecorations();
      while (beanClass != null) {
        BeanDecorationInfo decorationInfo = DECORATIONS.get(beanClass.getName());
        if (decorationInfo != null) {
          return decorationInfo;
        }
        beanClass = beanClass.getSuperclass();
      }
    }
    return null;
  }

  private static void loadDecorations() throws Exception {
    if (m_loadDecorations) {
      return;
    }
    m_loadDecorations = true;
    InputStream stream = Activator.getFile("templates/Decorations.xml");
    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
    parser.parse(stream, new DefaultHandler() {
      BeanDecorationInfo m_lastDecoration;

      @Override
      public void startElement(String uri, String localName, String name, Attributes attributes)
          throws SAXException {
        if (name.equals("decoration")) {
          BeanDecorationInfo parent = DECORATIONS.get(attributes.getValue("parent"));
          m_lastDecoration = new BeanDecorationInfo(parent);
          DECORATIONS.put(attributes.getValue("class"), m_lastDecoration);
        } else if (name.equals("preferred")) {
          m_lastDecoration.setPreferredProperties(getProperties(attributes));
        } else if (name.equals("advanced")) {
          m_lastDecoration.setAdvancedProperties(getProperties(attributes));
        } else if (name.equals("hidden")) {
          m_lastDecoration.setHiddenProperties(getProperties(attributes));
        }
      }
    });
    IOUtils.closeQuietly(stream);
  }

  private static String[] getProperties(Attributes attributes) {
    return attributes.getValue("properties").split(" ");
  }
}