/**
 * Copyright (C) 2015 Deveryware S.A. All Rights Reserved.
 */
package com.twillouer.lc;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 *
 */
@SupportedAnnotationTypes({ "lombok.Data" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class AnnotationDataProcessor extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationDataProcessor.class);

    /** Utilitaire pour accéder au système de fichiers */
    private Filer filer;

    /** Utilitaire pour afficher des messages lors de la compilation */
    private Messager messager;

    /**
     * Initialisation de l'Annotation Processor. Permet surtout de récupérer des références vers le Filer et le Messager
     */
    @Override
    public void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    /**
     * {@inheritDoc}
     *
     * @param annotations
     * @param roundEnv
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        Messager messager = processingEnv.getMessager();
        try {
            for (TypeElement te : annotations) {
                for (Element e : roundEnv.getElementsAnnotatedWith(te)) {
                    LOGGER.info("coucou " + e.getKind());
                    if (e.getKind() == ElementKind.CLASS) {
                        LOGGER.info("coucou " + e.toString());
                        TypeElement classElement = (TypeElement) e;
                        PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
                        LOGGER.info("coucou " + packageElement.getSimpleName().toString());
                        LOGGER.info("coucou " + packageElement.getQualifiedName().toString());
                        LOGGER.info("coucou " + packageElement.toString());

                        messager.printMessage(Diagnostic.Kind.NOTE, "Printing: " + e.toString());

                        emitFile(packageElement.getSimpleName().toString(), e.getSimpleName().toString());
                    }
                }
            }
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Error: " + e.toString());
        }

        return true;
    }

    public void emitFile(String packageName, String className) throws IOException
    {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .addStatement("$T.mock($N.class)", Mockito.class, className)
                .build();

        MethodSpec constructor = MethodSpec.methodBuilder("should_create_constructors")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Test.class)
                .returns(void.class)
                // .addStatement("final $N instance = new $N()", className, className)
                .addStatement("final $N instance = $T.mock($N.class)", className, Mockito.class, className)
                .build();

        final String testClassName = className + "LombokCoverageTest";
        TypeSpec helloWorld = TypeSpec.classBuilder(testClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value", "$S", "lombokTestProcessor").build())
                .addMethod(main)
                .addMethod(constructor)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, helloWorld).build();
        JavaFileObject javaFileObject = filer.createSourceFile(packageName + (packageName.isEmpty() ? "" : ".") + testClassName);
        final Writer writer = javaFileObject.openWriter();
        try {
            javaFile.writeTo(writer);
            // javaFile.writeTo(System.out);
            writer.flush();
        } finally {
            writer.close();
        }
    }
}
