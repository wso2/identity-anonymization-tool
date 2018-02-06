package org.wso2.carbon.privacy.forgetme.api.report;

/**
 * Report generator to be passed to each instruction to generate a report.
 * 
 */
public interface ReportAppender {

    /**
     * Appends a section to the report.
     *
     * @param format
     * @param data
     */
    void appendSection(String format, Object ... data);

    /**
     * Appends single line to the report
     *
     * @param format
     * @param data
     */
    void append(String format, Object ... data);

    /**
     * Appends a section end to the report section.
     * @param format
     * @param data
     */
    void appendSectionEnd(String format, Object ... data);
}
