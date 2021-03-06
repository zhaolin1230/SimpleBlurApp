package com.test.blur.edit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.test.blur.R;
import com.test.blur.about.AboutActivity;
import com.test.blur.data.ImageInfo;
import com.test.blur.utils.ActivityUtils;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class EditActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE = 23;

    private EditContract.Presenter mPresenter;

    private Toolbar mToolbar;

    private boolean selected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        initToolbar();

        EditFragment editFragment = (EditFragment) getSupportFragmentManager()
                .findFragmentById(R.id.edit_content_frame);
        if (editFragment == null) {
            editFragment = EditFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), editFragment,
                    R.id.edit_content_frame);
        }

        mPresenter = new EditPresenter(editFragment);
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.edit_tool_bar);
        setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.edit_save_item:
                mPresenter.saveImage();
                break;
            case R.id.edit_open_item:
                EditActivityPermissionsDispatcher.gotoMatisseWithCheck(this);
                break;
            case R.id.edit_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mToolbar.setBackgroundColor(Color.TRANSPARENT);
            getWindow().getDecorView().setBackgroundColor(Color.BLACK);

            List<Uri> selected = Matisse.obtainResult(data);
            if (selected != null && selected.size() > 0) {
                Uri uri = selected.get(0);
                ImageInfo imageInfo = new ImageInfo(uri);
                mPresenter.showImage(imageInfo);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EditActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void gotoMatisse() {
        Matisse.from(this)
                .choose(MimeType.allOf())
                .countable(true)
                .maxSelectable(1)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new PicassoEngine())
                .forResult(REQUEST_CODE_CHOOSE);
    }

}
