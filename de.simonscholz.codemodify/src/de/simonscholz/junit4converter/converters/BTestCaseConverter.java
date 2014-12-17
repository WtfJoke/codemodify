package de.simonscholz.junit4converter.converters;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

public class BTestCaseConverter implements Converter {

	private final static String TESTCASE_NAME = "BTestCase";
	private final static String TESTCASE_QUALIFIEDNAME = "CH.obj.Libraries.UnitTesting.BTestCase";

	private final TestConversionHelper helper;
	private boolean wasConverted;

	BTestCaseConverter(ASTRewrite rewriter, ImportRewrite importRewriter) {
		this.helper = new TestConversionHelper(rewriter, importRewriter);
	}

	@Override
	public boolean isConvertable(TypeDeclaration typeDeclaration) {
		return helper.isTestCase(TESTCASE_NAME, typeDeclaration);
	}

	@Override
	public void convert(TypeDeclaration typeDeclaration) {
		wasConverted = true;
		helper.addRule(typeDeclaration);
		helper.removeSuperClass(typeDeclaration.getSuperclassType(),
				TESTCASE_QUALIFIEDNAME);
		helper.replaceCallsOfSuperClass(typeDeclaration);
	}

	@Override
	public boolean wasConverted() {
		return wasConverted;
	}

}
