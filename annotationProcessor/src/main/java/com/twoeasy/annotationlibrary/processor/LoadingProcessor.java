  package com.twoeasy.annotationlibrary.processor;

import com.google.auto.service.AutoService;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.sun.source.util.Trees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.xml.internal.xsom.impl.Ref;
import com.twoeasy.annotationlibrary.annotation.Loading;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.twoeasy.annotationlibrary.annotation.Loading")
@AutoService(Processor.class)
public class LoadingProcessor extends AbstractProcessor {
    final String PACKAG_ENAME_CN = "cn";
    final String PACKAGE_NAME_WSJIU = "wsjiu";
    final String PACKAGE_NAME_TWOEASY = "twoEasy";
    final String PACKAGE_NAME_COMPONENT = "component";
    final String CLASS_NAME = "LoadingBeanner";
    final String METHOD_NAME_LOADING = "loading";
    final String METHOD_NAME_CANCEL = "cancel";
    final String THIS = "this";

    private Trees trees;
    private TreeMaker treeMaker;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        JavacProcessingEnvironment javacProcessingEnvironment = (JavacProcessingEnvironment) processingEnvironment;
        this.trees = Trees.instance(processingEnvironment);
        this.treeMaker = TreeMaker.instance(javacProcessingEnvironment.getContext());
    }



    @Deprecated
    public boolean Deprecatedprocess(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        JavacElements elementUtils = (JavacElements) processingEnv.getElementUtils();
        for(Element element : roundEnvironment.getElementsAnnotatedWith(Loading.class)) {
            Loading loading = element.getAnnotation(Loading.class);

            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) elementUtils.getTree(element);
            JCTree.JCBlock body = jcMethodDecl.getBody();
            jcMethodDecl.body = treeMaker.Block(0, List.of(
                    createLoadingJCStatement(elementUtils, loading.context()),
                    jcMethodDecl.body
                    )
            );
            JCTree.JCStatement[] jcStatements = new JCTree.JCStatement[body.getStatements().size() * 2];
            int index = 0;
            for (JCTree.JCStatement jcStatement : body.getStatements()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, jcStatement.toString());
                if(jcStatement instanceof JCTree.JCStatement.JCReturn) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "ss");
                    //jcStatement = treeMaker.Block(0, List.of(treeMaker.Exec(treeMaker.Apply(List.nil(), select, List.nil())), jcStatement));
                    jcStatements[index++] = createCancelJCStatement(elementUtils);
                }
                jcStatements[index++] = jcStatement;
            }
            JCTree.JCStatement[] jcStatementFill = new JCTree.JCStatement[index--];
            while(index >= 0) jcStatementFill[index] = jcStatements[index--];
            body.stats = List.from(jcStatementFill);
            jcMethodDecl.body = treeMaker.Block(0, List.of(jcMethodDecl.body, createCancelJCStatement(elementUtils)));
    }
        return false;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        JavacProcessingEnvironment javacProcessingEnvironment = (JavacProcessingEnvironment)processingEnv;
        JavacElements elementUtils = javacProcessingEnvironment.getElementUtils();
        for(Element element : roundEnvironment.getElementsAnnotatedWith(Loading.class)) {
            JCTree jcTree = elementUtils.getTree(element);
            Loading loading = element.getAnnotation(Loading.class);
            if(jcTree instanceof JCTree.JCMethodDecl) {
                JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
                JCTree.JCStatement loadingStatement = createLoadingJCStatement(elementUtils, loading.context());
                JCTree.JCStatement cancelStatement = createCancelJCStatement(elementUtils);
                // 将loading插入代码与方法体组成try的语句块
                JCTree.JCBlock tryBlock = treeMaker.Block(0, List.of(loadingStatement, jcMethodDecl.body));
                // cancel语句作为finally语句块，保证一定会执行
                JCTree.JCBlock finallyBlock = treeMaker.Block(0, List.of(cancelStatement));
                // try finally组成新的语句块作为方法体
                JCTree.JCTry jcTry = treeMaker.Try(tryBlock,List.nil(), finallyBlock);
                JCTree.JCBlock newBody = treeMaker.Block(0, List.of(jcTry));
                jcMethodDecl.body = newBody;
            }
        }
        return false;
    }

    public JCTree.JCStatement createCancelJCStatement(JavacElements elementUtils) {
        JCTree.JCIdent ident = treeMaker.Ident(elementUtils.getName(PACKAG_ENAME_CN));
        JCTree.JCFieldAccess select = treeMaker.Select(ident,
                elementUtils.getName(PACKAGE_NAME_WSJIU));
        select = treeMaker.Select(select,
                elementUtils.getName(PACKAGE_NAME_TWOEASY));
        select = treeMaker.Select(select,
                elementUtils.getName(PACKAGE_NAME_COMPONENT));
        select = treeMaker.Select(select,
                elementUtils.getName(CLASS_NAME));
        select = treeMaker.Select(select,
                elementUtils.getName(METHOD_NAME_CANCEL));
        return treeMaker.Exec(treeMaker.Apply(List.nil(), select, List.nil()));
    }

    public JCTree.JCStatement createLoadingJCStatement(JavacElements elementUtils, String context) {
        JCTree.JCIdent ident = treeMaker.Ident(elementUtils.getName(PACKAG_ENAME_CN));
        JCTree.JCFieldAccess select = treeMaker.Select(ident, elementUtils.getName(PACKAGE_NAME_WSJIU));
        select = treeMaker.Select(select, elementUtils.getName(PACKAGE_NAME_TWOEASY));
        select = treeMaker.Select(select, elementUtils.getName(PACKAGE_NAME_COMPONENT));
        select = treeMaker.Select(select, elementUtils.getName(CLASS_NAME));
        select = treeMaker.Select(select, elementUtils.getName(METHOD_NAME_LOADING));
        JCTree.JCMethodInvocation jcMethodInvocation;
        if(context == null || context.length() == 0) {
            jcMethodInvocation = treeMaker.Apply(List.nil(), select, List.of(treeMaker.Ident(elementUtils.getName(THIS))));
        }else {
            jcMethodInvocation = treeMaker.Apply(List.nil(), select, List.of(treeMaker.Ident(elementUtils.getName(context))));
        }
        return treeMaker.Exec(jcMethodInvocation);
    }


}
