package de.simonscholz.junit4converter.converters;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

/**
 * Converts any subclass of JTestCase by temporary renaming that class to
 * {@link CustomTestCaseConverter#CUSTOMTESTCASE_CLASSNAME}
 *
 */
public class CustomTestCaseConverter implements Converter {

	private static final String CUSTOMTESTCASE_CLASSNAME = "CustomTestCase";
	private final TestConversionHelper helper;
	private boolean wasConverted;

	CustomTestCaseConverter(ASTRewrite rewriter, ImportRewrite importRewriter) {
		this.helper = new TestConversionHelper(rewriter, importRewriter);
	}

	@Override
	public boolean isConvertable(TypeDeclaration typeDeclaration) {
		return helper.isTestCase(CUSTOMTESTCASE_CLASSNAME, typeDeclaration);
	}

	@Override
	public void convert(TypeDeclaration typeDeclaration) {
		wasConverted = true;
		helper.replaceCallsOfSuperClass(typeDeclaration);
	}

	@Override
	public boolean wasConverted() {
		return wasConverted;
	}

}
