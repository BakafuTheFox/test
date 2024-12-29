import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        String outputPath = "";
        String prefix = "";
        boolean appendMode = false;
        boolean shortStats = false;
        boolean fullStats = false;

        List<String> filePaths = new ArrayList<>();
        // аргументы командной строки
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                    if (i + 1 < args.length) {
                        outputPath = args[++i];
                    } else {
                        System.err.println("Ошибка: отсутствует путь для файлов вывода после -o");
                        return;
                    }
                    break;
                case "-p":
                    if (i + 1 < args.length) {
                        prefix = args[++i];
                    } else {
                        System.err.println("Ошибка: отсутствует префикс после -p");
                        return;
                    }
                    break;
                case "-a":
                    appendMode = true;
                    break;
                case "-s":
                    shortStats = true;
                    break;
                case "-f":
                    fullStats = true;
                    break;
                default:
                    filePaths.add(args[i]);
                    break;
            }
        }
        Map<String, Boolean> appendModes = new HashMap<>();
        appendModes.put("integer", appendMode);
        appendModes.put("float", appendMode);
        appendModes.put("string", appendMode);
        if (filePaths.isEmpty()) {
            System.err.println("Ошибка: файл ввода не обнаружен");
            return;
        }
        //сбор статистики
        Map<String, Integer> typeCounts = new HashMap<>();
        typeCounts.put("integers", 0);
        typeCounts.put("floats", 0);
        typeCounts.put("strings", 0);

        long intSum = 0, intMin = Long.MAX_VALUE, intMax = Long.MIN_VALUE;
        double floatSum = 0, floatMin = Double.MAX_VALUE, floatMax = -Double.MAX_VALUE;
        int stringMinLen = Integer.MAX_VALUE, stringMaxLen = 0;

        try {
            for (String filePath : filePaths) {
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Object obj = parseLine(line);
                        try {
                            if (obj instanceof Long) {
                                long value = (Long)obj;
                                writeToFile(outputPath, prefix, "integers.txt", String.valueOf(value), appendModes.get("integer"));
                                typeCounts.put("integers", typeCounts.get("integers") + 1);
                                intSum += value;
                                intMin = Math.min(intMin, value);
                                intMax = Math.max(intMax, value);
                                appendModes.put("integer", true);
                            } else if (obj instanceof Double) {
                                double value = (double)obj;
                                writeToFile(outputPath, prefix, "floats.txt", String.valueOf(value), appendModes.get("float"));
                                typeCounts.put("floats", typeCounts.get("floats") + 1);
                                floatSum += value;
                                floatMin = Math.min(floatMin, value);
                                floatMax = Math.max(floatMax, value);
                                appendModes.put("float", true);
                            } else if (obj instanceof String) {
                                writeToFile(outputPath, prefix, "strings.txt", line, appendModes.get("string"));
                                typeCounts.put("strings", typeCounts.get("strings") + 1);
                                stringMinLen = Math.min(stringMinLen, line.length());
                                stringMaxLen = Math.max(stringMaxLen, line.length());
                                appendModes.put("string", true);
                            }
                        } catch (Exception e) {
                            System.err.println("Ошибка записи статистики: " + e);
                            System.err.println("Ошибка из-за строки: " + line);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка чтения файла " + filePath + ": " + e.getMessage());
                }
            }

            printStatistics(typeCounts, intSum, intMin, intMax, floatSum, floatMin, floatMax, stringMinLen, stringMaxLen, shortStats, fullStats);

        } catch (Exception e) {
            System.err.println("Неизвестная ошибка: " + e.getMessage());
        }
    }

    //проверка типа переменной
    public static Object parseLine(String line) {
        try {
            return Long.parseLong(line);
        } catch (NumberFormatException e) {
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException ex) {
                return line;
            }
        }
    }

    //запись в файл
    private static void writeToFile(String outputPath, String prefix, String fileName, String content, boolean appendMode) throws IOException {
        String fullPath = Paths.get(outputPath, prefix + fileName).toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath, appendMode))) {
            writer.write(content);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Ошибка записи" + e);
        }
        ;
    }

    //вывод статистики в консоль
    private static void printStatistics(Map<String, Integer> typeCounts, long intSum, long intMin, long intMax, double floatSum, double floatMin, double floatMax, int stringMinLen, int stringMaxLen, boolean shortStats, boolean fullStats) {
        System.out.println("Statistics:");
        System.out.println("Integers: " + typeCounts.get("integers"));
        System.out.println("Floats: " + typeCounts.get("floats"));
        System.out.println("Strings: " + typeCounts.get("strings"));

        if (fullStats) {
            if (typeCounts.get("integers") > 0) {
                System.out.printf("Integer Stats - Min: %d, Max: %d, Sum: %d, Mean: %.2f%n", intMin, intMax, intSum, (double) intSum / typeCounts.get("integers"));
            }
            if (typeCounts.get("floats") > 0) {
                System.out.printf("Float Stats - Min: %.2f, Max: %.2f, Sum: %.2f, Mean: %.2f%n", floatMin, floatMax, floatSum, floatSum / typeCounts.get("floats"));
            }
            if (typeCounts.get("strings") > 0) {
                System.out.printf("String Length Stats - Shortest: %d, Longest: %d%n", stringMinLen, stringMaxLen);
            }
        }
    }
}
