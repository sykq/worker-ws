package org.psc.workerws.rules;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scripting.groovy.GroovyScriptFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class RuleEvaluator {

    @Value("classpath:rule1.groovy")
    private Resource rule1Resource;

    @Value("classpath:rule2.groovy")
    private Resource rule2Resource;

    @Value("classpath:rule3.groovy")
    private Resource rule3Resource;

    public boolean evaluate() throws IOException {
        var scriptWriter = new StringWriter();
        IOUtils.copy(rule1Resource.getInputStream(), scriptWriter, StandardCharsets.UTF_8);
        var scriptContent = scriptWriter.toString();

//        ScriptSource scriptSource = new StaticScriptSource(scriptContent);
//
//        GroovyScriptFactory scriptFactory = new GroovyScriptFactory("classpath:rule1.groovy");

        var binding = new Binding();
        binding.setVariable("param1", "hello");
        binding.setVariable("printParam", "Hi!");
        var shell = new GroovyShell(binding);
        var script = shell.parse(scriptContent);
        Boolean result = (Boolean) script.run();
        log.info(result.toString());
        return result;
    }

    public boolean evaluate2() throws IOException {
        var scriptWriter = new StringWriter();
        IOUtils.copy(rule2Resource.getInputStream(), scriptWriter, StandardCharsets.UTF_8);
        var scriptContent = scriptWriter.toString();

//        ScriptSource scriptSource = new StaticScriptSource(scriptContent);
//
//        GroovyScriptFactory scriptFactory = new GroovyScriptFactory("classpath:rule1.groovy");

        var binding = new Binding();
        binding.setVariable("param1", "hello");
        var shell = new GroovyShell(binding);
        var script = shell.parse(scriptContent);
        String result = (String) script.invokeMethod("getMeToo", new String[]{"hello", "bye"});
        log.info(result);
        return result.equals("got: hello and bye");
    }

    public boolean evaluate3() throws IOException {
        var scriptWriter = new StringWriter();
        IOUtils.copy(rule1Resource.getInputStream(), scriptWriter, StandardCharsets.UTF_8);
        var scriptContent = scriptWriter.toString();

        var binding = new Binding();
        binding.setVariable("param1", "hello");
        binding.setProperty("printParam", "printMe");
        var shell = new GroovyShell(binding);
        var script = shell.parse(scriptContent);
        Boolean evalResult = (Boolean) script.evaluate(scriptContent);
        log.info(evalResult.toString());
        return evalResult;
    }

    public boolean evaluate4() throws IOException {
        var scriptWriter = new StringWriter();
        IOUtils.copy(rule2Resource.getInputStream(), scriptWriter, StandardCharsets.UTF_8);
        var scriptContent = scriptWriter.toString();

        var binding = new Binding();
        binding.setVariable("param1", "hello");
        var shell = new GroovyShell(binding);
        var script = shell.parse(scriptContent);

        String result = (String) script.invokeMethod("getMe", new String[]{"hello"});
        log.info(result);
        return result.equals("got: hello");
    }

    public boolean evaluate5() throws IOException {
        var scriptWriter = new StringWriter();
        IOUtils.copy(rule3Resource.getInputStream(), scriptWriter, StandardCharsets.UTF_8);
        var scriptContent = scriptWriter.toString();

        var binding = new Binding();
        var shell = new GroovyShell(binding);
        var script = shell.parse(scriptContent);

        var run1Binding = new Binding();
        run1Binding.setVariable("param1", "ney");
        run1Binding.setVariable("printParam", "PTRIINT:MEE!!");
        script.setBinding(run1Binding);
        String result = (String) script.run();
        log.info(result);
        var firstRun = result.equals("ney");

        var run2Binding = new Binding();
        run2Binding.setVariable("param1", "OK");
        run2Binding.setVariable("printParam", "PTRIINT:MEE!! again");
        script.setBinding(run2Binding);
        result = (String) script.run();
        log.info(result);
        var secondRun = result.equals("OK");
        
        return firstRun && secondRun;
    }

    public boolean evaluateSpring() throws IOException {

        GroovyScriptFactory scriptFactory = new GroovyScriptFactory("classpath");

        return false;
    }
}
