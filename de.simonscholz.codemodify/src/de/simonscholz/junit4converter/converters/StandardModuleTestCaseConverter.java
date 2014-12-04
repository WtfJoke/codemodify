package de.simonscholz.junit4converter.converters;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

import de.simonscholz.junit4converter.JUnit4Converter;

public class StandardModuleTestCaseConverter extends JUnit4Converter implements
		Converter {
	private static final String MODULETESTCASE_CLASSNAME = "StandardModuleTestCase";
	private static final String MODULETESTCASE_QUALIFIEDNAME = "CH.obj.Application.StandardModuleTestCase";
	private static final String MODULETEST_CLASSNAME = "StandardModuleTest";
	private static final String MODULETEST_QUALIFIEDNAME = "CH.obj.Application.StandardModuleTest";
	private static final String BEFORE_METHOD = "checkPreconditions";

	private final AST ast;
	private final ASTRewrite rewriter;
	private final ImportRewrite importRewriter;
	private boolean wasModified;

	StandardModuleTestCaseConverter(AST ast, ASTRewrite rewriter,
			ImportRewrite importRewriter) {
		this.ast = ast;
		this.rewriter = rewriter;
		this.importRewriter = importRewriter;
	}

	@Override
	public boolean isConveratable(TypeDeclaration typeDeclaration) {
		Type superclassType = typeDeclaration.getSuperclassType();
		if (superclassType != null && superclassType.isSimpleType()) {
			SimpleType superType = (SimpleType) superclassType;
			return MODULETESTCASE_CLASSNAME.equals(superType.getName()
					.getFullyQualifiedName());
		}
		return false;
	}

	@Override
	public void convert(TypeDeclaration typeDeclaration) {
		wasModified = true;
		replaceSuperClass(typeDeclaration.getSuperclassType());
		convertCheckPreConditionsIntoBefore(typeDeclaration.getMethods());
	}

	private void convertCheckPreConditionsIntoBefore(
			MethodDeclaration... methods) {
		for (MethodDeclaration method : methods) {
			String methodName = method.getName().getFullyQualifiedName();
			if (methodName.equals(BEFORE_METHOD)) {
				removeAnnotation(rewriter, method, OVERRIDE_ANNOTATION_NAME);
				createMarkerAnnotation(ast, rewriter, method,
						BEFORE_ANNOTATION_NAME);
				convertProtectedToPublic(ast, rewriter, method);
				importRewriter.addImport(BEFORE_ANNOTATION_QUALIFIED_NAME);
			}
		}
	}

	private void replaceSuperClass(Type superclassType) {
		SimpleType newNoDBTestRulesProviderSuperType = ast.newSimpleType(ast
				.newSimpleName(MODULETEST_CLASSNAME));
		rewriter.replace(superclassType, newNoDBTestRulesProviderSuperType,
				null);
		importRewriter.removeImport(MODULETESTCASE_QUALIFIEDNAME);
		importRewriter.addImport(MODULETEST_QUALIFIEDNAME);
		wasModified = true;
	}

	@Override
	public boolean wasConverted() {
		return wasModified;
	}
}
