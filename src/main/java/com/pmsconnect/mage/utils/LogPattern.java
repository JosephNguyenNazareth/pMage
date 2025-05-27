package com.pmsconnect.mage.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogPattern {
    String regex;
    Pattern pattern;
    List<String> groupNames;

    public LogPattern(String template) {
        this.groupNames = new ArrayList<>();
        this.transform(template);
        this.pattern = Pattern.compile(regex);
    }

    public String getRegex() {
        return regex;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public List<String> getGroupNames() {
        return groupNames;
    }

    private void transform(String template) {
        // Regex to match either {field} or [{field}]
        Pattern placeholderPattern = Pattern.compile("(\\[)?\\{(.*?)}(])?");
        Matcher matcher = placeholderPattern.matcher(template);

        StringBuffer sb = new StringBuffer();
        int lastEnd = 0;

        while (matcher.find()) {
            // Escape the literal text before the current match
            String literalPart = template.substring(lastEnd, matcher.start());
            sb.append(Pattern.quote(literalPart));

            String fieldName = matcher.group(2);
            this.groupNames.add(fieldName);

            // Add regex group for placeholder
            if (matcher.group(1) != null && matcher.group(3) != null)
                sb.append("\\[(.*?)\\]");
            else
                sb.append("(.*?)");

            lastEnd = matcher.end();
        }

        // Append and escape the remaining literal
        sb.append(Pattern.quote(template.substring(lastEnd)));
        this.regex = sb.toString();
    }
}
