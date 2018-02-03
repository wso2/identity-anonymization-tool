package org.wso2.carbon.privacy.forgetme.logs;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;
import org.wso2.carbon.privacy.forgetme.logs.beans.Patterns;
import org.wso2.carbon.privacy.forgetme.logs.exception.LogProcessorException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogProcessor {

    private final static Charset ENCODING = StandardCharsets.UTF_8;
    private final static String DIRECTORY_PATH = "/home/sathya/Desktop/pseudonym/logs";

    private static Logger log = LoggerFactory.getLogger(LogProcessor.class);

    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            // Expected arguments are not provided.
            log.info("Usage: java -jar GDPR-Compliance-LogProcessor-1.0-SNAPSHOT.jar <username> <tenant-domain>" +
                    " <userstore-domain>");
            System.exit(0);
        } else {
            log.info("User details: " + args[0] + " " + args[1] + " " + args[2]);
        }

        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUsername(args[0]);
        userIdentifier.setTenantDomain(args[1]);
        userIdentifier.setUserStoreDomain(args[2]);
        userIdentifier.setPseudonym(UUID.randomUUID().toString());

        process(userIdentifier);
    }

    /**
     * Process the log files in the configured directory path to identify and replace the occurrences of the user
     * identifier with a pseudonym.
     *
     * @param userIdentifier
     * @throws LogProcessorException
     */
    private static void process(UserIdentifier userIdentifier) throws LogProcessorException {

        Map<String, String> templatePatternData = getTemplatePatternData(userIdentifier);
        LogProcessorReport logProcessorReport = new LogProcessorReport(DIRECTORY_PATH, "PDF");

        // Reading the list of patterns to be searched in logs from external configuration file.
        Patterns patterns = readXML("conf/patterns.xml");
        List<Patterns.Pattern> patternList = patterns.getPattern();

        // Iterating through the list of files inside the repository/logs directory.
        List<String> fileList = getFileList();
        for (String fileName : fileList) {
            Path path = Paths.get(DIRECTORY_PATH + File.separator + fileName);

            try (
                    BufferedReader reader = Files.newBufferedReader(path, ENCODING);
                    LineNumberReader lineReader = new LineNumberReader(reader);
                    BufferedWriter writer = new BufferedWriter(
                            new FileWriter(DIRECTORY_PATH + File.separator + fileName + ".temp"))
            ) {
                String line;
                while ((line = lineReader.readLine()) != null) {

                    String replacement = line;
                    boolean patternMatched = false;

                    // Check the line for any detectPattern matches.
                    for (Patterns.Pattern pattern : patternList) {
                        String formattedDetectPattern = StrSubstitutor.replace(pattern.getDetectPattern(),
                                templatePatternData).trim();
                        Pattern regexp = Pattern.compile(formattedDetectPattern);
                        Matcher matcher = regexp.matcher(replacement);

                        if (matcher.find()) {
                            // Pattern match hit.
                            patternMatched = true;
                            String formattedReplacePattern = StrSubstitutor.replace(pattern.getReplacePattern(),
                                    templatePatternData);

                            /* Here, if the replacePattern is not empty replace the username occurrences in the
                            line. If it is empty, it indicates that a possible match is found in the current line. */
                            if (StringUtils.isNotBlank(formattedReplacePattern)) {
                                replacement =
                                        replacement.replaceAll(formattedReplacePattern, userIdentifier.getPseudonym());
                                logProcessorReport.addToReport(fileName, lineReader.getLineNumber(), true);

                            } else {
                                logProcessorReport.addToReport(fileName, lineReader.getLineNumber(), false);
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

            // Replace original file with the temporary file.
            replaceFile(path);
            logProcessorReport.printReport();
        }
    }

    /**
     * Read xml file for regex pattern configurations.
     *
     * @param path Path to config file.
     * @return Patterns object.
     * @throws LogProcessorException
     */
    private static Patterns readXML(String path) throws LogProcessorException {

        log.info("Reading pattern configuration file...");
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Patterns.class);

            File xml = new File(path);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Patterns patterns = (Patterns) unmarshaller.unmarshal(xml);

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
    private static List<String> getFileList() throws LogProcessorException {

        ArrayList<String> fileNames = new ArrayList<>();
        try {
            Files.walk(Paths.get(DIRECTORY_PATH)).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    String fileName = filePath.getFileName().toString();
                    fileNames.add(fileName);
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