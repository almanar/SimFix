/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;

import cofix.common.config.Constant;
import cofix.common.node.CodeBlock;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.core.match.CodeBlockMatcher;

/**
 * @author Jiajun
 * @datae Jun 29, 2017
 */
public class SimpleFilter {
	private List<CodeBlock> _candidates = new ArrayList<>();
	private CodeBlock _buggyCode = null;
	
	public SimpleFilter(CodeBlock buggyCode) {
		_buggyCode = buggyCode;
	}
	
	public List<Pair<CodeBlock, Float>> filter(String srcPath, double guard){
		List<String> files = JavaFile.ergodic(srcPath, new ArrayList<String>());
		CollectorVisitor collectorVisitor = new CollectorVisitor();
		for(String file : files){
			String content = JavaFile.readFileToString(file);
			CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(content, ASTParser.K_COMPILATION_UNIT);
			collectorVisitor.setUnit(unit);
			unit.accept(collectorVisitor);
		}
		return filter(guard);
	}
	
	private List<Pair<CodeBlock, Float>> filter(double guard){
		List<Pair<CodeBlock, Float>> filtered = new ArrayList<>();
		int delta = Constant.MAX_BLOCK_LINE - _buggyCode.getCurrentLine();
		delta = delta > 0 ? delta : 0;
		guard = guard + ((0.95 - guard) * delta / Constant.MAX_BLOCK_LINE );
		System.out.println("Real guard value : " + guard);
		for(CodeBlock block : _candidates){
			Float similarity = CodeBlockMatcher.getSimilarity(_buggyCode, block);
			if(similarity < guard){
				continue;
			}
			filtered.add(new Pair<CodeBlock, Float>(block, similarity));
		}
		
		Collections.sort(filtered, new Comparator<Pair<CodeBlock, Float>>() {
			@Override
			public int compare(Pair<CodeBlock, Float> o1, Pair<CodeBlock, Float> o2) {
				if(o1.getSecond() < o2.getSecond()){
					return 1;
				} else if(o1.getSecond() > o2.getSecond()){
					return -1;
				} else {
					return 0;
				}
			}
		});
		return filtered;
	}
	
	class CollectorVisitor extends ASTVisitor{
		
		private CompilationUnit _unit = null;
		
		public void setUnit(CompilationUnit unit){
			_unit = unit;
		}
		
		@Override
		public boolean visit(SimpleName node) {
//			String name = node.getFullyQualifiedName();
//			Pair<String, String> classAndMethodName = NodeUtils.getTypeDecAndMethodDec(node);
//			Type type = ProjectInfo.getVariableType(classAndMethodName.getFirst(), classAndMethodName.getSecond(), name);
//			Variable variable = new Variable(node, type, name);
//			if(_buggyCode.getVariables().containsKey(variable)){
//				ASTNode parent = node.getParent();
//				while(parent != null && !(parent instanceof MethodDeclaration)){
//					parent = parent.getParent();
//				}
//				// filter out anonymous classes
//				if(parent != null && !(parent.getParent() instanceof AnonymousClassDeclaration)){
//					int line = _unit.getLineNumber(node.getStartPosition());
//					CodeSearch codeSearch = new CodeSearch(_unit, line, _buggyCode.getCurrentLine());
//					CodeBlock codeBlock = new CodeBlock(_unit, codeSearch.getASTNodes());
//					_candidates.add(codeBlock);
//				}
//			}
			return true;
		}
		
	}
	
}
