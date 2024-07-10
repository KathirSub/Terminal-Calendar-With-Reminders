package edu.curtin.terminalgriddemo;

import org.python.core.PyException;
import org.python.util.PythonInterpreter;

//Obtained from Lectures 6B

public class ScriptHandler {
    public void runScript(String pythonScript) {

        System.out.println("Executing Python script: " + pythonScript);
        System.out.println("Starting Main Application, Please Wait....");
        try (PythonInterpreter interpreter = new PythonInterpreter()) {
            String script = pythonScript.substring(pythonScript.indexOf("\"") + 1, pythonScript.lastIndexOf("\""));
            interpreter.exec(script);
        } catch (PyException e) {
            System.out.println("Python script execution error: " + e.getMessage());
        }
    }
}
