package nl.vu.hdt2vec.exec;

import static nl.peterbloem.kit.Functions.tic;
import static nl.peterbloem.kit.Functions.toc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.plot.BarnesHutTsne;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import nl.peterbloem.kit.Global;

public class Run
{

	
	@Option(name="--hdt-file", usage="Input file: an RDF file in some format that is supported by OpenRDF, or in HDT format.", required=true)
	private static File file;
	
	@Option(name="--targets", usage="A list (one IRI per line) of the nodes to analyse. This can be a CSV file, in which case only the first element on ech line is used.")
	private static File targetsFile;
	
	@Option(name="--sequences", usage="A file containing the sequences to be used. If this argument is set hdtFile is ignored.")
	private static File sequencesFile;
	
	@Option(name="--sentence-length", usage="Sentence length.")
	private static int sentenceLength = 5;
	
	@Option(name="--corpus-length", usage="Number of sentences to generate.")
	private static int corpusLength = 5000000;	
	
	@Option(name="--layer-size", usage="The size of the hidden layer.")
	private static int layerSize = 100;	
	
	@Option(name="--window-size", usage="The size of the context that word2vec should reproduce.")
	private static int windowSize = 5;	
	
	public static void main(String[] args) 
			throws FileNotFoundException, IOException 
	{
		Run run = new Run();
		
		// * Parse the command-line arguments
    	CmdLineParser parser = new CmdLineParser(run);
    	try
		{
			parser.parseArgument(args);
		} catch (CmdLineException e)
		{
	    	System.err.println(e.getMessage());
	        System.err.println("java -jar hdt2vec.jar [options...]");
	        parser.printUsage(System.err);
	        
	        System.exit(1);	
	    }
		
		Global.log().info("File loaded ...");
		
		// * Read targets
		List<String> targets = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader(targetsFile));
		
		String line = reader.readLine();
		while(line != null) 
		{			
			targets.add(line.split(",")[0].trim());
			line = reader.readLine();
		} 
		
		
		// * TODO: Get sentence Iterator 
		SentenceIterator it = null;
		
		TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
		
        Global.log().info("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(layerSize)
                .seed(42)
                .windowSize(windowSize)
                .iterate(it)
                .tokenizerFactory(t)
                .build();

        Global.log().info("Fitting Word2Vec model....");
        tic();
        vec.fit();
        Global.log().info("Model finished, " + toc() + " seconds taken");
        
        int numWords = vec.getVocab().numWords();
        
        WordVectorSerializer.writeWordVectors(vec, new File("full-vectors.tsv"));
        
        Global.log().info("Plot TSNE....");
        BarnesHutTsne tsne = new BarnesHutTsne.Builder()
                .setMaxIter(1000)
                .stopLyingIteration(250)
                .learningRate(500)
                .useAdaGrad(false)
                .theta(0.5)
                .setMomentum(0.5)
                .normalize(true)
                .usePca(false)
                .build();
        
        vec.lookupTable().plotVocab(tsne, numWords, new File("tsne-vectors.tsv"));
        
	}
	
}
