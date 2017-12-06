package project.dajver.com.ppttoimage.task;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by gleb on 12/6/17.
 */

public class PPTToImageTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private OnFileWasConvertedListener onFileWasConvertedListener;

    public PPTToImageTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        HSLFSlideShow ppt = null;
        FileOutputStream out = null;
        File newFile = getCacheDir(context);;
        try {
            final String pathToFile = strings[0];
            final String extension = pathToFile.substring(pathToFile.lastIndexOf("."));
            if (extension.toLowerCase().equals(".ppt")) {
                POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(pathToFile));
                ppt = new HSLFSlideShow(fs);
                PictureData[] pdata = ppt.getPictures();
                PictureData pict = pdata[0];
                byte[] data = pict.getData();
                if (newFile.exists()) {
                    newFile.delete();
                }
                out = new FileOutputStream(newFile);
                out.write(data);
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile != null ? newFile.getAbsolutePath() : null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            File file = new File(result);
            if (file.exists()) {
                onFileWasConvertedListener.onFileWasConverted(file.getAbsolutePath());
            }
        }
    }

    public void setOnFileWasConvertedListener(OnFileWasConvertedListener onFileWasConvertedListener) {
        this.onFileWasConvertedListener = onFileWasConvertedListener;
    }

    private File getCacheDir(Context context) {
        return context.getCacheDir();
    }

    public interface OnFileWasConvertedListener {
        void onFileWasConverted(String path);
    }
}