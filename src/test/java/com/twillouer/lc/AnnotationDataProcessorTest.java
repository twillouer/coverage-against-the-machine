package com.twillouer.lc;

import com.google.testing.compile.CompilationRule;
import com.google.testing.compile.JavaFileObjects;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

import org.junit.Rule;
import org.junit.Test;

public class AnnotationDataProcessorTest {

    @Rule
    public CompilationRule compilationRule = new CompilationRule();

    @Test
    public void should_handle_data()
    {
        assert_().about(javaSource())
                .that(JavaFileObjects.forSourceString("HelloWorld", "@lombok.Data final class HelloWorld {}"))
                .processedWith(new AnnotationDataProcessor())
                .compilesWithoutError()
                .and()
                .generatesFiles(JavaFileObjects.forResource("HelloWorldLombokCoverageTest"));
    }
}