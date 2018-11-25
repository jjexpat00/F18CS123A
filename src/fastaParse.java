import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class fastaParse {

    public String getFasta() throws IOException {

        String base = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
        String query = "1516214787,1516461622,1516461432,1516461375,1516461514,1516461413,1516461451,1516461584,1516461704,1516461565,1516461470,1516461394,1516460539,1516461337,1516461546";
        String apikey = "c0f2110548387a0d1b543dfd4594ddeb7e08";

        URL url = new URL(base + "efetch.fcgi?db=nuccore&id=" + query + "&rettype=fasta&retmode=text&api_key=" + apikey);
        InputStream in = url.openStream();
        Files.copy(in, Paths.get("f.fasta"), StandardCopyOption.REPLACE_EXISTING);
        in.close();

        BufferedReader xin = new BufferedReader(new FileReader("f.fasta"));

        boolean first = true;
        String seq = "";

        String line;
        while( (line = xin.readLine() ) != null) {
            if (first == true) {
                first = false;
            }
            else {
                seq += line;
            }
        }
        return seq;
    }




}

