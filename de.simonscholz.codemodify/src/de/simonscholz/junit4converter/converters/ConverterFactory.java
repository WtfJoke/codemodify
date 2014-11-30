package de.simonscholz.junit4converter.converters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

public class ConverterFactory {
	private ConverterFactory() {
	}

	public static boolean convert(AST ast, ASTRewrite rewriter,
			ImportRewrite importRewriter, TypeDeclaration typeDeclaration) {
		boolean wasConverted = false;
		for (Converter converter : getConverters(ast, rewriter, importRewriter)) {
			if (converter.isConveratable(typeDeclaration)) {
				wasConverted = true;
				converter.convert(typeDeclaration);
			}
		}
		return wasConverted;
	}

	private static List<Converter> getConverters(AST ast, ASTRewrite rewriter,
			ImportRewrite importRewriter) {
		List<Converter> converters = new ArrayList<>(2);
		converters.add(new JTestCaseConverter(rewriter, importRewriter));
		converters
				.add(new NoDBTestCaseConverter(ast, rewriter, importRewriter));
		return converters;
	}
}
