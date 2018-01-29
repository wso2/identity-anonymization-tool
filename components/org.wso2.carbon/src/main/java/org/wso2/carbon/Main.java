package main.java.org.wso2.carbon;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;
import org.wso2.carbon.beans.Patterns;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private final static Charset ENCODING = StandardCharsets.UTF_8;
    private final static String directory =
            "/home/sathya/Downloads/VATTENFALLPROD-33/wso2is-5.3.0/repository/logs/logs";

    //Java characters that have to be escaped in regular expressions are: \.[]{}()*+-?^$|

    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            /* Expected arguments are not provided. */
            System.out.println(
                    "Usage: java -jar GDPR-Compliance-LogProcessor-1.0-SNAPSHOT.jar <username> <tenant-domain> <userstore-domain>");
            System.exit(0);
        } else {
            System.out.println("User details: " + args[0] + " " + args[1] + " " + args[2]);
        }

        Map<String, String> templatePatternData = new HashMap<>();
        templatePatternData.put(LogProcessorConstants.USERNAME, args[0]);
        templatePatternData.put(LogProcessorConstants.TENANT_DOMAIN, args[1]);
        //TODO: need a way to obtain tenant ID from provided tenant domain.
        templatePatternData.put(LogProcessorConstants.TENANT_ID, String.valueOf(-1234));
        if (StringUtils.equalsIgnoreCase(LogProcessorConstants.PRIMARY_USERSTORE_DOMAIN, args[2])) {
            templatePatternData.put(LogProcessorConstants.USERSTORE_DOMAIN, "");
        } else {
            templatePatternData.put(LogProcessorConstants.USERSTORE_DOMAIN, StringUtils.capitalize(args[2]));
        }

        /* Generate random string for the username. */
        String randomUUID = UUID.randomUUID().toString();

        /* Reading the list of patterns to be searched in logs from external configuration file. */
        Patterns patterns = readXML("conf/patterns.xml");
        List<Patterns.Pattern> patternList = patterns.getPattern();

        /* Iterating through the list of files inside the repository/logs directory. */
        Files.walk(Paths.get(directory)).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                String fileName = filePath.getFileName().toString();
                Path path = Paths.get(directory + File.separator + fileName);

                try (
                        BufferedReader reader = Files.newBufferedReader(path, ENCODING);
                        LineNumberReader lineReader = new LineNumberReader(reader);
                        BufferedWriter writer = new BufferedWriter(new FileWriter(directory + File.separator + fileName
                                + ".temp"))
                ) {
                    String line;
                    while ((line = lineReader.readLine()) != null) {

                        String replacement = line;
                        boolean patternMatched = false;

                        /* Check the line for any detectPattern matches. */
                        for (Patterns.Pattern pattern : patternList) {
                            String formattedDetectPattern = StrSubstitutor.replace(pattern.getDetectPattern(),
                                    templatePatternData).trim();
                            Pattern regexp = Pattern.compile(formattedDetectPattern);
                            Matcher matcher = regexp.matcher(line);

                            if (matcher.find()) {
                                // Pattern match hit.
                                patternMatched = true;
                                //log.info("Found [" + matcher.group() + "] starting at "
                                  //      + matcher.start() + " and ending at " + (matcher.end() - 1));
                                String formattedReplacePattern = StrSubstitutor.replace(pattern.getReplacePattern(),
                                        templatePatternData);
                                replacement = replacement.replaceAll(formattedReplacePattern, randomUUID);
                            }
                        }
                        if (patternMatched) {
                            writer.write(replacement + '\n');

                        } else {
                            writer.write(line + '\n');
                        }
                    }
                } catch (IOException ex) {
                    //log error
                }
            }
        });
    }


    private static Patterns readXML(String path) throws JAXBException {

        //log.info("Reading pattern configuration file...");
        JAXBContext jc = JAXBContext.newInstance(Patterns.class);

        File xml = new File(path);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Patterns patterns = (Patterns) unmarshaller.unmarshal(xml);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //log.info("Read successful.");
        return patterns;
    }
}
