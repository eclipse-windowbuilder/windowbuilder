package org.eclipse.wb.core.databinding.xsd.component;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

public final class ContextFactory {
	private ContextFactory() {
	}

	public static JAXBContext createContext() throws JAXBException {
		return JAXBContext.newInstance(ContextFactory.class.getPackageName(), ContextFactory.class.getClassLoader());
	}
}
