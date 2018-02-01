package org.wso2.carbon.privacy.forgetme.logs;

public class LogProcessorReport {

    private StringBuilder stringBuilder;

    public LogProcessorReport() {

        stringBuilder = new StringBuilder();
    }

    public void addToReport(int lineNumber, String message, boolean isReplaced) {

        String lineContent;
        if (isReplaced) {
            lineContent = "Replaced line: " + lineNumber + ". " + message + '\n';
        } else {
            lineContent = "Possible match for the user identifier in line: " + lineNumber + ". " + message + "\n";
        }
        stringBuilder.append(lineContent);
    }

    public void printReport() {
        // print Report.
    }
}
