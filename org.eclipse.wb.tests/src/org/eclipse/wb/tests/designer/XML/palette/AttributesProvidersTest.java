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
package org.eclipse.wb.tests.designer.XML.palette;

import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.xml.editor.palette.model.AttributesProvider;
import org.eclipse.wb.internal.core.xml.editor.palette.model.AttributesProviders;
import org.eclipse.wb.tests.designer.TestUtils;

import org.eclipse.core.runtime.IConfigurationElement;

import static org.assertj.core.api.Assertions.assertThat;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Tests for {@link AttributesProviders}.
 * 
 * @author scheglov_ke
 */
public class AttributesProvidersTest extends AbstractPaletteTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // PaletteManager
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AttributesProviders#get(IConfigurationElement)}.
   */
  public void test_getIConfigurationElement() throws Exception {
    String POINT_ID = "org.eclipse.wb.tests.testPoint";
    try {
      // add dynamic extension
      {
        String contribution = "<testObject attr='someValue'/>";
        TestUtils.addDynamicExtension(POINT_ID, contribution);
      }
      // prepare IConfigurationElement
      IConfigurationElement configurationElement;
      {
        List<IConfigurationElement> elements =
            ExternalFactoriesHelper.getElements(POINT_ID, "testObject");
        assertThat(elements).hasSize(1);
        configurationElement = elements.get(0);
      }
      // test AttributesProvider
      AttributesProvider provider = AttributesProviders.get(configurationElement);
      assertEquals("someValue", provider.getAttribute("attr"));
      assertNull(provider.getAttribute("noSuchAttribute"));
    } finally {
      TestUtils.removeDynamicExtension(POINT_ID);
    }
  }

  /**
   * Test for {@link AttributesProviders#get(Attributes)}.
   */
  public void test_getXML() throws Exception {
    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
    parser.parse(
        new InputSource(new StringReader("<rootElement attr='someValue'/>")),
        new DefaultHandler() {
          @Override
          public void startElement(String uri, String localName, String name, Attributes attributes)
              throws SAXException {
            AttributesProvider provider = AttributesProviders.get(attributes);
            assertEquals("someValue", provider.getAttribute("attr"));
            assertNull(provider.getAttribute("noSuchAttribute"));
          }
        });
  }

  /**
   * Test for {@link AttributesProviders#get(Map)}.
   */
  public void test_getMap() throws Exception {
    Map<String, String> attributes = ImmutableMap.of("attr", "someValue");
    AttributesProvider provider = AttributesProviders.get(attributes);
    assertEquals("someValue", provider.getAttribute("attr"));
    assertNull(provider.getAttribute("noSuchAttribute"));
  }
}
