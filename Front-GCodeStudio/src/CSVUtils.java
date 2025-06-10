import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVUtils {
    // Charge un fichier CSV dans une liste de String[] (chaque ligne = tableau de colonnes)
    public static List<String[]> loadCSVToList(File csvFile) throws IOException {
        List<String[]> dataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Ignore entête
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                dataList.add(values);
            }
        }
        return dataList;
    }
}
