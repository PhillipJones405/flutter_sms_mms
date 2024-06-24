package com.example.sms_mms;

import static android.content.Intent.EXTRA_STREAM;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Telephony;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;


/**
 * SmsMmsPlugin
 */
public class SmsMmsPlugin implements FlutterPlugin, MethodCallHandler {

    @Nullable
    private Context context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        MethodChannel channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "sms_mms");
        context = flutterPluginBinding.getApplicationContext();
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("sendMms")) {

            @Nullable List<String> filePaths = call.argument("paths");
            List<String> address = call.argument("recipientNumbers");
            String message = call.argument("message");

            StringBuilder addressString = new StringBuilder();

            for (int i = 0; i < (address != null ? address.size() : 0); i++) {
                addressString.append(address.get(i)).append(";");
            }

            try {
                if (filePaths == null) {
                    result.error("NULL_FILE_PATHS", "File paths are null", null);
                } else {
                    sendSMS(filePaths, addressString.toString(), message);
                    result.success(null);
                }
            } catch (Exception e) {
                result.error("SEND_SMS_FAILED", e.getMessage(), null);
            }

        } else {
            result.notImplemented();
        }
    }

    protected void sendSMS(@Nullable List<String> filePaths, String address, String message) throws Exception {
        List<Uri> fileUris = new ArrayList<>();
        String DEFAULT_MESSAGE_PACKAGE_NAME = "";

        String packageName = Telephony.Sms.getDefaultSmsPackage(context);
        if (packageName != null) {
            DEFAULT_MESSAGE_PACKAGE_NAME = packageName;
        }
        if (packageName == null) {
            throw new Exception("Default sms app not found");
        }

        if (filePaths != null) {
            for (String filePath : filePaths) {
                File file = new File(filePath);
                assert context != null;
                String providerAuthority = context.getPackageName() + ".flutter.mms";
                Uri fileUri = FileProvider.getUriForFile(context, providerAuthority, file);
                fileUris.add(fileUri);
            }
        }

        String action = filePaths != null ? Intent.ACTION_SEND_MULTIPLE : Intent.ACTION_VIEW;

        Intent shareIntent = new Intent(action);
        shareIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shareIntent.putExtra("sms_body", message);

        if (filePaths != null) {
            shareIntent.setType("image/*");
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(fileUris));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (context != null) {
                for (Uri fileUri : fileUris) {
                    context.grantUriPermission(DEFAULT_MESSAGE_PACKAGE_NAME, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
            shareIntent.putExtra("address", address);
        } else {
            shareIntent.setData(Uri.fromParts("sms", address, null));
        }

        shareIntent.setPackage(DEFAULT_MESSAGE_PACKAGE_NAME);

        try {
            if (context != null) {
                context.startActivity(shareIntent);
            }
        } catch (Exception e) {
            if (filePaths != null) {
                throw new Exception("File format not supported by Messages");
            } else {
                throw new Exception("Unable to launch Messages");
            }
        }
    }
}
