package com.kontranik.gridforartist;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements OnTouchListener, ColorPickerDialogListener, NumberPicker.OnValueChangeListener {

    private static final String TAG = "MainActivity";

    private static int RESULT_LOAD_IMAGE = 124;

    // Give your color picker dialog unique IDs if you have multiple dialogs.
    private static final int DIALOG_ID = 346;

    GridImageView gridImageView;
    Button switchButton;
    ImageButton lockButton, diagButton;
    TextView statusTextView;

    static Grid grid;

    static boolean stateGrid = false;

    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridImageView = findViewById(R.id.imageView_Grid);

        statusTextView = findViewById(R.id.textView_Status);

        switchButton = findViewById(R.id.button_SwitchGrid);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stateGrid = !stateGrid;
                checkStateGrid();
            }
        });

        lockButton = findViewById(R.id.imageButton_LockGrid);
        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                grid.locked = ! grid.locked;
                checkLockStat();
            }
        });

        diagButton = findViewById(R.id.imageButton_DiagGrid);
        diagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                grid.diagonal = !grid.diagonal;
                checkDiagonal();
            }
        });

        ImageButton buttonLoadImage = (ImageButton) findViewById(R.id.imageButton_PickImage);
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        ImageButton buttonSaveImage = (ImageButton) findViewById(R.id.imageButton_SaveImage);
        buttonSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    saveImage(gridImageView);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), R.string.alert_cdnt_save_image, Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageButton buttonPickColor = (ImageButton) findViewById(R.id.imageButton_ColorGrid);
        buttonPickColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pickColor();
            }
        });

        gridImageView.setOnTouchListener(this);


        newGrid();

        if (shouldAskPermissions()) {
            askPermissions();
        }
    }

    private void newGrid() {

        grid = new Grid(gridImageView);

        checkLockStat();
        checkStateGrid();
        checkDiagonal();
        updateStatus();
    }

    private void pickColor() {
        ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(false)
                .setDialogId(DIALOG_ID)
                .setColor(grid.color)
                .setShowAlphaSlider(true)
                .show( MainActivity.this );
    }

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSetColor:
                pickColor();
                return true;
            case R.id.menuSetStroke:
                NumberPickerDialog newFragment = new NumberPickerDialog();
                newFragment.setValueChangeListener(this);
                newFragment.show(getSupportFragmentManager(), "Grid stroke");
                return true;
            case R.id.menuResetGrid:
                newGrid();
                gridImageView.invalidate();
                return true;
            case R.id.menuResteOffset:
                grid.offsetX = 0;
                grid.offsetY = 0;
                gridImageView.invalidate();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        grid.stroke = numberPicker.getValue();
        gridImageView.invalidate();
    }

    private void checkStateGrid() {
        if ( stateGrid ) {
            switchButton.setText(R.string.ButtonGrid);
            if (grid.locked) {
                Toast.makeText(  this.getApplicationContext(), R.string.Toast_GridIsLocked, Toast.LENGTH_SHORT).show();
            }
        } else {
            switchButton.setText(R.string.ButtonImage);
        }
    }


    private void saveImage(GridImageView iv) throws IOException {
        // get Image with painted Canvas
        //iv.setDrawingCacheEnabled(true);
        //Bitmap bitmap = iv.getDrawingCache();
        //Bitmap bitmap = loadBitmapFromView(iv);

        Bitmap bitmap = iv.getBitmap();

        FileOutputStream outStream = null;
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/grided");
        dir.mkdirs();
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);
        outStream = new FileOutputStream(outFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        outStream.flush();
        outStream.close();

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(outFile));
        sendBroadcast(intent);

        Toast.makeText(this.getApplicationContext(), R.string.alert_image_saved, Toast.LENGTH_SHORT).show();
    }

    private void checkDiagonal() {
        if ( grid.diagonal) {
            diagButton.setImageResource(R.drawable.ic_clear_black_24dp);
        } else {
            diagButton.setImageResource(R.drawable.ic_crop_square_black_24dp);
        }
        gridImageView.invalidate();
    }

    private void checkLockStat() {
        if (grid.locked) {
            lockButton.setImageResource(R.drawable.ic_lock_outline_black_24dp);
            Toast.makeText(this.getApplicationContext(), R.string.Toast_GridIsLocked, Toast.LENGTH_SHORT).show();
        } else {
            lockButton.setImageResource(R.drawable.ic_lock_open_black_24dp);
            Toast.makeText(this.getApplicationContext(), R.string.Toast_GridIsUnLocked, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            if ( cursor != null)  {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                gridImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                newGrid();
            }
        }
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case DIALOG_ID:
                // We got result from the dialog that is shown when clicking on the icon in the action bar.
                // Toast.makeText(MainActivity.this, "Selected Color: #" + Integer.toHexString(color), Toast.LENGTH_SHORT).show();
                grid.color =  color;
                gridImageView.invalidate();
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        // Log.d(TAG, "onDialogDismissed() called with: dialogId = [" + dialogId + "]");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        dumpEvent(event);
        // Handle touch events here...

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted
                mode = NONE;
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                oldDist = spacing(event);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG)
                {
                    matrix.set(savedMatrix);
                    if ( ! stateGrid ) {
                        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                    } else  {
                        if ( !grid.locked) {
                            grid.offsetX =  (int) ( ( event.getX() - start.x )  );
                            grid.offsetY =  (int) ( ( event.getY() - start.y )  );
                        }
                    }
                }
                else if (mode == ZOOM)
                {
                    // pinch zooming
                    float newDist = spacing(event);
                    if (newDist > 10f)
                    {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        if ( ! stateGrid ) {
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        } else {
                            if ( !grid.locked) {
                                if (scale > 1) {
                                    grid.growCell(gridImageView);
                                } else {
                                    grid.shrinkCell(gridImageView);
                                }
                            }
                        }
                    }
                }
                break;
        }


        view.setImageMatrix(matrix); // display the transformation on screen
        view.invalidate();

        updateStatus();

        return true; // indicate event was handled
    }

    private void updateStatus() {
        statusTextView.setText(
                getString(R.string.status_text, grid.offsetX, grid.offsetY, grid.cols, grid.rows ) );
    }


    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event)
    {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        //Log.d("Touch Events ---------", sb.toString());
    }
}

