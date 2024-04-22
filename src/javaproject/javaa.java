/**
*
* @author Ayşe Verda Gülcemal
* @since Nisan 2024
* <p>
* javaa sınıfı ile reposu atılan github dosyası klonlanır,istenilen analiz işlemlerini yapar
* </p>
*/
 

package javaproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

public class javaa {

    public static void main(String[] args) {
        try {
            clearAndCloneRepository(); // Klasörü temizle ve klonla
            analyzeRepository(); // Klonlanan repository'yi analiz et
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void clearAndCloneRepository() throws IOException {
        Path directory = Paths.get("./repo"); // Klonlanacak klasör
        if (Files.exists(directory)) {
            // Eğer "repo" klasörü zaten varsa, içeriğini temizle
            deleteDirectory(directory.toFile());
        } else {
            // "repo" klasörü yoksa oluştur
            Files.createDirectory(directory);
        }

        cloneRepository(directory);
    }

    private static void cloneRepository(Path directory) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("GITHUB REPOSITORY LİNKİNİ GİRİNİZ :");
            String repoUrl = reader.readLine().trim();

            // Dizini kontrol et ve oluştur
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            ProcessBuilder builder = new ProcessBuilder("git", "clone", repoUrl);
            builder.directory(directory.toFile()); // Klonlanacak klasörü belirt
            Process process = builder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }



    private static void analyzeRepository() {
        try {
            Path directory = Paths.get("./repo"); // Klonlanan repository'nin dizini
            Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(javaa::analyzeJavaFile); // Her Java dosyası için analiz yap
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void analyzeJavaFile(Path filePath) {
        try {
            String content = Files.readString(filePath);
            Pattern pattern = Pattern.compile("(?<=class\\s)[a-zA-Z0-9_]+");
            Matcher matcher = pattern.matcher(content);
            int totalLineCount = 0;
            int codeLineCount = 0;
            int commentLineCount = 0;
            int javadocLineCount = 0;
            int functionCount = 0; 

            boolean inMultiLineComment = false;

            for (String line : Files.readAllLines(filePath)) {
                totalLineCount++;

                line = line.trim();

                if (line.isEmpty()) {
                    continue; // Boş satırı atla
                   
                }

                // Tek satırlık yorum satırlarını say
                if (line.startsWith("//")) {
                    commentLineCount++;
                    continue;
                }

                // Çok satırlı yorum kontrolü
                if (inMultiLineComment) {
                    if (!line.contains("*/")) {
                        commentLineCount++; // Çok satırlı yorum içeriğini say
                    } else {
                        inMultiLineComment = false; // Çok satırlı yorum sonu, bu satırı sayma
                    }
                    continue;
                }

                if (line.startsWith("/*") && !line.contains("*/")&& !line.startsWith("/**")) {
                    inMultiLineComment = true; // Çok satırlı yorum başlangıcı, bu satırı sayma
                    continue;
                } else if (line.startsWith("/*") && line.contains("*/")&& !line.startsWith("/**")) {
                    // Tek satırda başlayıp biten çok satırlı yorumlar
                    // Bu durumda içerik satırı olmadığı için sayma
                    continue;
                }

                // Kod satırlarını say ve aynı zamanda satır sonu yorumlarını kontrol et
                if (!inMultiLineComment) {
                    //codeLineCount++;
                    if (line.contains("//")) {
                        commentLineCount++; // Satır içi yorumu yorum satırı olarak say
                    }
                }
            }
            
            
            for (String line : Files.readAllLines(filePath)) {
              //  totalLineCount++;

                line = line.trim();

                if (line.isEmpty()) {
                    continue; // Boş satırı atla
                   
                }

                // Tek satırlık yorum satırlarını say
                if (line.startsWith("//")) {
                   // commentLineCount++;
                    continue;
                }

                // Çok satırlı yorum kontrolü
                if (inMultiLineComment) {
                    if (!line.contains("*/")) {
                       // commentLineCount++; // Çok satırlı yorum içeriğini say
                    } else {
                        inMultiLineComment = false; // Çok satırlı yorum sonu, bu satırı sayma
                    }
                    continue;
                }

                if (line.startsWith("/*") && !line.contains("*/")) {
                    inMultiLineComment = true; // Çok satırlı yorum başlangıcı, bu satırı sayma
                    continue;
                } else if (line.startsWith("/*") && line.contains("*/")) {
                    // Tek satırda başlayıp biten çok satırlı yorumlar
                    // Bu durumda içerik satırı olmadığı için sayma
                    continue;
                }

                // Kod satırlarını say ve aynı zamanda satır sonu yorumlarını kontrol et
                if (!inMultiLineComment) {
                    codeLineCount++;
                    if (line.contains("//")) {
                       // commentLineCount++; // Satır içi yorumu yorum satırı olarak say
                    }
                }
            }

            
            
            

            // Fonksiyon sayısını hesapla
            functionCount = countFunctions(content);

            // Javadoc satırı sayısını hesapla
            javadocLineCount = countJavadocLines(content);

            double YG = ((javadocLineCount + commentLineCount) * 0.8) / functionCount;
            double YH = (codeLineCount / (double) functionCount) * 0.3;
            double yorumSapmaYuzdesi = ((100 * YG) / YH) - 100;

            while (matcher.find()) {
                System.out.println("--------------------------------");
                System.out.println("Dosya adı: " + filePath.getFileName());
                System.out.println("Javadoc satırı sayısı: " + javadocLineCount + " lines");
                System.out.println("Yorum satırı sayısı: " + commentLineCount + " lines" );
                System.out.println("Kod satırı sayısı: " + codeLineCount + " lines");
                System.out.println("Toplam satır sayısı(LOC): " + totalLineCount + " lines");
                System.out.println("Fonksiyon sayısı: " + functionCount);
                System.out.printf("Yorum Sapma Yüzdesi: %.2f%%\n", yorumSapmaYuzdesi);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int countFunctions(String content) {
        int functionCount = 0;
        String[] lines = content.split("\\r?\\n");
        boolean inComment = false;
        boolean inFunction = false;

        for (String line : lines) {
            line = line.trim();

            // Yorum satırı kontrolü
            if (line.startsWith("//")) {
                continue;
             
            }

            // Çok satırlı yorum kontrolü
            if (line.startsWith("/*")) {
                inComment = true;
            }

            if (inComment) {
                if (line.contains("*/")) {
                    inComment = false;
                }
                continue;
            }

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("class")) {
                continue;
            }

            if (line.startsWith("public") || line.startsWith("private") || line.startsWith("protected")) {
                inFunction = true;
            }

            // Fonksiyon kapatma parantezi kontrolü
            if (inFunction && line.endsWith("}")) {
                functionCount++;
                inFunction = false;
            }
        }

        return functionCount;
    }

    private static int countJavadocLines(String content) {
        int javadocLineCount = 0;
        boolean inJavadoc = false;

        // İçinde metin olmayan veya sadece boşluklar içeren satırlar
        Pattern emptyLinePattern = Pattern.compile("^\\s*$");

        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();

            if (inJavadoc) {
                if (line.contains("*/")) {
                    inJavadoc = false;
                    continue;
                }
                javadocLineCount++;
                continue;
            }

            if (line.startsWith("/**")) {
                inJavadoc = true;
                continue;
            }

            Matcher matcher = emptyLinePattern.matcher(line);
            if (matcher.matches()) {
                continue; // Boş satırları atla
            }

            // Javadoc aralığında olmayan satırları atla
            if (!inJavadoc) {
                continue;
            }

            // Javadoc bloğunun sonunu bulamadıysak ve içinde metin varsa,
            // bu da bir Javadoc satırıdır.
            javadocLineCount++;
        }
    
        return javadocLineCount;
    }

    // Verilen dizini ve içeriğini silen yöntem
    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
} 

