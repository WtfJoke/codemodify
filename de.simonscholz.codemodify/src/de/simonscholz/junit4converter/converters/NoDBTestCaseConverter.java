package de.simonscholz.junit4converter.converters;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

public class NoDBTestCaseConverter implements Converter {

	private static final String NODB_TESTRULES_PROVIDER = "NoDBTestRulesProvider";
	private static final String NODB_TESTRULES_PROVIDER_QUALIFIEDNAME = "CH.obj.Application.NoDBTestRulesProvider";
	private static final String NODB_TESTCASE_CLASSNAME = "NoDBTestCase";
	private static final String NODB_TESTCASE_QUALIFIEDNAME = "CH.obj.Application.NoDBTestCase";
	private final AST ast;
	private final ASTRewrite rewriter;
	private final ImportRewrite importRewriter;
	private final TestConversionHelper helper;
	private boolean wasModified;

	NoDBTestCaseConverter(AST ast, ASTRewrite rewriter,
			ImportRewrite importRewriter) {
		this.ast = ast;
		this.rewriter = rewriter;
		this.importRewriter = importRewriter;
		this.helper = new TestConversionHelper(rewriter, importRewriter);
	}

	@Override
	public boolean isConvertable(TypeDeclaration typeDeclaration) {
		return helper.isTestCase(NODB_TESTCASE_CLASSNAME, typeDeclaration);
	}

	@Override
	public void convert(TypeDeclaration typeDeclaration) {
		replaceSuperClass(typeDeclaration.getSuperclassType());
	}

	@Override
	public boolean wasConverted() {
		return wasModified;
	}

	private void replaceSuperClass(Type superclassType) {
		SimpleType newNoDBTestRulesProviderSuperType = ast.newSimpleType(ast
				.newSimpleName(NODB_TESTRULES_PROVIDER));
		rewriter.replace(superclassType, newNoDBTestRulesProviderSuperType,
				null);
		importRewriter.removeImport(NODB_TESTCASE_QUALIFIEDNAME);
		importRewriter.addImport(NODB_TESTRULES_PROVIDER_QUALIFIEDNAME);
		wasModified = true;
	}

}
