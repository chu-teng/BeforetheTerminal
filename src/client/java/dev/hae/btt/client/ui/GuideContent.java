package dev.hae.btt.client.ui;

import dev.doctor4t.wathe.api.Role;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;








public final class GuideContent {

    private GuideContent() {}











    public enum Kind { SECTION, HEADING, BODY }


    public record Line(Kind kind, String text, int color) {}


    public static final int ACCENT = 0xFFF2B84B;

    public static final int INK = 0xFF392B1B;


    private static final String[] RULE_LABELS =
            {"类别", "席位", "阵营", "理智", "狂气", "道具", "能力", "处决", "死亡", "尾声", "结局"};


    private static final Pattern ENTRY = Pattern.compile("^(\\d+)?\\s*【([^】]+)】\\s*(.*)$");

    private static final Pattern ENTRY_LABEL = Pattern.compile("^(\\d+)\\s*[、.．]\\s*([^：:]{1,16})[：:]\\s*(.*)$");

    public record Chapter(String id, List<Line> lines) {
        public Text title() {
            return Text.translatable("btt.guide." + id + ".title");
        }
    }





    private static final String[][] SECTIONS = {

            {"rules", "序言", "角色", "流程", "能力", "场景", "模式", "奇遇"},


            {"items", "免费道具", "乘客基础道具", "凶手基础道具"},

            {"passengers", "执法乘客", "行动乘客", "信息乘客", "辅助乘客"},
            {"killers", "主犯凶手", "从犯凶手"},
            {"neutrals", "独行中立", "外人中立", "狂人中立"},
            {"traits", "关系特质", "通用特质", "乘客特质", "凶手特质", "独行特质", "外人特质"},
    };


    public static int roleOrder(Role role) {
        if (role == null) return Integer.MAX_VALUE;
        String name = SeatAssignment.displayName(role).getString();
        int order = 0;
        for (String[] group : SECTIONS) {
            if (!group[0].equals("passengers") && !group[0].equals("killers") && !group[0].equals("neutrals")) continue;
            for (int i = 1; i < group.length; i++) {
                String body = Text.translatable(langKey(group[0], group[i], "body")).getString();
                for (String line : body.split("\n")) {
                    Matcher entry = ENTRY.matcher(line.trim());
                    if (!entry.matches()) continue;
                    String heading = entry.group(2);
                    if (heading.equals(name) || cjkOnly(heading).equals(name)
                            || heading.substring(cjkOnly(heading).length()).trim().equalsIgnoreCase(name)) return order;
                    order++;
                }
            }
        }
        return 1000 + (Roles.kindOf(role) == null ? 99 : Roles.kindOf(role).ordinal());
    }

    public static List<Chapter> build() {
        List<Chapter> list = new ArrayList<>();
        for (String[] group : SECTIONS) {
            List<Line> lines = new ArrayList<>();
            for (int i = 1; i < group.length; i++) {
                String section = group[i];
                lines.add(new Line(Kind.SECTION,
                        Text.translatable(langKey(group[0], section, "title")).getString(), ACCENT));
                parseSection(lines, group[0], section,
                        Text.translatable(langKey(group[0], section, "body")).getString());
            }
            list.add(new Chapter(group[0], lines));
        }
        return list;
    }





    private static String langKey(String group, String section, String part) {
        if (group.equals("rules") && section.equals("序言")) return "btt.guide.preface." + part;
        return "btt.guide." + group + "." + section + "." + part;
    }







    public static String[] locate(Role role) {
        if (role == null) return null;
        Roles.Kind kind = Roles.kindOf(role);
        if (kind == null) return null;
        return switch (kind) {
            case ENFORCER -> new String[]{"passengers", "执法乘客"};
            case ACTION -> new String[]{"passengers", "行动乘客"};
            case INFORMATION -> new String[]{"passengers", "信息乘客"};
            case SUPPORT -> new String[]{"passengers", "辅助乘客"};
            case PRINCIPAL -> new String[]{"killers", "主犯凶手"};
            case ACCOMPLICE -> new String[]{"killers", "从犯凶手"};
            case LONE -> new String[]{"neutrals", "独行中立"};
            case OUTSIDER -> new String[]{"neutrals", "外人中立"};
            case MAD -> new String[]{"neutrals", "狂人中立"};
        };
    }














    private static void parseSection(List<Line> out, String group, String section, String body) {
        boolean selectedRole = group.equals("passengers") || group.equals("killers") || group.equals("neutrals");
        boolean traits = group.equals("traits");


        boolean flow = group.equals("rules")
                && (section.equals("流程") || section.equals("角色") || section.equals("能力"));
        boolean entryHeaded = group.equals("rules")
                && !section.equals("序言") && !section.equals("场景")
                && !section.equals("角色") && !section.equals("流程") && !section.equals("能力");
        boolean prevWasOutcome = false;
        for (String raw : body.split("\n")) {
            String line = raw.trim();
            if (line.isEmpty()) {
                prevWasOutcome = false;
                continue;
            }
            if (selectedRole) {
                Matcher m = ENTRY.matcher(line);
                if (!m.matches()) continue;
                addEntry(out, cjkOnly(m.group(2)), m.group(3), ACCENT);
                continue;
            }
            if (traits) {
                if (!line.startsWith("【")) continue;
                out.add(new Line(Kind.BODY, line, INK));
                continue;
            }
            if (flow) {
                if (line.startsWith("生还尾声宣言")) continue;
                if (prevWasOutcome) {
                    prevWasOutcome = false;
                    continue;
                }
                String label = ruleLabel(line);
                if (label != null) {
                    out.add(new Line(Kind.HEADING, label, ACCENT));
                    String rest = line.substring(label.length()).trim();
                    if (!rest.isEmpty()) out.add(new Line(Kind.BODY, rest, INK));
                } else {
                    out.add(new Line(Kind.BODY, line, INK));
                }
                prevWasOutcome = line.contains("——");
                continue;
            }
            if (entryHeaded) {
                Matcher m = ENTRY.matcher(line);
                if (m.matches()) {
                    addEntry(out, cjkOnly(m.group(2)), m.group(3), ACCENT);
                    continue;
                }
                Matcher labeled = ENTRY_LABEL.matcher(line);
                if (labeled.matches()) {
                    addEntry(out, cjkOnly(labeled.group(2)), labeled.group(3), ACCENT);
                    continue;
                }
            }
            out.add(new Line(Kind.BODY, line, INK));
        }
    }


    private static void addEntry(List<Line> out, String name, String body, int fallbackColor) {
        out.add(new Line(Kind.HEADING, name, roleColor(name, fallbackColor)));
        if (body != null && !body.isBlank()) out.add(new Line(Kind.BODY, body.trim(), INK));
    }


    private static int roleColor(String cnName, int fallback) {
        for (Role role : Roles.allRoles()) {
            if (SeatAssignment.displayName(role).getString().equals(cnName)) return role.color();
        }
        return fallback;
    }


    private static String cjkOnly(String raw) {
        StringBuilder sb = new StringBuilder();
        for (char c : raw.trim().toCharArray()) {
            if (c >= '\u4E00' && c <= '\u9FFF') sb.append(c);
            else break;
        }
        return sb.length() > 0 ? sb.toString() : raw.trim();
    }


    private static String ruleLabel(String line) {
        for (String label : RULE_LABELS) {
            if (line.startsWith(label) && line.length() > label.length()
                    && (line.charAt(label.length()) == ' ' || line.charAt(label.length()) == '\u3000')) {
                return label;
            }
        }
        return null;
    }


    public static List<String> preface() {
        List<String> lines = new ArrayList<>();
        for (String line : Text.translatable("btt.guide.preface.body").getString().split("\n")) lines.add(line);
        return lines;
    }
}
