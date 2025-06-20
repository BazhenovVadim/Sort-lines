package ru.vadim;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class App {
    static boolean isValidLine(String line) //Тут валидация строк
    {
        int count = 0;
        for (int i = 0; i < line.length(); i++) if (line.charAt(i) == '"') count++;
        return count % 2 == 0;
    }


    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();//Засекаем время
        if (args.length != 1) {
            System.err.println("Usage: java -jar app.jar <path-to-input.gz>");
            System.exit(1);
        }//Тут просто обработка запуска
        Path input = Paths.get(args[0]);
        int count = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(Files.newInputStream(input)), StandardCharsets.UTF_8), 8192)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (isValidLine(line)) count++;
            }
        }// Тут считаем строки

        // В блоке ниже загоняем строки в массив с помощью билдера и чаров (стринга много памяти ест, поэтому чары)
        String[] original = new String[count];
        int n = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(Files.newInputStream(input)), StandardCharsets.UTF_8), 8192)) {
            StringBuilder sb = new StringBuilder();
            int ch;
            while ((ch = br.read()) != -1) {
                if (ch == '\n') {
                    String line = sb.toString();
                    sb.setLength(0);
                    if (!isValidLine(line)) continue;
                    original[n++] = line;
                } else {
                    sb.append((char) ch);
                }
            }
            if (sb.length() > 0) {
                String line = sb.toString();
                if (isValidLine(line)) original[n++] = line;
            }
        }// Получаем массив отсортированных строк

        DSU dsu = new DSU(n);// Реализация дсу, тут в общем просто прогоняем по строкам, разбиваем и кладем в мапу
        // Если ключ в мапе уже есть, объединяем в дсу
        Map<String, Integer> map = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            String line = original[i];
            int col = 0;
            int start = 0;
            for (int j = 0; j <= line.length(); j++) {
                if (j == line.length() || line.charAt(j) == ';') {
                    if (start < j) {
                        String v = line.substring(start, j);
                        if (!v.isEmpty()) {
                            String key = (v + "#" + col).intern();
                            Integer prev = map.putIfAbsent(key, i);
                            if (prev != null) dsu.union(i, prev);
                        }
                    }
                    start = j + 1;
                    col++;
                }
            }
        }

        Map<Integer, LinkedHashSet<String>> groups = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = dsu.find(i);
            groups.computeIfAbsent(root, k -> new LinkedHashSet<>()).add(original[i]);
        }//Каждую строку по её корню (результату find(i)) помещаем в соответствующую группу

        List<List<String>> result = new ArrayList<>();// тут просто убираем лишнее
        for (LinkedHashSet<String> g : groups.values()) {
            if (g.size() > 1) result.add(new ArrayList<>(g));
        }
        result.sort((a, b) -> b.size() - a.size());

        try (PrintWriter out = new PrintWriter("output.txt", StandardCharsets.UTF_8)) {
            out.println(result.size());
            int gid = 1;
            for (List<String> g : result) {
                out.println();
                out.println("Группа " + gid++);
                for (String s : g) out.println(s);
            }
        }//запись результатов в файл

        long endTime = System.currentTimeMillis();
        System.out.println("Групп с более чем одним элементом: " + result.size());
        System.out.println("Время выполнения: " + (endTime - startTime) + " мс");
    }
}
