package com.troy.cameralib;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CameraView;
import com.troy.cameralib.util.DisplayUtil;
import com.troy.cameralib.util.FileUtil;
import com.troy.cameralib.view.MaskView;

/**
 * Author: Troy
 * Date: 2017/8/30
 * Email: 810196673@qq.com
 * Des: CameraActivity
 */

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    //控件View
    private CameraView mCameraView;
    private MaskView viewMask;
    private ImageButton ibtCapture;
    private ImageView ivReturn;

    private int RECT_WIDTH; //拍摄区域宽度
    private int RECT_HEIGHT; //拍摄区域高度

    private float ratio; //高宽比
    private float cameraRatio; // 相机高宽比

    private Point rectPictureSize;
    private int mCameraWidth;
    private int mCameraHeight;
    private Uri imageUri;
    private String imagePath;
    private int leftRight;
    private int topBottom;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        setupViews(getIntent());
        initView();
        mCameraView.addCallback(mCallback);

    }

    private void setupViews(@NonNull Intent mIntent){
        leftRight = mIntent.getIntExtra(EasyCamera.EXTRA_MARGIN_BY_WIDTH,0);
        topBottom = mIntent.getIntExtra(EasyCamera.EXTRA_MARGIN_BY_HEIGHT,0);
        ratio = mIntent.getFloatExtra(EasyCamera.EXTRA_VIEW_RATIO, 1f);
        imageUri = mIntent.getParcelableExtra(EasyCamera.EXTRA_OUTPUT_URI);
        imagePath = FileUtil.getRealFilePath(this,imageUri);
    }

    private void initView() {
        mCameraView = (CameraView) findViewById(R.id.camera_view);
        viewMask = (MaskView) findViewById(R.id.view_mask);
        ibtCapture = (ImageButton) findViewById(R.id.ibt_capture);
        ivReturn = (ImageView) findViewById(R.id.iv_return);

        ibtCapture.setOnClickListener(this);
        ivReturn.setOnClickListener(this);

        AspectRatio currentRatio = mCameraView.getAspectRatio();
        cameraRatio = currentRatio.toFloat();
        mCameraWidth = (int) DisplayUtil.getScreenWidth(this);
        mCameraHeight = (int) (mCameraWidth * cameraRatio);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = mCameraWidth;
        layoutParams.height = mCameraHeight;
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        viewMask.setLayoutParams(layoutParams);

        if (ratio > cameraRatio) {
            //如果传过来的ratio比屏幕的高宽比大，那么需要以屏幕高为标准
            RECT_HEIGHT = mCameraHeight - topBottom; //以宽为准，到CameraView上下保留一定的间距
            RECT_WIDTH = (int) (RECT_HEIGHT / ratio);
        } else {
            RECT_WIDTH = mCameraWidth - leftRight; //以宽为准，到CameraView两边保留一定的间距
            RECT_HEIGHT = (int) (RECT_WIDTH * ratio);
        }
        if (viewMask != null) {
            Rect screenCenterRect = DisplayUtil.createCenterScreenRect(mCameraWidth, mCameraHeight, RECT_WIDTH, RECT_HEIGHT);
            viewMask.setCenterRect(screenCenterRect);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    private CameraView.Callback mCallback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            super.onCameraOpened(cameraView);
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            super.onCameraClosed(cameraView);
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);
            Bitmap bitmap = null;
            int picWidth; //拍照返回的bitmap的宽度（非取景框内图片）
            int picHeight; //拍照返回的bitmap的高度
            if (data != null) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成类图
            }
            //保存图片到sdcard
            if (bitmap != null) {
                //如果高宽比和camera的高宽比不一样，说明被旋转了90or270度
                if(bitmap.getHeight() / (float)bitmap.getWidth() == cameraRatio){
                    picWidth = bitmap.getWidth();
                    picHeight = bitmap.getHeight();
                }else{
                    picHeight = bitmap.getWidth();
                    picWidth = bitmap.getHeight();
                }
                if (rectPictureSize == null) {
                    rectPictureSize = DisplayUtil.createCenterPictureRect(ratio, cameraRatio, picWidth, picHeight);
                }
                int x = picWidth / 2 - rectPictureSize.x / 2;
                int y = picHeight / 2 - rectPictureSize.y / 2;
                Bitmap rectBitmap = Bitmap.createBitmap(bitmap, x, y, rectPictureSize.x, rectPictureSize.y);
                int imageWidth = rectBitmap.getWidth();
                int imageHeight = rectBitmap.getHeight();
                FileUtil.saveBitmap(rectBitmap,imagePath);
                setResultUri(imageUri,imageWidth,imageHeight);

                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                if (!rectBitmap.isRecycled()) {
                    rectBitmap.recycle();
                }
                finish();
            }
        }

    };

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.ibt_capture) {
            mCameraView.takePicture();
        } else if (i == R.id.iv_return) {
            CameraActivity.this.finish();
        }
    }


    /**
     * @param uri 图片Uri
     * @param imageWidth 图片宽
     * @param imageHeight 图片高
     */
    protected void setResultUri(Uri uri,int imageWidth,int imageHeight) {
        setResult(RESULT_OK, new Intent()
                .putExtra(EasyCamera.EXTRA_OUTPUT_URI, uri)
                .putExtra(EasyCamera.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                .putExtra(EasyCamera.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
        );
    }


    /********************************** 以下是权限检查部分 ********************************/
    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                    .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }


    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                                                             String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            switch (requestCode) {
                case REQUEST_CAMERA_PERMISSION:
                    if (permissions.length != 1 || grantResults.length != 1) {
                        throw new RuntimeException(getString(R.string.error_camera_permission));
                    }
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(), R.string.camera_permission_not_granted, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

    }

    /******************************** 以上是权限部分 ********************************/


}
