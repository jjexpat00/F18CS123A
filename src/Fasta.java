import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class Fasta {

    /**
     * Method makes Entrez API call to get FASTA file from query, creating or replacing "f.fasta".
     * @throws IOException
     */
    public void getFasta() throws IOException {

        String base = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
        String query = "1516214787,1516461622,1516461432,1516461375,1516461514,1516461413,1516461451,1516461584,1516461704,1516461565,1516461470,1516461394,1516460539,1516461337,1516461546";
        String apikey = "c0f2110548387a0d1b543dfd4594ddeb7e08";

        URL url = new URL(base + "efetch.fcgi?db=nuccore&id=" + query + "&rettype=fasta&retmode=text&api_key=" + apikey);
        InputStream in = url.openStream();
        Files.copy(in, Paths.get("f.fasta"), StandardCopyOption.REPLACE_EXISTING);
        in.close();

    }

    /**
     * Method prints the entire FASTA query to verify its existence
     * @return FASTA query
     * @throws IOException
     */
    public String printFasta() throws IOException {

        String newline = System.getProperty("line.separator");
        BufferedReader in = new BufferedReader(new FileReader("f.fasta"));
        String seq = "";
        String line;

        while( (line = in.readLine() ) != null) {
            seq += line + newline;

        }
        return seq;

    }
}

