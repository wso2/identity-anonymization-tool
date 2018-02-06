package org.wso2.carbon.privacy.forgetme.logs.processor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.report.ReportAppender;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;
import org.wso2.carbon.privacy.forgetme.logs.LogProcessorConstants;
import org.wso2.carbon.privacy.forgetme.logs.beans.Patterns;
import org.wso2.carbon.privacy.forgetme.logs.exception.LogProcessorException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class LogFileProcessor {

    private final static Charset ENCODING = StandardCharsets.UTF_8;

    private static Logger log = LoggerFactory.getLogger(LogFileProcessor.class);


    public static void processFiles(UserIdentifier userIdentifier, ReportAppender reportAppender,
            List<Patterns.Pattern> patternList, List<File> fileList) throws LogProcessorException {


        Map<String, String> templatePatternData = getTemplatePatternData(userIdentifier);
        for (File file : fileList) {
            reportAppender.appendSection("File %s", file.getAbsolutePath());
            try (BufferedReader reader = Files.newBufferedReader(file.toPath(), ENCODING);
                    LineNumberReader lineReader = new LineNumberReader(reader);
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file.toPath().toString() + ".temp"))) {
                String line;
                while ((line = lineReader.readLine()) != null) {

                    String replacement = line;
                    boolean patternMatched = false;

                    // Check the line for any detectPattern matches.
                    for (Patterns.Pattern pattern : patternList) {
                        String formattedDetectPattern = StrSubstitutor
                                .replace(pattern.getDetectPattern(), templatePatternData).trim();
                        Pattern regexp = Pattern.compile(formattedDetectPattern);
                        Matcher matcher = regexp.matcher(replacement);

                        if (matcher.find()) {
                            // Pattern match hit.
                            patternMatched = true;
                            String formattedReplacePattern = StrSubstitutor
                                    .replace(pattern.getReplacePattern(), templatePatternData);

                            /* Here, if the replacePattern is not empty replace the username occurrences in the
                            line. If it is empty, it indicates that a possible match is found in the current line. */
                            if (StringUtils.isNotBlank(formattedReplacePattern)) {
                                replacement = replacement
                                        .replaceAll(formattedReplacePattern, userIdentifier.getPseudonym());
                                reportAppender.append("Replaced, %d, %b", lineReader.getLineNumber(), true);

                            } else {
                                reportAppender.append("Not Replaced, %d, %b", lineReader.getLineNumber(), true);
                            }
                        }
                    }
                    if (patternMatched) {
                        writer.write(replacement + '\n');

                    } else {
                        writer.write(line + '\n');
                    }
                }
            } catch (IOException ex) {
                log.error("Error occurred while file read/write operation.", ex);
            }

        }
    }

    /**
     * Read xml file for regex pattern configurations.
     *
     * @param xmlFile The config file.
     * @return Patterns object.
     * @throws LogProcessorException
     */
    public static Patterns readXML(File xmlFile) throws LogProcessorException {

        log.info("Reading pattern configuration file...");
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Patterns.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Patterns patterns = (Patterns) unmarshaller.unmarshal(xmlFile);

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            log.info("Read successful.");
            return patterns;
        } catch (JAXBException ex) {
            throw new LogProcessorException("Error occurred while unmarshalling xml content.", ex);
        }
    }

    /**
     * Replace original log file with the generated temp file.
     *
     * @param filePath Log file path.
     * @throws LogProcessorException
     */
    private static void replaceFile(Path filePath) throws LogProcessorException {

        String fileName = filePath.getFileName().toString();
        if (Files.exists(filePath)) {

            try {
                log.info("Deleting File From The Configured Path: " + filePath.toString());

                Files.delete(filePath);
                Path tempFilePath = Paths.get(filePath + ".temp");
                Files.move(tempFilePath, tempFilePath.resolveSibling(fileName));
                log.info("Renamed the temp file '" + fileName + ".temp' to '" + fileName + "'");

            } catch (IOException ex) {
                throw new LogProcessorException("Error occurred while delete/rename file operation.", ex);
            }
        }
    }

    /**
     * Get list of filenames in the configured directory path.
     *
     * @return
     * @throws LogProcessorException
     */
    public static List<File> getFileList(String path) throws LogProcessorException {

        ArrayList<File> fileNames = new ArrayList<>();
        try {
            Files.walk(Paths.get(path)).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    fileNames.add(filePath.toFile());
                }
            });
        } catch (IOException e) {
            throw new LogProcessorException("Error occurred while getting the file names from the directory path.", e);
        }
        return fileNames;
    }

    /**
     * Get actual data for configured templates in the regexes. E.g. ${username} in regex will be replaced with the
     * actual username.
     *
     * @param userIdentifier
     * @return Map of templates and their corresponding values.
     */
    private static Map<String, String> getTemplatePatternData(UserIdentifier userIdentifier) {

        HashMap<String, String> patternData = new HashMap<>();
        patternData.put(LogProcessorConstants.USERNAME, userIdentifier.getUsername());
        patternData.put(LogProcessorConstants.TENANT_DOMAIN, userIdentifier.getTenantDomain());
        //TODO: need a way to obtain tenant ID from provided tenant domain.
        patternData.put(LogProcessorConstants.TENANT_ID, String.valueOf(-1234));
        if (StringUtils.equalsIgnoreCase(LogProcessorConstants.PRIMARY_USERSTORE_DOMAIN,
                userIdentifier.getUserStoreDomain())) {
            patternData.put(LogProcessorConstants.USERSTORE_DOMAIN, "");
        } else {
            patternData.put(LogProcessorConstants.USERSTORE_DOMAIN,
                    StringUtils.capitalize(userIdentifier.getUserStoreDomain()));
        }
        return patternData;
    }
}