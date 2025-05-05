package com.pmsconnect.mage.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogPattern {
    String regex;
    Pattern pattern;
    List<String> groupNames;

    public LogPattern(String template) {
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

    private void transform (String template) {
        // Regex to match either {field} or [{field}]
        Pattern placeholderPattern = Pattern.compile("(\\[)?\\{(.*?)}(])?");
        Matcher matcher = placeholderPattern.matcher(template);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String fieldName = matcher.group(2);
            this.groupNames.add(fieldName);

            // Preserve brackets if present
            String replacement = "";
            if (matcher.group(1) != null && matcher.group(3) != null)
                replacement = "\\\\[(.*?)\\\\]";
            else
                replacement = "(.*?)";

            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        // Escape remaining literals in the template (e.g., . or parentheses)
        this.regex = sb.toString().replace(".", "\\.").replace("(", "\\(").replace(")", "\\)");
    }
}
