package nl.vu.hdt2vec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;

import nl.peterbloem.kit.Series;

public class SAIterator implements SentenceIterator
{	
	private File file;
	private boolean direction;
	private Iterator<CSVRecord> masterIt;
	
	public SAIterator(File file, boolean direction)
		throws IOException
	{
		this.file = file;
		this.direction = direction;
				

	}

	@Override
	public String nextSentence()
	{
		CSVRecord record = masterIt.next();

		String out = "";
		for(int i : Series.series(record.size()))
		{
			if(i > 0)
				out += " ";
			out += record.get(i).hashCode();
		}
		
		return out;
	}

	@Override
	public boolean hasNext()
	{
		return masterIt.hasNext();
	}

	@Override
	public void reset()
	{
		Reader reader;
		try
		{
			reader = new FileReader(file);
			masterIt = CSVFormat.DEFAULT.parse(reader).iterator();		
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public SentencePreProcessor getPreProcessor()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPreProcessor(SentencePreProcessor preProcessor)
	{
		// TODO Auto-generated method stub
		
	}
	

}
