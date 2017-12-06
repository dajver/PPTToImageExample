package project.dajver.com.ppttoimage;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import project.dajver.com.ppttoimage.task.PPTToImageTask;
import project.dajver.com.ppttoimage.task.utils.ImageFilePathUtils;

public class MainActivity extends AppCompatActivity implements PPTToImageTask.OnFileWasConvertedListener,
        MultiplePermissionsListener {

    @BindView(R.id.image)
    ImageView imageView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private int PICKFILE_REQUEST_CODE = 1213;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(this)
                .check();
    }

    @OnClick(R.id.image)
    public void onImageClick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            progressBar.setVisibility(View.VISIBLE);
            String fPath = ImageFilePathUtils.getPath(this, data.getData());

            PPTToImageTask pdfToImageTask = new PPTToImageTask(this);
            pdfToImageTask.setOnFileWasConvertedListener(this);
            pdfToImageTask.execute(fPath);
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onFileWasConverted(String path) {
        progressBar.setVisibility(View.GONE);
        Bitmap myBitmap = BitmapFactory.decodeFile(new File(path).getAbsolutePath());
        imageView.setImageBitmap(myBitmap);
    }

    @Override
    public void onPermissionsChecked(MultiplePermissionsReport report) { }

    @Override
    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) { }
}
