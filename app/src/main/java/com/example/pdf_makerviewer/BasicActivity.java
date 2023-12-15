package com.example.pdf_makerviewer;


import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContracts.TakePicture;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.mayank.simplecropview.CropImageView;

import com.mayank.simplecropview.callback.CropCallback;
import com.mayank.simplecropview.callback.LoadCallback;
import com.mayank.simplecropview.callback.SaveCallback;
import com.mayank.simplecropview.util.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BasicActivity extends Activity {
    private static final String TAG = BasicActivity.class.getSimpleName();

    private static final int REQUEST_PICK_IMAGE = 10011;
    private static final int REQUEST_SAVE_PICK_IMAGE = 10012;
    private static final String PROGRESS_DIALOG = "ProgressDialog";
    private static final String KEY_FRAME_RECT = "FrameRect";
    private static final String KEY_SOURCE_URI = "SourceUri";
    ArrayList<Bitmap> list;
      boolean pdfDoneOrNot=false;



//     after click on done

    // Views ///////////////////////////////////////////////////////////////////////////////////////
    private CropImageView mCropView;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    private RectF mFrameRect = null;
    private Uri mSourceUri = null;
    private Uri uri = null;
    // Lifecycle Method ////////////////////////////////////////////////////////////////////////////

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);
        mCropView = findViewById(R.id.cropImageView);
        list = new ArrayList<Bitmap>();
        if (savedInstanceState != null) {
            // restore data
            mFrameRect = savedInstanceState.getParcelable(KEY_FRAME_RECT);
            mSourceUri = savedInstanceState.getParcelable(KEY_SOURCE_URI);
        }

            Intent intent = getIntent();
            String data =intent.getStringExtra("JL");

          //  createNewUri(BasicActivity.this, mCompressFormat);
            mCropView.setImageURI(Uri.parse(data));
            mSourceUri=Uri.parse(data);

        if (mSourceUri == null) {
            // default data
          //  mSourceUri = getUriFromDrawableResId(this, R.drawable.sample4);
        }
        // load image
        mCropView.load(mSourceUri)
                .initialFrameRect(mFrameRect)
                .useThumbnail(true)
                .execute(mLoadCallback);
    }


    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save data
        outState.putParcelable(KEY_FRAME_RECT, mCropView.getActualCropRect());
        outState.putParcelable(KEY_SOURCE_URI, mCropView.getSourceUri());
    }

    public void startResultActivity(Uri uri) {
        if (isFinishing()) return;
        if(!pdfDoneOrNot){
            Intent intent = new Intent(this,MainActivity.class);
            intent.putExtra("DONE",pdfDoneOrNot);
            intent.putExtra("RESULT",uri);
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(this,MainActivity.class);
            intent.putExtra("DONE",pdfDoneOrNot);
            intent.putExtra("RESULT",uri);
            startActivity(intent);
            Toast.makeText(this,"Complete",Toast.LENGTH_SHORT).show();
            finish();
        }


    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonDone:
                cropImage();
                break;
            case R.id.buttonFitImage:
                mCropView.setCropMode(CropImageView.CropMode.FIT_IMAGE);
                break;
            case R.id.button1_1:
                mCropView.setCropMode(CropImageView.CropMode.SQUARE);
                break;
            case R.id.button3_4:
                mCropView.setCropMode(CropImageView.CropMode.RATIO_3_4);
                break;
            case R.id.button4_3:
                mCropView.setCropMode(CropImageView.CropMode.RATIO_4_3);
                break;
            case R.id.button9_16:
                mCropView.setCropMode(CropImageView.CropMode.RATIO_9_16);
                break;
            case R.id.button16_9:
                mCropView.setCropMode(CropImageView.CropMode.RATIO_16_9);
                break;
            case R.id.buttonCustom:
                mCropView.setCustomRatio(7, 5);
                break;
            case R.id.buttonFree:
                mCropView.setCropMode(CropImageView.CropMode.FREE);
                break;
            case R.id.buttonCircle:
                mCropView.setCropMode(CropImageView.CropMode.CIRCLE);
                break;
            case R.id.buttonShowCircleButCropAsSquare:
                mCropView.setCropMode(CropImageView.CropMode.CIRCLE_SQUARE);
                break;
            case R.id.buttonRotateLeft:
                mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);
                break;
            case R.id.buttonRotateRight:
                mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                break;
            case R.id.buttonPickImage:
               // pickImage();
                 pdfDoneComplete();
                break;
        }
    }

    private void pdfDoneComplete()  {
        pdfDoneOrNot=true;
        mCropView.crop(mSourceUri).execute(mCropCallback);

    }


    public void pickImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),
                    REQUEST_PICK_IMAGE);
        } else if(checkPermissions(REQUEST_PICK_IMAGE)){
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_SAVE_PICK_IMAGE);
        }
    }

    public void cropImage() {
        if(checkPermissions(REQUEST_PICK_IMAGE))
            mCropView.crop(mSourceUri).execute(mCropCallback);
    }

    public void showProgress() {
//    ProgressDialogFragment f = ProgressDialogFragment.getInstance();
//    getFragmentManager().beginTransaction().add(f, PROGRESS_DIALOG).commitAllowingStateLoss();
    }

    public void dismissProgress() {
//    if (!isResumed()) return;
//    android.support.v4.app.FragmentManager manager = getFragmentManager();
//    if (manager == null) return;
//    ProgressDialogFragment f = (ProgressDialogFragment) manager.findFragmentByTag(PROGRESS_DIALOG);
//    if (f != null) {
//      getFragmentManager().beginTransaction().remove(f).commitAllowingStateLoss();
//    }
    }


    public Uri createSaveUri() {
        return createNewUri(BasicActivity.this, mCompressFormat);
    }

    public static String getDirPath() {
        String dirPath = "";
        File imageDir = null;
        File extStorageDir = Environment.getExternalStorageDirectory();
        if (extStorageDir.canWrite()) {
            imageDir = new File(extStorageDir.getPath() + "/simplecropview");
        }
        if (imageDir != null) {
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }
            if (imageDir.canWrite()) {
                dirPath = imageDir.getPath();
            }
        }
        return dirPath;
    }



    public static Uri createNewUri(Context context, Bitmap.CompressFormat format) {
        long currentTimeMillis = System.currentTimeMillis();
        Date today = new Date(currentTimeMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String title = dateFormat.format(today);
        String dirPath = getDirPath();
        String fileName = "scv" + title + "." + getMimeType(format);
        String path = dirPath + "/" + fileName;
        File file = new File(path);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + getMimeType(format));
        values.put(MediaStore.Images.Media.DATA, path);
        long time = currentTimeMillis / 1000;
        values.put(MediaStore.MediaColumns.DATE_ADDED, time);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time);
        if (file.exists()) {
            values.put(MediaStore.Images.Media.SIZE, file.length());
        }

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return uri;
    }




    public static String getMimeType(Bitmap.CompressFormat format) {
        switch (format) {
            case JPEG:
                return "jpeg";
            case PNG:
                return "png";
        }
        return "png";
    }


//    public static Uri createTempUri(Context context) {
//        return Uri.fromFile(new File(context.getCacheDir(), "cropped"));
//    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (resultCode == Activity.RESULT_OK) {
            // reset frame rect
            mFrameRect = null;
            switch (requestCode) {
                case REQUEST_PICK_IMAGE:
                    mSourceUri = result.getData();
                    mCropView.load(mSourceUri)
                            .initialFrameRect(mFrameRect)
                            .useThumbnail(true)
                            .execute(mLoadCallback);
                    break;
                case REQUEST_SAVE_PICK_IMAGE:
                    mSourceUri = Utils.ensureUriPermission(BasicActivity.this, result);
                    mCropView.load(mSourceUri)
                            .initialFrameRect(mFrameRect)
                            .useThumbnail(true)
                            .execute(mLoadCallback);
                    break;



            }
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                     int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if(requestCode == REQUEST_PICK_IMAGE) pickImage();
            else if(requestCode==REQUEST_SAVE_PICK_IMAGE) cropImage();
        }
//    BasicFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
    // Callbacks ///////////////////////////////////////////////////////////////////////////////////

    private final LoadCallback mLoadCallback = new LoadCallback() {
        @Override public void onSuccess() {
        }

        @Override public void onError(Throwable e) {
        }
    };


    private final CropCallback mCropCallback = new CropCallback() {
        @Override public void onSuccess(Bitmap cropped) {
            mCropView.save(cropped)
                    .compressFormat(mCompressFormat)
                    .execute(createSaveUri(), mSaveCallback);
           }

        @Override public void onError(Throwable e) {
        }
    };




    private final SaveCallback mSaveCallback = new SaveCallback() {
        @Override public void onSuccess(Uri outputUri) {
                 // dismissProgress();
            System.out.println("UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU");
             startResultActivity(outputUri);

           }

        @Override public void onError(Throwable e) {
            dismissProgress();
        }
    };

    public boolean checkPermissions(int reqCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, reqCode);
            return false;
        } else return true;
    }
}