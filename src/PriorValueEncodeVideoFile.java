import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

import ac.ArithmeticEncoder;
import io.OutputStreamBitSink;

public class PriorValueEncodeVideoFile {

	public static void main(String[] args) throws IOException {
		String input_file_name = "../data/out.dat";
		String output_file_name = "../data/prior-value-compressed.dat";
        int video_width = 64;
        int video_height = 64;
        int frame_mem = 1;

		int range_bit_width = 40;

		System.out.println("Encoding text file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);

		int num_symbols = (int) new File(input_file_name).length();
		int num_frames = num_symbols/(video_width * video_height);
				
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

		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);

		// First 4 bytes are the number of symbols encoded
		bit_sink.write(num_symbols, 32);		

		// Next byte is the width of the range registers
		bit_sink.write(range_bit_width, 8);

		// Now encode the input
		FileInputStream fis = new FileInputStream(input_file_name);
		
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

					int next_symbol = fis.read();
					frame[x][y] = (byte) next_symbol;
					encoder.encode(next_symbol, model, bit_sink);
					
					// Update model used
					model.addToCount(next_symbol);
				}
			}
		}
		fis.close();

		// Finish off by emitting the middle pattern 
		// and padding to the next word
		
		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();
		
		System.out.println("Done");
	}
}
