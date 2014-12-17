package de.simonscholz.junit4converter.converters;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

public class JTestCaseConverter implements Converter {
	private static final String JTESTCASE_CLASSNAME = "JTestCase";
	private static final String JTESTCASE_QUALIFIEDNAME = "CH.obj.Libraries.UnitTesting.JTestCase";

	private final TestConversionHelper helper;
	private boolean wasModified;

	JTestCaseConverter(ASTRewrite rewriter, ImportRewrite importRewriter) {
		helper = new TestConversionHelper(rewriter, importRewriter);
	}

	@Override
	public boolean isConvertable(TypeDeclaration typeDeclaration) {
		return helper.isTestCase(JTESTCASE_CLASSNAME, typeDeclaration);
	}

	@Override
	public void convert(TypeDeclaration typeDeclaration) {
		wasModified = true;
		helper.removeSuperClass(typeDeclaration.getSuperclassType(),
				JTESTCASE_QUALIFIEDNAME);
		helper.addDBRule(typeDeclaration);
		helper.replaceCallsOfSuperClass(typeDeclaration);
	}

	@Override
	public boolean wasConverted() {
		return wasModified;
	}
}
