import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class PriorValueDecodeVideoFile {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "../data/prior-value-compressed.dat";
		String output_file_name = "../data/reuncompressed.dat";
		int video_width = 64;
        int video_height = 64;
		int frame_mem = 1;

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		Integer[] symbols = new Integer[256];
		
		for (int i=0; i<256; i++) {
			symbols[i] = i;
		}

		// Create 256 models. Model chosen depends on value of symbol prior to 
		// symbol being encoded.
		
		FreqCountIntegerSymbolModel[] models = new FreqCountIntegerSymbolModel[256];
		
		for (int i=0; i<256; i++) {
			// Create new model with default count of 1 for all symbols
			models[i] = new FreqCountIntegerSymbolModel(symbols);
		}
		
		// Read in number of symbols encoded

		int num_symbols = bit_source.next(32);
		int num_frames = num_symbols/(video_width * video_height);

		// Read in range bit width and setup the decoder

		int range_bit_width = bit_source.next(8);
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(range_bit_width);

		// Decode and produce output.
		
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		System.out.println("Number of encoded symbols: " + num_symbols);
		
		FileOutputStream fos = new FileOutputStream(output_file_name);

		// Use model 0 as initial model.
		FreqCountIntegerSymbolModel model = models[0];

		List<byte[][]> frames = new ArrayList<byte[][]>(); 

		for (int frame_ct = 0; frame_ct < num_frames; frame_ct++) {
			
			byte[][] frame = new byte[video_width][video_height];
			frames.add(0, frame);
			if (frames.size() > frame_mem + 1)
				frames.remove(frames.size() - 1);

			for (int y = 0; y < video_height; y++){
				for (int x = 0; x < video_width; x++){
					int prev_sum = 0;
					for (int i = 1; i < frames.size(); i++)
                        prev_sum += frames.get(i)[x][y];
					
                    if (frames.size() > 1)
                        model = models[(prev_sum / (frames.size() - 1)) + 128];

					int sym = decoder.decode(model, bit_source);
					frame[x][y] = (byte) sym;
					fos.write(sym);
					
					// Update model used
					model.addToCount(sym);
				}
			}
		}

		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();
	}
}
