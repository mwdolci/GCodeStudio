import java.io.File;
import java.util.Locale;

public class FormatUtils {
    public static String formatDuration(String valueSeconds) {
        double totalSecondsDouble = Double.parseDouble(valueSeconds);

        if (totalSecondsDouble < 60.0) {
            // Détecter le nombre de décimales dans la chaîne d'origine
            int decimals = 0;
            int indexOfDot = valueSeconds.indexOf('.');
            if (indexOfDot >= 0) {
                decimals = valueSeconds.length() - indexOfDot - 1;
            }

            // Limiter les décimales à 3 max
            if (decimals == 1) {
                return String.format(Locale.US, "%.1f sec.", totalSecondsDouble);
            } else if (decimals == 2) {
                return String.format(Locale.US, "%.2f sec.", totalSecondsDouble);
            } else {
                return String.format(Locale.US, "%.3f sec.", totalSecondsDouble);
            }
        }

        int totalSeconds = (int) Math.round(totalSecondsDouble);

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append(" h ");
        if (minutes > 0 || hours > 0) sb.append(minutes).append(" min. ");
        sb.append(seconds).append(" sec.");

        return sb.toString().trim();
    }

    public static String shortenPath(String path, int maxLength) {
        if (path == null || path.length() <= maxLength) {
            return path;
        }

        path = path.replace("\\", "/"); // Normalise les séparateurs : remplace tous les \ par /

        String fileName = new File(path).getName();

        if (fileName.length() + 5 >= maxLength) {
            return "..." + fileName.substring(fileName.length() - (maxLength - 3));
        }

        int keepLength = maxLength - fileName.length() - 5; // 5 pour /.../
        String start = path.substring(0, Math.min(keepLength, path.length()));
        
        start = start.replaceAll("/+$", ""); // Supprime les éventuels / en trop à la fin

        return start + "/.../" + fileName;
    }
}
