import compbio.data.msa.MsaWS;
import compbio.data.sequence.Alignment;
import compbio.data.sequence.ClustalAlignmentUtil;
import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.SequenceUtil;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;
import compbio.ws.client.Services;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.*;
import java.net.URL;
import java.nio.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Fasta {

    /**
     * Method makes Entrez API call to get FASTA file from query, creating or replacing "f.fasta".
     * @throws IOException
     */
    public void get20Fasta() throws IOException {

        String base = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
        String query = "299758068,401716659,1604348732,649570659,"
        		+ "1145600086,557848584,754294407,754294533,745991130,377580535,"
        		+ "326986753,270358976,269978850,268537544,265692811,262333025,"
        		+ "257127086,255689498,1200764935,1344462112,756762854";//"299758068,1605170266";
        String apikey = APIKey.getKey(); // API Key redacted

        URL url = new URL(base + "efetch.fcgi?db=protein&id=" + query + "&rettype=fasta&retmode=text&api_key=" + apikey);
        InputStream in = url.openStream();
        Files.copy(in, Paths.get("f.fasta"), StandardCopyOption.REPLACE_EXISTING);
        in.close();

    }
    
    public void get16kFasta() throws IOException {

        String base = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
        String term = "(pb1%5BGene%20Name%5D)%20AND%20h1n1";
        String webenv1 = "NCID_1_251316772_130.14.22.33_9001_1555886122_99252089_0MetA0_S_MegaStore";
        //String webenv2 = "NCID_1_191351020_130.14.22.76_9001_1555886135_1367036124_0MetA0_S_MegaStore";
        
        String apikey = APIKey.getKey(); // API Key redacted

        URL url = new URL(base + "efetch.fcgi?db=protein&WebEnv=" + webenv1 + "&retstart=0&query_key=1&rettype=fasta&retmode=text&retmax=10000");//&api_key=" + apikey);
        InputStream in = url.openStream();
        Files.copy(in, Paths.get("f.fasta"), StandardCopyOption.REPLACE_EXISTING);
        
        
        try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        URL url2 = new URL(base + "efetch.fcgi?db=protein&WebEnv=" + webenv1 + "&retstart=10001&query_key=1&rettype=fasta&retmode=text&retmax=10000");
        InputStream in2 = url2.openStream();
        Files.copy(in2, Paths.get("f2.fasta"), StandardCopyOption.REPLACE_EXISTING);
		
        in.close();
        in2.close();
        
        
        List<Path> inputs = Arrays.asList(
                Paths.get("f.fasta"),
                Paths.get("f2.fasta")
        );
        
        Path output = Paths.get("f3.fasta");

        // Charset for read and write
        Charset charset = StandardCharsets.UTF_8;

        // Join files (lines)
        for (Path path : inputs) {
            List<String> lines = Files.readAllLines(path, charset);
            Files.write(output, lines, charset, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        }
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

    /**
     * Method makes call to JABAWS to perform MSA on previously fetched sequence. Gives user a set of options of MSA algorithms.
     * @throws JobSubmissionException
     * @throws ResultNotAvailableException
     * @throws InterruptedException
     * @throws IOException
     */
    public void getMSA() throws JobSubmissionException, ResultNotAvailableException, InterruptedException, IOException {

        if (Paths.get("f3.fasta") == null) {
             System.out.println("No FASTA found.");
             return;
        }

        Scanner sc = new Scanner(System.in);
        System.out.println("Muscle: mu");
        System.out.println("Mafft: ma");
        System.out.println("ClustalO: co");
        System.out.println("ClustalW: cw");
        System.out.println("Select MSA option: ");
        String input = sc.nextLine();

        getMSAHelper(input);

    }

    /**
     * Helper method to send user input selection to JABAWS for MSA. Future implementation will run MSA locally.
     * @param serviceName User input selection
     * @throws IOException
     * @throws JobSubmissionException
     * @throws InterruptedException
     * @throws ResultNotAvailableException
     */
    private void getMSAHelper(String serviceName) throws IOException, JobSubmissionException, InterruptedException, ResultNotAvailableException {

        String qualifiedServiceName = "http://msa.data.compbio/01/01/2010/";
        String host = "http://www.compbio.dundee.ac.uk/jabaws";

        // Default service is Clustal Omega
        Services clustal = Services.ClustalWS;

        if (serviceName.equalsIgnoreCase("co")) {
            clustal = Services.ClustalOWS;
            qualifiedServiceName = "http://msa.data.compbio/01/12/2010/";   // Later implementation on their end requires different service name
            System.out.println("Now aligning with ClustalOmega...");
        }
        else if (serviceName.equalsIgnoreCase("mu")){
            clustal = Services.MuscleWS;
            System.out.println("Now aligning with Muscle...");
        }
        else if (serviceName.equalsIgnoreCase("cw")){
            clustal = Services.ClustalWS;
            System.out.println("Now aligning with ClustalW...");
        }
        else if (serviceName.equalsIgnoreCase("ma")){
            clustal = Services.MafftWS;
            System.out.println("Now aligning with Mafft...");
        }

        URL url = new URL(host + "/" + clustal.toString() + "?wsdl");
        QName qname = new QName(qualifiedServiceName, clustal.toString());
        Service serv = Service.create(url, qname);
        MsaWS msaws = serv.getPort(new QName(qualifiedServiceName, clustal + "Port"), MsaWS.class);

        List<FastaSequence> fastalist = SequenceUtil.readFasta(new FileInputStream("f3.fasta"));
        String jobId = msaws.align(fastalist);

        while (msaws.getJobStatus(jobId) != JobStatus.FINISHED) {
            Thread.sleep(1000);                 // wait one second, then recheck status
            System.out.println("Waiting...");
        }

        File file = new File("out.msa");    // Removes older MSA outputs
        file.delete();
        Alignment alignment = msaws.getResult(jobId);
        ClustalAlignmentUtil.writeClustalAlignment(new FileWriter("out.msa"), alignment);
        System.out.println("Alignment completed. See \"out.msa\" file.");
    }

}

