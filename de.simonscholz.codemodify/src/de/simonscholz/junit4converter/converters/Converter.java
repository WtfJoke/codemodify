package de.simonscholz.junit4converter.converters;

import org.eclipse.jdt.core.dom.TypeDeclaration;

interface Converter {

	boolean isConveratable(TypeDeclaration typeDeclaration);

	void convert(TypeDeclaration typeDeclaration);

	boolean wasConverted();
}
