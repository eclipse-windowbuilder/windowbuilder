package org.eclipse.wb.tests.designer.XML.model.generic;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.xml.model.utils.CopyPropertyTopSupport;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import org.apache.commons.lang.ArrayUtils;

/**
 * Test for {@link CopyPropertyTopSupport}.
 *
 * @author scheglov_ke
 */
public class CopyPropertyTopTest extends AbstractCoreTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_copyExisting() throws Exception {
		prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY, new String[]{
				"  <parameters>",
				"    <parameter name='x-copyPropertyTop from=enabled to=MyEnabled category=preferred'/>",
		"  </parameters>"});
		parse(
				"// filler filler filler filler filler filler",
				"<Shell>",
				"  <t:MyComponent wbp:name='component'/>",
				"</Shell>");
		refresh();
		CompositeInfo component = getObjectByName("component");
		// test property
		Property property = component.getPropertyByTitle("MyEnabled");
		assertNotNull(property);
		assertSame(PropertyCategory.PREFERRED, property.getCategory());
		assertEquals(Boolean.TRUE, property.getValue());
		// next time same Property should be returned
		assertSame(property, component.getPropertyByTitle("MyEnabled"));
	}

	public void test_ignoreNoParameters() throws Exception {
		prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY, new String[]{
				"  <parameters>",
				"    <parameter name='x-copyPropertyTop '/>",
		"  </parameters>"});
		CompositeInfo component = parse("<t:MyComponent/>");
		refresh();
		// test property
		Property property = component.getPropertyByTitle("no-matter");
		assertNull(property);
	}

	public void test_ignoreNotExisting() throws Exception {
		prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY, new String[]{
				"  <parameters>",
				"    <parameter name='x-copyPropertyTop from=noSuchProperty to=MyEnabled'/>",
		"  </parameters>"});
		CompositeInfo component = parse("<t:MyComponent/>");
		refresh();
		// test property
		Property property = component.getPropertyByTitle("MyEnabled");
		assertNull(property);
	}
}