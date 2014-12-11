package de.simonscholz.junit4converter.converters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

public class ConverterFactory {
	private List<Converter> converters;

	private ConverterFactory(AST ast, ASTRewrite rewriter,
			ImportRewrite importRewriter) {
		converters = new ArrayList<>(4);
		converters.add(new JTestCaseConverter(rewriter, importRewriter));
		converters
				.add(new NoDBTestCaseConverter(ast, rewriter, importRewriter));
		converters.add(new StandardModuleTestCaseConverter(ast, rewriter,
				importRewriter));
		converters.add(new CustomTestCaseConverter(rewriter, importRewriter));
	}

	public static ConverterFactory create(AST ast, ASTRewrite rewriter,
			ImportRewrite importRewriter) {
		return new ConverterFactory(ast, rewriter, importRewriter);
	}

	public boolean convert(TypeDeclaration typeDeclaration) {
		boolean wasConverted = false;
		for (Converter converter : converters) {
			if (converter.isConvertable(typeDeclaration)) {
				converter.convert(typeDeclaration);
				wasConverted = true;
			}
		}
		return wasConverted;
	}
}
