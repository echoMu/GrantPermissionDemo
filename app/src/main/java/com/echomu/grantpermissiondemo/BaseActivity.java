package com.echomu.grantpermissiondemo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public abstract class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    /**
     * Context对象,使用Activity
     */
    protected AppCompatActivity mContext;
    /**
     * 唯一请求码
     */
    protected static final int CHECK_CODE = 10;
    /**
     * 权限组
     */
    private String[] mPermissionGroup;
    /**
     * 用户拒绝次数
     */
    private int mDeniedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext=this;
    }

    @AfterPermissionGranted(CHECK_CODE)
    protected void checkPermission(){
        //得到调用者需要的权限组
        mPermissionGroup=getPermissionGroup();

        if (mPermissionGroup == null || mPermissionGroup.length == 0) {
            throw new RuntimeException("请设置需要申请的权限组！");
        }

        if (!EasyPermissions.hasPermissions(mContext,mPermissionGroup)){
            Log.d("echoMu","没有权限！");
            //应用没有获得该权限
            PermissionRequest request=new PermissionRequest.Builder(mContext,CHECK_CODE,mPermissionGroup)
                    .setRationale(getPermissionNotifyStr()==null?getString(R.string.toast_permission_notify):getPermissionNotifyStr())
                    .setNegativeButtonText("拒绝")
                    .setPositiveButtonText("允许")
                    .build();
            EasyPermissions.requestPermissions(request);
        }else {
            Log.d("echoMu","已经有权限啦！");
        }
    }

    /**
     * 获取权限列表
     *
     * @return 权限数组
     */
    protected String[] getPermissionGroup() {
        return null;
    }

    /**
     * 设置权限的提示语
     *
     * @return
     */
    protected String getPermissionNotifyStr() {
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * 获取权限通过
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        mDeniedCount=0;
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        // 也就是被设置了不再询问的情况
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Log.d("echoMu","请求权限");
            new AppSettingsDialog
                    .Builder(mContext)
                    .setTitle("请求权限")
                    .setPositiveButton("设置")
                    .setRationale(getString(R.string.label_permission_rationale))
                    .build()
                    .show();

            return;
        }

        //被拒绝了第一次
        mDeniedCount++;
        Log.d("echoMu","mDeniedCount:"+mDeniedCount);
        //重新进入页面,调用了checkPermission,并且被拒绝了
        if (mPermissionGroup != null && mDeniedCount == 1) {
            finish();
        }

//        if (mDeniedCount >= 2) {
//            finish();
//        }
//        //再次请求
//        Log.d("echoMu","再次请求");
//        checkPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            Log.d("echoMu","回到页面后再次判断是否有权限");
            //回到页面后再次判断是否有权限
            checkPermission();
        }
    }

}
