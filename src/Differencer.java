import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Differencer {

	public static void main(String[] args) throws IOException {
		String input_file_name = "../data/out.dat";
		String output_file_name = "../data/differenced.dat";
		int video_width = 64;
        int video_height = 64;

		FileInputStream fis = new FileInputStream(input_file_name);
		FileOutputStream fos = new FileOutputStream(output_file_name);

        int num_symbols = (int) new File(input_file_name).length();
		int num_frames = num_symbols/(video_width * video_height);

		int[][] frame = new int[video_width][video_height];
        frame[0][0] = 0; 

		for (int frame_ct = 0; frame_ct < num_frames; frame_ct++) {
			int temp = fis.read();
            fos.write(temp - frame[0][0] + 128);
			frame[0][0] = temp;

			for (int y = 0; y < video_height; y++){
                if (y != 0){
                    frame[0][y] = fis.read();
                    fos.write(frame[0][y] - frame[0][y - 1] + 128);
                }
				for (int x = 1; x < video_width; x++){
					frame[x][y] = fis.read();
                    fos.write(frame[x][y] - frame[x - 1][y] + 128);
				}
			}
		}

		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();
	}
}
